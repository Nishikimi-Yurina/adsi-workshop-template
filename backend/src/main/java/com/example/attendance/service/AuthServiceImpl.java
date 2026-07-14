package com.example.attendance.service;

import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmployeeCode(request.employeeCode())
                .orElseThrow(() -> new BadCredentialsException("ユーザー名またはパスワードが正しくありません"));

        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new BadCredentialsException("ユーザー名またはパスワードが正しくありません");
        }

        return new LoginResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getRole()
        );
    }

    @Override
    public EmployeeResponse getCurrentUser(String employeeCode) {
        Employee employee = getEmployee(employeeCode);
        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole()
        );
    }

    @Override
    public Long getEmployeeId(String employeeCode) {
        return getEmployee(employeeCode).getId();
    }

    @Override
    public Employee getEmployee(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new BadCredentialsException("認証エラーが発生しました"));
    }
}
