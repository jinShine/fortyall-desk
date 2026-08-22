package com.buzz.fortyall_desk.product.repository;

import com.buzz.fortyall_desk.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByCenterIdAndActiveTrue(Long centerId);
    Optional<Product> findByIdAndCenterId(Long id, Long centerId);
}
