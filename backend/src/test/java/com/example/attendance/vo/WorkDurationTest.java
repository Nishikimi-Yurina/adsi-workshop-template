package com.example.attendance.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkDurationTest {

    @Test
    @DisplayName("8時間勤務の場合、残業は0分")
    void calculate_8hours_noOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 17, 0);

        var result = WorkDuration.calculate(clockIn, clockOut);

        assertThat(result.totalMinutes()).isEqualTo(480);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("10時間勤務の場合、残業は120分")
    void calculate_10hours_120minutesOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 19, 0);

        var result = WorkDuration.calculate(clockIn, clockOut);

        assertThat(result.totalMinutes()).isEqualTo(600);
        assertThat(result.overtimeMinutes()).isEqualTo(120);
    }

    @Test
    @DisplayName("6時間勤務の場合、残業は0分")
    void calculate_6hours_noOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 15, 0);

        var result = WorkDuration.calculate(clockIn, clockOut);

        assertThat(result.totalMinutes()).isEqualTo(360);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("退勤がnullの場合、nullを返す")
    void calculate_nullClockOut_returnsNull() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);

        var result = WorkDuration.calculate(clockIn, null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("出勤がnullの場合、nullを返す")
    void calculate_nullClockIn_returnsNull() {
        var result = WorkDuration.calculate(null, LocalDateTime.of(2026, 7, 14, 17, 0));

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("479分勤務の場合、残業は0分")
    void calculate_479minutes_noOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 16, 59);

        var result = WorkDuration.calculate(clockIn, clockOut);

        assertThat(result.totalMinutes()).isEqualTo(479);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("481分勤務の場合、残業は1分")
    void calculate_481minutes_1minuteOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 17, 1);

        var result = WorkDuration.calculate(clockIn, clockOut);

        assertThat(result.totalMinutes()).isEqualTo(481);
        assertThat(result.overtimeMinutes()).isEqualTo(1);
    }

    @Test
    @DisplayName("0分勤務（出退勤同時刻）の場合、残業は0分")
    void calculate_0minutes_noOvertime() {
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 9, 0);

        var result = WorkDuration.calculate(clockIn, clockOut);

        assertThat(result.totalMinutes()).isEqualTo(0);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
    }
}
