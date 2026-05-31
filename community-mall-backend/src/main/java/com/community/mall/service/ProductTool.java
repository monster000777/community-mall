package com.community.mall.service;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
import com.community.mall.entity.Product;

/**
 * AI 智能体可调用的本地商品查询工具
 */
@Component
public class ProductTool {

    @Autowired
    private ProductService productService;

    @Tool("根据商品名称关键词搜索店内当前上架商品的最新库存与价格。当用户主动询问商品是否有货、查询价格、或者表示想要购买某类商品时，必须优先调用此工具查询库存储备。若用户仅在闲聊或没有提到具体商品，则无需调用。")
    public String searchProducts(@P("商品名称关键词") String keyword) {
        System.out.println("[ProductTool] AI is calling searchProducts with keyword: " + keyword);
        if (keyword == null || keyword.trim().isEmpty() || "ALL".equalsIgnoreCase(keyword.trim())) {
            return "未指定具体的商品搜索关键词。";
        }
        
        List<Product> products = productService.searchForAi(keyword.trim());
        if (products == null || products.isEmpty()) {
            return "抱歉，店内目前没有找到与 '" + keyword + "' 相关的商品。";
        }

        return products.stream()
                .map(p -> p.getProductName() + "（价格：" + p.getPrice() + "元）")
                .collect(Collectors.joining("，"));
    }
}
