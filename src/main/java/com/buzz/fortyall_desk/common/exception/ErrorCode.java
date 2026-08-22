package com.buzz.fortyall_desk.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    OTP_INVALID(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다"),
    OTP_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),

    TENANT_VIOLATION(HttpStatus.FORBIDDEN, "다른 센터의 데이터입니다"),

    PASS_NOT_FOUND(HttpStatus.NOT_FOUND, "수업권을 찾을 수 없습니다"),
    PASS_NOT_ACTIVE(HttpStatus.CONFLICT, "사용할 수 없는 수업권입니다"),
    PASS_EXPIRED(HttpStatus.CONFLICT, "수업권이 만료되었습니다"),
    PASS_INSUFFICIENT(HttpStatus.CONFLICT, "예약 가능 횟수가 부족합니다"),

    SLOT_TAKEN(HttpStatus.CONFLICT, "이미 예약된 시간입니다"),
    LESSON_NOT_FOUND(HttpStatus.NOT_FOUND, "레슨을 찾을 수 없습니다"),
    ALREADY_ATTENDED(HttpStatus.CONFLICT, "이미 출석 처리된 레슨입니다"),
    QR_INVALID(HttpStatus.BAD_REQUEST, "QR 토큰이 유효하지 않습니다"),

    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다"),
    IDEMPOTENCY_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "Idempotency-Key 헤더가 필요합니다");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() { return status; }
    public String message() { return message; }
}
