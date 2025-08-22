package com.KDT.mosi.web.exception;

import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException; // ✨✨✨ import 추가

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice   // Controller에서 발생된 예외를 처리하는 클래스라는 것를 springboot에 알림
public class ApiExceptionHandler {

    /**
     * 유효성 검증 실패 시 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
        MethodArgumentNotValidException ex) {

        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> details = new HashMap<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.error("Validation error: {}", details);

        ApiResponse<Void> response = ApiResponse.withDetails(
            ApiResponseCode.VALIDATION_ERROR,
            details,
            null
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 비즈니스 유효성 검증 예외 처리
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessValidationException(
        BusinessValidationException ex) {

        log.error("Business validation error: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.withDetails(
            ApiResponseCode.BUSINESS_ERROR,
            ex.getDetails(),
            null
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 엔티티를 찾을 수 없을 때 처리
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElementException(
        NoSuchElementException ex) {

        log.error("Entity not found: {}", ex.getMessage());
        Map<String, String> map = new HashMap<>();
        map.put("1", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.withDetails(
            ApiResponseCode.ENTITY_NOT_FOUND,
            map,
            null
        );
        return ResponseEntity.ok(response);
    }

    /**
     * static resource (정적 자원) 예외 처리
     */
    @ExceptionHandler(NoResourceFoundException.class) // ✨✨✨ 이 핸들러를 추가합니다.
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
        // ✨✨✨ 로그를 남기지 않거나, INFO 레벨로만 남깁니다.
        log.info("Requested static resource not found: {}", ex.getMessage());

        // ✨✨✨ 404 Not Found 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * 기타 예외 처리 (NoResourceFoundException 제외)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception occurred", ex);

        ApiResponse<Void> response = ApiResponse.of(
            ApiResponseCode.INTERNAL_SERVER_ERROR,
            null
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    //파일크기 확인
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileSizeLimit(MaxUploadSizeExceededException ex) {
        Map<String,String> details = Map.of(
            "maxFileSize",  "5MB"
        );
        ApiResponse<Void> body = ApiResponse.withDetails(
            ApiResponseCode.FILE_TOO_LARGE,   // ← 상수명만 맞춰주면 끝
            details,
            null
        );
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE) // 413
            .body(body);
    }
}