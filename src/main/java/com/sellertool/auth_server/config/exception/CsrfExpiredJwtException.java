package com.sellertool.auth_server.config.exception;

public class CsrfExpiredJwtException extends RuntimeException{
    public CsrfExpiredJwtException(String msg) {
        super(msg);
    }
    public CsrfExpiredJwtException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
