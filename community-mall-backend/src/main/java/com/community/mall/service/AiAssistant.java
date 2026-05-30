package com.community.mall.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiAssistant {

    @SystemMessage(
        "你是一名社区团购平台的专业文案策划。"
      + "请根据商品名称和关键词，撰写一段约 50 字的商品推广文案。"
      + "文案风格要生动、有吸引力，能够激发用户的购买欲望。"
      + "【极其重要】：只输出生成的商品推广文案内容本身。绝对不要包含任何前言（如'为您生成的文案如下：'）、后语、任何多余的客套话或解释说明！"
    )
    String generateProductCopy(@UserMessage String prompt);
}
