package com.community.mall.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import com.community.mall.service.ProductTool;
import com.community.mall.service.CustomerAiService;
import com.community.mall.service.AiAssistant;

import org.springframework.web.client.RestTemplate;

@Configuration
public class AiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${openai.model-name:gpt-4o}")
    private String modelName;

    @Bean
    public ChatModel chatLanguageModel() {
        String finalBaseUrl = (openAiBaseUrl == null || openAiBaseUrl.trim().isEmpty()) 
                ? "https://api.openai.com/v1" : openAiBaseUrl.trim();
        String finalModelName = (modelName == null || modelName.trim().isEmpty()) 
                ? "gpt-4o" : modelName.trim();

        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .baseUrl(finalBaseUrl)
                .modelName(finalModelName)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public dev.langchain4j.memory.chat.ChatMemoryProvider chatMemoryProvider(StringRedisTemplate redisTemplate) {
        RedisChatMemoryStore memoryStore = new RedisChatMemoryStore(redisTemplate);
        return memoryId -> dev.langchain4j.memory.chat.MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(memoryStore)
                .build();
    }

    /**
     * 智能导购 AI 服务（绑定 ProductTool + 会话记忆）
     */
    @Bean
    public CustomerAiService customerAiService(ChatModel chatLanguageModel,
            dev.langchain4j.memory.chat.ChatMemoryProvider chatMemoryProvider,
            ProductTool productTool) {
        return dev.langchain4j.service.AiServices.builder(CustomerAiService.class)
                .chatModel(chatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(productTool)
                .build();
    }

    /**
     * 商品文案 AI 助手（无 Tool，无会话记忆）
     */
    @Bean
    public AiAssistant aiAssistant(ChatModel chatLanguageModel) {
        return dev.langchain4j.service.AiServices.create(AiAssistant.class, chatLanguageModel);
    }
}
