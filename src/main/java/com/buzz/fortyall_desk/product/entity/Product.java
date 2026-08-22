package com.buzz.fortyall_desk.product.entity;

import com.buzz.fortyall_desk.center.entity.Center;
import com.buzz.fortyall_desk.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int sessionCount;

    @Column(nullable = false)
    private int validDays;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public Product(Center center, String name, int durationMinutes, int capacity,
                   int sessionCount, int validDays, BigDecimal price, String description) {
        this.center = center;
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.capacity = capacity;
        this.sessionCount = sessionCount;
        this.validDays = validDays;
        this.price = price;
        this.description = description;
    }
}
