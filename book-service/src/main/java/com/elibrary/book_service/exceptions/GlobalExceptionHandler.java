package com.elibrary.book_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CopyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCopyNotFound(CopyNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(StatusMatchingException.class)
    public ResponseEntity<Map<String, String>> handleStatusMatching(StatusMatchingException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TitleAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleTitleAlreadyExists(TitleAlreadyExistsException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", ex.getMessage());
        if (ex.getExistingId() != null) {
            error.put("existingId", ex.getExistingId());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TitleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTitleNotFound(TitleNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ActiveLoansExistException.class)
    public ResponseEntity<Map<String, String>> handleActiveLoansExist(ActiveLoansExistException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
