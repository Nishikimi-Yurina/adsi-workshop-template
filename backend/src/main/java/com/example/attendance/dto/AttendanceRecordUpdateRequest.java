package com.example.attendance.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AttendanceRecordUpdateRequest(
        @NotNull LocalDateTime clockIn,
        LocalDateTime clockOut
) {
}
