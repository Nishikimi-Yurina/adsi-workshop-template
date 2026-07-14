package com.example.attendance.repository;

import com.example.attendance.entity.OvertimeRequest;
import com.example.attendance.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {

    List<OvertimeRequest> findByEmployeeId(Long employeeId);

    List<OvertimeRequest> findByEmployeeIdAndStatus(Long employeeId, ApprovalStatus status);

    List<OvertimeRequest> findByStatus(ApprovalStatus status);
}
