package com.sellertool.auth_server.utils;

public interface CustomCookieInterface {

    final static String COOKIE_DOMAIN = "localhost"; // PROD : viewday.co.kr | DEV : localhost

    final static Integer JWT_TOKEN_COOKIE_EXPIRATION = 5 * 24 * 60 * 60; // seconds - 5일
    final static Integer CSRF_TOKEN_COOKIE_EXPIRATION = 5; // seconds - 5s

    final static Integer PHONE_AUTH_COOKIE_EXPIRATION = 5 * 60; // seconds - 5분
    final static Integer PHONE_AUTH_VF_COOKIE_EXPIRATION = 10 * 60; // seconds - 10분

    final static Integer EMAIL_AUTH_COOKIE_EXPIRATION = 5 * 60; // seconds - 5분
    final static Integer EMAIL_AUTH_VF_COOKIE_EXPIRATION = 10 * 60; // seconds - 10분

    final static boolean SECURE = false; // PROD : true | DEV : false
}
