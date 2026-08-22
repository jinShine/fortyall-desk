package com.buzz.fortyall_desk.auth.support;

import com.buzz.fortyall_desk.auth.dto.AuthPrincipal;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;

public final class TenantContext {
    private static final ThreadLocal<AuthPrincipal> HOLDER = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(AuthPrincipal principal) { HOLDER.set(principal); }

    public static void clear() { HOLDER.remove(); }

    public static AuthPrincipal current() {
        AuthPrincipal p = HOLDER.get();
        if (p == null) throw new ApiException(ErrorCode.TOKEN_INVALID);
        return p;
    }

    public static Long centerId() { return current().centerId(); }

    public static Long membershipId() { return current().membershipId(); }
}
