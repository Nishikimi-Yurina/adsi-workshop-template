package com.example.attendance.dto;

import com.example.attendance.entity.OvertimeRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OvertimeRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate date,
        BigDecimal expectedHours,
        String reason,
        String status
) {
    public static OvertimeRequestResponse from(OvertimeRequest entity, String employeeName) {
        return new OvertimeRequestResponse(
                entity.getId(),
                entity.getEmployeeId(),
                employeeName,
                entity.getDate(),
                entity.getExpectedHours(),
                entity.getReason(),
                entity.getStatus().name()
        );
    }
}
