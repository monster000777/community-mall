package com.community.mall.service.impl;

import com.community.mall.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信服务模拟实现
 */
@Slf4j
@Service
public class MockSmsServiceImpl implements SmsService {

    @Override
    public void sendVerificationCode(String phone, String code) {
        log.info("【社区商城】正在向手机号 {} 发送验证码：{}", phone, code);
        // 这里可以打印出明显的标记，方便测试时在日志中查找
        System.out.println("\n************************************************");
        System.out.println("短信验证码已发送至: " + phone);
        System.out.println("验证码为: " + code);
        System.out.println("************************************************\n");
    }
}
