package com.sellertool.auth_server.utils;

public interface CustomJwtInterface {
    final static Integer JWT_TOKEN_EXPIRATION = 10 * 60 * 1000;  // milliseconds - 10분
//    final static Integer JWT_TOKEN_EXPIRATION = 1 * 1000;  // milliseconds - 10분

    final static Integer REFRESH_TOKEN_JWT_EXPIRATION = 5*24*60*60*1000;   // milliseconds - 5일/**/
//    final static Integer REFRESH_TOKEN_JWT_EXPIRATION = 10*1000;   // milliseconds - 5일

    final static Integer CSRF_TOKEN_JWT_EXPIRATION = 5 * 1000;  // milliseconds - 5000ms -> 5s
}
