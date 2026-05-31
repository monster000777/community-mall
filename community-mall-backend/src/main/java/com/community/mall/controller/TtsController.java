package com.community.mall.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/tts")
public class TtsController {

    @Value("${mimo.api-key}")
    private String apiKey;

    @Value("${mimo.base-url}")
    private String baseUrl;

    @Value("${mimo.model}")
    private String model;

    @Value("${mimo.voice}")
    private String voice;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping
    public ResponseEntity<byte[]> speech(@RequestBody Map<String, String> params) {
        String text = params.get("text");
        if (text == null || text.trim().isEmpty()) {
            log.warn("[TTS] 收到空文本播放请求");
            return ResponseEntity.badRequest().build();
        }

        // 强校验：在没有配置 mimo 秘钥或基地址不合法时，拒绝云端请求并立即返回 400
        if (apiKey == null || apiKey.isBlank() || baseUrl == null || !baseUrl.startsWith("http")) {
            log.warn("[TTS] 云端 API 密钥未配置或基地址非法，跳过云端合成，baseUrl: {}", baseUrl);
            return ResponseEntity.badRequest().build();
        }

        try {
            // 1. 计算文本的 MD5 摘要并尝试检索 Redis 缓存（加入 voice 参数隔离音色缓存）
            String textMd5 = getMd5(text);
            String cacheKey = "tts:cache:" + (voice != null ? voice.trim() : "default") + ":" + textMd5;
            
            String cachedBase64 = null;
            try {
                cachedBase64 = redisTemplate.opsForValue().get(cacheKey);
            } catch (Exception re) {
                // Redis 发生异常时打印警告并优雅降级，允许直接向外部 API 请求
                log.warn("[TTS] 检索 Redis 缓存异常: {}", re.getMessage());
            }

            String base64Audio;
            if (cachedBase64 != null && !cachedBase64.isEmpty()) {
                log.info("[TTS] 命中 Redis 缓存，文本 MD5: {}", textMd5);
                base64Audio = cachedBase64;
            } else {
                log.info("[TTS] 未命中缓存，请求云端 TTS 接口，文本 MD5: {}", textMd5);
                
                String cleanedBaseUrl = baseUrl != null ? baseUrl.trim() : "";
                if (cleanedBaseUrl.endsWith("/")) {
                    cleanedBaseUrl = cleanedBaseUrl.substring(0, cleanedBaseUrl.length() - 1);
                }
                String url = cleanedBaseUrl + "/chat/completions";

                HttpHeaders headers = new HttpHeaders();
                headers.set("api-key", apiKey);
                headers.setContentType(MediaType.APPLICATION_JSON);

                List<Map<String, String>> messages = Arrays.asList(
                        Map.of("role", "user", "content", "活泼可爱的女生语音"),
                        Map.of("role", "assistant", "content", text)
                );

                Map<String, Object> body = new HashMap<>();
                body.put("model", model);
                body.put("messages", messages);
                body.put("audio", Map.of("format", "wav", "voice", voice));

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                log.info("[TTS] 正在请求 MIMO TTS API，服务地址: {}, 文本长度: {}", url, text.length());

                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    log.error("[TTS] 外部 API 响应异常，状态码: {}", response.getStatusCode());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }

                Map<?, ?> result = response.getBody();
                if (result == null) {
                    log.error("[TTS] 外部 API 响应体为空");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }

                List<?> choices = (List<?>) result.get("choices");
                Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                Map<?, ?> message = (Map<?, ?>) choice.get("message");
                Map<?, ?> audio = (Map<?, ?>) message.get("audio");
                base64Audio = (String) audio.get("data");

                if (base64Audio == null || base64Audio.isEmpty()) {
                    log.error("[TTS] 接口返回音频数据为空: {}", result);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }

                // 2. 将云端返回的 base64 音频存入 Redis 缓存，过期时间为 7 天
                try {
                    redisTemplate.opsForValue().set(cacheKey, base64Audio, 7, TimeUnit.DAYS);
                    log.info("[TTS] 已将音频数据存入 Redis 缓存，Key: {}", cacheKey);
                } catch (Exception we) {
                    log.warn("[TTS] 写入 Redis 缓存异常: {}", we.getMessage());
                }
            }

            byte[] audioBytes;
            try {
                audioBytes = Base64.getDecoder().decode(base64Audio);
            } catch (IllegalArgumentException e) {
                log.error("[TTS] Base64 音频数据解码失败: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentType(MediaType.parseMediaType("audio/wav"));
            log.info("[TTS] 成功获取语音字节流，大小: {} 字节", audioBytes.length);
            return new ResponseEntity<>(audioBytes, respHeaders, HttpStatus.OK);

        } catch (Exception e) {
            log.error("[TTS] 服务端处理异常: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 计算文本的 MD5 Hex 摘要
     */
    private String getMd5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("[TTS] MD5 计算失败，退回到 hashCode 标识: {}", e.getMessage());
            return String.valueOf(text.hashCode());
        }
    }
}
