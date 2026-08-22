package com.buzz.fortyall_desk.lesson.service;

import com.buzz.fortyall_desk.lesson.entity.Lesson;
import com.buzz.fortyall_desk.lesson.entity.LessonParticipant;
import com.buzz.fortyall_desk.lesson.repository.LessonParticipantRepository;
import com.buzz.fortyall_desk.lesson.repository.LessonRepository;
import com.buzz.fortyall_desk.account.repository.MembershipRepository;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import com.buzz.fortyall_desk.lesson.dto.LessonDto.AttendanceResult;
import com.buzz.fortyall_desk.lesson.entity.LessonParticipant.AttendanceStatus;
import com.buzz.fortyall_desk.pass.entity.LessonPass;
import com.buzz.fortyall_desk.pass.repository.LessonPassRepository;
import com.buzz.fortyall_desk.pass.service.PassService;
import com.buzz.fortyall_desk.pass.entity.PassTransaction.TransactionType;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final QrService qrService;
    private final LessonRepository lessonRepository;
    private final LessonParticipantRepository participantRepository;
    private final LessonPassRepository passRepository;
    private final MembershipRepository membershipRepository;
    private final PassService passService;

    @Transactional
    public AttendanceResult attendByQr(String qrToken) {
        Long coachMembershipId = TenantContext.membershipId();
        Long memberMembershipId = qrService.verify(qrToken);

        var member = membershipRepository
                .findByIdAndCenterId(memberMembershipId, TenantContext.centerId())
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION,
                        "다른 센터 회원의 QR입니다"));

        LocalDate today = LocalDate.now();
        LessonParticipant target = findTodayLesson(coachMembershipId, memberMembershipId, today);
        return attend(target);
    }

    @Transactional
    public AttendanceResult attendManually(Long participantId) {
        LessonParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!participant.getLesson().getCenter().getId().equals(TenantContext.centerId())) {
            throw new ApiException(ErrorCode.TENANT_VIOLATION);
        }
        return attend(participant);
    }

    private AttendanceResult attend(LessonParticipant participant) {
        if (participant.getStatus() == AttendanceStatus.ATTENDED) {
            throw new ApiException(ErrorCode.ALREADY_ATTENDED);
        }

        LessonPass pass = resolvePass(participant);
        LessonPass locked = passRepository.findByIdForUpdate(pass.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.PASS_NOT_FOUND));

        int remaining = passService.record(locked, TransactionType.ATTEND, -1, participant.getId(),
                "%s 출석".formatted(participant.getLesson().getLessonDate()));
        participant.markAttended(locked);

        int attended = countAttended(locked.getId());
        String session = attended + "/" + locked.getSessionCountSnapshot();

        log.info("[출석] member={} pass={} -1회 → 잔여 {}회 ({})",
                participant.getMembership().getName(), locked.getId(), remaining, session);

        return new AttendanceResult(participant.getId(), participant.getMembership().getName(),
                locked.getProductNameSnapshot(), session, remaining,
                AttendanceStatus.ATTENDED.name(),
                "출석 처리 완료 — 오늘 %s회차, 잔여 %d회".formatted(session, remaining));
    }

    @Transactional
    public AttendanceResult changeStatus(Long participantId, AttendanceStatus status) {
        LessonParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!participant.getLesson().getCenter().getId().equals(TenantContext.centerId())) {
            throw new ApiException(ErrorCode.TENANT_VIOLATION);
        }
        LessonPass pass = resolvePass(participant);
        int remaining = passService.remaining(pass.getId());

        TransactionType type = switch (status) {
            case NO_SHOW -> TransactionType.NO_SHOW;
            case LATE_CANCEL -> TransactionType.LATE_CANCEL;
            default -> null;
        };
        if (type != null) {
            LessonPass locked = passRepository.findByIdForUpdate(pass.getId()).orElseThrow();
            remaining = passService.record(locked, type, -1, participant.getId(),
                    "%s %s".formatted(participant.getLesson().getLessonDate(), status.name()));
        }
        participant.changeStatus(status);

        return new AttendanceResult(participant.getId(), participant.getMembership().getName(),
                pass.getProductNameSnapshot(), countAttended(pass.getId()) + "/"
                + pass.getSessionCountSnapshot(), remaining, status.name(),
                type == null ? "차감 없이 처리했습니다 (정당한 결번)"
                             : "1회 차감했습니다. 관리자가 복구할 수 있습니다.");
    }

    private LessonParticipant findTodayLesson(Long coachId, Long memberId, LocalDate today) {
        List<Lesson> lessons = lessonRepository.findAllByCoachIdAndLessonDate(coachId, today);
        return lessons.stream()
                .flatMap(l -> l.getParticipants().stream())
                .filter(p -> p.getMembership().getId().equals(memberId))
                .filter(p -> p.getStatus() == AttendanceStatus.SCHEDULED)
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.LESSON_NOT_FOUND,
                        "오늘 이 코치의 예정된 레슨이 없습니다"));
    }

    private LessonPass resolvePass(LessonParticipant participant) {
        if (participant.getLessonPass() != null) return participant.getLessonPass();
        return passRepository.findActiveByMembershipId(participant.getMembership().getId())
                .stream().findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.PASS_NOT_FOUND,
                        "활성 수업권이 없습니다. 충전 유예 처리가 필요합니다."));
    }

    private int countAttended(Long passId) {
        return (int) passService.ledger(passId).transactions().stream()
                .filter(t -> t.type().equals(TransactionType.ATTEND.name()))
                .count();
    }
}
