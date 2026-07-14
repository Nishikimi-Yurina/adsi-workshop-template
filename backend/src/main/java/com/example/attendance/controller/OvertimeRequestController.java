package com.example.attendance.controller;

import com.example.attendance.dto.OvertimeRequestCreateRequest;
import com.example.attendance.dto.OvertimeRequestResponse;
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

    public OvertimeRequestController(OvertimeRequestService overtimeRequestService) {
        this.overtimeRequestService = overtimeRequestService;
    }

    @GetMapping
    public ResponseEntity<List<OvertimeRequestResponse>> findAll(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        var results = status != null
                ? overtimeRequestService.findAllByStatus(status, authentication.getName())
                : overtimeRequestService.findAll(authentication.getName());
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<OvertimeRequestResponse> create(
            @Valid @RequestBody OvertimeRequestCreateRequest request,
            Authentication authentication) {
        var response = overtimeRequestService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<OvertimeRequestResponse> approve(
            @PathVariable Long id,
            Authentication authentication) {
        var response = overtimeRequestService.approve(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<OvertimeRequestResponse> reject(
            @PathVariable Long id,
            Authentication authentication) {
        var response = overtimeRequestService.reject(id, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
