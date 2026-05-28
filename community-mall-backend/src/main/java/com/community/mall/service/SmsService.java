package com.community.mall.service;

/**
 * 短信服务接口
 */
public interface SmsService {
    /**
     * 发送短信验证码
     */
    void sendVerificationCode(String phone, String code);
}
