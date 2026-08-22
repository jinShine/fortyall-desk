package com.buzz.fortyall_desk.product.service;

import com.buzz.fortyall_desk.product.entity.Product;
import com.buzz.fortyall_desk.product.repository.ProductRepository;
import com.buzz.fortyall_desk.auth.support.TenantContext;
import com.buzz.fortyall_desk.center.repository.CenterRepository;
import com.buzz.fortyall_desk.common.exception.ApiException;
import com.buzz.fortyall_desk.common.exception.ErrorCode;
import com.buzz.fortyall_desk.product.dto.ProductDto.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CenterRepository centerRepository;

    @Transactional
    public ProductView create(CreateRequest req) {
        var center = centerRepository.findById(TenantContext.centerId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        Product product = productRepository.save(new Product(center, req.name(),
                req.durationMinutes(), req.capacity(), req.sessionCount(),
                req.validDays(), req.price(), req.description()));
        return view(product);
    }

    @Transactional(readOnly = true)
    public List<ProductView> list() {
        return productRepository.findAllByCenterIdAndActiveTrue(TenantContext.centerId())
                .stream().map(this::view).toList();
    }

    private ProductView view(Product p) {
        return new ProductView(p.getId(), p.getName(), p.getDurationMinutes(), p.getCapacity(),
                p.getSessionCount(), p.getValidDays(), p.getPrice(), p.getDescription());
    }
}
