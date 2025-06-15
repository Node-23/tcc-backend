package com.tcc.tccbackend.Model;

import com.tcc.tccbackend.DTO.EmployeeDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity(name = "employees")
@Table(name = "employees")
public class Employee implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String address;

    @NotBlank
    private String role;

    @NotNull
    @PositiveOrZero
    private BigDecimal salary;

    @NotNull
    private LocalDate hiredate;

    @NotBlank
    private String status;

    public Employee() {
    }

    public Employee(EmployeeDTO employeeDTO) {
        this.name = employeeDTO.name();
        this.email = employeeDTO.email();
        this.phone = employeeDTO.phone();
        this.address = employeeDTO.address();
        this.role = employeeDTO.role();
        this.salary = employeeDTO.salary();
        this.hiredate = employeeDTO.hiredate();
        this.status = employeeDTO.status();
    }
}