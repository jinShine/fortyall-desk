package com.buzz.fortyall_desk.auth.controller;

import com.buzz.fortyall_desk.auth.service.AuthService;
import com.buzz.fortyall_desk.auth.dto.AuthDto.*;
import com.buzz.fortyall_desk.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/otp/request")
    public ApiResponse<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequest req) {
        authService.requestOtp(req.phone());
        return ApiResponse.of(Map.of("message", "인증번호를 발송했습니다. 서버 콘솔을 확인하세요."));
    }

    @PostMapping("/otp/verify")
    public ApiResponse<LoginResult> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        return ApiResponse.of(authService.verifyOtp(req.phone(), req.code()));
    }

    @PostMapping("/centers/signup")
    public ApiResponse<LoginResult> signupCenter(@Valid @RequestBody CenterSignupRequest req) {
        return ApiResponse.of(authService.signupCenter(req));
    }

    @PostMapping("/centers/{centerId}/select")
    public ApiResponse<LoginResult> selectCenter(@PathVariable Long centerId,
                                                 @Valid @RequestBody CenterSelectRequest req) {
        return ApiResponse.of(authService.selectCenter(req.tempToken(), centerId));
    }
}
