package com.example.attendance.service;

import com.example.attendance.dto.OvertimeRequestCreateRequest;
import com.example.attendance.dto.OvertimeRequestResponse;

import java.util.List;

public interface OvertimeRequestService {

    OvertimeRequestResponse create(OvertimeRequestCreateRequest request, Long employeeId);

    List<OvertimeRequestResponse> findAll(Long employeeId, String role, String statusFilter);

    OvertimeRequestResponse approve(Long id, Long approverId);

    OvertimeRequestResponse reject(Long id, Long approverId);
}
