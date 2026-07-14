package com.example.attendance.controller;

import com.example.attendance.dto.MonthlyReportResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.MonthlyReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final MonthlyReportService monthlyReportService;
    private final EmployeeRepository employeeRepository;

    public ReportController(MonthlyReportService monthlyReportService, EmployeeRepository employeeRepository) {
        this.monthlyReportService = monthlyReportService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @RequestParam String yearMonth,
            Authentication authentication) {
        Long employeeId = getEmployeeId(authentication);
        return ResponseEntity.ok(monthlyReportService.getReport(employeeId, YearMonth.parse(yearMonth)));
    }

    @GetMapping("/monthly/all")
    public ResponseEntity<List<MonthlyReportResponse>> getAllMonthlyReports(
            @RequestParam String yearMonth,
            Authentication authentication) {
        Employee employee = getEmployee(authentication);
        if (employee.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理者のみアクセス可能です");
        }
        return ResponseEntity.ok(monthlyReportService.getAllReports(YearMonth.parse(yearMonth)));
    }

    private Long getEmployeeId(Authentication authentication) {
        return getEmployee(authentication).getId();
    }

    private Employee getEmployee(Authentication authentication) {
        return employeeRepository.findByEmployeeCode(authentication.getName()).orElseThrow();
    }
}
