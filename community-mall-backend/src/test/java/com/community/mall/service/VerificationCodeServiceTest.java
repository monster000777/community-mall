package com.community.mall.service;

import com.community.mall.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 验证码服务单元测试：防暴力破解计数与每日发送上限
 */
@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private VerificationCodeService service;

    private static final String PHONE = "13800000000";
    private static final String CODE_KEY = "sms:code:" + PHONE;
    private static final String FAIL_KEY = "sms:fail:" + PHONE;
    private static final String DAILY_KEY = "sms:daily:" + PHONE;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new VerificationCodeService();
        try {
            java.lang.reflect.Field field = VerificationCodeService.class.getDeclaredField("redisTemplate");
            field.setAccessible(true);
            field.set(service, redisTemplate);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void 校验成功_删除验证码与失败计数() {
        when(valueOperations.get(CODE_KEY)).thenReturn("123456");

        assertTrue(service.verifyCode(PHONE, "123456"));

        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(FAIL_KEY);
    }

    @Test
    void 校验失败_累加失败计数() {
        when(valueOperations.get(CODE_KEY)).thenReturn("123456");
        when(valueOperations.increment(FAIL_KEY)).thenReturn(1L);

        assertFalse(service.verifyCode(PHONE, "000000"));

        verify(redisTemplate).expire(eq(FAIL_KEY), anyLong(), eq(TimeUnit.MINUTES));
        // 失败但未达上限时验证码不能被删除（保证用户还能重试）
        verify(redisTemplate, never()).delete(CODE_KEY);
    }

    @Test
    void 连续失败达到上限_作废验证码并抛异常() {
        when(valueOperations.get(CODE_KEY)).thenReturn("123456");
        when(valueOperations.increment(FAIL_KEY)).thenReturn(5L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.verifyCode(PHONE, "000000"));
        assertTrue(ex.getMessage().contains("重新获取"));

        // 达到上限后验证码立即作废
        verify(redisTemplate).delete(CODE_KEY);
        verify(redisTemplate).delete(FAIL_KEY);
    }

    @Test
    void 验证码不存在_返回失败不计数() {
        when(valueOperations.get(CODE_KEY)).thenReturn(null);

        assertFalse(service.verifyCode(PHONE, "123456"));
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    void 每日发送超过上限_拒绝发送() {
        when(redisTemplate.hasKey("sms:limit:" + PHONE)).thenReturn(false);
        when(valueOperations.increment(DAILY_KEY)).thenReturn(11L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.generateCode(PHONE));
        assertTrue(ex.getMessage().contains("上限"));
    }

    @Test
    void 正常发送_设置过期与频率限制() {
        when(redisTemplate.hasKey("sms:limit:" + PHONE)).thenReturn(false);
        when(valueOperations.increment(DAILY_KEY)).thenReturn(1L);

        String code = service.generateCode(PHONE);

        assertEquals(6, code.length());
        verify(valueOperations).set(eq(CODE_KEY), eq(code), eq(5L), eq(TimeUnit.MINUTES));
        verify(redisTemplate).expire(eq(DAILY_KEY), anyLong(), eq(TimeUnit.HOURS));
    }
}
