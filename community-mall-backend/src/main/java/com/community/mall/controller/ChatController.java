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
                // 宽口径匹配所有在 <tool_call> 与 </tool_call> 之间的字符（不仅限 JSON 格式）
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<tool_call>\\s*(.+?)\\s*</tool_call>", java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = pattern.matcher(answer);
                if (matcher.find()) {
                    String tagContent = matcher.group(1).trim();
                    try {
                        String keyword = null;

                        // 1. 尝试用键值对正则提取包含 keyword、name 或 input 等关键字的值
                        java.util.regex.Pattern pCompat = java.util.regex.Pattern.compile("(?i)\\b(keyword|name|input)\\b\\s*[\":]*\\s*\\\"?([^\\n\\\"\\}]+)\\\"?");
                        java.util.regex.Matcher mCompat = pCompat.matcher(tagContent);
                        if (mCompat.find()) {
                            keyword = mCompat.group(2).trim();
                        }

                        // 2. 兜底：若提取失败，抓取除工具保留词外，所有被双引号包裹的非空单词
                        if (keyword == null || keyword.isEmpty()) {
                            java.util.regex.Pattern pQuote = java.util.regex.Pattern.compile("\"([^\"]+?)\"");
                            java.util.regex.Matcher mQuote = pQuote.matcher(tagContent);
                            java.util.Set<String> excludeWords = new java.util.HashSet<>(java.util.Arrays.asList(
                                "searchProducts", "tool_name", "tool_input", "keyword", "tool", "args", "name", "tool_call"
                            ));
                            while (mQuote.find()) {
                                String val = mQuote.group(1).trim();
                                if (!excludeWords.contains(val) && !val.isEmpty()) {
                                    keyword = val;
                                    break;
                                }
                            }
                        }

                        // 3. 终极兜底：直接匹配提取标签内容中的第一个中文字符串
                        if (keyword == null || keyword.isEmpty()) {
                            java.util.regex.Pattern pChinese = java.util.regex.Pattern.compile("([\\u4e00-\\u9fa5]+)");
                            java.util.regex.Matcher mChinese = pChinese.matcher(tagContent);
                            if (mChinese.find()) {
                                keyword = mChinese.group(1).trim();
                            }
                        }

                        System.out.println("[ChatController] Compat parsed keyword: " + keyword);

                        if (keyword != null && !keyword.isEmpty()) {
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
