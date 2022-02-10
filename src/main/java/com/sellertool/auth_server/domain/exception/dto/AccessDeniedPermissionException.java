package com.sellertool.auth_server.domain.exception.dto;

/**
 * 403
 */
public class AccessDeniedPermissionException extends RuntimeException{
    public AccessDeniedPermissionException() {
        super();
    }
    public AccessDeniedPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
    public AccessDeniedPermissionException(String message) {
        super(message);
    }
    public AccessDeniedPermissionException(Throwable cause) {
        super(cause);
    }
}
