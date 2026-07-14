package com.example.attendance.service;

import com.example.attendance.dto.MonthlyReportResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.vo.WorkDuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MonthlyReportServiceImpl implements MonthlyReportService {

    private final AttendanceRecordRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public MonthlyReportServiceImpl(AttendanceRecordRepository attendanceRepository,
                                    EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public MonthlyReportResponse getReport(Long employeeId, YearMonth yearMonth) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow();
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<AttendanceRecord> records = attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end);
        return buildReport(employee, yearMonth, records);
    }

    @Override
    public List<MonthlyReportResponse> getAllReports(YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<Employee> employees = employeeRepository.findAll();
        List<AttendanceRecord> allRecords = attendanceRepository.findByDateBetween(start, end);

        var recordsByEmployee = allRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(AttendanceRecord::getEmployeeId));

        return employees.stream().map(employee -> {
            List<AttendanceRecord> records = recordsByEmployee.getOrDefault(employee.getId(), List.of());
            return buildReport(employee, yearMonth, records);
        }).toList();
    }

    private MonthlyReportResponse buildReport(Employee employee, YearMonth yearMonth, List<AttendanceRecord> records) {
        int workDays = 0;
        int totalWorkMinutes = 0;
        int totalOvertimeMinutes = 0;

        for (AttendanceRecord record : records) {
            WorkDuration duration = WorkDuration.calculate(record.getClockIn(), record.getClockOut());
            if (duration != null) {
                workDays++;
                totalWorkMinutes += duration.totalMinutes();
                totalOvertimeMinutes += duration.overtimeMinutes();
            }
        }

        return new MonthlyReportResponse(
                employee.getId(),
                employee.getName(),
                yearMonth.toString(),
                workDays,
                totalWorkMinutes,
                totalOvertimeMinutes
        );
    }
}
