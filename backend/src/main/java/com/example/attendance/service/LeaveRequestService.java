package com.example.attendance.service;

import com.example.attendance.dto.LeaveRequestCreateRequest;
import com.example.attendance.dto.LeaveRequestResponse;

import java.util.List;

public interface LeaveRequestService {

    LeaveRequestResponse create(LeaveRequestCreateRequest request, String employeeCode);

    List<LeaveRequestResponse> findAll(String employeeCode);

    List<LeaveRequestResponse> findAllByStatus(String statusFilter, String employeeCode);

    LeaveRequestResponse approve(Long id, String approverCode);

    LeaveRequestResponse reject(Long id, String approverCode);
}
