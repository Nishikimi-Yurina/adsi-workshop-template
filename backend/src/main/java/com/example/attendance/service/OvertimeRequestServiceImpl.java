package com.example.attendance.service;

import com.example.attendance.dto.OvertimeRequestCreateRequest;
import com.example.attendance.dto.OvertimeRequestResponse;
import com.example.attendance.entity.OvertimeRequest;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.OvertimeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OvertimeRequestServiceImpl implements OvertimeRequestService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final EmployeeRepository employeeRepository;

    public OvertimeRequestServiceImpl(OvertimeRequestRepository overtimeRequestRepository,
                                      EmployeeRepository employeeRepository) {
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public OvertimeRequestResponse create(OvertimeRequestCreateRequest request, Long employeeId) {
        var entity = OvertimeRequest.builder()
                .employeeId(employeeId)
                .date(request.date())
                .expectedHours(request.expectedHours())
                .reason(request.reason())
                .build();
        var saved = overtimeRequestRepository.save(entity);
        var employeeName = getEmployeeName(employeeId);
        return OvertimeRequestResponse.from(saved, employeeName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OvertimeRequestResponse> findAll(Long employeeId, String role, String statusFilter) {
        List<OvertimeRequest> requests;
        if ("ADMIN".equals(role)) {
            requests = statusFilter != null
                    ? overtimeRequestRepository.findByStatus(ApprovalStatus.valueOf(statusFilter))
                    : overtimeRequestRepository.findAll();
        } else {
            requests = statusFilter != null
                    ? overtimeRequestRepository.findByEmployeeIdAndStatus(employeeId, ApprovalStatus.valueOf(statusFilter))
                    : overtimeRequestRepository.findByEmployeeId(employeeId);
        }
        return requests.stream()
                .map(r -> OvertimeRequestResponse.from(r, getEmployeeName(r.getEmployeeId())))
                .toList();
    }

    @Override
    public OvertimeRequestResponse approve(Long id, Long approverId) {
        var entity = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("残業申請が見つかりません: " + id));
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        entity.setStatus(ApprovalStatus.APPROVED);
        entity.setApproverId(approverId);
        var saved = overtimeRequestRepository.save(entity);
        return OvertimeRequestResponse.from(saved, getEmployeeName(saved.getEmployeeId()));
    }

    @Override
    public OvertimeRequestResponse reject(Long id, Long approverId) {
        var entity = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("残業申請が見つかりません: " + id));
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        entity.setStatus(ApprovalStatus.REJECTED);
        entity.setApproverId(approverId);
        var saved = overtimeRequestRepository.save(entity);
        return OvertimeRequestResponse.from(saved, getEmployeeName(saved.getEmployeeId()));
    }

    private String getEmployeeName(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(e -> e.getName())
                .orElse("不明");
    }
}
