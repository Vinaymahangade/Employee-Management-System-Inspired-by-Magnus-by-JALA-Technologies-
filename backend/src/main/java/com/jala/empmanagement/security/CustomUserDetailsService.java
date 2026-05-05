package com.jala.empmanagement.security;

import com.jala.empmanagement.entity.Employee;
import com.jala.empmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom UserDetailsService that loads employee details from PostgreSQL.
 * Spring Security uses this during authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Employee not found with email: " + email
            ));

        // Map role to Spring Security GrantedAuthority with ROLE_ prefix
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())
        );

        return User.builder()
            .username(employee.getEmail())
            .password(employee.getPassword())
            .authorities(authorities)
            .accountLocked(employee.getStatus() == Employee.Status.INACTIVE)
            .build();
    }
}
