package com.buzz.fortyall_desk.schedule.service;

import com.buzz.fortyall_desk.schedule.entity.RegularEnrollment;
import com.buzz.fortyall_desk.schedule.entity.ScheduleParticipant;
import com.buzz.fortyall_desk.schedule.entity.StandingSchedule;
import com.buzz.fortyall_desk.schedule.repository.RegularEnrollmentRepository;
import com.buzz.fortyall_desk.schedule.repository.ScheduleParticipantRepository;
import com.buzz.fortyall_desk.schedule.repository.StandingScheduleRepository;
import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.account.repository.MembershipRepository;
import com.buzz.fortyall_desk.account.entity.Role;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import com.buzz.fortyall_desk.pass.entity.LessonPass;
import com.buzz.fortyall_desk.pass.repository.LessonPassRepository;
import com.buzz.fortyall_desk.product.entity.Product;
import com.buzz.fortyall_desk.product.repository.ProductRepository;
import com.buzz.fortyall_desk.schedule.dto.ScheduleDto.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final StandingScheduleRepository scheduleRepository;
    private final ScheduleParticipantRepository participantRepository;
    private final RegularEnrollmentRepository enrollmentRepository;
    private final MembershipRepository membershipRepository;
    private final ProductRepository productRepository;
    private final LessonPassRepository passRepository;

    @Transactional
    public ScheduleView create(CreateRequest req) {
        Long centerId = TenantContext.centerId();

        Membership coach = membershipRepository.findByIdAndCenterId(req.coachMembershipId(), centerId)
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION));
        if (!coach.hasRole(Role.COACH)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "코치 역할이 없는 멤버십입니다");
        }
        Product product = productRepository.findByIdAndCenterId(req.productId(), centerId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        StandingSchedule schedule = scheduleRepository.save(new StandingSchedule(
                coach.getCenter(), coach, product, req.dayOfWeek(), req.startTime()));
        log.info("[고정 스케줄] {} {} {} — {}", schedule.getDayOfWeek(), schedule.getStartTime(),
                product.getName(), coach.getName());
        return view(schedule);
    }

    @Transactional
    public ScheduleView addParticipant(Long scheduleId, AddParticipantRequest req) {
        Long centerId = TenantContext.centerId();

        StandingSchedule schedule = scheduleRepository.findByIdAndCenterId(scheduleId, centerId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        Membership member = membershipRepository.findByIdAndCenterId(req.membershipId(), centerId)
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION));

        RegularEnrollment enrollment = enrollmentRepository.findByMembershipId(member.getId())
                .orElseGet(() -> {
                    LessonPass activePass = resolvePass(req.lessonPassId(), member.getId());
                    return enrollmentRepository.save(new RegularEnrollment(member, activePass));
                });

        participantRepository.save(new ScheduleParticipant(schedule, enrollment));
        log.info("[참가자 추가] schedule={} member={} enrollment={}",
                scheduleId, member.getName(), enrollment.getId());
        return view(schedule);
    }

    @Transactional
    public ScheduleView releaseParticipant(Long scheduleId, Long enrollmentId) {
        StandingSchedule schedule = scheduleRepository
                .findByIdAndCenterId(scheduleId, TenantContext.centerId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        participantRepository.findActiveByScheduleId(scheduleId).stream()
                .filter(sp -> sp.getRegularEnrollment().getId().equals(enrollmentId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND))
                .release();

        if (participantRepository.countByStandingScheduleIdAndActiveTrue(scheduleId) == 0) {
            schedule.terminate();
            log.info("[스케줄 종료] schedule={} — 활성 참가자 0명, 슬롯이 빈칸으로", scheduleId);
        }
        return view(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleView> list() {
        return scheduleRepository.findActiveByCenterId(TenantContext.centerId())
                .stream().map(this::view).toList();
    }

    private LessonPass resolvePass(Long passId, Long membershipId) {
        if (passId != null) {
            return passRepository.findById(passId)
                    .orElseThrow(() -> new ApiException(ErrorCode.PASS_NOT_FOUND));
        }

        return passRepository.findActiveByMembershipId(membershipId).stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.PASS_NOT_FOUND,
                        "활성 수업권이 없습니다. 먼저 발급하세요."));
    }

    private ScheduleView view(StandingSchedule s) {
        List<ParticipantView> participants = participantRepository
                .findActiveByScheduleId(s.getId()).stream()
                .map(sp -> {
                    RegularEnrollment e = sp.getRegularEnrollment();
                    return new ParticipantView(e.getId(), e.getMembership().getId(),
                            e.getMembership().getName(), e.getStatus().name(),
                            e.getActivePass() == null ? null : e.getActivePass().getId());
                }).toList();
        return new ScheduleView(s.getId(), s.getCoach().getName(), s.getProduct().getName(),
                s.getDayOfWeek(), s.getStartTime(), s.endTime(), s.getStatus().name(), participants);
    }
}
