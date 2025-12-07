package com.community.mall.controller.admin;

import com.community.mall.common.Result;
import com.community.mall.service.AiAssistant;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Map;

@RestController
@RequestMapping("/admin/ai")
public class AiController {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    private AiAssistant aiAssistant;

    @PostConstruct
    public void init() {
        this.aiAssistant = AiServices.create(AiAssistant.class, chatLanguageModel);
    }

    @PostMapping("/generate")
    public Result<String> generate(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        String keywords = params.get("keywords");

        if (name == null || keywords == null) {
            return Result.error("Product name and keywords are required");
        }

        String prompt = "Product Name: " + name + ", Keywords: " + keywords;
        System.out.println("AI Request Prompt: " + prompt);
        try {
            String copy = aiAssistant.generateProductCopy(prompt);
            System.out.println("AI Response Copy: " + copy);
            return Result.success(copy);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI Generation failed: " + e.getMessage());
        }
    }
}
