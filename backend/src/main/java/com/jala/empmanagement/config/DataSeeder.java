package com.jala.empmanagement.config;

import com.jala.empmanagement.entity.Employee;
import com.jala.empmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data seeder that runs at application startup.
 * Creates a default admin account if no employees exist.
 *
 * Default credentials:
 *   Email:    admin@jala.com
 *   Password: admin123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            log.info("No employees found. Seeding default admin and user accounts...");

            // Admin account
            Employee admin = Employee.builder()
                .name("Super Admin")
                .email("admin@jala.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Employee.Role.ADMIN)
                .status(Employee.Status.ACTIVE)
                .department("Management")
                .designation("System Administrator")
                .phone("9999999999")
                .build();

            // Sample user account
            Employee user = Employee.builder()
                .name("John Doe")
                .email("john@jala.com")
                .password(passwordEncoder.encode("user123"))
                .role(Employee.Role.USER)
                .status(Employee.Status.ACTIVE)
                .department("Engineering")
                .designation("Software Developer")
                .phone("8888888888")
                .build();

            // Second sample user
            Employee user2 = Employee.builder()
                .name("Jane Smith")
                .email("jane@jala.com")
                .password(passwordEncoder.encode("user123"))
                .role(Employee.Role.USER)
                .status(Employee.Status.INACTIVE)
                .department("HR")
                .designation("HR Manager")
                .phone("7777777777")
                .build();

            employeeRepository.save(admin);
            employeeRepository.save(user);
            employeeRepository.save(user2);

            log.info("Seeded: admin@jala.com (admin123), john@jala.com (user123), jane@jala.com (user123)");
        }
    }
}
