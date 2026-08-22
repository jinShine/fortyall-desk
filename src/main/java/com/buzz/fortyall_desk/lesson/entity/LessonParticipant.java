package com.buzz.fortyall_desk.lesson.entity;

import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.common.entity.BaseEntity;
import com.buzz.fortyall_desk.pass.entity.LessonPass;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "lesson_participant",

       uniqueConstraints = @UniqueConstraint(name = "uk_participant_lesson_member",
                                             columnNames = {"lesson_id", "membership_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LessonParticipant extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_pass_id")
    private LessonPass lessonPass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.SCHEDULED;

    @Column(nullable = false)
    private boolean reserved = false;

    public LessonParticipant(Lesson lesson, Membership membership, LessonPass pass, boolean reserved) {
        this.lesson = lesson;
        this.membership = membership;
        this.lessonPass = pass;
        this.reserved = reserved;
    }

    public void markAttended(LessonPass deductedFrom) {
        this.status = AttendanceStatus.ATTENDED;
        this.lessonPass = deductedFrom;
        this.reserved = false;
    }

    public void changeStatus(AttendanceStatus status) {
        this.status = status;
        if (status != AttendanceStatus.SCHEDULED) this.reserved = false;
    }

    public enum AttendanceStatus {
        SCHEDULED, ATTENDED, EARLY_CANCEL, LATE_CANCEL, NO_SHOW, SUSPENDED
    }
}
