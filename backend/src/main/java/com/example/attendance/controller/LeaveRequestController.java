package com.example.attendance.controller;

import com.example.attendance.dto.LeaveRequestCreateRequest;
import com.example.attendance.dto.LeaveRequestResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeRepository employeeRepository;

    public LeaveRequestController(LeaveRequestService leaveRequestService,
                                  EmployeeRepository employeeRepository) {
        this.leaveRequestService = leaveRequestService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestResponse>> findAll(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        var employee = getEmployee(authentication);
        var results = leaveRequestService.findAll(
                employee.getId(), employee.getRole().name(), status);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> create(
            @Valid @RequestBody LeaveRequestCreateRequest request,
            Authentication authentication) {
        var employee = getEmployee(authentication);
        var response = leaveRequestService.create(request, employee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestResponse> approve(
            @PathVariable Long id,
            Authentication authentication) {
        var employee = getEmployee(authentication);
        var response = leaveRequestService.approve(id, employee.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> reject(
            @PathVariable Long id,
            Authentication authentication) {
        var employee = getEmployee(authentication);
        var response = leaveRequestService.reject(id, employee.getId());
        return ResponseEntity.ok(response);
    }

    private Employee getEmployee(Authentication authentication) {
        return employeeRepository.findByEmployeeCode(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("ユーザーが見つかりません"));
    }
}
