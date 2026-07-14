package com.example.attendance.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OvertimeRequestCreateRequest(
        @NotNull LocalDate date,
        @NotNull BigDecimal expectedHours,
        String reason
) {}
