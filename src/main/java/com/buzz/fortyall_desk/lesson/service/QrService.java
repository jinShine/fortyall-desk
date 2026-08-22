package com.buzz.fortyall_desk.lesson.service;

import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import com.buzz.fortyall_desk.lesson.dto.LessonDto.QrToken;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QrService {
    private static final int TTL_SECONDS = 60;
    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] secret;

    public QrService(@Value("${fortyall.qr.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public QrToken issue(Long membershipId) {
        long expiresAt = Instant.now().getEpochSecond() + TTL_SECONDS;
        String payload = membershipId + ":" + expiresAt;
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(
                (payload + ":" + sign(payload)).getBytes(StandardCharsets.UTF_8));
        return new QrToken(token, Instant.ofEpochSecond(expiresAt).toString(), TTL_SECONDS);
    }

    public Long verify(String token) {
        String decoded;
        try {
            decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.QR_INVALID);
        }
        String[] parts = decoded.split(":");
        if (parts.length != 3) throw new ApiException(ErrorCode.QR_INVALID);

        String payload = parts[0] + ":" + parts[1];
        if (!sign(payload).equals(parts[2])) {
            throw new ApiException(ErrorCode.QR_INVALID, "QR 서명이 일치하지 않습니다");
        }
        if (Long.parseLong(parts[1]) < Instant.now().getEpochSecond()) {
            throw new ApiException(ErrorCode.QR_INVALID, "QR이 만료되었습니다. 회원 화면을 새로 고쳐주세요.");
        }
        return Long.parseLong(parts[0]);
    }

    @SneakyThrows
    private String sign(String payload) {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(secret, ALGORITHM));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
