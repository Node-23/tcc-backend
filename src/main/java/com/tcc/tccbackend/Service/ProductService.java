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

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = generateFileName(newProduct.getName(), imageFile.getOriginalFilename());
            String fileUrl = null;
            try {
                fileUrl = uploadFileToS3(imageFile, fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            newProduct.setPhoto(fileUrl);
            SaveProduct(newProduct);
        }
        return newProduct;
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

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata); // LINHA ALTERADA

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
            metadataError.put("productName", product.getName()); // Usar getProductName() se existir
            metadataError.put("os", System.getProperty("os.name"));
            metadataError.put("errorMessage", e.getMessage());
            metadataError.put("stackTrace", e.toString()); // Captura a stack trace completa da exceção
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