package com.buzz.fortyall_desk.auth.dto;

import com.buzz.fortyall_desk.account.entity.Role;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;

public final class AuthDto {
    private AuthDto() {}

    public record OtpRequest(@NotBlank String phone) {}

    public record OtpVerifyRequest(@NotBlank String phone, @NotBlank String code) {}

    public record CenterSignupRequest(@NotBlank String phone, @NotBlank String code,
                                      @NotBlank String centerName, @NotBlank String adminName) {}

    public record CenterSelectRequest(@NotBlank String tempToken) {}

    public record CenterSummary(Long centerId, String centerName, Long membershipId, Set<Role> roles) {}

    public record LoginResult(String accessToken, String tempToken,
                             List<CenterSummary> centers, CenterSummary current) {}
}
