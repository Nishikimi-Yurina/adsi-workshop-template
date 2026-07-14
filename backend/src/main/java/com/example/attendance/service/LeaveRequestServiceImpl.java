package com.example.attendance.service;

import com.example.attendance.dto.LeaveRequestCreateRequest;
import com.example.attendance.dto.LeaveRequestResponse;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.enums.LeaveType;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository,
                                   EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public LeaveRequestResponse create(LeaveRequestCreateRequest request, Long employeeId) {
        var entity = LeaveRequest.builder()
                .employeeId(employeeId)
                .leaveType(LeaveType.valueOf(request.leaveType()))
                .date(request.date())
                .reason(request.reason())
                .build();
        var saved = leaveRequestRepository.save(entity);
        var employeeName = getEmployeeName(employeeId);
        return LeaveRequestResponse.from(saved, employeeName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findAll(Long employeeId, String role, String statusFilter) {
        List<LeaveRequest> requests;
        if ("ADMIN".equals(role)) {
            requests = statusFilter != null
                    ? leaveRequestRepository.findByStatus(ApprovalStatus.valueOf(statusFilter))
                    : leaveRequestRepository.findAll();
        } else {
            requests = statusFilter != null
                    ? leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, ApprovalStatus.valueOf(statusFilter))
                    : leaveRequestRepository.findByEmployeeId(employeeId);
        }
        return requests.stream()
                .map(r -> LeaveRequestResponse.from(r, getEmployeeName(r.getEmployeeId())))
                .toList();
    }

    @Override
    public LeaveRequestResponse approve(Long id, Long approverId) {
        var entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("休暇申請が見つかりません: " + id));
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        entity.setStatus(ApprovalStatus.APPROVED);
        entity.setApproverId(approverId);
        var saved = leaveRequestRepository.save(entity);
        return LeaveRequestResponse.from(saved, getEmployeeName(saved.getEmployeeId()));
    }

    @Override
    public LeaveRequestResponse reject(Long id, Long approverId) {
        var entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("休暇申請が見つかりません: " + id));
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        entity.setStatus(ApprovalStatus.REJECTED);
        entity.setApproverId(approverId);
        var saved = leaveRequestRepository.save(entity);
        return LeaveRequestResponse.from(saved, getEmployeeName(saved.getEmployeeId()));
    }

    private String getEmployeeName(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(e -> e.getName())
                .orElse("不明");
    }
}
