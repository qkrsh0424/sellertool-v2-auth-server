package com.sellertool.auth_server.domain.exception.dto;

/**
 * 401
 */
public class InvalidUserAuthException extends RuntimeException{
    public InvalidUserAuthException() {
        super();
    }
    public InvalidUserAuthException(String message, Throwable cause) {
        super(message, cause);
    }
    public InvalidUserAuthException(String message) {
        super(message);
    }
    public InvalidUserAuthException(Throwable cause) {
        super(cause);
    }
}
