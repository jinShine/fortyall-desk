package com.buzz.fortyall_desk.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "idempotency_record",
       uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key", columnNames = "idem_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idem_key", nullable = false, length = 100)
    private String key;

    @Column(nullable = false, length = 200)
    private String resultType;

    @Lob
    @Column(nullable = false, length = 100_000)
    private String resultJson;

    public IdempotencyRecord(String key, String resultType, String resultJson) {
        this.key = key;
        this.resultType = resultType;
        this.resultJson = resultJson;
    }
}
