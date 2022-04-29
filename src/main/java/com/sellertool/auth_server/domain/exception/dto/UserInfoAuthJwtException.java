package com.sellertool.auth_server.domain.exception.dto;

public class UserInfoAuthJwtException extends RuntimeException {
    public UserInfoAuthJwtException() {
    }

    public UserInfoAuthJwtException(String message) {
        super(message);
    }

    public UserInfoAuthJwtException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserInfoAuthJwtException(Throwable cause) {
        super(cause);
    }
}
