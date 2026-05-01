package org.practice.sporty.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> badJson(HttpMessageNotReadableException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        log.warn("json: {}", msg);
        return ResponseEntity.badRequest().body(err("bad_request", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badArg(IllegalArgumentException ex) {
        log.warn("arg: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(err("bad_request", ex.getMessage()));
    }

    private static Map<String, Object> err(String code, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", code);
        m.put("message", message);
        return m;
    }
}
