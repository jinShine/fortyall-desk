package com.buzz.fortyall_desk.lesson.entity;

import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.common.entity.BaseEntity;
import com.buzz.fortyall_desk.schedule.entity.StandingSchedule;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "lesson",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_lesson_schedule_date",
                             columnNames = {"standing_schedule_id", "lesson_date"}),

           @UniqueConstraint(name = "uk_lesson_coach_slot",
                             columnNames = {"coach_membership_id", "lesson_date", "start_time"})
       })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lesson extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standing_schedule_id")
    private StandingSchedule standingSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coach_membership_id", nullable = false)
    private Membership coach;

    @Column(name = "lesson_date", nullable = false)
    private LocalDate lessonDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LessonOrigin origin;

    @Column(nullable = false)
    private boolean manuallyModified = false;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonParticipant> participants = new ArrayList<>();

    public Lesson(Center center, StandingSchedule schedule, Membership coach,
                  LocalDate date, LocalTime start, LocalTime end, LessonOrigin origin) {
        this.center = center;
        this.standingSchedule = schedule;
        this.coach = coach;
        this.lessonDate = date;
        this.startTime = start;
        this.endTime = end;
        this.origin = origin;
    }

    public void addParticipant(LessonParticipant participant) {
        this.participants.add(participant);
    }

    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        return startTime.isBefore(otherEnd) && endTime.isAfter(otherStart);
    }

    public enum LessonOrigin { REGULAR, SELF_BOOKED }
}
