package org.k1000o.ms.asignacion.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleException(
            ResourceNotFoundException exp,
            HttpServletRequest request
    ) {
        return response(request.getRequestURI(), exp.getMessage(),HttpStatus.NOT_FOUND,null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exp,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        exp.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return response(request.getRequestURI(), exp.getMessage(),HttpStatus.BAD_REQUEST,errors);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(
            Exception exp,
            HttpServletRequest request
    ) {
        return response(request.getRequestURI(), exp.getMessage() +exp.getClass().getSimpleName() ,HttpStatus.INTERNAL_SERVER_ERROR,null);
    }

    private ResponseEntity<ApiError> response(String uri,String message, HttpStatus httpStatus,Map<String, String> errors) {
        ApiError apiError = ApiError.builder()
                .path(uri)
                .message(message)
                .statusCode(httpStatus.value())
                //.timestamp(Instant.now())
                .errors(errors)
                .build();
        return new ResponseEntity<>(apiError, httpStatus);
    }
}
