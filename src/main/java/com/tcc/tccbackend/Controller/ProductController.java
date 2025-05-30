package com.tcc.tccbackend.Controller;

import com.tcc.tccbackend.DTO.ProductDTO;
import com.tcc.tccbackend.Model.Product;
import com.tcc.tccbackend.Service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Iterable<Product>> getAllProducts() {
        Iterable<Product> products = productService.findAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Iterable<Product>> getProductsByUser(@PathVariable Long userId) {
        List<Product> products = productService.findProductByOwner(userId);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PostMapping(path = "/register",consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Product> createProduct(
            @RequestPart("product") ProductDTO productData,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        Product newProduct = productService.createProduct(productData, imageFile);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

//    @GetMapping("/{name}")
//    public Optional<Product> getProductByName(@PathVariable String name) {
//        return productService.findProductByName(name);
//    }
}