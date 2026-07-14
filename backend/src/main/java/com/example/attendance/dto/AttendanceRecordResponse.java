package com.example.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceRecordResponse(
        Long id,
        LocalDate date,
        LocalDateTime clockIn,
        LocalDateTime clockOut,
        Integer workMinutes,
        Integer overtimeMinutes
) {
}
