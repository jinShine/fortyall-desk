package com.buzz.fortyall_desk.auth.service;

import com.buzz.fortyall_desk.account.entity.Account;
import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.account.entity.MembershipRole;
import com.buzz.fortyall_desk.account.entity.Role;
import com.buzz.fortyall_desk.account.repository.AccountRepository;
import com.buzz.fortyall_desk.account.repository.MembershipRepository;
import com.buzz.fortyall_desk.auth.dto.AuthPrincipal;
import com.buzz.fortyall_desk.auth.dto.AuthDto.*;
import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.center.repository.CenterRepository;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final OtpService otpService;
    private final TokenStore tokenStore;
    private final AccountRepository accountRepository;
    private final MembershipRepository membershipRepository;
    private final CenterRepository centerRepository;

    public void requestOtp(String phone) {
        otpService.request(phone);
    }

    @Transactional
    public LoginResult signupCenter(CenterSignupRequest req) {
        otpService.verify(req.phone(), req.code());

        Account account = accountRepository.findByLoginPhone(req.phone())
                .orElseGet(() -> accountRepository.save(new Account(req.phone())));

        Center center = centerRepository.save(new Center(req.centerName()));

        Membership admin = new Membership(center, req.adminName(), req.phone());
        admin.addRole(Role.ADMIN);
        admin.addRole(Role.COACH);
        admin.activate(account);
        membershipRepository.save(admin);

        return issue(account, admin);
    }

    @Transactional
    public LoginResult verifyOtp(String phone, String code) {
        otpService.verify(phone, code);

        Account account = accountRepository.findByLoginPhone(phone)
                .orElseGet(() -> accountRepository.save(new Account(phone)));

        membershipRepository
                .findByContactPhoneAndStatus(phone, Membership.MembershipStatus.PENDING)
                .ifPresent(pending -> pending.activate(account));

        List<Membership> memberships = membershipRepository.findAllByAccountId(account.getId());
        if (memberships.isEmpty()) {
            throw new ApiException(ErrorCode.FORBIDDEN, "소속된 센터가 없습니다. 센터 관리자에게 문의하세요.");
        }
        if (memberships.size() == 1) {
            return issue(account, memberships.get(0));
        }

        String tempToken = tokenStore.issue(new AuthPrincipal(account.getId(), null, null, Set.of()));
        return new LoginResult(null, tempToken,
                memberships.stream().map(AuthService::summarize).collect(Collectors.toList()), null);
    }

    @Transactional(readOnly = true)
    public LoginResult selectCenter(String tempToken, Long centerId) {
        AuthPrincipal temp = tokenStore.resolve(tempToken)
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID));

        Membership target = membershipRepository.findAllByAccountId(temp.accountId()).stream()
                .filter(m -> m.getCenter().getId().equals(centerId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION));

        tokenStore.revoke(tempToken);
        return issue(target.getAccount(), target);
    }

    private LoginResult issue(Account account, Membership membership) {
        Set<Role> roles = membership.getRoles().stream()
                .map(MembershipRole::getRole).collect(Collectors.toSet());
        String token = tokenStore.issue(new AuthPrincipal(
                account.getId(), membership.getId(), membership.getCenter().getId(), roles));
        return new LoginResult(token, null, null, summarize(membership));
    }

    private static CenterSummary summarize(Membership m) {
        return new CenterSummary(m.getCenter().getId(), m.getCenter().getName(), m.getId(),
                m.getRoles().stream().map(MembershipRole::getRole).collect(Collectors.toSet()));
    }
}
