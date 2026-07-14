package com.example.attendance.repository;

import com.example.attendance.entity.Employee;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.enums.LeaveType;
import com.example.attendance.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LeaveRequestRepositoryTest {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

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
    @DisplayName("社員IDで休暇申請を検索できる")
    void findByEmployeeId_existingEmployee_returnsRequests() {
        // Arrange
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20))
                .reason("旅行")
                .build());

        // Act
        var results = leaveRequestRepository.findByEmployeeId(employee.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLeaveType()).isEqualTo(LeaveType.PAID_LEAVE);
    }

    @Test
    @DisplayName("ステータスで休暇申請をフィルタできる")
    void findByStatus_pending_returnsPendingOnly() {
        // Arrange
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20))
                .build());

        // Act
        var results = leaveRequestRepository.findByStatus(ApprovalStatus.PENDING);

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(ApprovalStatus.PENDING);
    }
}
