package com.example.attendance.service;

import com.example.attendance.dto.OvertimeRequestCreateRequest;
import com.example.attendance.dto.OvertimeRequestResponse;

import java.util.List;

public interface OvertimeRequestService {

    OvertimeRequestResponse create(OvertimeRequestCreateRequest request, String employeeCode);

    List<OvertimeRequestResponse> findAll(String employeeCode);

    List<OvertimeRequestResponse> findAllByStatus(String statusFilter, String employeeCode);

    OvertimeRequestResponse approve(Long id, String approverCode);

    OvertimeRequestResponse reject(Long id, String approverCode);
}
