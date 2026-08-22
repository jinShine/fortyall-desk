package com.buzz.fortyall_desk.auth.service;

import com.buzz.fortyall_desk.auth.dto.AuthPrincipal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {
    private final Map<String, AuthPrincipal> tokens = new ConcurrentHashMap<>();

    public String issue(AuthPrincipal principal) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, principal);
        return token;
    }

    public Optional<AuthPrincipal> resolve(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void revoke(String token) { tokens.remove(token); }
}
