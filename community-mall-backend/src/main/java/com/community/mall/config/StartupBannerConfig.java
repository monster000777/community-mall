package com.community.mall.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 项目启动成功后打印自定义 Banner
 */
@Component
public class StartupBannerConfig implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        printSuccessBanner();
    }

    private void printSuccessBanner() {
        System.out.println("\n==========================================\n");

        // 读取并打印 success-banner.txt（可选）
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("success-banner.txt")),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            System.out.println("(ง •_•)ง  启动成功！  (ง •_•)ง");
        }

        System.out.println("\n==========================================\n");
    }
}