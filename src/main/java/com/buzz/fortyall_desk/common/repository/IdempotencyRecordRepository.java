package com.buzz.fortyall_desk.common.repository;

import com.buzz.fortyall_desk.common.entity.IdempotencyRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByKey(String key);
}
