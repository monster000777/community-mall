package com.community.mall.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.community.mall.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.error(400, message);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        return Result.error(400, message);
    }
    
    /**
     * 处理 Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        String message = "";
        switch (e.getType()) {
            case NotLoginException.NOT_TOKEN:
                message = "未提供Token";
                break;
            case NotLoginException.INVALID_TOKEN:
                message = "Token无效";
                break;
            case NotLoginException.TOKEN_TIMEOUT:
                message = "Token已过期";
                break;
            case NotLoginException.BE_REPLACED:
                message = "Token已被顶下线";
                break;
            case NotLoginException.KICK_OUT:
                message = "Token已被踢下线";
                break;
            default:
                message = "未登录";
        }
        return Result.error(401, message);
    }
    
    /**
     * 处理 Sa-Token 角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        return Result.error(403, "无此角色权限：" + e.getRole());
    }
    
    /**
     * 处理 Sa-Token 权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        return Result.error(403, "无此操作权限：" + e.getPermission());
    }
    
    /**
     * 处理请求体解析异常
     *
     * JSON 格式错误或编码非法（如非 UTF-8）时给出友好提示，
     * 而非落入通用 500（冒烟测试发现的前端体验改进点）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error(400, "请求体格式错误，请检查 JSON 格式与编码");
    }

    /**
     * 处理业务异常
     *
     * BusinessException 的 message 由业务代码显式抛出，可安全返回给前端展示
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 处理运行时异常
     *
     * 记录完整堆栈供排查；message 可能包含 SQL/空指针等内部细节，不直接返回给客户端
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("未捕获的运行时异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("未捕获的系统异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}
