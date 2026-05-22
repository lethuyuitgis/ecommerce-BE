package com.shopcuathuy.service;

import com.shopcuathuy.dto.response.ProductPageResponseDTO;
import com.shopcuathuy.dto.response.ProductResponseDTO;

import com.shopcuathuy.dto.request.CreateProductRequestDTO;
import com.shopcuathuy.dto.request.UpdateProductRequestDTO;
import java.util.Map;

public interface ProductService {
    ProductResponseDTO getProductById(String id);
    ProductPageResponseDTO searchProducts(String keyword, int page, int size, 
                                          String categoryId, Double minPrice, Double maxPrice, 
                                          Double minRating, String sortBy, String direction);
    ProductPageResponseDTO getProductsByCategory(String categoryId, int page, int size, String sortBy, String direction);
    ProductPageResponseDTO getProductsByCategorySlug(String slug, int page, int size, 
                                                     Double minPrice, Double maxPrice, 
                                                     Double minRating, String subcategory,
                                                     String sortBy, String direction);
    ProductPageResponseDTO getFeaturedProducts(int page, int size);
    ProductPageResponseDTO getFlashSaleProducts(int page, int size);
    ProductPageResponseDTO getAllProducts(int page, int size, String sortBy, String direction);
    ProductResponseDTO createProduct(String sellerId, CreateProductRequestDTO request);
    ProductResponseDTO updateProduct(String id, UpdateProductRequestDTO request, String userId);
    void deleteProduct(String id, String userId);
    Map<String, Object> getProductStats(String productId, int days);
}
