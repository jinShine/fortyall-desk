package com.buzz.fortyall_desk.pass.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class PassDto {
    private PassDto() {}

    public record IssueRequest(@NotNull Long membershipId,
                               @NotNull Long productId,
                               @NotNull @Positive BigDecimal paidAmount,
                               LocalDate validFrom,
                               boolean prepaid) {}

    public record AdjustRequest(int delta, String memo) {}

    public record PassView(Long passId, String productName, String status,
                           int issued, int remaining, int reserved, int usable,
                           LocalDate validFrom, LocalDate validUntil,
                           BigDecimal paidAmount) {}

    public record TransactionView(Long id, String type, int delta, int balanceAfter,
                                  String memo, String occurredAt) {}

    public record LedgerView(Long passId, String productName, int remaining,
                             List<TransactionView> transactions) {}

    public record IssueResult(Long passId, String status, int issued, int remaining,
                              String message) {}
}
