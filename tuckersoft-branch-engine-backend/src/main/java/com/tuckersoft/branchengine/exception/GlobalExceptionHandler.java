package com.tuckersoft.branchengine.exception;

import com.tuckersoft.branchengine.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    ResponseEntity<ErrorResponse> app(AppException e,HttpServletRequest r){
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getType(),e.getMessage(),Instant.now(),r.getRequestURI()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e,HttpServletRequest r){
        String msg=e.getBindingResult().getFieldErrors().stream().findFirst()
            .map(x->x.getField()+": "+x.getDefaultMessage()).orElse("Validation failed");
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR",msg,Instant.now(),r.getRequestURI()));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> other(Exception e,HttpServletRequest r){
        return ResponseEntity.status(500).body(new ErrorResponse("INTERNAL_ERROR",e.getMessage()==null?"Unexpected error":e.getMessage(),Instant.now(),r.getRequestURI()));
    }
}
