package com.tcc.tccbackend.Model;

import com.tcc.tccbackend.DTO.ProductDTO;
import com.tcc.tccbackend.Repository.UserRepository;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity(name = "products")
@Table(name = "products")
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(unique = true)
    private String name;
    @NotBlank
    private String code;
    @NotBlank
    private String category;
    @NotBlank
    private BigDecimal price;
    @NotBlank
    private BigDecimal profit;
    @NotBlank
    private int quantity;
    private String photo;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;


    public Product(String name, String code, String category, BigDecimal price, BigDecimal profit, int quantity, String photo, User owner) {
        this.name = name;
        this.code = code;
        this.category = category;
        this.price = price;
        this.profit = profit;
        this.quantity = quantity;
        this.photo = photo;
        this.owner = owner;
    }

    public Product() {

    }
}
