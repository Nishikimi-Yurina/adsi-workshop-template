package com.example.attendance.dto;

public record MonthlyReportResponse(
        Long employeeId,
        String employeeName,
        String yearMonth,
        int workDays,
        int totalWorkMinutes,
        int totalOvertimeMinutes
) {
}
