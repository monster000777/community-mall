package com.community.mall.common;

import com.community.mall.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller 层异常转统一响应工具
 *
 * 配合 GlobalExceptionHandler 使用：Controller 内若仍需自行捕获异常，
 * 统一走本工具，保证：
 * - BusinessException（业务提示） → message 原样返回给前端
 * - 其它异常（SQL/空指针等系统异常） → 记录完整堆栈，仅返回通用文案，避免泄漏内部细节
 */
@Slf4j
public class ExceptionSupport {

    private ExceptionSupport() {
    }

    public static Result<Void> toResult(Exception e) {
        if (e instanceof BusinessException) {
            return Result.error(e.getMessage());
        }
        log.error("接口处理异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}
