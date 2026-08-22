package com.buzz.fortyall_desk.schedule.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class ScheduleDto {
    private ScheduleDto() {}

    public record CreateRequest(@NotNull Long coachMembershipId,
                                @NotNull Long productId,
                                @NotNull DayOfWeek dayOfWeek,
                                @NotNull LocalTime startTime) {}

    public record AddParticipantRequest(@NotNull Long membershipId, Long lessonPassId) {}

    public record ScheduleView(Long scheduleId, String coachName, String productName,
                               DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                               String status, List<ParticipantView> participants) {}

    public record ParticipantView(Long enrollmentId, Long membershipId, String memberName,
                                  String enrollmentStatus, Long activePassId) {}

    public record MaterializeResult(LocalDate from, LocalDate to, int horizonDays,
                                    int lessonsCreated, int lessonsSkipped, String message) {}
}
