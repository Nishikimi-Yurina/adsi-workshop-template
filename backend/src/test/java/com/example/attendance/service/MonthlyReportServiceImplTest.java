package com.example.attendance.service;

import com.example.attendance.dto.MonthlyReportResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonthlyReportServiceImplTest {

    private AttendanceRecordRepository attendanceRepository;
    private EmployeeRepository employeeRepository;
    private MonthlyReportServiceImpl service;

    @BeforeEach
    void setUp() {
        attendanceRepository = mock(AttendanceRecordRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        service = new MonthlyReportServiceImpl(attendanceRepository, employeeRepository);
    }

    @Test
    @DisplayName("月次レポート: 勤務日数・総勤務時間・残業時間が正しく集計される")
    void getReport_calculatesCorrectly() {
        var employee = Employee.builder().id(1L).name("山田太郎").role(Role.EMPLOYEE).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var records = List.of(
                AttendanceRecord.builder().employeeId(1L)
                        .date(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 1, 18, 0)).build(),
                AttendanceRecord.builder().employeeId(1L)
                        .date(LocalDate.of(2026, 7, 2))
                        .clockIn(LocalDateTime.of(2026, 7, 2, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 2, 20, 0)).build()
        );
        when(attendanceRepository.findByEmployeeIdAndDateBetween(eq(1L), any(), any())).thenReturn(records);

        MonthlyReportResponse result = service.getReport(1L, YearMonth.of(2026, 7));

        assertThat(result.employeeName()).isEqualTo("山田太郎");
        assertThat(result.workDays()).isEqualTo(2);
        assertThat(result.totalWorkMinutes()).isEqualTo(540 + 660);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(60 + 180);
    }

    @Test
    @DisplayName("月次レポート: 退勤なしのレコードは集計に含まれない")
    void getReport_noClockOut_notCounted() {
        var employee = Employee.builder().id(1L).name("山田太郎").role(Role.EMPLOYEE).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var records = List.of(
                AttendanceRecord.builder().employeeId(1L)
                        .date(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                        .clockOut(null).build()
        );
        when(attendanceRepository.findByEmployeeIdAndDateBetween(eq(1L), any(), any())).thenReturn(records);

        MonthlyReportResponse result = service.getReport(1L, YearMonth.of(2026, 7));

        assertThat(result.workDays()).isEqualTo(0);
        assertThat(result.totalWorkMinutes()).isEqualTo(0);
    }
}
