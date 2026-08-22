package com.buzz.fortyall_desk.pass.entity;

import com.buzz.fortyall_desk.account.entity.Membership;
import com.buzz.fortyall_desk.common.entity.BaseEntity;
import com.buzz.fortyall_desk.product.entity.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "lesson_pass")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LessonPass extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String productNameSnapshot;

    @Column(nullable = false)
    private int sessionCountSnapshot;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PassStatus status;

    public LessonPass(Membership membership, Product product, BigDecimal paidAmount,
                      LocalDate validFrom, boolean prepaid) {
        this.membership = membership;
        this.product = product;
        this.productNameSnapshot = product.getName();
        this.sessionCountSnapshot = product.getSessionCount();
        this.paidAmount = paidAmount;
        this.validFrom = validFrom;
        this.validUntil = validFrom.plusDays(product.getValidDays());

        this.status = prepaid ? PassStatus.PENDING : PassStatus.ACTIVE;
    }

    public void markExhausted() { this.status = PassStatus.EXHAUSTED; }

    public void restoreToActive(int remaining, LocalDate today) {
        if (remaining > 0 && !today.isAfter(validUntil)) {
            this.status = PassStatus.ACTIVE;
        }
    }

    public boolean usableFor(LocalDate lessonDate) {
        return status == PassStatus.ACTIVE
                && !lessonDate.isBefore(validFrom)
                && !lessonDate.isAfter(validUntil);
    }

    public enum PassStatus { PENDING, ACTIVE, EXHAUSTED, EXPIRED, CANCELLED }
}
