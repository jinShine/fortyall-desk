package com.buzz.fortyall_desk.common.service;

import com.buzz.fortyall_desk.common.entity.IdempotencyRecord;
import com.buzz.fortyall_desk.common.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class IdempotentExecutor {
    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Transactional
    <T> T runAndRecord(String key, Supplier<T> operation) {
        T result = operation.get();
        repository.saveAndFlush(new IdempotencyRecord(
                key, result.getClass().getName(), objectMapper.writeValueAsString(result)));
        return result;
    }
}
