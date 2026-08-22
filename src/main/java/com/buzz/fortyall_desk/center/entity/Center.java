package com.buzz.fortyall_desk.center.entity;

import com.buzz.fortyall_desk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "center")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Center extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CenterStatus status = CenterStatus.ACTIVE;

    @Column(nullable = false)
    private int bookingWindowDays = 30;

    @Column(nullable = false)
    private int renewalGraceDays = 7;

    public Center(String name) {
        this.name = name;
    }

    public enum CenterStatus { ACTIVE, SUSPENDED, TERMINATED }
}
