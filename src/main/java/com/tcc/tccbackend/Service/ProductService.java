package com.tcc.tccbackend.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tcc.tccbackend.DTO.ProductDTO;
import com.tcc.tccbackend.Exceptions.InvalidDataException;
import com.tcc.tccbackend.Model.Product;
import com.tcc.tccbackend.Model.User;
import com.tcc.tccbackend.Repository.ProductRepository;
import com.tcc.tccbackend.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LogService logService;
    private final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public ProductService(ProductRepository productRepository, UserRepository userRepository, LogService logService, AmazonS3 s3Client) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.logService = logService;
        this.s3Client = s3Client;
    }

    public Iterable<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(ProductDTO productDTO, MultipartFile imageFile){
        Product newProduct = convertDtoToProduct(productDTO);

        try {
            SaveProduct(newProduct);
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = generateFileName(newProduct.getName(), Objects.requireNonNull(imageFile.getOriginalFilename()));
                String fileUrl;
                fileUrl = uploadFileToS3(imageFile, fileName);
                newProduct.setPhoto(fileUrl);
            }
            this.productRepository.save(newProduct);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return newProduct;
    }

    public Product updateProduct(Long id, ProductDTO productDTO, MultipartFile imageFile) {
        Optional<Product> existingProductOptional = productRepository.findById(id);
        if (existingProductOptional.isEmpty()) {
            logService.warn("Attempted to update non-existent product with ID: " + id, "ProductService.updateProduct", "updateProduct", id.toString(), null, null);
            throw new InvalidDataException("Produto não encontrado com o ID: " + id);
        }

        Product existingProduct = existingProductOptional.get();

//        User owner = userRepository.findById(productDTO.ownerId())
//                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        existingProduct.setName(productDTO.name());
        existingProduct.setCode(productDTO.code());
        existingProduct.setCategory(productDTO.category());
        existingProduct.setPrice(productDTO.price());
        existingProduct.setProfit(productDTO.profit());
        existingProduct.setQuantity(productDTO.quantity());
//        existingProduct.setOwner(owner);

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = generateFileName(existingProduct.getName(), Objects.requireNonNull(imageFile.getOriginalFilename()));
            String fileUrl;
            try {
                fileUrl = uploadFileToS3(imageFile, fileName);
                existingProduct.setPhoto(fileUrl);
            } catch (IOException e) {
                logService.error("Failed to upload image for product update: " + e.getMessage(), "ProductService.updateProduct", "uploadFileToS3", existingProduct.getId().toString(), null, e.toString());
                throw new RuntimeException("Erro ao fazer upload da imagem: " + e.getMessage(), e);
            }
        }

        ValidateProduct(existingProduct);
        try {
            productRepository.save(existingProduct);
            logService.info("Product updated successfully: " + existingProduct.getName() + " (ID: " + existingProduct.getId() + ")", "ProductService.updateProduct", "updateProduct", existingProduct.getId().toString());
            return existingProduct;
        } catch (DataIntegrityViolationException ex) {
            String constrainField = GeneralService.getConstrainField(ex);
            logService.error("Failed to update product due to data integrity: " + constrainField, "ProductService.updateProduct", "updateProduct", existingProduct.getId().toString(), "", ex.toString());
            throw new InvalidDataException("Erro de integridade de dados ao atualizar produto: " + constrainField);
        } catch (Exception e) {
            logService.error("Failed to update product: " + e.getMessage(), "ProductService.updateProduct", "updateProduct", existingProduct.getId().toString(), "", e.toString());
            throw new RuntimeException("Erro ao atualizar o produto: " + e.getMessage(), e);
        }
    }


    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            logService.warn("Attempted to delete non-existent product with ID: " + id, "ProductService.deleteProduct", "deleteProduct", id.toString(), null, null);
            throw new InvalidDataException("Produto não encontrado com o ID: " + id);
        }
        try {
            productRepository.deleteById(id);
            logService.info("Product with ID: " + id + " deleted successfully.", "ProductService.deleteProduct", "deleteProduct", id.toString());
        } catch (Exception e) {
            logService.error("Failed to delete product with ID: " + id + ". Error: " + e.getMessage(), "ProductService.deleteProduct", "deleteProduct", id.toString(), null, e.toString());
            throw new RuntimeException("Erro ao deletar o produto com ID: " + id, e);
        }
    }

    private String generateFileName(String productName, String originalFileName) {
        String cleanedProductName = productName.replaceAll("[^a-zA-Z0-9.-]", "_");
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }
        return cleanedProductName + "_" + fileExtension;
    }

    private String uploadFileToS3(MultipartFile file, String fileName) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata);

        s3Client.putObject(putObjectRequest);

        return s3Client.getUrl(bucketName, fileName).toString();
    }

    private void SaveProduct(Product product){
        ValidateProduct(product);
        try{
            this.productRepository.save(product);

            Map<String, Object> metadataSuccess = new HashMap<>();
            metadataSuccess.put("productId", product.getId());
            metadataSuccess.put("productName", product.getName());
            metadataSuccess.put("os", System.getProperty("os.name"));
            logService.info("Product saved successfully: " + metadataSuccess, "ProductService.SaveProduct", "SaveProduct", product.getOwner().getId().toString());

        } catch (Exception e) {
            Map<String, Object> metadataError = new HashMap<>();
            metadataError.put("productId", product.getId());
            metadataError.put("productName", product.getName());
            metadataError.put("os", System.getProperty("os.name"));
            metadataError.put("errorMessage", e.getMessage());
            metadataError.put("stackTrace", e.toString());
            logService.error("Failed to save product: " + metadataError, "ProductService.SaveProduct", "SaveProduct", product.getOwner().getId().toString(), "", Arrays.toString(e.getStackTrace()));

            String msg = "Product id: "+product.getId()+", Product name: "+product.getName()+" - Error: "+ e.getMessage() +" - OS: " + System.getProperty("os.name") + "\n Stacktrace: " + e;
            logger.error(msg);
            // emailService.sendEmail("User id: " + user.getId() + " , User name:" + user.getName() + " - Data integrity error", msg);
            throw new RuntimeException(e);
        }
    }

    private void ValidateProduct(Product product){
       if(product.getName().trim().isEmpty() || product.getCategory().trim().isEmpty() || product.getOwner() == null || product.getPrice() == null || product.getProfit() == null || product.getQuantity() < 0){
            throw new InvalidDataException("Dados inválidos");
       }
    }

    public List<Product> findProductByOwner(Long ownerId) {
        return productRepository.findAllByOwnerId(ownerId);
    }

    public Optional<Product> findProductByName(String name) {
        return productRepository.findByName(name);
    }

    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product convertDtoToProduct(ProductDTO dto) {
        User owner = userRepository.findById(dto.ownerId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        Product product = new Product();
        product.setId(dto.id());
        product.setName(dto.name());
        product.setCode(dto.code());
        product.setCategory(dto.category());
        product.setPrice(dto.price());
        product.setProfit(dto.profit());
        product.setQuantity(dto.quantity());
        product.setOwner(owner);
        return product;
    }
}