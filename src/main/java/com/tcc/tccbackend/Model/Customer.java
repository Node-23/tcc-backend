package com.tcc.tccbackend.Model;

import com.tcc.tccbackend.DTO.CustomerDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity(name = "customers")
@Table(name = "customers")
public class Customer implements Serializable {
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

    public Customer() {
    }

    public Customer(CustomerDTO customerDTO) {
        this.name = customerDTO.name();
        this.email = customerDTO.email();
        this.phone = customerDTO.phone();
        this.address = customerDTO.address();
    }
}