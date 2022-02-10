package com.sellertool.auth_server.domain.signup.controller;

import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.domain.signup.dto.SignupDto;
import com.sellertool.auth_server.domain.signup.service.SignupBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/v1/signup")
public class SignupApiV1 {
    private final SignupBusinessService signupBusinessService;

    @Autowired
    public SignupApiV1(
            SignupBusinessService signupBusinessService
    ) {
        this.signupBusinessService = signupBusinessService;
    }

    @PostMapping("")
    public ResponseEntity<?> signup(@RequestBody SignupDto signupDto) {
        Message message = new Message();

        signupBusinessService.signup(signupDto);

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        return new ResponseEntity<>(message, message.getStatus());
    }
}
