package com.tcc.tccbackend.Model;

import com.tcc.tccbackend.DTO.SaleDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "sales")
@Table(name = "sales")
public class Sale implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long clientId;

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime date;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String status;

    @NotBlank
    private BigDecimal total;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
    }

    public Sale(SaleDTO saleDTO) {
        this.clientId = saleDTO.clientId();
        this.employeeId = saleDTO.employeeId();
        this.date = saleDTO.date();
        this.paymentMethod = saleDTO.paymentMethod();
        this.status = saleDTO.status();

    }

    public void addSaleItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }

    public void removeSaleItem(SaleItem item) {
        items.remove(item);
        item.setSale(null);
    }
}