package com.tcc.tccbackend.DTO;

import com.tcc.tccbackend.Model.Customer;
import com.tcc.tccbackend.Model.Employee;
import com.tcc.tccbackend.Model.Product; // Importe a classe Product

import java.io.Serializable;
import java.util.List;

public record SalesOverviewDTO(
        List<Customer> customers,
        List<Employee> employees,
        List<OutputSaleDTO> sales,
        List<Product> products
) implements Serializable {}