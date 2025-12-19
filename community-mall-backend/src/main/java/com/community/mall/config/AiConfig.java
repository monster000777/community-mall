package com.community.mall.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import com.community.mall.service.KeywordExtractionService;

@Configuration
public class AiConfig {

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String openAiBaseUrl;

    @Value("${openai.model-name:qwen-max}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .baseUrl(openAiBaseUrl)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public com.community.mall.service.CustomerAiService customerAiService(ChatLanguageModel chatLanguageModel) {
        return dev.langchain4j.service.AiServices.create(com.community.mall.service.CustomerAiService.class,
                chatLanguageModel);
    }

    @Bean
    public KeywordExtractionService keywordExtractionService(ChatLanguageModel chatLanguageModel) {
        return dev.langchain4j.service.AiServices.create(KeywordExtractionService.class, chatLanguageModel);
    }
}
