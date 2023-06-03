package com.myorg.nuxeo.proxylayer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GeneralExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Error", "Please Contact SmartDoc Support"));
    }
}
