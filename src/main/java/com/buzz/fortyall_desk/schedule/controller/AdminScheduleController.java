package com.buzz.fortyall_desk.schedule.controller;

import com.buzz.fortyall_desk.schedule.service.MaterializerService;
import com.buzz.fortyall_desk.schedule.service.ScheduleService;
import com.buzz.fortyall_desk.common.dto.ApiResponse;
import com.buzz.fortyall_desk.common.service.IdempotencyService;
import com.buzz.fortyall_desk.schedule.dto.ScheduleDto.*;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/schedules")
@RequiredArgsConstructor
public class AdminScheduleController {
    private final ScheduleService scheduleService;
    private final MaterializerService materializerService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    public ApiResponse<ScheduleView> create(@Valid @RequestBody CreateRequest req) {
        return ApiResponse.of(scheduleService.create(req));
    }

    @GetMapping
    public ApiResponse<List<ScheduleView>> list() {
        return ApiResponse.of(scheduleService.list());
    }

    @PostMapping("/{scheduleId}/participants")
    public ApiResponse<ScheduleView> addParticipant(@PathVariable Long scheduleId,
                                                    @Valid @RequestBody AddParticipantRequest req) {
        return ApiResponse.of(scheduleService.addParticipant(scheduleId, req));
    }

    @DeleteMapping("/{scheduleId}/participants/{enrollmentId}")
    public ApiResponse<ScheduleView> releaseParticipant(@PathVariable Long scheduleId,
                                                        @PathVariable Long enrollmentId) {
        return ApiResponse.of(scheduleService.releaseParticipant(scheduleId, enrollmentId));
    }

    @PostMapping("/materialize")
    public ApiResponse<MaterializeResult> materialize(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ApiResponse.of(idempotencyService.execute(idempotencyKey, MaterializeResult.class,
                materializerService::materializeCurrentCenter));
    }
}
