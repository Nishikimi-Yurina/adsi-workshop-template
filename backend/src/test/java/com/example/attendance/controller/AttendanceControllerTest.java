package com.example.attendance.controller;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.dto.AttendanceRecordUpdateRequest;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("POST /api/attendance/clock-in: 出勤打刻が成功する")
    void clockIn_success() throws Exception {
        when(authService.getEmployeeId("admin")).thenReturn(1L);
        var response = new AttendanceRecordResponse(1L, LocalDate.now(), LocalDateTime.now(), null, null, null);
        when(attendanceService.clockIn(1L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/clock-in").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("POST /api/attendance/clock-in: 二重打刻で409")
    void clockIn_conflict() throws Exception {
        when(authService.getEmployeeId("admin")).thenReturn(1L);
        when(attendanceService.clockIn(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/attendance/clock-in").with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("POST /api/attendance/clock-out: 退勤打刻が成功する")
    void clockOut_success() throws Exception {
        when(authService.getEmployeeId("admin")).thenReturn(1L);
        var response = new AttendanceRecordResponse(1L, LocalDate.now(),
                LocalDateTime.now().minusHours(8), LocalDateTime.now(), 480, 0);
        when(attendanceService.clockOut(1L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/clock-out").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOut").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("GET /api/attendance/records: 月別勤怠一覧を取得できる")
    void getRecords_success() throws Exception {
        when(authService.getEmployeeId("admin")).thenReturn(1L);
        var records = List.of(
                new AttendanceRecordResponse(1L, LocalDate.of(2026, 7, 1),
                        LocalDateTime.of(2026, 7, 1, 9, 0), LocalDateTime.of(2026, 7, 1, 18, 0), 540, 60)
        );
        when(attendanceService.getRecords(eq(1L), eq(YearMonth.of(2026, 7)))).thenReturn(records);

        mockMvc.perform(get("/api/attendance/records").param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workMinutes").value(540));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("PUT /api/attendance/records/{id}: 打刻修正が成功する")
    void updateRecord_success() throws Exception {
        when(authService.getEmployeeId("admin")).thenReturn(1L);
        var response = new AttendanceRecordResponse(1L, LocalDate.of(2026, 7, 14),
                LocalDateTime.of(2026, 7, 14, 8, 30), LocalDateTime.of(2026, 7, 14, 17, 30), 540, 60);
        when(attendanceService.updateRecord(eq(1L), eq(1L), any())).thenReturn(response);

        var request = new AttendanceRecordUpdateRequest(
                LocalDateTime.of(2026, 7, 14, 8, 30),
                LocalDateTime.of(2026, 7, 14, 17, 30));

        mockMvc.perform(put("/api/attendance/records/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workMinutes").value(540));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("GET /api/attendance/records: yearMonthが不正な場合400が返る")
    void getRecords_invalidYearMonth_returns400() throws Exception {
        when(authService.getEmployeeId("admin")).thenReturn(1L);

        mockMvc.perform(get("/api/attendance/records").param("yearMonth", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("日付形式が不正です。YYYY-MM の形式で指定してください"));
    }

    @Test
    @DisplayName("未認証でアクセスすると401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
