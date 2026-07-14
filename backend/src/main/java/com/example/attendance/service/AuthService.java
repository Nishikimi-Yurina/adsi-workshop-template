package com.example.attendance.service;

import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.Employee;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    EmployeeResponse getCurrentUser(String employeeCode);

    Long getEmployeeId(String employeeCode);

    Employee getEmployee(String employeeCode);
}
