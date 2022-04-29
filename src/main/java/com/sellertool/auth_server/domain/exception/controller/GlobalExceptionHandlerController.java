package com.sellertool.auth_server.domain.exception.controller;

import com.sellertool.auth_server.domain.exception.dto.*;
import com.sellertool.auth_server.domain.message.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandlerController {
    @ExceptionHandler(value = {AccessDeniedPermissionException.class})
    public ResponseEntity<?> accessDeniedPermissionException(AccessDeniedPermissionException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.FORBIDDEN);
        message.setMessage("access_denied");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {InvalidUserAuthException.class})
    public ResponseEntity<?> invalidUserAuthException(InvalidUserAuthException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.UNAUTHORIZED);
        message.setMessage("invalid_user");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {ConflictErrorException.class})
    public ResponseEntity<?> conflictErrorException(ConflictErrorException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.CONFLICT);
        message.setMessage("conflicted");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {NotAllowedAccessException.class})
    public ResponseEntity<?> notAllowedAccessException(NotAllowedAccessException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.NOT_ACCEPTABLE);
        message.setMessage("not_acceptable");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {NotMatchedFormatException.class})
    public ResponseEntity<?> notMatchedFormatException(NotMatchedFormatException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.BAD_REQUEST);
        message.setMessage("not_matched_format");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {UserInfoAuthJwtException.class})
    public ResponseEntity<?> userInfoAuthException(UserInfoAuthJwtException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.BAD_REQUEST);
        message.setMessage("user_info_auth_error");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {EmailAuthException.class})
    public ResponseEntity<?> emailAuthException(EmailAuthException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.BAD_REQUEST);
        message.setMessage("email_send_error");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.BAD_REQUEST);
        message.setMessage("data_error");
        message.setMemo("올바르지 않은 데이터 형식이 존재합니다.");
        return new ResponseEntity<>(message, message.getStatus());
    }
}
