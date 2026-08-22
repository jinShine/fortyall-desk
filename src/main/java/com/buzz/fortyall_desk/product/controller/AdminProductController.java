package com.buzz.fortyall_desk.product.controller;

import com.buzz.fortyall_desk.product.service.ProductService;
import com.buzz.fortyall_desk.common.dto.ApiResponse;
import com.buzz.fortyall_desk.product.dto.ProductDto.*;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductView> create(@Valid @RequestBody CreateRequest req) {
        return ApiResponse.of(productService.create(req));
    }

    @GetMapping
    public ApiResponse<List<ProductView>> list() {
        return ApiResponse.of(productService.list());
    }
}
