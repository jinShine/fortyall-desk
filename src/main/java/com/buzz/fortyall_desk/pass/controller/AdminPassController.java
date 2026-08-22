package com.buzz.fortyall_desk.pass.controller;

import com.buzz.fortyall_desk.pass.service.PassService;
import com.buzz.fortyall_desk.common.dto.ApiResponse;
import com.buzz.fortyall_desk.common.service.IdempotencyService;
import com.buzz.fortyall_desk.pass.dto.PassDto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/passes")
@RequiredArgsConstructor
public class AdminPassController {
    private final PassService passService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    public ApiResponse<IssueResult> issue(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody IssueRequest req) {
        return ApiResponse.of(idempotencyService.execute(
                idempotencyKey, IssueResult.class, () -> passService.issue(req)));
    }

    @GetMapping("/{passId}/transactions")
    public ApiResponse<LedgerView> ledger(@PathVariable Long passId) {
        return ApiResponse.of(passService.ledger(passId));
    }

    @PostMapping("/{passId}/adjust")
    public ApiResponse<PassView> adjust(@PathVariable Long passId,
                                        @RequestBody AdjustRequest req) {
        return ApiResponse.of(passService.adjust(passId, req.delta(), req.memo()));
    }

    @PostMapping("/{passId}/restore")
    public ApiResponse<PassView> restore(@PathVariable Long passId,
                                         @RequestBody AdjustRequest req) {
        return ApiResponse.of(passService.restore(passId, null, req.memo()));
    }
}
