package com.wjc.codetest;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * GlobalExceptionHandler
 *
 * 1. 문제:
 *    - 컨트롤러별 개별 예외 처리 로직 부재:
 *        • Validation 실패, 비즈니스 예외, 시스템 예외가 동일하게 500으로 응답됨
 *        • 클라이언트가 오류 원인을 파악하기 어려움
 *    - 응답 포맷 불일치:
 *        • 예외 발생 시 ResponseEntity<String> 형태로 반환되어 상태 코드 외의 정보 부재
 *        • API 문서 및 로그에서 에러 분석 어려움
 *
 * 2. 원인:
 *    - 초기 구조에서 @ControllerAdvice 및 @ExceptionHandler 기반 전역 예외 처리 미도입
 *    - 예외 타입별 구분(Validation, Runtime, System)에 대한 처리 미흡
 *
 * 3. 개선안:
 *    - 전역 예외 처리 클래스(GlobalExceptionHandler) 도입:
 *        • @ControllerAdvice로 모든 컨트롤러의 예외를 한 곳에서 처리
 *        • 예외 유형별 @ExceptionHandler 메서드 추가:
 *            → MethodArgumentNotValidException (DTO Validation 실패)
 *            → ConstraintViolationException (단일 파라미터 검증 실패)
 *            → RuntimeException (비즈니스 로직 예외)
 *            → Exception (그 외 예기치 못한 예외)
 *        • ErrorResponse DTO를 통해 상태 코드, 에러 유형, 메시지, 발생 시각을 통일된 JSON 포맷으로 반환
 */

@Slf4j
@ControllerAdvice(value = {"com.wjc.codetest.product.controller"})
public class GlobalExceptionHandler {

    /** DTO Validation 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("[ValidationError] {}", message);
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), message));
    }

    /** 단일 파라미터 검증 실패 (@RequestParam, @PathVariable 등) */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("[ConstraintViolation] {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), ex.getMessage()));
    }

    /** 비즈니스 예외 (예: 상품을 찾을 수 없음) */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("[RuntimeException] {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.name(), ex.getMessage()));
    }

    /** 그 외 모든 예외 처리 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("[UnhandledException]", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.name(), "Unexpected error occurred"));
    }
}