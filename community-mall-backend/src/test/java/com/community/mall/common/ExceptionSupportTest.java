package com.community.mall.common;

import com.community.mall.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 异常转统一响应工具测试：业务异常透传 message，系统异常只回通用文案（防泄漏）
 */
class ExceptionSupportTest {

    @Test
    void 业务异常_原样返回message() {
        Result<Void> result = ExceptionSupport.toResult(new BusinessException("库存不足"));

        assertEquals(500, result.getCode());
        assertEquals("库存不足", result.getMessage());
    }

    @Test
    void 系统异常_不泄漏内部message() {
        RuntimeException sqlLike = new RuntimeException(
                "BadSqlGrammarException: SELECT * FROM user WHERE 1=1; Syntax error near...");

        Result<Void> result = ExceptionSupport.toResult(sqlLike);

        assertEquals(500, result.getCode());
        assertEquals("系统异常，请稍后重试", result.getMessage());
        assertNotEquals(sqlLike.getMessage(), result.getMessage());
    }

    @Test
    void 空指针异常_返回通用文案() {
        Result<Void> result = ExceptionSupport.toResult(new NullPointerException("secret path E:\\x\\y"));

        assertEquals("系统异常，请稍后重试", result.getMessage());
    }
}
