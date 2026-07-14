package com.example.attendance.dto;

import com.example.attendance.enums.Role;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String name,
        String email,
        Role role
) {
}
