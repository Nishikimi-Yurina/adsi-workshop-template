package com.example.attendance.service;

import com.example.attendance.dto.LeaveRequestCreateRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.enums.LeaveType;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveRequestServiceImpl service;

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
    @DisplayName("休暇申請作成: ステータスがPENDINGで作成される")
    void create_validRequest_returnsPending() {
        // Arrange
        var request = new LeaveRequestCreateRequest(LeaveType.PAID_LEAVE, LocalDate.of(2026, 7, 20), "旅行");
        var saved = LeaveRequest.builder()
                .id(1L)
                .employeeId(1L)
                .leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20))
                .reason("旅行")
                .status(ApprovalStatus.PENDING)
                .build();
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any())).thenReturn(saved);

        // Act
        var result = service.create(request, "EMP001");

        // Assert
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.leaveType()).isEqualTo("PAID_LEAVE");
    }

    @Test
    @DisplayName("一般社員は自分の申請のみ取得できる")
    void findAll_employee_returnsOwnOnly() {
        // Arrange
        var leaveRequest = LeaveRequest.builder()
                .id(1L).employeeId(1L).leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20)).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(List.of(leaveRequest));
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
        var leaveRequest = LeaveRequest.builder()
                .id(1L).employeeId(1L).leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20)).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        when(leaveRequestRepository.findAll()).thenReturn(List.of(leaveRequest));
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
        var entity = LeaveRequest.builder()
                .id(1L).employeeId(1L).leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20)).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(leaveRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));
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
        var entity = LeaveRequest.builder()
                .id(1L).employeeId(1L).leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20)).status(ApprovalStatus.APPROVED).build();
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThatThrownBy(() -> service.approve(1L, "ADMIN001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("この申請は既に処理済みです");
    }

    @Test
    @DisplayName("自分の申請は承認できない")
    void approve_ownRequest_throwsException() {
        // Arrange
        var entity = LeaveRequest.builder()
                .id(1L).employeeId(1L).leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20)).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(entity));

        // Act & Assert
        assertThatThrownBy(() -> service.approve(1L, "EMP001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("自分の申請を承認することはできません");
    }

    @Test
    @DisplayName("却下: ステータスがREJECTEDに変わる")
    void reject_pendingRequest_changesStatusToRejected() {
        // Arrange
        var entity = LeaveRequest.builder()
                .id(1L).employeeId(1L).leaveType(LeaveType.PAID_LEAVE)
                .date(LocalDate.of(2026, 7, 20)).status(ApprovalStatus.PENDING).build();
        when(employeeRepository.findByEmployeeCode("ADMIN001")).thenReturn(Optional.of(admin));
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(leaveRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // Act
        var result = service.reject(1L, "ADMIN001");

        // Assert
        assertThat(result.status()).isEqualTo("REJECTED");
    }
}
