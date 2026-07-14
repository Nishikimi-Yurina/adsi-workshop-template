package com.example.attendance.controller;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.dto.MonthlyReportResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AuthService;
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
    private AuthService authService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @WithMockUser(username = "user01")
    @DisplayName("GET /api/reports/monthly: 自分の月次レポートを取得できる")
    void getMonthlyReport_success() throws Exception {
        when(authService.getEmployeeId("user01")).thenReturn(2L);
        var report = new MonthlyReportResponse(2L, "山田太郎", "2026-07", 20, 9600, 400);
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
        var admin = Employee.builder().id(1L).employeeCode("admin").name("管理者").role(Role.ADMIN).build();
        when(authService.getEmployee("admin")).thenReturn(admin);
        var reports = List.of(
                new MonthlyReportResponse(1L, "管理者", "2026-07", 22, 10560, 0),
                new MonthlyReportResponse(2L, "山田太郎", "2026-07", 20, 9600, 400)
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
        var employee = Employee.builder().id(2L).employeeCode("user01").name("山田").role(Role.EMPLOYEE).build();
        when(authService.getEmployee("user01")).thenReturn(employee);

        mockMvc.perform(get("/api/reports/monthly/all").param("yearMonth", "2026-07"))
                .andExpect(status().isForbidden());
    }
}
