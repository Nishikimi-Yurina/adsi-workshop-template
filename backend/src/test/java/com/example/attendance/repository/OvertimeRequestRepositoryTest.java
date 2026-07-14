package com.example.attendance.repository;

import com.example.attendance.entity.Employee;
import com.example.attendance.entity.OvertimeRequest;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OvertimeRequestRepositoryTest {

    @Autowired
    private OvertimeRequestRepository overtimeRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP001")
                .name("テスト太郎")
                .email("test@example.com")
                .password("hashed")
                .role(Role.EMPLOYEE)
                .build());
    }

    @Test
    @DisplayName("社員IDで残業申請を検索できる")
    void findByEmployeeId_existingEmployee_returnsRequests() {
        // Arrange
        overtimeRequestRepository.save(OvertimeRequest.builder()
                .employeeId(employee.getId())
                .date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("2.50"))
                .reason("納期対応")
                .build());

        // Act
        var results = overtimeRequestRepository.findByEmployeeId(employee.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getExpectedHours()).isEqualByComparingTo(new BigDecimal("2.50"));
    }

    @Test
    @DisplayName("ステータスで残業申請をフィルタできる")
    void findByStatus_pending_returnsPendingOnly() {
        // Arrange
        overtimeRequestRepository.save(OvertimeRequest.builder()
                .employeeId(employee.getId())
                .date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("1.00"))
                .build());

        // Act
        var results = overtimeRequestRepository.findByStatus(ApprovalStatus.PENDING);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(ApprovalStatus.PENDING);
    }
}
