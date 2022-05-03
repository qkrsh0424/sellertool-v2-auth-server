package com.sellertool.auth_server.domain.exception.controller;

import com.sellertool.auth_server.domain.message.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandlerController {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<?> methodArgumentNotValidException(MethodArgumentNotValidException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.BAD_REQUEST);
        message.setMessage("data_invalidation");
        message.setMemo("올바르지 않은 데이터 형식이 존재합니다.");
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {FileNotFoundException.class})
    public ResponseEntity<?> fileNotFoundException(FileNotFoundException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.NOT_FOUND);
        message.setMessage("file_not_found");
        message.setMemo("요청된 파일을 찾을 수 없습니다.");
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<?> ExceptionErrorHandler(Exception e) {
        log.error("ERROR STACKTRACE => {}", e.getStackTrace());

        Message message = new Message();
        message.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        message.setMessage("error");
        message.setMemo("undefined error.");

        return new ResponseEntity<>(message, message.getStatus());
    }
}
