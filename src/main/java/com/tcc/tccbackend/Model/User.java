package com.tcc.tccbackend.Model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tcc.tccbackend.DTO.UserDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@Entity(name = "users")
@Table(name = "users")
public class User implements Serializable {
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
    @JsonIgnore
    private String password;
    @NonNull
    private String jwt;
    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Product> products;

    public User(UserDTO userDTO) {
        this.name = userDTO.name();
        this.email = userDTO.email();
        this.password = userDTO.password();
    }

    public User() {

    }
}
