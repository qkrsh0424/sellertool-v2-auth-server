package com.sellertool.auth_server.domain.user.controller;

import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.domain.user.dto.UserDto;
import com.sellertool.auth_server.domain.user.service.UserBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
    public ResponseEntity<?> getInfoOwn(HttpServletResponse response){
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        message.setData(userBusinessService.getInfoOwn(response));

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

    @PutMapping("/info/own")
    public ResponseEntity<?> updateInfoOwn(HttpServletRequest request, HttpServletResponse response, @RequestBody @Valid UserDto userDto){
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        userBusinessService.updateInfoOwn(request, response, userDto);

        return new ResponseEntity<>(message, message.getStatus());
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> password){
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        userBusinessService.changePassword(password);

        return new ResponseEntity<>(message, message.getStatus());
    }
}
