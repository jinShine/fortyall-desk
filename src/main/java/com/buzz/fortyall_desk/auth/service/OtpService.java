package com.buzz.fortyall_desk.auth.service;

import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OtpService {
    private static final Duration TTL = Duration.ofMinutes(5);

    private static final String DEV_CODE = "123456";

    private final Map<String, Otp> issued = new ConcurrentHashMap<>();

    public void request(String phone) {
        Otp otp = new Otp(DEV_CODE, LocalDateTime.now().plus(TTL));
        issued.put(phone, otp);
        log.info("[SMS mock] {} 님께 인증번호 {} 발송 (유효 {}분)", phone, otp.code(), TTL.toMinutes());
    }

    public void verify(String phone, String code) {
        Otp otp = issued.get(phone);
        if (otp == null || !otp.code().equals(code)) throw new ApiException(ErrorCode.OTP_INVALID);
        if (otp.expiresAt().isBefore(LocalDateTime.now())) throw new ApiException(ErrorCode.OTP_EXPIRED);
        issued.remove(phone);
    }

    private record Otp(String code, LocalDateTime expiresAt) {}
}
