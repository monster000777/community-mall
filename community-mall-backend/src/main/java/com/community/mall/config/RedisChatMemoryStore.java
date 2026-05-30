package com.community.mall.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分布式持久化会话记忆存储器（基于 Redis）
 */
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;
    private static final String REDIS_KEY_PREFIX = "community:mall:ai:session:";
    private static final long EXPIRE_TIME_DAYS = 7; // 会话保留7天

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = REDIS_KEY_PREFIX + memoryId;
        List<String> list = redisTemplate.opsForList().range(key, 0, -1);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream()
                .map(ChatMessageDeserializer::messageFromJson)
                .collect(Collectors.toList());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = REDIS_KEY_PREFIX + memoryId;
        redisTemplate.delete(key);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<String> serialized = messages.stream()
                .map(ChatMessageSerializer::messageToJson)
                .collect(Collectors.toList());
        redisTemplate.opsForList().rightPushAll(key, serialized);
        redisTemplate.expire(key, EXPIRE_TIME_DAYS, TimeUnit.DAYS);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = REDIS_KEY_PREFIX + memoryId;
        redisTemplate.delete(key);
    }
}
