package com.example.attendance.vo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record WorkDuration(int totalMinutes, int overtimeMinutes) {

    private static final int STANDARD_MINUTES = 480;

    public static WorkDuration calculate(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (clockIn == null || clockOut == null) {
            return null;
        }
        int total = (int) ChronoUnit.MINUTES.between(clockIn, clockOut);
        int overtime = Math.max(0, total - STANDARD_MINUTES);
        return new WorkDuration(total, overtime);
    }
}
