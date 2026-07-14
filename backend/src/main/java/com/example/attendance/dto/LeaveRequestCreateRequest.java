package com.example.attendance.dto;

import com.example.attendance.enums.LeaveType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRequestCreateRequest(
        @NotNull LeaveType leaveType,
        @NotNull LocalDate date,
        String reason
) {}
