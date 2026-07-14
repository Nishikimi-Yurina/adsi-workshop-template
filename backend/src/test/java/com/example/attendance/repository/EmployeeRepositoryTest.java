package com.example.attendance.repository;

import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("社員コードで検索すると該当する社員が返される")
    void findByEmployeeCode_existingCode_returnsEmployee() {
        Optional<Employee> result = employeeRepository.findByEmployeeCode("admin");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("管理者");
        assertThat(result.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("存在しない社員コードで検索すると空が返される")
    void findByEmployeeCode_nonExistingCode_returnsEmpty() {
        Optional<Employee> result = employeeRepository.findByEmployeeCode("unknown");

        assertThat(result).isEmpty();
    }
}
