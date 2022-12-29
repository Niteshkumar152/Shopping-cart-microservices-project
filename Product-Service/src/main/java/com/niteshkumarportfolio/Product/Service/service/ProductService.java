package com.niteshkumarportfolio.Product.Service.service;

import com.niteshkumarportfolio.Product.Service.model.ProductRequest;
import com.niteshkumarportfolio.Product.Service.model.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
