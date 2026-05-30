package com.community.mall.controller;

import com.community.mall.common.Result;
import com.community.mall.service.CustomerAiService;
import com.community.mall.service.ProductTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 智能导购对话控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private CustomerAiService customerAiService;

    @Autowired
    private ProductTool productTool;

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
            // 直接调用智能导购服务（底层采用 Function Calling 与 Redis 共享存储）
            System.out.println("Calling AI Service...");
            long start = System.currentTimeMillis();
            String answer = customerAiService.chat(sessionId, question);
            long end = System.currentTimeMillis();
            System.out.println("AI Response (Time: " + (end - start) + "ms): " + answer);

            // 容错处理：部分大模型/中转代理对标准 Function Calling 兼容性不佳，直接在 text 中返回了 <tool_call> 标签
            if (answer != null && answer.contains("<tool_call>")) {
                System.out.println("[ChatController] Detected literal <tool_call> tag. Executing manual fallback...");
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<tool_call>\\s*(\\{.+?\\})\\s*</tool_call>", java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = pattern.matcher(answer);
                if (matcher.find()) {
                    String jsonStr = matcher.group(1);
                    try {
                        com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(jsonStr);
                        String toolName = json.getString("tool_name");
                        com.alibaba.fastjson2.JSONObject toolInput = json.getJSONObject("tool_input");

                        if ("searchProducts".equals(toolName) && toolInput != null) {
                            String keyword = toolInput.getString("keyword");
                            // 调用本地商品检索工具
                            String toolResult = productTool.searchProducts(keyword);
                            System.out.println("[ChatController] Manual Tool Result: " + toolResult);

                            // 将工具执行结果作为下一次上下文提问，重新请求大模型进行最终整合回答
                            String followUpQuestion = "【工具 searchProducts 执行结果】：\n" + toolResult 
                                    + "\n\n请以此最新真实库存价格为准，继续以导购'团团'的语气，直接回复我刚才关于商品的问题。";
                            answer = customerAiService.chat(sessionId, followUpQuestion);
                            System.out.println("[ChatController] Final Refined Response: " + answer);
                        }
                    } catch (Exception ex) {
                        System.err.println("[ChatController] Failed to parse and execute manual tool call: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }

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
