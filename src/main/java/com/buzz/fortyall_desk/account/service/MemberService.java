package com.buzz.fortyall_desk.account.service;

import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.account.entity.MembershipRole;
import com.buzz.fortyall_desk.account.entity.Role;
import com.buzz.fortyall_desk.account.repository.MembershipRepository;
import com.buzz.fortyall_desk.account.dto.MemberDto.*;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.center.repository.CenterRepository;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MembershipRepository membershipRepository;
    private final CenterRepository centerRepository;

    @Transactional
    public MemberView create(CreateRequest req) {
        var center = centerRepository.findById(TenantContext.centerId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        Membership membership = new Membership(center, req.name(), req.contactPhone());
        Set<Role> roles = (req.roles() == null || req.roles().isEmpty())
                ? Set.of(Role.MEMBER) : req.roles();
        roles.forEach(membership::addRole);

        membershipRepository.save(membership);
        log.info("[회원 생성] {} ({}) — 활성화 대기. 첫 OTP 인증 시 계정과 연결된다",
                req.name(), req.contactPhone());
        return view(membership);
    }

    @Transactional(readOnly = true)
    public List<MemberView> list() {
        return membershipRepository.findAllByCenterIdOrderByNameAsc(TenantContext.centerId())
                .stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public MemberView get(Long membershipId) {
        return membershipRepository.findByIdAndCenterId(membershipId, TenantContext.centerId())
                .map(this::view)
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION));
    }

    private MemberView view(Membership m) {
        return new MemberView(m.getId(), m.getName(), m.getContactPhone(), m.getStatus().name(),
                m.getRoles().stream().map(MembershipRole::getRole).collect(Collectors.toSet()),
                m.getAccount() != null);
    }
}
