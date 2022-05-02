package com.sellertool.auth_server.domain.user_info_auth.controller;

import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.domain.user_info_auth.service.UserInfoAuthBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth/v1/user-info-auth")
@RequiredArgsConstructor
public class UserInfoAuthApiV1 {
    private final UserInfoAuthBusinessService userInfoAuthBusinessService;

    @GetMapping("/phone")
    public ResponseEntity<?> getPhoneAuthNumber(@RequestParam Map<String, Object> params, HttpServletResponse response){
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        userInfoAuthBusinessService.getPhoneAuthNumber(params, response);

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/phone/verify")
    public ResponseEntity<?> verifyPhoneAuthNumber(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        userInfoAuthBusinessService.verifyPhoneAuthNumber(request, response, params);

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/email")
    public ResponseEntity<?> getEmailAuthNumber(@RequestParam Map<String, Object> params, HttpServletResponse response) throws IOException {
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        userInfoAuthBusinessService.getEmailAuthNumber(params, response);

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/email/verify")
    public ResponseEntity<?> verifyEmailAuthNumber(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        userInfoAuthBusinessService.verifyEmailAuthNumber(request, response, params);

        return new ResponseEntity<>(message, message.getStatus());
    }
}
