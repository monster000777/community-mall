package com.community.mall.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 */
@Service
public class VerificationCodeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CODE_PREFIX = "sms:code:";
    private static final String LIMIT_PREFIX = "sms:limit:";
    private static final long EXPIRE_TIME = 5; // 5分钟有效期
    private static final long LIMIT_TIME = 1;  // 1分钟限制
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成并保存验证码
     */
    public String generateCode(String phone) {
        // 检查发送频率限制
        if (Boolean.TRUE.equals(redisTemplate.hasKey(LIMIT_PREFIX + phone))) {
            throw new RuntimeException("发送太频繁，请稍后再试");
        }

        // 生成6位数字验证码
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        
        // 保存验证码到Redis
        redisTemplate.opsForValue().set(CODE_PREFIX + phone, code, EXPIRE_TIME, TimeUnit.MINUTES);
        
        // 设置频率限制
        redisTemplate.opsForValue().set(LIMIT_PREFIX + phone, "1", LIMIT_TIME, TimeUnit.MINUTES);
        
        return code;
    }

    /**
     * 校验验证码
     */
    public boolean verifyCode(String phone, String code) {
        String savedCode = redisTemplate.opsForValue().get(CODE_PREFIX + phone);
        if (savedCode != null && savedCode.equals(code)) {
            // 校验成功后删除验证码
            redisTemplate.delete(CODE_PREFIX + phone);
            return true;
        }
        return false;
    }
}
