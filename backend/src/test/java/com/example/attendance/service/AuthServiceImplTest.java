package com.example.attendance.service;

import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private EmployeeRepository employeeRepository;
    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthServiceImpl(employeeRepository, passwordEncoder);
    }

    @Test
    @DisplayName("正しい認証情報でログインするとLoginResponseが返される")
    void login_validCredentials_returnsLoginResponse() {
        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("admin")
                .name("管理者")
                .email("admin@example.com")
                .password(encodedPassword)
                .role(Role.ADMIN)
                .build();

        when(employeeRepository.findByEmployeeCode("admin")).thenReturn(Optional.of(employee));

        LoginResponse result = authService.login(new LoginRequest("admin", rawPassword));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.employeeCode()).isEqualTo("admin");
        assertThat(result.name()).isEqualTo("管理者");
        assertThat(result.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("存在しない社員コードでログインすると例外が発生する")
    void login_nonExistingCode_throwsBadCredentials() {
        when(employeeRepository.findByEmployeeCode("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("パスワードが間違っているとBadCredentialsExceptionが発生する")
    void login_wrongPassword_throwsBadCredentials() {
        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("admin")
                .name("管理者")
                .password(passwordEncoder.encode("correctPassword"))
                .role(Role.ADMIN)
                .build();

        when(employeeRepository.findByEmployeeCode("admin")).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrongPassword")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("社員コードで現在のユーザー情報を取得できる")
    void getCurrentUser_existingCode_returnsEmployeeResponse() {
        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("admin")
                .name("管理者")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(employeeRepository.findByEmployeeCode("admin")).thenReturn(Optional.of(employee));

        EmployeeResponse result = authService.getCurrentUser("admin");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("admin@example.com");
    }
}
