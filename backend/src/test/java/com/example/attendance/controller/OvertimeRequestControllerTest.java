package com.example.attendance.controller;

import com.example.attendance.dto.OvertimeRequestResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.OvertimeRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OvertimeRequestController.class)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.example.attendance.config.SecurityConfig.class)
class OvertimeRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OvertimeRequestService overtimeRequestService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    private final Employee employee = Employee.builder()
            .id(1L).employeeCode("EMP001").name("テスト太郎").role(Role.EMPLOYEE).build();

    private final Employee admin = Employee.builder()
            .id(2L).employeeCode("ADMIN001").name("管理者").role(Role.ADMIN).build();

    @Test
    @WithMockUser(username = "EMP001")
    @DisplayName("GET /api/overtime-requests: 一覧取得で200を返す")
    void findAll_authenticated_returns200() throws Exception {
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        when(overtimeRequestService.findAll(1L, "EMPLOYEE", null)).thenReturn(List.of());

        mockMvc.perform(get("/api/overtime-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "EMP001")
    @DisplayName("POST /api/overtime-requests: 申請作成で201を返す")
    void create_validRequest_returns201() throws Exception {
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        var response = new OvertimeRequestResponse(1L, 1L, "テスト太郎",
                LocalDate.of(2026, 7, 20), new BigDecimal("2.50"), "納期対応", "PENDING");
        when(overtimeRequestService.create(any(), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/overtime-requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"date":"2026-07-20","expectedHours":2.50,"reason":"納期対応"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    @DisplayName("PUT /api/overtime-requests/{id}/approve: 管理者が承認で200を返す")
    void approve_admin_returns200() throws Exception {
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        var response = new OvertimeRequestResponse(1L, 1L, "テスト太郎",
                LocalDate.of(2026, 7, 20), new BigDecimal("2.50"), "納期対応", "APPROVED");
        when(overtimeRequestService.approve(1L, 2L)).thenReturn(response);

        mockMvc.perform(put("/api/overtime-requests/1/approve").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "EMP001", roles = "EMPLOYEE")
    @DisplayName("PUT /api/overtime-requests/{id}/approve: 一般社員は403を返す")
    void approve_employee_returns403() throws Exception {
        mockMvc.perform(put("/api/overtime-requests/1/approve").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
