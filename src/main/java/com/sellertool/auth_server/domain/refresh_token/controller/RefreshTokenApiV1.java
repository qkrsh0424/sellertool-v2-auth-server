package com.sellertool.auth_server.domain.refresh_token.controller;

import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.domain.refresh_token.service.RefreshTokenBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth/v1/refresh")
public class RefreshTokenApiV1 {
    private final RefreshTokenBusinessService refreshTokenBusinessService;

    @Autowired
    public RefreshTokenApiV1(
            RefreshTokenBusinessService refreshTokenBusinessService
    ) {
        this.refreshTokenBusinessService = refreshTokenBusinessService;
    }

    @PostMapping("")
    public ResponseEntity<?> getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        Message message = new Message();

        refreshTokenBusinessService.issueAccessToken(request, response);
        message.setStatus(HttpStatus.OK);
        message.setMessage("success");

        return new ResponseEntity<>(message, message.getStatus());
    }
}
