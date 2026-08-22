package com.buzz.fortyall_desk.pass.service;

import com.buzz.fortyall_desk.pass.entity.LessonPass;
import com.buzz.fortyall_desk.pass.entity.PassTransaction;
import com.buzz.fortyall_desk.pass.repository.LessonPassRepository;
import com.buzz.fortyall_desk.pass.repository.PassTransactionRepository;
import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.account.repository.MembershipRepository;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import com.buzz.fortyall_desk.lesson.repository.LessonParticipantRepository;
import com.buzz.fortyall_desk.pass.entity.LessonPass.PassStatus;
import com.buzz.fortyall_desk.pass.dto.PassDto.*;
import com.buzz.fortyall_desk.pass.entity.PassTransaction.TransactionType;
import com.buzz.fortyall_desk.product.entity.Product;
import com.buzz.fortyall_desk.product.repository.ProductRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassService {
    private final LessonPassRepository passRepository;
    private final PassTransactionRepository transactionRepository;
    private final MembershipRepository membershipRepository;
    private final ProductRepository productRepository;
    private final LessonParticipantRepository participantRepository;

    @Transactional
    public IssueResult issue(IssueRequest req) {
        Long centerId = TenantContext.centerId();

        Membership membership = membershipRepository
                .findByIdAndCenterId(req.membershipId(), centerId)
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION));
        Product product = productRepository
                .findByIdAndCenterId(req.productId(), centerId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        LocalDate validFrom = req.validFrom() != null ? req.validFrom() : LocalDate.now();
        LessonPass pass = passRepository.save(
                new LessonPass(membership, product, req.paidAmount(), validFrom, req.prepaid()));

        transactionRepository.save(new PassTransaction(
                pass, TransactionType.ISSUE, product.getSessionCount(), null,
                "%s 발급 (%,d원 승인)".formatted(product.getName(), req.paidAmount().intValue())));

        int remaining = remaining(pass.getId());
        log.info("[발급] membership={} pass={} +{}회 → 잔여 {}회",
                membership.getName(), pass.getId(), product.getSessionCount(), remaining);

        return new IssueResult(pass.getId(), pass.getStatus().name(),
                product.getSessionCount(), remaining,
                "%s %d회 발급 완료 — 사용 가능 %d회"
                        .formatted(product.getName(), product.getSessionCount(), remaining));
    }

    @Transactional(readOnly = true)
    public int remaining(Long passId) {
        return transactionRepository.sumDeltaByPassId(passId);
    }

    @Transactional(readOnly = true)
    public int reserved(Long passId) {
        return participantRepository.countReservedByPassId(passId);
    }

    @Transactional
    public int record(LessonPass pass, TransactionType type, int delta,
                      Long participantId, String memo) {
        transactionRepository.save(new PassTransaction(pass, type, delta, participantId, memo));
        int remaining = remaining(pass.getId());

        if (remaining <= 0 && pass.getStatus() == PassStatus.ACTIVE) {
            pass.markExhausted();
            log.info("[소진] pass={} — 재등록은 새 Pass 발급으로만 (불변식 2)", pass.getId());
        } else if (delta > 0 && pass.getStatus() == PassStatus.EXHAUSTED) {
            pass.restoreToActive(remaining, LocalDate.now());
        }
        return remaining;
    }

    @Transactional
    public PassView adjust(Long passId, int delta, String memo) {
        LessonPass pass = loadInCenter(passId);
        record(pass, TransactionType.ADJUST, delta, null,
                memo == null ? "관리자 조정" : memo);
        return view(pass);
    }

    @Transactional
    public PassView restore(Long passId, Long participantId, String memo) {
        LessonPass pass = loadInCenter(passId);
        record(pass, TransactionType.RESTORE, 1, participantId,
                memo == null ? "노쇼 차감 복구" : memo);
        return view(pass);
    }

    @Transactional(readOnly = true)
    public List<PassView> listByMembership(Long membershipId) {
        membershipRepository.findByIdAndCenterId(membershipId, TenantContext.centerId())
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION));
        return passRepository.findAllByMembershipIdOrderByIdAsc(membershipId).stream()
                .map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public LedgerView ledger(Long passId) {
        LessonPass pass = loadInCenter(passId);
        List<TransactionView> rows = new ArrayList<>();
        int balance = 0;
        for (PassTransaction t : transactionRepository.findAllByLessonPassIdOrderByIdAsc(passId)) {
            balance += t.getDelta();
            rows.add(new TransactionView(t.getId(), t.getType().name(), t.getDelta(), balance,
                    t.getMemo(), t.getCreatedAt().toString()));
        }
        return new LedgerView(passId, pass.getProductNameSnapshot(), balance, rows);
    }

    @Transactional(readOnly = true)
    public LessonPass validateForBooking(Long passId, LocalDate lessonDate) {
        LessonPass pass = loadInCenter(passId);
        if (pass.getStatus() != PassStatus.ACTIVE) throw new ApiException(ErrorCode.PASS_NOT_ACTIVE);
        if (!pass.usableFor(lessonDate)) throw new ApiException(ErrorCode.PASS_EXPIRED);
        if (remaining(passId) - reserved(passId) <= 0) {
            throw new ApiException(ErrorCode.PASS_INSUFFICIENT);
        }
        return pass;
    }

    public PassView view(LessonPass pass) {
        int remaining = remaining(pass.getId());
        int reserved = reserved(pass.getId());
        return new PassView(pass.getId(), pass.getProductNameSnapshot(), pass.getStatus().name(),
                pass.getSessionCountSnapshot(), remaining, reserved, remaining - reserved,
                pass.getValidFrom(), pass.getValidUntil(), pass.getPaidAmount());
    }

    private LessonPass loadInCenter(Long passId) {
        LessonPass pass = passRepository.findById(passId)
                .orElseThrow(() -> new ApiException(ErrorCode.PASS_NOT_FOUND));
        if (!pass.getMembership().getCenter().getId().equals(TenantContext.centerId())) {
            throw new ApiException(ErrorCode.TENANT_VIOLATION);
        }
        return pass;
    }
}
