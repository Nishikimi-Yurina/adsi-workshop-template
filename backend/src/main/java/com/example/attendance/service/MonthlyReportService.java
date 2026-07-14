package com.example.attendance.service;

import com.example.attendance.dto.MonthlyReportResponse;

import java.time.YearMonth;
import java.util.List;

public interface MonthlyReportService {

    MonthlyReportResponse getReport(Long employeeId, YearMonth yearMonth);

    List<MonthlyReportResponse> getAllReports(YearMonth yearMonth);
}
