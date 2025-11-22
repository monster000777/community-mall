package com.community.mall.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI communityMallOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("社区团购系统 API 文档")
                        .description("社区团购系统后端接口说明")
                        .version("v1.0.0"));
    }
}
