package com.community.mall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射配置：用于访问上传的文件
 */
@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解决 jar 包部署时 upload.path 相对路径问题，统一转为绝对路径
        String uploadRoot;
        if (new File(uploadPath).isAbsolute()) {
            uploadRoot = uploadPath;
        } else {
            uploadRoot = System.getProperty("user.dir") + File.separator + uploadPath;
        }

        // 去除末尾的 / 或 \，确保路径格式统一
        uploadRoot = uploadRoot.replaceAll("[/\\\\]+$", "") + File.separator;

        // 注意：应用 context-path 为 /api，此处使用 "/uploads/**"，外部访问路径为 /api/uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadRoot);
    }
}
