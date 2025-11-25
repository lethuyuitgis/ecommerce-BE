package com.shopcuathuy.service;

import com.shopcuathuy.dto.response.ProductPageResponseDTO;
import com.shopcuathuy.dto.response.ProductResponseDTO;

import java.util.Map;

public interface ProductService {
    ProductResponseDTO getProductById(String id);
    ProductPageResponseDTO searchProducts(String keyword, int page, int size);
    ProductPageResponseDTO getProductsByCategory(String categoryId, int page, int size, String sortBy, String direction);
    ProductPageResponseDTO getProductsByCategorySlug(String slug, int page, int size, 
                                                     Double minPrice, Double maxPrice, 
                                                     Double minRating, String subcategory);
    ProductPageResponseDTO getFeaturedProducts(int page, int size);
    ProductPageResponseDTO getFlashSaleProducts(int page, int size);
    ProductPageResponseDTO getAllProducts(int page, int size, String sortBy, String direction);
    ProductResponseDTO updateProduct(String id, com.shopcuathuy.dto.request.UpdateProductRequestDTO request);
    void deleteProduct(String id);
    Map<String, Object> getProductStats(String productId, int days);
}
