// src/main/java/com/tcc/tccbackend/Service/CustomerService.java
package com.tcc.tccbackend.Service;

import com.tcc.tccbackend.DTO.CustomerDTO;
import com.tcc.tccbackend.Exceptions.FieldAlreadyInUseException;
import com.tcc.tccbackend.Exceptions.InvalidDataException;
import com.tcc.tccbackend.Exceptions.InvalidEmailException;
import com.tcc.tccbackend.Model.Customer;
import com.tcc.tccbackend.Repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    public CustomerService(CustomerRepository customerRepository, LogService logService) {
        this.customerRepository = customerRepository;
        this.logService = logService;
    }

    public Iterable<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer createCustomer(CustomerDTO customerDTO) {
        Customer newCustomer = new Customer(customerDTO);
        SaveCustomer(newCustomer);
        return newCustomer;
    }

    public Customer updateCustomer(Long id, CustomerDTO customerDTO) {
        Optional<Customer> existingCustomerOptional = customerRepository.findById(id);
        if (existingCustomerOptional.isEmpty()) {
            logService.warn("Attempted to update non-existent customer with ID: " + id, "CustomerService.updateCustomer", "updateCustomer", id.toString(), null, null);
            throw new InvalidDataException("Cliente não encontrado com o ID: " + id);
        }

        Customer existingCustomer = existingCustomerOptional.get();

        // Check if email or name are being changed to an already existing one by another customer
        if (!existingCustomer.getEmail().equals(customerDTO.email()) && customerRepository.findByEmail(customerDTO.email()).isPresent()) {
            throw new FieldAlreadyInUseException("Email do cliente");
        }
        if (!existingCustomer.getName().equals(customerDTO.name()) && customerRepository.findByName(customerDTO.name()).isPresent()) {
            throw new FieldAlreadyInUseException("Nome do cliente");
        }


        existingCustomer.setName(customerDTO.name());
        existingCustomer.setEmail(customerDTO.email());
        existingCustomer.setPhone(customerDTO.phone());
        existingCustomer.setAddress(customerDTO.address());

        ValidateCustomer(existingCustomer); // Revalidar o cliente após a atualização dos dados
        try {
            customerRepository.save(existingCustomer);
            logService.info("Customer updated successfully: " + existingCustomer.getName() + " (ID: " + existingCustomer.getId() + ")", "CustomerService.updateCustomer", "updateCustomer", existingCustomer.getId().toString());
            return existingCustomer;
        } catch (DataIntegrityViolationException ex) {
            String constrainField = GeneralService.getConstrainField(ex);
            logService.error("Failed to update customer due to data integrity: " + constrainField, "CustomerService.updateCustomer", "updateCustomer", existingCustomer.getId().toString(), "", ex.toString());
            throw new FieldAlreadyInUseException(constrainField);
        } catch (Exception e) {
            logService.error("Failed to update customer: " + e.getMessage(), "CustomerService.updateCustomer", "updateCustomer", existingCustomer.getId().toString(), "", e.toString());
            throw new RuntimeException(e);
        }
    }


    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            logService.warn("Attempted to delete non-existent customer with ID: " + id, "CustomerService.deleteCustomer", "deleteCustomer", id.toString(), null, null);
            throw new InvalidDataException("Cliente não encontrado com o ID: " + id);
        }
        try {
            customerRepository.deleteById(id);
            logService.info("Customer with ID: " + id + " deleted successfully.", "CustomerService.deleteCustomer", "deleteCustomer", id.toString());
        } catch (Exception e) {
            logService.error("Failed to delete customer with ID: " + id + ". Error: " + e.getMessage(), "CustomerService.deleteCustomer", "deleteCustomer", id.toString(), "", e.toString());
            throw new RuntimeException("Erro ao deletar o cliente com ID: " + id, e);
        }
    }

    private void SaveCustomer(Customer customer) {
        ValidateCustomer(customer);
        try {
            this.customerRepository.save(customer);
            logService.info("Customer saved successfully: " + customer.getName(), "CustomerService.SaveCustomer", "SaveCustomer", customer.getId() != null ? customer.getId().toString() : "new");
        } catch (DataIntegrityViolationException ex) {
            String constrainField = GeneralService.getConstrainField(ex);
            logService.error("Failed to save customer due to data integrity: " + constrainField, "CustomerService.SaveCustomer", "SaveCustomer", customer.getId() != null ? customer.getId().toString() : "new", "", ex.toString());
            throw new FieldAlreadyInUseException(constrainField);
        } catch (Exception e) {
            logService.error("Failed to save customer: " + e.getMessage(), "CustomerService.SaveCustomer", "SaveCustomer", customer.getId() != null ? customer.getId().toString() : "new", "", e.toString());
            throw new RuntimeException(e);
        }
    }

    private void ValidateCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().trim().isEmpty() ||
                customer.getEmail() == null || customer.getEmail().trim().isEmpty() ||
                customer.getPhone() == null || customer.getPhone().trim().isEmpty() ||
                customer.getAddress() == null || customer.getAddress().trim().isEmpty()) {
            throw new InvalidDataException("Todos os campos do cliente (nome, email, telefone, endereço) são obrigatórios.");
        }
        if (!isValidEmail(customer.getEmail())) {
            throw new InvalidEmailException("Insira um email válido para o cliente.");
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findCustomerById(Long id) {
        return customerRepository.findById(id);
    }
}