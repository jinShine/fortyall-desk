package com.buzz.fortyall_desk.lesson.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class LessonDto {
    private LessonDto() {}

    public record QrToken(String token, String expiresAt, int ttlSeconds) {}

    public record ScanRequest(@NotBlank String qrToken) {}

    public record AttendanceResult(Long lessonParticipantId, String memberName, String productName,
                                   String session, int remaining, String status, String message) {}

    public record BookingRequest(@NotNull Long coachMembershipId,
                                 @NotNull Long lessonPassId,
                                 @NotNull LocalDate date,
                                 @NotNull LocalTime startTime) {}

    public record BookingResult(Long lessonId, Long lessonParticipantId, LocalDate date,
                                LocalTime startTime, LocalTime endTime,
                                int remaining, int reserved, int usable, String message) {}

    public record SlotView(LocalTime startTime, LocalTime endTime, boolean available, String reason) {}

    public record LessonView(Long lessonId, LocalDate date, LocalTime startTime, LocalTime endTime,
                             String coachName, String origin, List<ParticipantView> participants) {}

    public record ParticipantView(Long participantId, Long membershipId, String memberName,
                                  String status, boolean reserved, Long lessonPassId) {}

    public record MyLessonView(Long lessonId, Long participantId, LocalDate date,
                               LocalTime startTime, LocalTime endTime, String coachName,
                               String origin, String status) {}
}
