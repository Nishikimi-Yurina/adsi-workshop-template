package com.example.attendance.controller;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.dto.LeaveRequestResponse;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.LeaveRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LeaveRequestController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class LeaveRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaveRequestService leaveRequestService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @WithMockUser(username = "EMP001")
    @DisplayName("GET /api/leave-requests: 一覧取得で200を返す")
    void findAll_authenticated_returns200() throws Exception {
        when(leaveRequestService.findAll("EMP001")).thenReturn(List.of());

        mockMvc.perform(get("/api/leave-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "EMP001")
    @DisplayName("GET /api/leave-requests?status=PENDING: ステータスフィルタで200を返す")
    void findAllByStatus_authenticated_returns200() throws Exception {
        when(leaveRequestService.findAllByStatus("PENDING", "EMP001")).thenReturn(List.of());

        mockMvc.perform(get("/api/leave-requests").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "EMP001")
    @DisplayName("POST /api/leave-requests: 申請作成で201を返す")
    void create_validRequest_returns201() throws Exception {
        var response = new LeaveRequestResponse(1L, 1L, "テスト太郎", "PAID_LEAVE",
                LocalDate.of(2026, 7, 20), "旅行", "PENDING");
        when(leaveRequestService.create(any(), eq("EMP001"))).thenReturn(response);

        mockMvc.perform(post("/api/leave-requests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leaveType":"PAID_LEAVE","date":"2026-07-20","reason":"旅行"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = "ADMIN")
    @DisplayName("PUT /api/leave-requests/{id}/approve: 管理者が承認で200を返す")
    void approve_admin_returns200() throws Exception {
        var response = new LeaveRequestResponse(1L, 1L, "テスト太郎", "PAID_LEAVE",
                LocalDate.of(2026, 7, 20), "旅行", "APPROVED");
        when(leaveRequestService.approve(1L, "ADMIN001")).thenReturn(response);

        mockMvc.perform(put("/api/leave-requests/1/approve").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "EMP001", roles = "EMPLOYEE")
    @DisplayName("PUT /api/leave-requests/{id}/approve: 一般社員は403を返す")
    void approve_employee_returns403() throws Exception {
        mockMvc.perform(put("/api/leave-requests/1/approve").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
