package com.sellertool.auth_server.domain.exception.dto;

/**
 * 400
 */
public class NotMatchedFormatException extends RuntimeException{
    public NotMatchedFormatException() {
        super();
    }
    public NotMatchedFormatException(String message, Throwable cause) {
        super(message, cause);
    }
    public NotMatchedFormatException(String message) {
        super(message);
    }
    public NotMatchedFormatException(Throwable cause) {
        super(cause);
    }
}
