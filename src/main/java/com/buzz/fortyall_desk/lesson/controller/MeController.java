package com.buzz.fortyall_desk.lesson.controller;

import com.buzz.fortyall_desk.lesson.service.BookingService;
import com.buzz.fortyall_desk.lesson.service.QrService;
import com.buzz.fortyall_desk.pass.repository.LessonPassRepository;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.common.dto.ApiResponse;
import com.buzz.fortyall_desk.common.service.IdempotencyService;
import com.buzz.fortyall_desk.lesson.dto.LessonDto.*;
import com.buzz.fortyall_desk.pass.dto.PassDto.PassView;
import com.buzz.fortyall_desk.pass.service.PassService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {
    private final BookingService bookingService;
    private final QrService qrService;
    private final PassService passService;
    private final IdempotencyService idempotencyService;
    private final com.buzz.fortyall_desk.pass.repository.LessonPassRepository passRepository;

    @GetMapping("/passes")
    public ApiResponse<List<PassView>> myPasses() {
        return ApiResponse.of(passRepository
                .findAllByMembershipIdOrderByIdAsc(TenantContext.membershipId())
                .stream().map(passService::view).toList());
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        List<PassView> passes = passRepository
                .findAllByMembershipIdOrderByIdAsc(TenantContext.membershipId())
                .stream().map(passService::view).toList();
        List<MyLessonView> upcoming = bookingService
                .myLessons(LocalDate.now(), LocalDate.now().plusDays(14));
        return ApiResponse.of(Map.of(
                "passes", passes,
                "upcomingLessons", upcoming,
                "nextLesson", upcoming.isEmpty() ? "없음" : upcoming.get(0)));
    }

    @GetMapping("/slots")
    public ApiResponse<List<SlotView>> slots(
            @RequestParam Long coachMembershipId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "30") int intervalMinutes) {
        return ApiResponse.of(bookingService.availableSlots(coachMembershipId, date, intervalMinutes));
    }

    @PostMapping("/bookings")
    public ApiResponse<BookingResult> book(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody BookingRequest req) {
        BookingResult result = idempotencyService.execute(idempotencyKey, BookingResult.class,
                () -> bookingService.book(req));
        return ApiResponse.of(result, bookingService.warningsFor(req.lessonPassId()));
    }

    @GetMapping("/lessons")
    public ApiResponse<List<MyLessonView>> myLessons(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.of(bookingService.myLessons(from, to));
    }

    @PostMapping("/qr")
    public ApiResponse<QrToken> qr() {
        return ApiResponse.of(qrService.issue(TenantContext.membershipId()));
    }
}
