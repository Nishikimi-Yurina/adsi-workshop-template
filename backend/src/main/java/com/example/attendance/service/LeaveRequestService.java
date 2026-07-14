package com.example.attendance.service;

import com.example.attendance.dto.LeaveRequestCreateRequest;
import com.example.attendance.dto.LeaveRequestResponse;

import java.util.List;

public interface LeaveRequestService {

    LeaveRequestResponse create(LeaveRequestCreateRequest request, Long employeeId);

    List<LeaveRequestResponse> findAll(Long employeeId, String role, String statusFilter);

    LeaveRequestResponse approve(Long id, Long approverId);

    LeaveRequestResponse reject(Long id, Long approverId);
}
