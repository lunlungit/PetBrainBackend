package com.aipetbrain.common;

/**
 * 权限拒绝异常
 */
public class PermissionDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    public PermissionDeniedException(String message) {
        super(message);
        this.code = 403;
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
        this.code = 403;
    }

    public PermissionDeniedException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}

