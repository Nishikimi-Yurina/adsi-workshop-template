package com.example.attendance.dto;

import com.example.attendance.entity.LeaveRequest;

import java.time.LocalDate;

public record LeaveRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String leaveType,
        LocalDate date,
        String reason,
        String status
) {
    public static LeaveRequestResponse from(LeaveRequest entity, String employeeName) {
        return new LeaveRequestResponse(
                entity.getId(),
                entity.getEmployeeId(),
                employeeName,
                entity.getLeaveType().name(),
                entity.getDate(),
                entity.getReason(),
                entity.getStatus().name()
        );
    }
}
