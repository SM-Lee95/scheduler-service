package com.util.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /** 서버 오류 발생 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> handleException(Exception e) {
        log.error("handleException", e);
        return ResponseEntity.ok(e.getMessage());
    }
}
