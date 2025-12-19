package com.community.mall.controller;

import com.community.mall.common.Result;
import com.community.mall.entity.Product;
import com.community.mall.service.CustomerAiService;
import com.community.mall.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能导购对话控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ProductService productService;

    @Autowired
    private com.community.mall.service.KeywordExtractionService keywordExtractionService;

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
            // 0. 提取关键词 (Keyword Extraction AI)
            System.out.println("Extracting keyword...");
            String keyword = keywordExtractionService.extract(question);
            System.out.println("Extracted Keyword: " + keyword);

            // 如果提取失败或无需搜索，则跳过
            List<Product> products = null;
            boolean searchPerformed = false;

            if (!"ALL".equalsIgnoreCase(keyword)) {
                // 1. 搜索商品 (Manual RAG)
                products = productService.searchForAi(keyword);
                searchPerformed = true;
                System.out.println("Found Products count: " + (products == null ? 0 : products.size()));
            } else {
                System.out.println("Skipping product search (Keyword is ALL)");
            }

            // 2. 构造上下文
            String inventoryContext;
            if (searchPerformed) {
                if (products == null || products.isEmpty()) {
                    inventoryContext = "未找到用户指定的商品，请委婉告知并推荐。";
                } else {
                    inventoryContext = products.stream()
                            .map(p -> p.getProductName() + "(" + p.getPrice() + "元)")
                            .collect(Collectors.joining(", "));
                }
            } else {
                inventoryContext = "用户未指定新商品，请根据聊天记忆（Context）继续对话。";
            }

            System.out.println("Inventory Context: " + inventoryContext);

            // 3. 调用 AI (with session ID)
            System.out.println("Calling AI Service...");
            long start = System.currentTimeMillis();
            String answer = customerAiService.chat(sessionId, question, inventoryContext);
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
