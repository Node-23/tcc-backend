package com.tcc.tccbackend.Service;

import com.tcc.tccbackend.DTO.ProductDTO;
import com.tcc.tccbackend.Exceptions.FieldAlreadyInUseException;
import com.tcc.tccbackend.Exceptions.InvalidDataException;
import com.tcc.tccbackend.Exceptions.InvalidEmailException;
import com.tcc.tccbackend.Exceptions.PasswordRulesException;
import com.tcc.tccbackend.Model.Product;
import com.tcc.tccbackend.Model.User;
import com.tcc.tccbackend.Repository.ProductRepository;
import com.tcc.tccbackend.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Iterable<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(ProductDTO productDTO, MultipartFile file){
        Product newProduct = convertDtoToProduct(productDTO);
        this.SaveProduct(newProduct);
        return newProduct;
    }

    private void SaveProduct(Product product){
        ValidateProduct(product);
        //TODO: send file to bucket
        try{
            logger.info("Product id: {}, User Product: {} saved - OS: {}", product.getId(), product.getName(), System.getProperty("os.name"));
            this.productRepository.save(product);
        } catch (Exception e) {
            String msg = "Product id: "+product.getId()+", Product name: "+product.getName()+" - Error: "+ e.getMessage() +" - OS: " + System.getProperty("os.name") + "\n Stacktrace: " + e;
            logger.error(msg);
//            emailService.sendEmail("User id: " + user.getId() + " , User name:" + user.getName() + " - Data integrity error", msg);
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