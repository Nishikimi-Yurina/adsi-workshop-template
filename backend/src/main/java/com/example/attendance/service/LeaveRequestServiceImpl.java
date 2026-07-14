package com.example.attendance.service;

import com.example.attendance.dto.LeaveRequestCreateRequest;
import com.example.attendance.dto.LeaveRequestResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.LeaveRequest;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public LeaveRequestResponse create(LeaveRequestCreateRequest request, String employeeCode) {
        var employee = findEmployeeByCode(employeeCode);
        var entity = LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(request.leaveType())
                .date(request.date())
                .reason(request.reason())
                .build();
        var saved = leaveRequestRepository.save(entity);
        return LeaveRequestResponse.from(saved, employee.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findAll(String employeeCode) {
        var employee = findEmployeeByCode(employeeCode);
        List<LeaveRequest> requests;
        if (employee.getRole() == Role.ADMIN) {
            requests = leaveRequestRepository.findAll();
        } else {
            requests = leaveRequestRepository.findByEmployeeId(employee.getId());
        }
        return toResponses(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findAllByStatus(String statusFilter, String employeeCode) {
        var employee = findEmployeeByCode(employeeCode);
        var status = ApprovalStatus.valueOf(statusFilter);
        List<LeaveRequest> requests;
        if (employee.getRole() == Role.ADMIN) {
            requests = leaveRequestRepository.findByStatus(status);
        } else {
            requests = leaveRequestRepository.findByEmployeeIdAndStatus(employee.getId(), status);
        }
        return toResponses(requests);
    }

    @Override
    public LeaveRequestResponse approve(Long id, String approverCode) {
        var approver = findEmployeeByCode(approverCode);
        var entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("休暇申請が見つかりません: " + id));
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        if (entity.getEmployeeId().equals(approver.getId())) {
            throw new IllegalStateException("自分の申請を承認することはできません");
        }
        entity.setStatus(ApprovalStatus.APPROVED);
        entity.setApproverId(approver.getId());
        var saved = leaveRequestRepository.save(entity);
        return LeaveRequestResponse.from(saved, getEmployeeName(saved.getEmployeeId()));
    }

    @Override
    public LeaveRequestResponse reject(Long id, String approverCode) {
        var approver = findEmployeeByCode(approverCode);
        var entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("休暇申請が見つかりません: " + id));
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        if (entity.getEmployeeId().equals(approver.getId())) {
            throw new IllegalStateException("自分の申請を却下することはできません");
        }
        entity.setStatus(ApprovalStatus.REJECTED);
        entity.setApproverId(approver.getId());
        var saved = leaveRequestRepository.save(entity);
        return LeaveRequestResponse.from(saved, getEmployeeName(saved.getEmployeeId()));
    }

    private List<LeaveRequestResponse> toResponses(List<LeaveRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        var employeeIds = requests.stream()
                .map(LeaveRequest::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, String> nameMap = employeeRepository.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));
        return requests.stream()
                .map(r -> LeaveRequestResponse.from(r, nameMap.getOrDefault(r.getEmployeeId(), "不明")))
                .toList();
    }

    private Employee findEmployeeByCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
    }

    private String getEmployeeName(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(Employee::getName)
                .orElse("不明");
    }
}
