package com.example.attendance.controller;

import com.example.attendance.dto.LeaveRequestCreateRequest;
import com.example.attendance.dto.LeaveRequestResponse;
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

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestResponse>> findAll(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        var results = status != null
                ? leaveRequestService.findAllByStatus(status, authentication.getName())
                : leaveRequestService.findAll(authentication.getName());
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> create(
            @Valid @RequestBody LeaveRequestCreateRequest request,
            Authentication authentication) {
        var response = leaveRequestService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestResponse> approve(
            @PathVariable Long id,
            Authentication authentication) {
        var response = leaveRequestService.approve(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponse> reject(
            @PathVariable Long id,
            Authentication authentication) {
        var response = leaveRequestService.reject(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
