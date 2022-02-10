package com.sellertool.auth_server.config.exception;

public class CsrfAccessDeniedException extends RuntimeException{
    public CsrfAccessDeniedException(String msg) {
        super(msg);
    }
    public CsrfAccessDeniedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
