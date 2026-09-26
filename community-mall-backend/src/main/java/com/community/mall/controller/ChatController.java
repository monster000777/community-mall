package com.community.mall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.mall.common.Result;
import com.community.mall.service.CustomerAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 智能导购对话控制器
 *
 * 底层通过 LangChain4j AiServices + @Tool 实现 Function Calling，
 * 大模型自动判断是否需要调用商品查询工具并提取参数。
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private CustomerAiService customerAiService;

    @PostMapping("/ask")
    public Result<String> ask(@RequestBody Map<String, String> params) {
        String question = params.get("question");
        String clientSessionId = params.getOrDefault("sessionId", "default");

        if (question == null || question.trim().isEmpty()) {
            return Result.success("OK", "请说点什么吧~");
        }

        // 安全修复：会话 ID 强制拼接当前登录用户，防止任意指定他人 sessionId
        // 读写他人的 AI 对话记忆（RedisChatMemoryStore 中的历史上下文）；
        // 客户端部分仅保留前 64 个字符，防止恶意构造超长 key 占用 Redis
        Long userId;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return Result.error(401, "请先登录后再使用智能客服");
        }
        String clientPart = clientSessionId.trim();
        if (clientPart.length() > 64) {
            clientPart = clientPart.substring(0, 64);
        }
        String sessionId = userId + "-" + clientPart;

        try {
            long start = System.currentTimeMillis();
            String answer = customerAiService.chat(sessionId, question);
            log.info("[ChatController] AI 响应耗时 {}ms，会话: {}", System.currentTimeMillis() - start, sessionId);
            return Result.success("获取成功", answer);
        } catch (Exception e) {
            log.error("[ChatController] AI 服务调用失败", e);
            return Result.error("AI服务暂时不可用，请稍后再试");
        }
    }
}
