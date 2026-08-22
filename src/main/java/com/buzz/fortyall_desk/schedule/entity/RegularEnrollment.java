package com.buzz.fortyall_desk.schedule.entity;

import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.common.entity.BaseEntity;
import com.buzz.fortyall_desk.pass.entity.LessonPass;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "regular_enrollment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegularEnrollment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_pass_id")
    private LessonPass activePass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_pass_id")
    private LessonPass nextPass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    private LocalDate holdUntil;

    private LocalDate graceUntil;

    public RegularEnrollment(Membership membership, LessonPass activePass) {
        this.membership = membership;
        this.activePass = activePass;
    }

    public void startGrace(LocalDate until) {
        this.status = EnrollmentStatus.GRACE;
        this.graceUntil = until;
    }

    public void backToActive() {
        this.status = EnrollmentStatus.ACTIVE;
        this.graceUntil = null;
    }

    public enum EnrollmentStatus { ACTIVE, HOLD, GRACE, TERMINATED }
}
