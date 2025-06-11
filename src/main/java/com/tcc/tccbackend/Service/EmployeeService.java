package com.tcc.tccbackend.Service;

import com.tcc.tccbackend.DTO.EmployeeDTO;
import com.tcc.tccbackend.Exceptions.FieldAlreadyInUseException;
import com.tcc.tccbackend.Exceptions.InvalidDataException;
import com.tcc.tccbackend.Exceptions.InvalidEmailException;
import com.tcc.tccbackend.Model.Employee;
import com.tcc.tccbackend.Repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    public EmployeeService(EmployeeRepository employeeRepository, LogService logService) {
        this.employeeRepository = employeeRepository;
        this.logService = logService;
    }

    public Iterable<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee createEmployee(EmployeeDTO employeeDTO) {
        Employee newEmployee = new Employee(employeeDTO);
        SaveEmployee(newEmployee);
        return newEmployee;
    }

    public Employee updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Optional<Employee> existingEmployeeOptional = employeeRepository.findById(id);
        if (existingEmployeeOptional.isEmpty()) {
            logService.warn("Attempted to update non-existent employee with ID: " + id, "EmployeeService.updateEmployee", "updateEmployee", id.toString(), null, null);
            throw new InvalidDataException("Funcionário não encontrado com o ID: " + id);
        }

        Employee existingEmployee = existingEmployeeOptional.get();

        // Check if email or name are being changed to an already existing one by another employee
        if (!existingEmployee.getEmail().equals(employeeDTO.email()) && employeeRepository.findByEmail(employeeDTO.email()).isPresent()) {
            throw new FieldAlreadyInUseException("Email do funcionário");
        }
        if (!existingEmployee.getName().equals(employeeDTO.name()) && employeeRepository.findByName(employeeDTO.name()).isPresent()) {
            throw new FieldAlreadyInUseException("Nome do funcionário");
        }

        existingEmployee.setName(employeeDTO.name());
        existingEmployee.setEmail(employeeDTO.email());
        existingEmployee.setPhone(employeeDTO.phone());
        existingEmployee.setAddress(employeeDTO.address());
        existingEmployee.setRole(employeeDTO.role());
        existingEmployee.setSalary(employeeDTO.salary());
        existingEmployee.setHiredate(employeeDTO.hiredate());
        existingEmployee.setStatus(employeeDTO.status());

        ValidateEmployee(existingEmployee); // Revalidar o funcionário após a atualização dos dados
        try {
            employeeRepository.save(existingEmployee);
            logService.info("Employee updated successfully: " + existingEmployee.getName() + " (ID: " + existingEmployee.getId() + ")", "EmployeeService.updateEmployee", "updateEmployee", existingEmployee.getId().toString());
            return existingEmployee;
        } catch (DataIntegrityViolationException ex) {
            String constrainField = GeneralService.getConstrainField(ex);
            logService.error("Failed to update employee due to data integrity: " + constrainField, "EmployeeService.updateEmployee", "updateEmployee", existingEmployee.getId().toString(), "", ex.toString());
            throw new FieldAlreadyInUseException(constrainField);
        } catch (Exception e) {
            logService.error("Failed to update employee: " + e.getMessage(), "EmployeeService.updateEmployee", "updateEmployee", existingEmployee.getId().toString(), "", e.toString());
            throw new RuntimeException(e);
        }
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            logService.warn("Attempted to delete non-existent employee with ID: " + id, "EmployeeService.deleteEmployee", "deleteEmployee", id.toString(), null, null);
            throw new InvalidDataException("Funcionário não encontrado com o ID: " + id);
        }
        try {
            employeeRepository.deleteById(id);
            logService.info("Employee with ID: " + id + " deleted successfully.", "EmployeeService.deleteEmployee", "deleteEmployee", id.toString());
        } catch (Exception e) {
            logService.error("Failed to delete employee with ID: " + id + ". Error: " + e.getMessage(), "EmployeeService.deleteEmployee", "deleteEmployee", id.toString(), "", e.toString());
            throw new RuntimeException("Erro ao deletar o funcionário com ID: " + id, e);
        }
    }

    private void SaveEmployee(Employee employee) {
        ValidateEmployee(employee);
        try {
            this.employeeRepository.save(employee);
            logService.info("Employee saved successfully: " + employee.getName(), "EmployeeService.SaveEmployee", "SaveEmployee", employee.getId() != null ? employee.getId().toString() : "new");
        } catch (DataIntegrityViolationException ex) {
            String constrainField = GeneralService.getConstrainField(ex);
            logService.error("Failed to save employee due to data integrity: " + constrainField, "EmployeeService.SaveEmployee", "SaveEmployee", employee.getId() != null ? employee.getId().toString() : "new", "", ex.toString());
            throw new FieldAlreadyInUseException(constrainField);
        } catch (Exception e) {
            logService.error("Failed to save employee: " + e.getMessage(), "EmployeeService.SaveEmployee", "SaveEmployee", employee.getId() != null ? employee.getId().toString() : "new", "", e.toString());
            throw new RuntimeException(e);
        }
    }

    private void ValidateEmployee(Employee employee) {
        if (employee.getName() == null || employee.getName().trim().isEmpty() ||
                employee.getEmail() == null || employee.getEmail().trim().isEmpty() ||
                employee.getPhone() == null || employee.getPhone().trim().isEmpty() ||
                employee.getAddress() == null || employee.getAddress().trim().isEmpty() ||
                employee.getRole() == null || employee.getRole().trim().isEmpty() ||
                employee.getSalary() == null || employee.getSalary().compareTo(BigDecimal.ZERO) < 0 ||
                employee.getHiredate() == null ||
                employee.getStatus() == null || employee.getStatus().trim().isEmpty()) {
            throw new InvalidDataException("Todos os campos do funcionário (nome, email, telefone, endereço, cargo, salário, data de contratação, status) são obrigatórios e válidos.");
        }
        if (!isValidEmail(employee.getEmail())) {
            throw new InvalidEmailException("Insira um email válido para o funcionário.");
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    public Optional<Employee> findEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public Optional<Employee> findEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
}