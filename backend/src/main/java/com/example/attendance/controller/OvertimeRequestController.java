package com.example.attendance.controller;

import com.example.attendance.dto.OvertimeRequestCreateRequest;
import com.example.attendance.dto.OvertimeRequestResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.OvertimeRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/overtime-requests")
public class OvertimeRequestController {

    private final OvertimeRequestService overtimeRequestService;
    private final EmployeeRepository employeeRepository;

    public OvertimeRequestController(OvertimeRequestService overtimeRequestService,
                                     EmployeeRepository employeeRepository) {
        this.overtimeRequestService = overtimeRequestService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<List<OvertimeRequestResponse>> findAll(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        var employee = getEmployee(authentication);
        var results = overtimeRequestService.findAll(
                employee.getId(), employee.getRole().name(), status);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<OvertimeRequestResponse> create(
            @Valid @RequestBody OvertimeRequestCreateRequest request,
            Authentication authentication) {
        var employee = getEmployee(authentication);
        var response = overtimeRequestService.create(request, employee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<OvertimeRequestResponse> approve(
            @PathVariable Long id,
            Authentication authentication) {
        var employee = getEmployee(authentication);
        var response = overtimeRequestService.approve(id, employee.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<OvertimeRequestResponse> reject(
            @PathVariable Long id,
            Authentication authentication) {
        var employee = getEmployee(authentication);
        var response = overtimeRequestService.reject(id, employee.getId());
        return ResponseEntity.ok(response);
    }

    private Employee getEmployee(Authentication authentication) {
        return employeeRepository.findByEmployeeCode(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
    }
}
