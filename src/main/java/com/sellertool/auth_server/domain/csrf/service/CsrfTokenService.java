package com.sellertool.auth_server.domain.csrf.service;

import com.sellertool.auth_server.utils.CsrfTokenUtils;
import com.sellertool.auth_server.utils.CustomCookieInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
public class CsrfTokenService {

    @Value("${csrf.token.secret}")
    private String csrfTokenSecret;
    
    private CsrfTokenUtils csrfTokenUtils;
    
    public void getCsrfToken(HttpServletResponse response) {
        this.csrfTokenUtils = new CsrfTokenUtils(csrfTokenSecret);

        // 토큰 생성 및 쿠키 설정
        String csrfTokenId = UUID.randomUUID().toString();
        String csrfJwtToken = CsrfTokenUtils.getCsrfJwtToken(csrfTokenId);

        ResponseCookie csrfJwt = ResponseCookie.from("auth_csrf_jwt", csrfJwtToken)
            .httpOnly(true)
            .domain(CustomCookieInterface.COOKIE_DOMAIN)
            .secure(CustomCookieInterface.SECURE)
            .sameSite("Strict")
            .path("/")
            .maxAge(CustomCookieInterface.CSRF_TOKEN_COOKIE_EXPIRATION)
            .build();

        ResponseCookie csrfToken = ResponseCookie.from("auth_csrf", csrfTokenId)
                .secure(CustomCookieInterface.SECURE)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .sameSite("Strict")
                .path("/")
                .maxAge(CustomCookieInterface.CSRF_TOKEN_COOKIE_EXPIRATION)
                .build();

        
        response.addHeader(HttpHeaders.SET_COOKIE, csrfJwt.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, csrfToken.toString());
//        response.setHeader("X-XSRF-TOKEN", csrfTokenId);
    }
}
