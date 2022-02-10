package com.sellertool.auth_server.config.exception;

public class CsrfNullPointerException extends RuntimeException{
    public CsrfNullPointerException(String msg) {
        super(msg);
    }
    public CsrfNullPointerException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
