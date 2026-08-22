package com.buzz.fortyall_desk.auth.support;

import com.buzz.fortyall_desk.auth.dto.AuthPrincipal;
import com.buzz.fortyall_desk.auth.service.TokenStore;
import com.buzz.fortyall_desk.account.entity.Role;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final TokenStore tokenStore;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.TOKEN_INVALID);
        }
        AuthPrincipal principal = tokenStore.resolve(header.substring(7))
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID));

        String path = request.getRequestURI();
        if (path.startsWith("/api/admin") && !principal.has(Role.ADMIN)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        if (path.startsWith("/api/coach")
                && !(principal.has(Role.COACH) || principal.has(Role.ADMIN))) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        TenantContext.set(principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
