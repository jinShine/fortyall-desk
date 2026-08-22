package com.buzz.fortyall_desk.pass.entity;

import com.buzz.fortyall_desk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "pass_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassTransaction extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_pass_id", nullable = false)
    private LessonPass lessonPass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false)
    private int delta;

    @Column(name = "lesson_participant_id")
    private Long lessonParticipantId;

    @Column(length = 200)
    private String memo;

    public PassTransaction(LessonPass pass, TransactionType type, int delta,
                           Long lessonParticipantId, String memo) {
        this.lessonPass = pass;
        this.type = type;
        this.delta = delta;
        this.lessonParticipantId = lessonParticipantId;
        this.memo = memo;
    }

    public enum TransactionType {
        ISSUE, ATTEND, NO_SHOW, LATE_CANCEL, RESTORE, ADJUST,
        EXPIRE, EXPIRE_RESTORE, CARRY_OVER, DEBT_SETTLE
    }
}
