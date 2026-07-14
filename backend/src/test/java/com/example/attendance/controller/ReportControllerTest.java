package com.example.attendance.controller;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.dto.MonthlyReportResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.MonthlyReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MonthlyReportService monthlyReportService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @WithMockUser(username = "user01")
    @DisplayName("GET /api/reports/monthly: 自分の月次レポートを取得できる")
    void getMonthlyReport_success() throws Exception {
        mockEmployee("user01", 2L, Role.EMPLOYEE);
        var report = new MonthlyReportResponse(2L, "山田太郎", "2026-07", 20, 9600, 400, 1, 0);
        when(monthlyReportService.getReport(eq(2L), eq(YearMonth.of(2026, 7)))).thenReturn(report);

        mockMvc.perform(get("/api/reports/monthly").param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("山田太郎"))
                .andExpect(jsonPath("$.workDays").value(20));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("GET /api/reports/monthly/all: 管理者は全社員レポートを取得できる")
    void getAllReports_admin_success() throws Exception {
        mockEmployee("admin", 1L, Role.ADMIN);
        var reports = List.of(
                new MonthlyReportResponse(1L, "管理者", "2026-07", 22, 10560, 0, 0, 0),
                new MonthlyReportResponse(2L, "山田太郎", "2026-07", 20, 9600, 400, 1, 0)
        );
        when(monthlyReportService.getAllReports(eq(YearMonth.of(2026, 7)))).thenReturn(reports);

        mockMvc.perform(get("/api/reports/monthly/all").param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "user01")
    @DisplayName("GET /api/reports/monthly/all: 一般社員は403")
    void getAllReports_employee_forbidden() throws Exception {
        mockEmployee("user01", 2L, Role.EMPLOYEE);

        mockMvc.perform(get("/api/reports/monthly/all").param("yearMonth", "2026-07"))
                .andExpect(status().isForbidden());
    }

    private void mockEmployee(String code, Long id, Role role) {
        var employee = Employee.builder().id(id).employeeCode(code).name("テスト").role(role).build();
        when(employeeRepository.findByEmployeeCode(code)).thenReturn(Optional.of(employee));
    }
}
