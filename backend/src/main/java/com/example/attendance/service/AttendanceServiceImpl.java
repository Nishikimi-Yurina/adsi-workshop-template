package com.example.attendance.service;

import com.example.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.dto.AttendanceRecordUpdateRequest;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.vo.WorkDuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository repository;

    public AttendanceServiceImpl(AttendanceRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public AttendanceRecordResponse clockIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        repository.findByEmployeeIdAndDate(employeeId, today).ifPresent(r -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "既に出勤打刻済みです");
        });

        AttendanceRecord record = AttendanceRecord.builder()
                .employeeId(employeeId)
                .date(today)
                .clockIn(LocalDateTime.now())
                .build();

        return toResponse(repository.save(record));
    }

    @Override
    public AttendanceRecordResponse clockOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = repository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "出勤打刻がありません"));

        if (record.getClockOut() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "既に退勤打刻済みです");
        }

        record.setClockOut(LocalDateTime.now());
        return toResponse(repository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getRecords(Long employeeId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return repository.findByEmployeeIdAndDateBetween(employeeId, start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AttendanceRecordResponse updateRecord(Long recordId, Long employeeId, AttendanceRecordUpdateRequest request) {
        AttendanceRecord record = repository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "レコードが存在しません"));

        if (!record.getEmployeeId().equals(employeeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "他人の打刻は修正できません");
        }

        record.setClockIn(request.clockIn());
        record.setClockOut(request.clockOut());
        return toResponse(repository.save(record));
    }

    private AttendanceRecordResponse toResponse(AttendanceRecord record) {
        WorkDuration duration = WorkDuration.calculate(record.getClockIn(), record.getClockOut());
        return new AttendanceRecordResponse(
                record.getId(),
                record.getDate(),
                record.getClockIn(),
                record.getClockOut(),
                duration != null ? duration.totalMinutes() : null,
                duration != null ? duration.overtimeMinutes() : null
        );
    }
}
