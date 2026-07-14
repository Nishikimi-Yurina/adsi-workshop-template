package com.example.attendance.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRequestCreateRequest(
        @NotNull String leaveType,
        @NotNull LocalDate date,
        String reason
) {}
