package com.aipetbrain.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理权限异常
     */
    @ExceptionHandler(PermissionDeniedException.class)
    public Result<?> handlePermissionDeniedException(PermissionDeniedException e, WebRequest request) {
        log.warn("权限异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e, WebRequest request) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return Result.error("操作失败：" + e.getMessage());
    }

    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, WebRequest request) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error("系统错误：" + e.getMessage());
    }
}

