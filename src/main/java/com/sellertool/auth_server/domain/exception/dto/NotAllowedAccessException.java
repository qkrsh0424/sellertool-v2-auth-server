package com.sellertool.auth_server.domain.exception.dto;

/**
 * 406 Not acceptable
 */
public class NotAllowedAccessException extends RuntimeException{
    public NotAllowedAccessException() {
        super();
    }
    public NotAllowedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    public NotAllowedAccessException(String message) {
        super(message);
    }
    public NotAllowedAccessException(Throwable cause) {
        super(cause);
    }
}
