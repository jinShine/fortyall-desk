package com.buzz.fortyall_desk.auth.dto;

import com.buzz.fortyall_desk.account.entity.Role;
import java.util.Set;

public record AuthPrincipal(Long accountId, Long membershipId, Long centerId, Set<Role> roles) {
    public boolean has(Role role) { return roles.contains(role); }
}
