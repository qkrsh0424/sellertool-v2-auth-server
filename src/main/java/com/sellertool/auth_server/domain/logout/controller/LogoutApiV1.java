package com.sellertool.auth_server.domain.logout.controller;

import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.utils.CustomCookieInterface;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth/v1/logout")
public class LogoutApiV1 {
    @PostMapping("")
    public ResponseEntity<?> logout(HttpServletResponse response){
        Message message = new Message();

        ResponseCookie accessToken = ResponseCookie.from("st_actoken", null)
                .path("/")
                .httpOnly(true)
                .sameSite("Strict")
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .maxAge(0)
                .secure(CustomCookieInterface.SECURE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessToken.toString());

        ResponseCookie tokenExpireTimeCookie = ResponseCookie.from("st_auth_exp", null)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .secure(CustomCookieInterface.SECURE)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, tokenExpireTimeCookie.toString());

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        message.setMemo("logout");

        return new ResponseEntity<>(message, message.getStatus());
    }
}
