package com.buzz.fortyall_desk.account.dto;

import com.buzz.fortyall_desk.account.entity.Role;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;

public final class MemberDto {
    private MemberDto() {}

    public record CreateRequest(@NotBlank String name, @NotBlank String contactPhone,
                                Set<Role> roles) {}

    public record MemberView(Long membershipId, String name, String contactPhone,
                             String status, Set<Role> roles, boolean activated) {}

    public record MemberDetail(MemberView member, List<Object> passes) {}
}
