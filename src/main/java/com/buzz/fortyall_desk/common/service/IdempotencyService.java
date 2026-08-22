package com.buzz.fortyall_desk.common.service;

import com.buzz.fortyall_desk.common.entity.IdempotencyRecord;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import com.buzz.fortyall_desk.common.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRecordRepository repository;
    private final IdempotentExecutor executor;
    private final ObjectMapper objectMapper;

    public <T> T execute(String key, Class<T> type, Supplier<T> operation) {
        if (key == null || key.isBlank()) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }
        var existing = repository.findByKey(key);
        if (existing.isPresent()) {
            log.info("[멱등] key={} 재요청 — 최초 결과 반환, 업무 로직 실행하지 않음", key);
            return read(existing.get(), type);
        }
        try {
            return executor.runAndRecord(key, operation);
        } catch (DataIntegrityViolationException e) {
            log.info("[멱등] key={} 동시 요청 충돌 — 유니크 제약이 막았고 롤백됨", key);
            return read(repository.findByKey(key).orElseThrow(() -> e), type);
        }
    }

    @SneakyThrows
    private <T> T read(IdempotencyRecord record, Class<T> type) {
        return objectMapper.readValue(record.getResultJson(), type);
    }
}
