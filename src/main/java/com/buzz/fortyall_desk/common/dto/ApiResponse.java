package com.buzz.fortyall_desk.common.dto;

import java.util.List;

public record ApiResponse<T>(T data, List<Warning> warnings) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> of(T data, List<Warning> warnings) {
        return new ApiResponse<>(data, warnings == null || warnings.isEmpty() ? null : warnings);
    }

    public record Warning(String code, String message) {}
}
