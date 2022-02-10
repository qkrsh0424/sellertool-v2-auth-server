package com.sellertool.auth_server.domain.user.controller;

import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.domain.user.service.UserBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/v1/user")
public class UserApiV1 {
    private final UserBusinessService userBusinessService;

    @Autowired
    public UserApiV1(
            UserBusinessService userBusinessService
    ) {
        this.userBusinessService = userBusinessService;
    }

    @GetMapping("/info/own")
    public ResponseEntity<?> getInfoOwn(){
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        message.setData(userBusinessService.getInfoOwn());

        return new ResponseEntity<>(message, message.getStatus());
    }

    @GetMapping("/check/username-duplicate")
    public ResponseEntity<?> checkUsernameDuplicate(@RequestParam Map<String, Object> params) {
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        message.setData(userBusinessService.checkUsernameDuplicate(params));

        return new ResponseEntity<>(message, message.getStatus());
    }
}
