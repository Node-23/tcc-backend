package com.tcc.tccbackend.Service;

import com.tcc.tccbackend.DTO.OutputSaleDTO;
import com.tcc.tccbackend.DTO.SaleDTO;
import com.tcc.tccbackend.DTO.SaleItemDTO;
import com.tcc.tccbackend.DTO.SalesOverviewDTO;
import com.tcc.tccbackend.Exceptions.InvalidDataException;
import com.tcc.tccbackend.Model.Customer;
import com.tcc.tccbackend.Model.Employee;
import com.tcc.tccbackend.Model.Product;
import com.tcc.tccbackend.Model.Sale;
import com.tcc.tccbackend.Model.SaleItem;
import com.tcc.tccbackend.Repository.CustomerRepository;
import com.tcc.tccbackend.Repository.EmployeeRepository;
import com.tcc.tccbackend.Repository.ProductRepository;
import com.tcc.tccbackend.Repository.SaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductRepository productRepository; // Já injetado
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(SaleService.class);

    public SaleService(SaleRepository saleRepository, CustomerRepository customerRepository,
                       EmployeeRepository employeeRepository, ProductRepository productRepository,
                       LogService logService) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.productRepository = productRepository;
        this.logService = logService;
    }

    public Iterable<OutputSaleDTO> findAllSales() {
        return saleRepository.findAll().stream()
                .map(this::convertToOutputSaleDTO)
                .collect(Collectors.toList());
    }

    public SalesOverviewDTO getSalesOverview() {
        List<Customer> customers = (List<Customer>) customerRepository.findAll();
        List<Employee> employees = (List<Employee>) employeeRepository.findAll();
        List<OutputSaleDTO> sales = (List<OutputSaleDTO>) findAllSales();
        List<Product> products = productRepository.findAll();
        return new SalesOverviewDTO(customers, employees, sales, products);
    }

    @Transactional
    public Sale createSale(SaleDTO saleDTO) {
        if (!customerRepository.existsById(saleDTO.clientId())) {
            logService.warn("Attempted to create sale with non-existent client ID: " + saleDTO.clientId(), "SaleService.createSale", "createSale", saleDTO.clientId().toString(), null, null);
            throw new InvalidDataException("Cliente com ID " + saleDTO.clientId() + " não encontrado.");
        }
        if (!employeeRepository.existsById(saleDTO.employeeId())) {
            logService.warn("Attempted to create sale with non-existent employee ID: " + saleDTO.employeeId(), "SaleService.createSale", "createSale", saleDTO.employeeId().toString(), null, null);
            throw new InvalidDataException("Funcionário com ID " + saleDTO.employeeId() + " não encontrado.");
        }

        Sale newSale = new Sale(saleDTO);

        if (saleDTO.items() == null || saleDTO.items().isEmpty()) {
            throw new InvalidDataException("A venda deve conter pelo menos um item.");
        }

        List<Product> productsToUpdate = new ArrayList<>();

        for (SaleItemDTO itemDTO : saleDTO.items()) {
            Optional<Product> productOptional = productRepository.findById(itemDTO.productId());
            if (productOptional.isEmpty()) {
                logService.warn("Attempted to create sale with non-existent product ID in items: " + itemDTO.productId(), "SaleService.createSale", "createSale", null, null, null);
                throw new InvalidDataException("Produto com ID " + itemDTO.productId() + " não encontrado nos itens da venda.");
            }
            Product product = productOptional.get();

            if (itemDTO.quantity() <= 0 || itemDTO.price().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidDataException("Quantidade, preço e desconto dos itens da venda devem ser valores positivos ou zero.");
            }
            if (itemDTO.quantity() > product.getQuantity()) {
                logService.warn("Insufficient stock for product ID: " + itemDTO.productId() + ". Requested: " + itemDTO.quantity() + ", Available: " + product.getQuantity(), "SaleService.createSale", "createSale", null, null, null);
                throw new InvalidDataException("Quantidade em estoque insuficiente para o produto: " + product.getName() + ". Disponível: " + product.getQuantity());
            }

            product.setQuantity(product.getQuantity() - itemDTO.quantity());
            productsToUpdate.add(product);

            newSale.addSaleItem(new SaleItem(itemDTO.productId(), itemDTO.productName(), itemDTO.quantity(), itemDTO.price()));
        }

        newSale.setTotal(newSale.getItems().stream().map(SaleItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        newSale.setStatus("FINALIZADA");
        ValidateSale(newSale);

        try {
            Sale savedSale = saleRepository.save(newSale);
            productRepository.saveAll(productsToUpdate);
            logService.info("Sale created successfully with ID: " + savedSale.getId(), "SaleService.createSale", "createSale", savedSale.getId().toString());
            return savedSale;
        } catch (Exception e) {
            logService.error("Failed to create sale: " + e.getMessage(), "SaleService.createSale", "createSale", null, null, e.toString());
            throw new RuntimeException("Erro ao criar a venda: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Sale updateSale(Long id, SaleDTO saleDTO) {
        Optional<Sale> existingSaleOptional = saleRepository.findById(id);
        if (existingSaleOptional.isEmpty()) {
            logService.warn("Attempted to update non-existent sale with ID: " + id, "SaleService.updateSale", "updateSale", id.toString(), null, null);
            throw new InvalidDataException("Venda não encontrada com o ID: " + id);
        }

        Sale existingSale = existingSaleOptional.get();

        if (!existingSale.getClientId().equals(saleDTO.clientId()) && !customerRepository.existsById(saleDTO.clientId())) {
            throw new InvalidDataException("Cliente com ID " + saleDTO.clientId() + " não encontrado.");
        }
        if (!existingSale.getEmployeeId().equals(saleDTO.employeeId()) && !employeeRepository.existsById(saleDTO.employeeId())) {
            throw new InvalidDataException("Funcionário com ID " + saleDTO.employeeId() + " não encontrado.");
        }

        List<Product> productsToRevert = new ArrayList<>();
        for (SaleItem oldItem : existingSale.getItems()) {
            Optional<Product> productOptional = productRepository.findById(oldItem.getProductId());
            productOptional.ifPresent(product -> {
                product.setQuantity(product.getQuantity() + oldItem.getQuantity());
                productsToRevert.add(product);
            });
        }
        productRepository.saveAll(productsToRevert);


        existingSale.setClientId(saleDTO.clientId());
        existingSale.setEmployeeId(saleDTO.employeeId());
        existingSale.setDate(saleDTO.date());
        existingSale.setPaymentMethod(saleDTO.paymentMethod());
        existingSale.setStatus(saleDTO.status());

        existingSale.getItems().clear();
        List<Product> productsToUpdate = new ArrayList<>();

        if (saleDTO.items() == null || saleDTO.items().isEmpty()) {
            throw new InvalidDataException("A venda deve conter pelo menos um item.");
        }
        for (SaleItemDTO itemDTO : saleDTO.items()) {
            Optional<Product> productOptional = productRepository.findById(itemDTO.productId());
            if (productOptional.isEmpty()) {
                logService.warn("Attempted to update sale with non-existent product ID in items: " + itemDTO.productId(), "SaleService.updateSale", "updateSale", existingSale.getId().toString(), null, null);
                throw new InvalidDataException("Produto com ID " + itemDTO.productId() + " não encontrado nos itens da venda.");
            }
            Product product = productOptional.get();

            if (itemDTO.quantity() <= 0 || itemDTO.price().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidDataException("Quantidade, preço e desconto dos itens da venda devem ser valores positivos ou zero.");
            }
            if (itemDTO.quantity() > product.getQuantity()) {
                logService.warn("Insufficient stock for product ID: " + itemDTO.productId() + " during update. Requested: " + itemDTO.quantity() + ", Available: " + product.getQuantity(), "SaleService.updateSale", "updateSale", existingSale.getId().toString(), null, null);
                throw new InvalidDataException("Quantidade em estoque insuficiente para o produto: " + product.getName() + " na atualização. Disponível: " + product.getQuantity());
            }

            product.setQuantity(product.getQuantity() - itemDTO.quantity());
            productsToUpdate.add(product);

            existingSale.addSaleItem(new SaleItem(itemDTO.productId(), itemDTO.productName(), itemDTO.quantity(), itemDTO.price()));
        }

        ValidateSale(existingSale);

        try {
            Sale updatedSale = saleRepository.save(existingSale);
            productRepository.saveAll(productsToUpdate);
            logService.info("Sale updated successfully: " + updatedSale.getId(), "SaleService.updateSale", "updateSale", updatedSale.getId().toString());
            return updatedSale;
        } catch (Exception e) {
            logService.error("Failed to update sale: " + e.getMessage(), "SaleService.updateSale", "updateSale", existingSale.getId().toString(), null, e.toString());
            throw new RuntimeException("Erro ao atualizar a venda: " + e.getMessage(), e);
        }
    }

    public void deleteSale(Long id) {
        Optional<Sale> existingSaleOptional = saleRepository.findById(id);
        if (existingSaleOptional.isEmpty()) {
            logService.warn("Attempted to delete non-existent sale with ID: " + id, "SaleService.deleteSale", "deleteSale", id.toString(), null, null);
            throw new InvalidDataException("Venda não encontrada com o ID: " + id);
        }

        Sale saleToDelete = existingSaleOptional.get();

        try {
            List<Product> productsToRevert = new ArrayList<>();
            for (SaleItem item : saleToDelete.getItems()) {
                Optional<Product> productOptional = productRepository.findById(item.getProductId());
                productOptional.ifPresent(product -> {
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productsToRevert.add(product);
                });
            }
            productRepository.saveAll(productsToRevert);

            saleRepository.deleteById(id);
            logService.info("Sale with ID: " + id + " deleted successfully and product quantities reverted.", "SaleService.deleteSale", "deleteSale", id.toString());
        } catch (Exception e) {
            logService.error("Failed to delete sale with ID: " + id + ". Error: " + e.getMessage(), "SaleService.deleteSale", "deleteSale", id.toString(), null, e.toString());
            throw new RuntimeException("Erro ao deletar a venda com ID: " + id, e);
        }
    }

    public Optional<OutputSaleDTO> findSaleById(Long id) {
        return saleRepository.findById(id).map(this::convertToOutputSaleDTO);
    }

    private void ValidateSale(Sale sale) {
        if (sale.getClientId() == null || sale.getEmployeeId() == null ||
                sale.getDate() == null ||
                sale.getPaymentMethod() == null || sale.getPaymentMethod().trim().isEmpty() ||
                sale.getStatus() == null || sale.getStatus().trim().isEmpty()) {
            throw new InvalidDataException("Todos os campos principais da venda (ID do cliente, ID do funcionário, data, método de pagamento, status) são obrigatórios.");
        }
        if (sale.getItems().isEmpty()) {
            throw new InvalidDataException("A venda deve conter pelo menos um item.");
        }
    }

    private OutputSaleDTO convertToOutputSaleDTO(Sale sale) {
        Customer client = customerRepository.findById(sale.getClientId())
                .orElseThrow(() -> new InvalidDataException("Cliente não encontrado para a venda com ID: " + sale.getId()));
        Employee employee = employeeRepository.findById(sale.getEmployeeId())
                .orElseThrow(() -> new InvalidDataException("Funcionário não encontrado para a venda com ID: " + sale.getId()));

        List<SaleItemDTO> itemDTOs = sale.getItems().stream()
                .map(item -> new SaleItemDTO(item.getProductId(), item.getProductName() ,item.getQuantity(), item.getPrice()))
                .collect(Collectors.toList());

        return new OutputSaleDTO(
                sale.getId(),
                client,
                employee,
                sale.getDate(),
                sale.getPaymentMethod(),
                sale.getStatus(),
                sale.getTotal(),
                itemDTOs
        );
    }
}