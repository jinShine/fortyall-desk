package com.buzz.fortyall_desk.lesson.service;

import com.buzz.fortyall_desk.lesson.entity.Lesson;
import com.buzz.fortyall_desk.lesson.entity.LessonParticipant;
import com.buzz.fortyall_desk.lesson.repository.LessonParticipantRepository;
import com.buzz.fortyall_desk.lesson.repository.LessonRepository;
import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.account.repository.MembershipRepository;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.dto.ApiResponse.Warning;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import com.buzz.fortyall_desk.lesson.entity.Lesson.LessonOrigin;
import com.buzz.fortyall_desk.lesson.dto.LessonDto.*;
import com.buzz.fortyall_desk.lesson.entity.LessonParticipant.AttendanceStatus;
import com.buzz.fortyall_desk.pass.entity.LessonPass;
import com.buzz.fortyall_desk.pass.service.PassService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final LessonRepository lessonRepository;
    private final LessonParticipantRepository participantRepository;
    private final MembershipRepository membershipRepository;
    private final PassService passService;

    @Transactional
    public BookingResult book(BookingRequest req) {
        Long centerId = TenantContext.centerId();
        Long myMembershipId = TenantContext.membershipId();

        Membership me = membershipRepository.findByIdAndCenterId(myMembershipId, centerId)
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION));
        Membership coach = membershipRepository.findByIdAndCenterId(req.coachMembershipId(), centerId)
                .orElseThrow(() -> new ApiException(ErrorCode.TENANT_VIOLATION));

        int window = me.getCenter().getBookingWindowDays();
        if (req.date().isAfter(LocalDate.now().plusDays(window))) {
            throw new ApiException(ErrorCode.SLOT_TAKEN,
                    "%d일 이내만 예약할 수 있습니다".formatted(window));
        }
        LessonPass pass = passService.validateForBooking(req.lessonPassId(), req.date());
        LocalTime endTime = req.startTime()
                .plusMinutes(pass.getProduct().getDurationMinutes());

        membershipRepository.findByIdForUpdate(coach.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

        boolean taken = lessonRepository
                .findAllByCoachIdAndLessonDate(coach.getId(), req.date()).stream()
                .anyMatch(l -> l.overlaps(req.startTime(), endTime));
        if (taken) throw new ApiException(ErrorCode.SLOT_TAKEN);

        Lesson lesson = new Lesson(me.getCenter(), null, coach, req.date(),
                req.startTime(), endTime, LessonOrigin.SELF_BOOKED);

        LessonParticipant participant = new LessonParticipant(lesson, me, pass, true);
        lesson.addParticipant(participant);
        try {
            lessonRepository.saveAndFlush(lesson);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.SLOT_TAKEN);
        }

        int remaining = passService.remaining(pass.getId());
        int reserved = passService.reserved(pass.getId());
        log.info("[셀프 예약] member={} {} {} — 잔여 {} 확보 {}",
                me.getName(), req.date(), req.startTime(), remaining, reserved);

        return new BookingResult(lesson.getId(), participant.getId(), req.date(),
                req.startTime(), endTime, remaining, reserved, remaining - reserved,
                "수업권 1회가 예약(확보)되었습니다");
    }

    @Transactional(readOnly = true)
    public List<Warning> warningsFor(Long passId) {
        List<Warning> warnings = new ArrayList<>();
        int usable = passService.remaining(passId) - passService.reserved(passId);
        long futureRegular = participantRepository
                .findMyLessons(TenantContext.membershipId(), LocalDate.now(), LocalDate.now().plusDays(90))
                .stream()
                .filter(p -> p.getLesson().getOrigin() == LessonOrigin.REGULAR)
                .filter(p -> p.getStatus() == AttendanceStatus.SCHEDULED)
                .count();
        if (futureRegular > usable) {
            warnings.add(new Warning("REGULAR_LESSON_SHORTAGE",
                    "예정된 고정 레슨 %d회가 있습니다. 이 예약 시 재등록이 필요할 수 있습니다."
                            .formatted(futureRegular)));
        }
        return warnings;
    }

    @Transactional(readOnly = true)
    public List<SlotView> availableSlots(Long coachMembershipId, LocalDate date, int intervalMinutes) {
        List<Lesson> booked = lessonRepository.findAllByCoachIdAndLessonDate(coachMembershipId, date);
        List<SlotView> slots = new ArrayList<>();
        for (LocalTime t = LocalTime.of(9, 0); t.isBefore(LocalTime.of(22, 0));
             t = t.plusMinutes(intervalMinutes)) {
            LocalTime end = t.plusMinutes(intervalMinutes);
            final LocalTime start = t;
            boolean conflict = booked.stream().anyMatch(l -> l.overlaps(start, end));
            slots.add(new SlotView(start, end, !conflict, conflict ? "예약됨" : null));
        }
        return slots;
    }

    @Transactional(readOnly = true)
    public List<MyLessonView> myLessons(LocalDate from, LocalDate to) {
        return participantRepository.findMyLessons(TenantContext.membershipId(), from, to).stream()
                .map(p -> new MyLessonView(p.getLesson().getId(), p.getId(),
                        p.getLesson().getLessonDate(), p.getLesson().getStartTime(),
                        p.getLesson().getEndTime(), p.getLesson().getCoach().getName(),
                        p.getLesson().getOrigin().name(), p.getStatus().name()))
                .toList();
    }
}
