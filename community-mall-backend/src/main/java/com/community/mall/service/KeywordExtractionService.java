package com.community.mall.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 关键词提取服务
 * 用于从用户口语化查询中提取核心商品关键词
 */
public interface KeywordExtractionService {

    @SystemMessage("你是一个关键词提取助手。请从用户的查询中提取出核心的【商品名称关键词】用于数据库搜索。\n" +
            "规则：\n" +
            "1. 只提取商品名词，去掉 '有没有', '想买', '多少钱', '这个' 等无关词汇。\n" +
            "2. 如果查询中不包含具体商品，返回 'ALL'。\n" +
            "3. 如果有多个商品，只提取第一个。\n" +
            "4. 不要返回任何解释，只返回关键词本身。\n" +
            "例如：\n" +
            "用户：'有没有红富士苹果？' -> 返回：'红富士'\n" +
            "用户：'想买点香蕉' -> 返回：'香蕉'\n" +
            "用户：'今天天气不错' -> 返回：'ALL'\n" +
            "用户：'波音747有卖吗' -> 返回：'波音747'")
    @UserMessage("{{userQuestion}}")
    String extract(@V("userQuestion") String userQuestion);
}
