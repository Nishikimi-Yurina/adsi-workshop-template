package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.dto.AttendanceRecordUpdateRequest;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AuthService authService;

    public AttendanceController(AttendanceService attendanceService, AuthService authService) {
        this.attendanceService = attendanceService;
        this.authService = authService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecordResponse> clockIn(Authentication authentication) {
        Long employeeId = authService.getEmployeeId(authentication.getName());
        return ResponseEntity.ok(attendanceService.clockIn(employeeId));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecordResponse> clockOut(Authentication authentication) {
        Long employeeId = authService.getEmployeeId(authentication.getName());
        return ResponseEntity.ok(attendanceService.clockOut(employeeId));
    }

    @GetMapping("/records")
    public ResponseEntity<List<AttendanceRecordResponse>> getRecords(
            @RequestParam String yearMonth,
            Authentication authentication) {
        Long employeeId = authService.getEmployeeId(authentication.getName());
        return ResponseEntity.ok(attendanceService.getRecords(employeeId, YearMonth.parse(yearMonth)));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<AttendanceRecordResponse> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRecordUpdateRequest request,
            Authentication authentication) {
        Long employeeId = authService.getEmployeeId(authentication.getName());
        return ResponseEntity.ok(attendanceService.updateRecord(id, employeeId, request));
    }
}
