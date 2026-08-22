package com.buzz.fortyall_desk.lesson.controller;

import com.buzz.fortyall_desk.lesson.repository.LessonRepository;
import com.buzz.fortyall_desk.lesson.service.AttendanceService;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.common.dto.ApiResponse;
import com.buzz.fortyall_desk.common.service.IdempotencyService;
import com.buzz.fortyall_desk.lesson.dto.LessonDto.*;
import com.buzz.fortyall_desk.lesson.entity.LessonParticipant.AttendanceStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coach")
@RequiredArgsConstructor
public class CoachController {
    private final AttendanceService attendanceService;
    private final LessonRepository lessonRepository;
    private final IdempotencyService idempotencyService;

    @PostMapping("/attendances")
    public ApiResponse<AttendanceResult> scan(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ScanRequest req) {
        return ApiResponse.of(idempotencyService.execute(idempotencyKey, AttendanceResult.class,
                () -> attendanceService.attendByQr(req.qrToken())));
    }

    @PostMapping("/participants/{participantId}/attend")
    public ApiResponse<AttendanceResult> attendManually(@PathVariable Long participantId) {
        return ApiResponse.of(attendanceService.attendManually(participantId));
    }

    @PatchMapping("/participants/{participantId}/status")
    public ApiResponse<AttendanceResult> changeStatus(@PathVariable Long participantId,
                                                      @RequestParam AttendanceStatus status) {
        return ApiResponse.of(attendanceService.changeStatus(participantId, status));
    }

    @GetMapping("/lessons")
    public ApiResponse<List<LessonView>> myLessons(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<LessonView> lessons = lessonRepository
                .findCoachLessons(TenantContext.membershipId(), from, to).stream()
                .map(l -> new LessonView(l.getId(), l.getLessonDate(), l.getStartTime(),
                        l.getEndTime(), l.getCoach().getName(), l.getOrigin().name(),
                        l.getParticipants().stream()
                                .map(p -> new ParticipantView(p.getId(), p.getMembership().getId(),
                                        p.getMembership().getName(), p.getStatus().name(),
                                        p.isReserved(),
                                        p.getLessonPass() == null ? null : p.getLessonPass().getId()))
                                .toList()))
                .toList();
        return ApiResponse.of(lessons);
    }
}
