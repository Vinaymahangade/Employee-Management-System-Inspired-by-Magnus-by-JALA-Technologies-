package com.jala.empmanagement;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the JALA Employee Management System.
 *
 * @author JALA Technologies
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@OpenAPIDefinition(
    info = @Info(
        title = "JALA Employee Management API",
        version = "1.0.0",
        description = "Production-level Employee Management System REST API",
        contact = @Contact(name = "JALA Technologies", email = "admin@jala.com")
    )
)
public class EmpManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmpManagementApplication.class, args);
    }
}
