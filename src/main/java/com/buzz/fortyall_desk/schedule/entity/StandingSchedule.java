package com.buzz.fortyall_desk.schedule.entity;

import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.common.entity.BaseEntity;
import com.buzz.fortyall_desk.product.entity.Product;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "standing_schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StandingSchedule extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coach_membership_id", nullable = false)
    private Membership coach;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    public StandingSchedule(Center center, Membership coach, Product product,
                            DayOfWeek dayOfWeek, LocalTime startTime) {
        this.center = center;
        this.coach = coach;
        this.product = product;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
    }

    public LocalTime endTime() {
        return startTime.plusMinutes(product.getDurationMinutes());
    }

    public void terminate() { this.status = ScheduleStatus.TERMINATED; }

    public enum ScheduleStatus { ACTIVE, TERMINATED }
}
