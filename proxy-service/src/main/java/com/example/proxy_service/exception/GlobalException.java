package com.example.proxy_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(
            ResponseStatusException exception) {

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(exception.getReason());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(
            Exception exception) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong. Please try again later.");
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(
            AccessDeniedException exception) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Access denied");
    }
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParameter(
            MissingServletRequestParameterException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Required parameter is missing: " + exception.getParameterName());
    }
}
