package com.buzz.fortyall_desk.schedule.service;

import com.buzz.fortyall_desk.schedule.entity.RegularEnrollment;
import com.buzz.fortyall_desk.schedule.entity.ScheduleParticipant;
import com.buzz.fortyall_desk.schedule.entity.StandingSchedule;
import com.buzz.fortyall_desk.schedule.repository.ScheduleParticipantRepository;
import com.buzz.fortyall_desk.schedule.repository.StandingScheduleRepository;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.lesson.entity.Lesson;
import com.buzz.fortyall_desk.lesson.entity.Lesson.LessonOrigin;
import com.buzz.fortyall_desk.lesson.entity.LessonParticipant;
import com.buzz.fortyall_desk.lesson.repository.LessonRepository;
import com.buzz.fortyall_desk.schedule.entity.RegularEnrollment.EnrollmentStatus;
import com.buzz.fortyall_desk.schedule.dto.ScheduleDto.MaterializeResult;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterializerService {
    public static final int HORIZON_DAYS = 90;

    private final StandingScheduleRepository scheduleRepository;
    private final ScheduleParticipantRepository participantRepository;
    private final LessonRepository lessonRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void runDaily() {
        log.info("[Materializer] 일일 배치 시작");
    }

    @Transactional
    public MaterializeResult materialize(Long centerId) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(HORIZON_DAYS);
        int created = 0;
        int skipped = 0;

        for (StandingSchedule schedule : scheduleRepository.findActiveByCenterId(centerId)) {
            List<ScheduleParticipant> participants =
                    participantRepository.findActiveByScheduleId(schedule.getId());

            for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
                if (date.getDayOfWeek() != schedule.getDayOfWeek()) continue;

                if (lessonRepository.existsByStandingScheduleIdAndLessonDate(schedule.getId(), date)) {
                    skipped++;
                    continue;
                }
                Center center = schedule.getCenter();
                Lesson lesson = new Lesson(center, schedule, schedule.getCoach(), date,
                        schedule.getStartTime(), schedule.endTime(), LessonOrigin.REGULAR);

                for (ScheduleParticipant sp : participants) {
                    RegularEnrollment enrollment = sp.getRegularEnrollment();

                    if (enrollment.getStatus() == EnrollmentStatus.HOLD) continue;

                    lesson.addParticipant(new LessonParticipant(
                            lesson, enrollment.getMembership(), enrollment.getActivePass(), false));
                }
                lessonRepository.save(lesson);
                created++;
            }
        }
        String message = "%d일치 생성 — 신규 %d건, 이미 존재해서 건너뜀 %d건"
                .formatted(HORIZON_DAYS, created, skipped);
        log.info("[Materializer] {}", message);
        return new MaterializeResult(from, to, HORIZON_DAYS, created, skipped, message);
    }

    public MaterializeResult materializeCurrentCenter() {
        return materialize(TenantContext.centerId());
    }
}
