package com.example.attendance.service;

import com.example.attendance.dto.OvertimeRequestCreateRequest;
import com.example.attendance.dto.OvertimeRequestResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.OvertimeRequest;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.OvertimeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public OvertimeRequestResponse create(OvertimeRequestCreateRequest request, String employeeCode) {
        var employee = findEmployeeByCode(employeeCode);
        var entity = OvertimeRequest.builder()
                .employeeId(employee.getId())
                .date(request.date())
                .expectedHours(request.expectedHours())
                .reason(request.reason())
                .build();
        var saved = overtimeRequestRepository.save(entity);
        return OvertimeRequestResponse.from(saved, employee.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OvertimeRequestResponse> findAll(String employeeCode) {
        var employee = findEmployeeByCode(employeeCode);
        List<OvertimeRequest> requests;
        if (employee.getRole() == Role.ADMIN) {
            requests = overtimeRequestRepository.findAll();
        } else {
            requests = overtimeRequestRepository.findByEmployeeId(employee.getId());
        }
        return toResponses(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OvertimeRequestResponse> findAllByStatus(String statusFilter, String employeeCode) {
        var employee = findEmployeeByCode(employeeCode);
        var status = ApprovalStatus.valueOf(statusFilter);
        List<OvertimeRequest> requests;
        if (employee.getRole() == Role.ADMIN) {
            requests = overtimeRequestRepository.findByStatus(status);
        } else {
            requests = overtimeRequestRepository.findByEmployeeIdAndStatus(employee.getId(), status);
        }
        return toResponses(requests);
    }

    @Override
    public OvertimeRequestResponse approve(Long id, String approverCode) {
        var approver = findEmployeeByCode(approverCode);
        var entity = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("残業申請が見つかりません: " + id));
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        if (entity.getEmployeeId().equals(approver.getId())) {
            throw new IllegalStateException("自分の申請を承認することはできません");
        }
        entity.setStatus(ApprovalStatus.APPROVED);
        entity.setApproverId(approver.getId());
        var saved = overtimeRequestRepository.save(entity);
        return OvertimeRequestResponse.from(saved, getEmployeeName(saved.getEmployeeId()));
    }

    @Override
    public OvertimeRequestResponse reject(Long id, String approverCode) {
        var approver = findEmployeeByCode(approverCode);
        var entity = overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("残業申請が見つかりません: " + id));
        if (entity.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        if (entity.getEmployeeId().equals(approver.getId())) {
            throw new IllegalStateException("自分の申請を却下することはできません");
        }
        entity.setStatus(ApprovalStatus.REJECTED);
        entity.setApproverId(approver.getId());
        var saved = overtimeRequestRepository.save(entity);
        return OvertimeRequestResponse.from(saved, getEmployeeName(saved.getEmployeeId()));
    }

    private List<OvertimeRequestResponse> toResponses(List<OvertimeRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        var employeeIds = requests.stream()
                .map(OvertimeRequest::getEmployeeId)
                .distinct()
                .toList();
        Map<Long, String> nameMap = employeeRepository.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));
        return requests.stream()
                .map(r -> OvertimeRequestResponse.from(r, nameMap.getOrDefault(r.getEmployeeId(), "不明")))
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
