package com.example.attendance.repository;

import com.example.attendance.entity.AttendanceRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AttendanceRecordRepositoryTest {

    @Autowired
    private AttendanceRecordRepository repository;

    @Test
    @DisplayName("社員IDと日付で勤怠レコードを検索できる")
    void findByEmployeeIdAndDate_existing_returnsRecord() {
        var record = AttendanceRecord.builder()
                .employeeId(1L)
                .date(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .build();
        repository.save(record);

        var result = repository.findByEmployeeIdAndDate(1L, LocalDate.of(2026, 7, 14));

        assertThat(result).isPresent();
        assertThat(result.get().getClockIn()).isEqualTo(LocalDateTime.of(2026, 7, 14, 9, 0));
    }

    @Test
    @DisplayName("存在しない日付で検索すると空が返る")
    void findByEmployeeIdAndDate_notExisting_returnsEmpty() {
        var result = repository.findByEmployeeIdAndDate(1L, LocalDate.of(2026, 7, 14));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("期間指定で勤怠レコードを取得できる")
    void findByEmployeeIdAndDateBetween_returnsRecords() {
        repository.save(AttendanceRecord.builder()
                .employeeId(1L).date(LocalDate.of(2026, 7, 1))
                .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0)).build());
        repository.save(AttendanceRecord.builder()
                .employeeId(1L).date(LocalDate.of(2026, 7, 15))
                .clockIn(LocalDateTime.of(2026, 7, 15, 9, 0)).build());
        repository.save(AttendanceRecord.builder()
                .employeeId(1L).date(LocalDate.of(2026, 8, 1))
                .clockIn(LocalDateTime.of(2026, 8, 1, 9, 0)).build());

        List<AttendanceRecord> result = repository.findByEmployeeIdAndDateBetween(
                1L, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        assertThat(result).hasSize(2);
    }
}
