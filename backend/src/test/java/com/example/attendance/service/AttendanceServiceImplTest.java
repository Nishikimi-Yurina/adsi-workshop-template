package com.example.attendance.service;

import com.example.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.dto.AttendanceRecordUpdateRequest;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.repository.AttendanceRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AttendanceServiceImplTest {

    private AttendanceRecordRepository repository;
    private AttendanceServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(AttendanceRecordRepository.class);
        service = new AttendanceServiceImpl(repository);
    }

    @Test
    @DisplayName("出勤打刻: 当日未打刻の場合、出勤時刻が記録される")
    void clockIn_notYetClockedIn_success() {
        when(repository.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());
        when(repository.save(any(AttendanceRecord.class))).thenAnswer(inv -> {
            AttendanceRecord r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        AttendanceRecordResponse result = service.clockIn(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.clockIn()).isNotNull();
        verify(repository).save(any(AttendanceRecord.class));
    }

    @Test
    @DisplayName("出勤打刻: 既に出勤済みの場合、409エラー")
    void clockIn_alreadyClockedIn_conflict() {
        var existing = AttendanceRecord.builder().id(1L).employeeId(1L).build();
        when(repository.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.clockIn(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    @DisplayName("退勤打刻: 出勤済みで未退勤の場合、退勤時刻が記録される")
    void clockOut_clockedIn_success() {
        var record = AttendanceRecord.builder()
                .id(1L).employeeId(1L)
                .date(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(8))
                .build();
        when(repository.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.of(record));
        when(repository.save(any(AttendanceRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceRecordResponse result = service.clockOut(1L);

        assertThat(result.clockOut()).isNotNull();
    }

    @Test
    @DisplayName("退勤打刻: 出勤していない場合、404エラー")
    void clockOut_notClockedIn_notFound() {
        when(repository.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.clockOut(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("退勤打刻: 既に退勤済みの場合、409エラー")
    void clockOut_alreadyClockedOut_conflict() {
        var record = AttendanceRecord.builder()
                .id(1L).employeeId(1L)
                .date(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(8))
                .clockOut(LocalDateTime.now())
                .build();
        when(repository.findByEmployeeIdAndDate(eq(1L), any(LocalDate.class))).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.clockOut(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    @DisplayName("打刻修正: 本人のレコードを修正できる")
    void updateRecord_ownRecord_success() {
        var record = AttendanceRecord.builder()
                .id(1L).employeeId(1L)
                .date(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(record));
        when(repository.save(any(AttendanceRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AttendanceRecordUpdateRequest(
                LocalDateTime.of(2026, 7, 14, 8, 30),
                LocalDateTime.of(2026, 7, 14, 17, 30)
        );

        AttendanceRecordResponse result = service.updateRecord(1L, 1L, request);

        assertThat(result.clockIn()).isEqualTo(LocalDateTime.of(2026, 7, 14, 8, 30));
        assertThat(result.clockOut()).isEqualTo(LocalDateTime.of(2026, 7, 14, 17, 30));
    }

    @Test
    @DisplayName("打刻修正: 他人のレコードは403エラー")
    void updateRecord_otherEmployee_forbidden() {
        var record = AttendanceRecord.builder()
                .id(1L).employeeId(2L)
                .date(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(record));

        var request = new AttendanceRecordUpdateRequest(
                LocalDateTime.of(2026, 7, 14, 8, 30), null
        );

        assertThatThrownBy(() -> service.updateRecord(1L, 1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    @DisplayName("勤怠一覧: 指定月の記録が取得できる")
    void getRecords_returnsMonthlyRecords() {
        var records = List.of(
                AttendanceRecord.builder().id(1L).employeeId(1L)
                        .date(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 1, 18, 0))
                        .build()
        );
        when(repository.findByEmployeeIdAndDateBetween(eq(1L), any(), any())).thenReturn(records);

        List<AttendanceRecordResponse> result = service.getRecords(1L, YearMonth.of(2026, 7));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).workMinutes()).isEqualTo(540);
        assertThat(result.get(0).overtimeMinutes()).isEqualTo(60);
    }
}
