package com.community.mall.service;

import com.community.mall.exception.BusinessException;
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
    /** 校验失败次数计数前缀 */
    private static final String FAIL_PREFIX = "sms:fail:";
    /** 每日发送条数计数前缀 */
    private static final String DAILY_PREFIX = "sms:daily:";
    private static final long EXPIRE_TIME = 5; // 5分钟有效期
    private static final long LIMIT_TIME = 1;  // 1分钟限制
    /** 同一验证码最多允许校验失败的次数，超过即作废（防暴力枚举） */
    private static final int MAX_VERIFY_FAILURES = 5;
    /** 同一手机号每日最多发送验证码条数 */
    private static final int MAX_DAILY_SENDS = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成并保存验证码
     */
    public String generateCode(String phone) {
        // 检查发送频率限制
        if (Boolean.TRUE.equals(redisTemplate.hasKey(LIMIT_PREFIX + phone))) {
            throw new BusinessException("发送太频繁，请稍后再试");
        }

        // 检查每日发送条数上限
        String dailyKey = DAILY_PREFIX + phone;
        Long sentToday = redisTemplate.opsForValue().increment(dailyKey);
        if (sentToday != null && sentToday == 1L) {
            // 当天首次发送，为计数器设置 24 小时过期
            redisTemplate.expire(dailyKey, 24, TimeUnit.HOURS);
        } else if (sentToday != null && sentToday > 1L && sentToday <= MAX_DAILY_SENDS) {
            // 兜底续期：若首次 expire 恰好失败（连接抖动），此处补上，避免计数器永不过期导致该号码永久无法收到验证码
            redisTemplate.expire(dailyKey, 24, TimeUnit.HOURS);
        }
        if (sentToday != null && sentToday > MAX_DAILY_SENDS) {
            throw new BusinessException("今日验证码发送次数已达上限，请明天再试");
        }

        // 生成6位数字验证码
        String code = String.format("%06d", RANDOM.nextInt(1000000));

        // 新验证码签发时清除旧的失败计数（否则换码后继承剩余试错次数，与计数初衷不符）
        redisTemplate.delete(FAIL_PREFIX + phone);

        // 保存验证码到Redis
        redisTemplate.opsForValue().set(CODE_PREFIX + phone, code, EXPIRE_TIME, TimeUnit.MINUTES);

        // 设置频率限制
        redisTemplate.opsForValue().set(LIMIT_PREFIX + phone, "1", LIMIT_TIME, TimeUnit.MINUTES);

        return code;
    }

    /**
     * 校验验证码
     *
     * 失败时累加失败计数（与验证码同生命周期），连续失败达到上限后立即作废验证码，
     * 防止在有效期内暴力枚举 6 位数字码。
     */
    public boolean verifyCode(String phone, String code) {
        String codeKey = CODE_PREFIX + phone;
        String savedCode = redisTemplate.opsForValue().get(codeKey);
        if (savedCode == null) {
            return false;
        }
        if (savedCode.equals(code)) {
            // 校验成功后删除验证码及失败计数
            redisTemplate.delete(codeKey);
            redisTemplate.delete(FAIL_PREFIX + phone);
            return true;
        }

        // 校验失败：累加失败次数
        String failKey = FAIL_PREFIX + phone;
        Long failures = redisTemplate.opsForValue().increment(failKey);
        if (failures != null && failures == 1L) {
            // 失败计数与验证码同生命周期，验证码过期后计数一并消失
            redisTemplate.expire(failKey, EXPIRE_TIME, TimeUnit.MINUTES);
        }
        if (failures != null && failures >= MAX_VERIFY_FAILURES) {
            // 连续失败次数过多，直接作废验证码
            redisTemplate.delete(codeKey);
            redisTemplate.delete(failKey);
            throw new BusinessException("验证码错误次数过多，已失效，请重新获取");
        }
        return false;
    }
}
