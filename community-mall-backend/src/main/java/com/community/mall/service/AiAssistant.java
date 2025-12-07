package com.community.mall.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiAssistant {

    @SystemMessage("You are a professional copywriter for a community group buying platform. " +
            "Write a 100-word attractive product description based on the product name and keywords. " +
            "The tone should be enthusiastic and encouraging.")
    String generateProductCopy(@UserMessage String prompt);
}
