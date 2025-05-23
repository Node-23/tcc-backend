package com.tcc.tccbackend.Repository;

import com.tcc.tccbackend.Model.Product;
import com.tcc.tccbackend.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    List<Product> findAllByOwnerId(Long ownerId);
}
