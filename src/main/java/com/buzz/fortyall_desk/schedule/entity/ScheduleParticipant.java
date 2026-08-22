package com.buzz.fortyall_desk.schedule.entity;

import com.buzz.fortyall_desk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "schedule_participant",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_schedule_participant",
               columnNames = {"standing_schedule_id", "regular_enrollment_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleParticipant extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "standing_schedule_id", nullable = false)
    private StandingSchedule standingSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regular_enrollment_id", nullable = false)
    private RegularEnrollment regularEnrollment;

    @Column(nullable = false)
    private boolean active = true;

    public ScheduleParticipant(StandingSchedule schedule, RegularEnrollment enrollment) {
        this.standingSchedule = schedule;
        this.regularEnrollment = enrollment;
    }

    public void release() { this.active = false; }
}
