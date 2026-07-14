package com.example.attendance.dto;

import com.example.attendance.enums.Role;

public record LoginResponse(
        Long id,
        String employeeCode,
        String name,
        Role role
) {
}
