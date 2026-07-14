package com.example.attendance.service;

import com.example.attendance.dto.OvertimeRequestCreateRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.OvertimeRequest;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.OvertimeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeRequestServiceImplTest {

    @Mock
    private OvertimeRequestRepository overtimeRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private OvertimeRequestServiceImpl service;

    private Employee employee;
    private Employee admin;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .name("テスト太郎")
                .role(Role.EMPLOYEE)
                .build();
        admin = Employee.builder()
                .id(2L)
                .employeeCode("ADMIN001")
                .name("管理者")
                .role(Role.ADMIN)
                .build();
    }

    @Test
    @DisplayName("残業申請作成: ステータスがPENDINGで作成される")
    void create_validRequest_returnsPending() {
        // Arrange
        var request = new OvertimeRequestCreateRequest(LocalDate.of(2026, 7, 20), new BigDecimal("2.50"), "納期対応");
        var saved = OvertimeRequest.builder()
                .id(1L)
                .employeeId(1L)
                .date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("2.50"))
                .reason("納期対応")
                .status(ApprovalStatus.PENDING)
                .build();
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        when(overtimeRequestRepository.save(any())).thenReturn(saved);

        // Act
        var result = service.create(request, "EMP001");

        // Assert
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.expectedHours()).isEqualByComparingTo(new BigDecimal("2.50"));
    }

    @Test
    @DisplayName("一般社員は自分の申請のみ取得できる")
    void findAll_employee_returnsOwnOnly() {
        // Arrange
        var overtime = OvertimeRequest.builder()
                .id(1L).employeeId(1L).date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("1.00")).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        when(overtimeRequestRepository.findByEmployeeId(1L)).thenReturn(List.of(overtime));
        when(employeeRepository.findAllById(List.of(1L))).thenReturn(List.of(employee));

        // Act
        var results = service.findAll("EMP001");

        // Assert
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("管理者は全申請を取得できる")
    void findAll_admin_returnsAll() {
        // Arrange
        var overtime = OvertimeRequest.builder()
                .id(1L).employeeId(1L).date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("1.00")).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        when(overtimeRequestRepository.findAll()).thenReturn(List.of(overtime));
        when(employeeRepository.findAllById(List.of(1L))).thenReturn(List.of(employee));

        // Act
        var results = service.findAll("ADMIN001");

        // Assert
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("承認: ステータスがAPPROVEDに変わる")
    void approve_pendingRequest_changesStatusToApproved() {
        // Arrange
        var entity = OvertimeRequest.builder()
                .id(1L).employeeId(1L).date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("1.00")).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        when(overtimeRequestRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(overtimeRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        var result = service.approve(1L, "ADMIN001");

        // Assert
        assertThat(result.status()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("承認済みの申請は再承認できない")
    void approve_alreadyApproved_throwsException() {
        // Arrange
        var entity = OvertimeRequest.builder()
                .id(1L).employeeId(1L).date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("1.00")).status(ApprovalStatus.APPROVED).build();
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        when(overtimeRequestRepository.findById(1L)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThatThrownBy(() -> service.approve(1L, "ADMIN001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("この申請は既に処理済みです");
    }

    @Test
    @DisplayName("自分の申請は承認できない")
    void approve_ownRequest_throwsException() {
        // Arrange
        var entity = OvertimeRequest.builder()
                .id(1L).employeeId(1L).date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("1.00")).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        when(overtimeRequestRepository.findById(1L)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThatThrownBy(() -> service.approve(1L, "EMP001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("自分の申請を承認することはできません");
    }

    @Test
    @DisplayName("却下: ステータスがREJECTEDに変わる")
    void reject_pendingRequest_changesStatusToRejected() {
        // Arrange
        var entity = OvertimeRequest.builder()
                .id(1L).employeeId(1L).date(LocalDate.of(2026, 7, 20))
                .expectedHours(new BigDecimal("1.00")).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        when(overtimeRequestRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(overtimeRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        var result = service.reject(1L, "ADMIN001");

        // Assert
        assertThat(result.status()).isEqualTo("REJECTED");
    }
}
