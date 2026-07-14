package com.example.attendance.service;

import com.example.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.dto.AttendanceRecordUpdateRequest;

import java.time.YearMonth;
import java.util.List;

public interface AttendanceService {

    AttendanceRecordResponse clockIn(Long employeeId);

    AttendanceRecordResponse clockOut(Long employeeId);

    List<AttendanceRecordResponse> getRecords(Long employeeId, YearMonth yearMonth);

    AttendanceRecordResponse updateRecord(Long recordId, Long employeeId, AttendanceRecordUpdateRequest request);
}
