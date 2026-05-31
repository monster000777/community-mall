package com.community.mall.controller;

import com.community.mall.common.Result;
import com.community.mall.service.CustomerAiService;
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
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private CustomerAiService customerAiService;

    @PostMapping("/ask")
    public Result<String> ask(@RequestBody Map<String, String> params) {
        String question = params.get("question");
        String sessionId = params.getOrDefault("sessionId", "default-user");

        if (question == null || question.trim().isEmpty()) {
            return Result.success("OK", "请说点什么吧~");
        }

        System.out.println("====== [ChatController] Start processing request ======");
        System.out.println("User Question: " + question);
        System.out.println("Session ID: " + sessionId);

        try {
            System.out.println("Calling AI Service...");
            long start = System.currentTimeMillis();
            String answer = customerAiService.chat(sessionId, question);
            long end = System.currentTimeMillis();
            System.out.println("AI Response (Time: " + (end - start) + "ms): " + answer);

            return Result.success("获取成功", answer);
        } catch (Exception e) {
            System.err.println("Error in ChatController: " + e.getMessage());
            e.printStackTrace();
            return Result.error("AI服务暂时不可用: " + e.getMessage());
        } finally {
            System.out.println("====== [ChatController] End processing request ======");
        }
    }
}
