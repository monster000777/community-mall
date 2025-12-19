package com.community.mall.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiAssistant {

    @SystemMessage(
        "你是一名社区团购平台的专业文案策划。"
      + "请根据商品名称和关键词，撰写一段约 50 字的商品推广文案。"
      + "文案风格要生动、有吸引力，能够激发用户的购买欲望。"
    )
    String generateProductCopy(@UserMessage String prompt);
}
