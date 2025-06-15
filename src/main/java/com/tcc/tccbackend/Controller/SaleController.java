package com.tcc.tccbackend.Controller;

import com.tcc.tccbackend.DTO.OutputSaleDTO;
import com.tcc.tccbackend.DTO.SaleDTO;
import com.tcc.tccbackend.DTO.SalesOverviewDTO;
import com.tcc.tccbackend.Model.Sale;
import com.tcc.tccbackend.Service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/sales/")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public ResponseEntity<Iterable<OutputSaleDTO>> getAllSales() {
        Iterable<OutputSaleDTO> sales = saleService.findAllSales();
        return new ResponseEntity<>(sales, HttpStatus.OK);
    }

    @GetMapping("/overview")
    public ResponseEntity<SalesOverviewDTO> getSalesOverview() {
        SalesOverviewDTO salesOverview = saleService.getSalesOverview();
        return new ResponseEntity<>(salesOverview, HttpStatus.OK);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sale createSale(@RequestBody @Valid SaleDTO saleDTO) {
        return saleService.createSale(saleDTO);
    }

    @PutMapping("{id}")
    public ResponseEntity<Sale> updateSale(@PathVariable Long id, @RequestBody @Valid SaleDTO saleDTO) {
        Sale updatedSale = saleService.updateSale(id, saleDTO);
        return new ResponseEntity<>(updatedSale, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public Optional<OutputSaleDTO> getSaleById(@PathVariable Long id) {
        return saleService.findSaleById(id);
    }
}