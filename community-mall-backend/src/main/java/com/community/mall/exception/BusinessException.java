package com.community.mall.exception;

/**
 * 业务异常类
 *
 * 用于携带可直接展示给用户的业务提示信息（如"库存不足"），
 * 与系统级异常（SQL 异常、空指针等）区分开：
 * 前者的 message 可以安全返回给前端，后者由全局处理器统一兜底，避免泄漏内部细节。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
