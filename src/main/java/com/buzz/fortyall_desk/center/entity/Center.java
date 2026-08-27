package com.buzz.fortyall_desk.center.entity;

import com.buzz.fortyall_desk.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "centers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Center extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CenterStatus status = CenterStatus.ACTIVE;

    private int bookingWindowDays = 30; // 셀프 예약 가능 범위

    private int renewalGraceDays = 7; // 재등록 유예 일수

    public enum CenterStatus {
        ACTIVE,
        SUSPENDED,
        TERMINATED
    }

    public Center(String name) {
        this.name = name;
    }
}
