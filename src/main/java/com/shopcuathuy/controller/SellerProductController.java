package com.shopcuathuy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.CreateProductRequestDTO;
import com.shopcuathuy.dto.request.SellerUpdateProductRequestDTO;
import com.shopcuathuy.dto.request.UpdateFeaturedRequestDTO;
import com.shopcuathuy.dto.request.UpdateFlashSaleRequestDTO;
import com.shopcuathuy.dto.response.ProductPageResponseDTO;
import com.shopcuathuy.dto.response.ProductResponseDTO;
import com.shopcuathuy.entity.Category;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductVariant;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.CategoryRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.service.ProductService;
import com.shopcuathuy.service.impl.ProductServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final ProductServiceImpl productServiceImpl;
    private final ObjectMapper objectMapper;

    @Autowired
    public SellerProductController(ProductRepository productRepository,
                                  SellerRepository sellerRepository,
                                  CategoryRepository categoryRepository,
                                  ProductService productService,
                                  ProductServiceImpl productServiceImpl,
                                  ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.categoryRepository = categoryRepository;
        this.productService = productService;
        this.productServiceImpl = productServiceImpl;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProductPageResponseDTO>> getSellerProducts(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String status) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage;

        if (q != null && !q.isEmpty()) {
            productPage = productRepository.findByNameContainingIgnoreCase(q, pageable);
        } else if (categoryId != null && !categoryId.isEmpty()) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            productPage = productRepository.findBySellerId(seller.getId(), pageable);
        }

        List<Product> filteredProducts = productPage.stream()
            .filter(p -> p.getSeller() != null && seller.getId().equals(p.getSeller().getId()))
            .collect(Collectors.toList());

        if (status != null && !status.isEmpty()) {
            try {
                Product.ProductStatus productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
                filteredProducts = filteredProducts.stream()
                    .filter(p -> p.getStatus() == productStatus)
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }

        List<ProductResponseDTO> productDTOs = filteredProducts.stream()
            .map(productServiceImpl::convertToDTO)
            .collect(Collectors.toList());

        ProductPageResponseDTO result = new ProductPageResponseDTO(
            productDTOs,
            filteredProducts.size(),
            (int) Math.ceil(filteredProducts.size() / (double) size),
            size,
            page
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check ownership - verify product belongs to seller
        if (product.getSeller() == null || !seller.getId().equals(product.getSeller().getId())) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied: Product does not belong to this seller"));
        }

        return ResponseEntity.ok(ApiResponse.success(productServiceImpl.convertToDTO(product)));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(
            @RequestBody CreateProductRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Category category = categoryRepository.findById(request.categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setSeller(seller);
        product.setCategory(category);
        product.setName(request.name);
        product.setDescription(request.description);
        product.setPrice(BigDecimal.valueOf(request.price));
        if (request.comparePrice != null) {
            product.setComparePrice(BigDecimal.valueOf(request.comparePrice));
        }
        product.setQuantity(request.quantity != null ? request.quantity : 0);
        product.setStatus(request.status != null ? 
            Product.ProductStatus.valueOf(request.status.toUpperCase()) : 
            Product.ProductStatus.ACTIVE);
        product.setSku(request.sku != null ? request.sku : UUID.randomUUID().toString());
        product.setIsFeatured(false);
        
        // Set optional fields with defaults
        product.setMinOrder(1); // Default minimum order quantity
        product.setRating(BigDecimal.ZERO);
        product.setTotalReviews(0);
        product.setTotalSold(0);
        product.setTotalViews(0);
        // cost is nullable, will be null if not provided

        // Handle images - create ProductImage entities
        if (request.images != null && !request.images.isEmpty()) {
            for (int i = 0; i < request.images.size(); i++) {
                com.shopcuathuy.entity.ProductImage productImage = new com.shopcuathuy.entity.ProductImage();
                productImage.setId(UUID.randomUUID().toString());
                productImage.setProduct(product);
                productImage.setImageUrl(request.images.get(i));
                productImage.setDisplayOrder(i);
                productImage.setIsPrimary(i == 0); // First image is primary
                productImage.setAltText(request.name); // Use product name as alt text
                product.getImages().add(productImage);
            }
        }
        
        // Handle videos - store as ProductImage with special handling or in description
        // Since there's no ProductVideo entity, videos can be stored in description or ignored
        // If needed, videos can be added to a separate field or stored as URLs in description
        // For now, videos are not stored separately as there's no entity for them
        
        // Note: shippingMethodId in request is not stored in Product entity
        // Shipping methods are typically associated with orders, not products
        // If needed, this can be stored in a separate product_shipping_methods table
        // or added as a field to Product entity in the future

        // Handle variants - create ProductVariant entities
        if (request.variants != null && !request.variants.isEmpty()) {
            for (Map<String, Object> variantMap : request.variants) {
                ProductVariant variant = new ProductVariant();
                variant.setId(UUID.randomUUID().toString());
                variant.setProduct(product);
                
                // Extract variant data from map
                if (variantMap.containsKey("variantName")) {
                    variant.setVariantName(String.valueOf(variantMap.get("variantName")));
                } else {
                    // Build variant name from attributes (e.g., "Red - Size M")
                    StringBuilder variantNameBuilder = new StringBuilder();
                    if (variantMap.containsKey("color")) {
                        variantNameBuilder.append(variantMap.get("color"));
                    }
                    if (variantMap.containsKey("size")) {
                        if (variantNameBuilder.length() > 0) variantNameBuilder.append(" - ");
                        variantNameBuilder.append("Size ").append(variantMap.get("size"));
                    }
                    variant.setVariantName(variantNameBuilder.length() > 0 
                        ? variantNameBuilder.toString() 
                        : "Default Variant");
                }
                
                if (variantMap.containsKey("variantSku")) {
                    variant.setVariantSku(String.valueOf(variantMap.get("variantSku")));
                } else {
                    variant.setVariantSku(product.getSku() + "-" + UUID.randomUUID().toString().substring(0, 8));
                }
                
                if (variantMap.containsKey("variantPrice")) {
                    Object priceObj = variantMap.get("variantPrice");
                    if (priceObj instanceof Number) {
                        variant.setVariantPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                    } else if (priceObj instanceof String) {
                        variant.setVariantPrice(new BigDecimal((String) priceObj));
                    }
                } else if (variantMap.containsKey("price")) {
                    Object priceObj = variantMap.get("price");
                    if (priceObj instanceof Number) {
                        variant.setVariantPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                    } else if (priceObj instanceof String) {
                        variant.setVariantPrice(new BigDecimal((String) priceObj));
                    }
                } else {
                    variant.setVariantPrice(product.getPrice());
                }
                
                if (variantMap.containsKey("variantQuantity")) {
                    Object qtyObj = variantMap.get("variantQuantity");
                    if (qtyObj instanceof Number) {
                        variant.setVariantQuantity(((Number) qtyObj).intValue());
                    } else if (qtyObj instanceof String) {
                        variant.setVariantQuantity(Integer.parseInt((String) qtyObj));
                    }
                } else if (variantMap.containsKey("quantity")) {
                    Object qtyObj = variantMap.get("quantity");
                    if (qtyObj instanceof Number) {
                        variant.setVariantQuantity(((Number) qtyObj).intValue());
                    } else if (qtyObj instanceof String) {
                        variant.setVariantQuantity(Integer.parseInt((String) qtyObj));
                    }
                } else {
                    variant.setVariantQuantity(0);
                }
                
                if (variantMap.containsKey("variantImage")) {
                    variant.setVariantImage(String.valueOf(variantMap.get("variantImage")));
                } else if (variantMap.containsKey("image")) {
                    variant.setVariantImage(String.valueOf(variantMap.get("image")));
                }
                
                // Store attributes as JSON string
                try {
                    // Remove system fields and keep only attribute fields
                    Map<String, Object> attributesMap = variantMap.entrySet().stream()
                        .filter(entry -> !entry.getKey().startsWith("variant") 
                            && !entry.getKey().equals("price") 
                            && !entry.getKey().equals("quantity")
                            && !entry.getKey().equals("image"))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    
                    if (!attributesMap.isEmpty()) {
                        variant.setAttributes(objectMapper.writeValueAsString(attributesMap));
                    }
                } catch (Exception e) {
                    // If JSON serialization fails, store empty string
                    variant.setAttributes("{}");
                }
                
                product.getVariants().add(variant);
            }
        }

        product = productRepository.save(product);
        return ResponseEntity.ok(ApiResponse.success(productServiceImpl.convertToDTO(product)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable String id,
            @RequestBody SellerUpdateProductRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (userId != null && product.getSeller() != null && 
            !userId.equals(product.getSeller().getUser().getId())) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        if (request.name != null) product.setName(request.name);
        if (request.description != null) product.setDescription(request.description);
        if (request.price != null) product.setPrice(BigDecimal.valueOf(request.price));
        if (request.comparePrice != null) product.setComparePrice(BigDecimal.valueOf(request.comparePrice));
        if (request.quantity != null) product.setQuantity(request.quantity);
        if (request.status != null) {
            try {
                product.setStatus(Product.ProductStatus.valueOf(request.status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        if (request.categoryId != null) {
            Category category = categoryRepository.findById(request.categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
        if (request.sku != null) product.setSku(request.sku);
        
        // Handle images update
        if (request.images != null) {
            product.getImages().clear();
            for (int i = 0; i < request.images.size(); i++) {
                com.shopcuathuy.entity.ProductImage productImage = new com.shopcuathuy.entity.ProductImage();
                productImage.setId(UUID.randomUUID().toString());
                productImage.setProduct(product);
                productImage.setImageUrl(request.images.get(i));
                productImage.setDisplayOrder(i);
                productImage.setIsPrimary(i == 0); // First image is primary
                productImage.setAltText(product.getName()); // Use product name as alt text
                product.getImages().add(productImage);
            }
        }

        // Handle variants update
        if (request.variants != null) {
            product.getVariants().clear();
            for (Map<String, Object> variantMap : request.variants) {
                ProductVariant variant = new ProductVariant();
                variant.setId(UUID.randomUUID().toString());
                variant.setProduct(product);
                
                // Extract variant data from map
                if (variantMap.containsKey("variantName")) {
                    variant.setVariantName(String.valueOf(variantMap.get("variantName")));
                } else {
                    // Build variant name from attributes (e.g., "Red - Size M")
                    StringBuilder variantNameBuilder = new StringBuilder();
                    if (variantMap.containsKey("color")) {
                        variantNameBuilder.append(variantMap.get("color"));
                    }
                    if (variantMap.containsKey("size")) {
                        if (variantNameBuilder.length() > 0) variantNameBuilder.append(" - ");
                        variantNameBuilder.append("Size ").append(variantMap.get("size"));
                    }
                    variant.setVariantName(variantNameBuilder.length() > 0 
                        ? variantNameBuilder.toString() 
                        : "Default Variant");
                }
                
                if (variantMap.containsKey("variantSku")) {
                    variant.setVariantSku(String.valueOf(variantMap.get("variantSku")));
                } else {
                    variant.setVariantSku(product.getSku() + "-" + UUID.randomUUID().toString().substring(0, 8));
                }
                
                if (variantMap.containsKey("variantPrice")) {
                    Object priceObj = variantMap.get("variantPrice");
                    if (priceObj instanceof Number) {
                        variant.setVariantPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                    } else if (priceObj instanceof String) {
                        variant.setVariantPrice(new BigDecimal((String) priceObj));
                    }
                } else if (variantMap.containsKey("price")) {
                    Object priceObj = variantMap.get("price");
                    if (priceObj instanceof Number) {
                        variant.setVariantPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                    } else if (priceObj instanceof String) {
                        variant.setVariantPrice(new BigDecimal((String) priceObj));
                    }
                } else {
                    variant.setVariantPrice(product.getPrice());
                }
                
                if (variantMap.containsKey("variantQuantity")) {
                    Object qtyObj = variantMap.get("variantQuantity");
                    if (qtyObj instanceof Number) {
                        variant.setVariantQuantity(((Number) qtyObj).intValue());
                    } else if (qtyObj instanceof String) {
                        variant.setVariantQuantity(Integer.parseInt((String) qtyObj));
                    }
                } else if (variantMap.containsKey("quantity")) {
                    Object qtyObj = variantMap.get("quantity");
                    if (qtyObj instanceof Number) {
                        variant.setVariantQuantity(((Number) qtyObj).intValue());
                    } else if (qtyObj instanceof String) {
                        variant.setVariantQuantity(Integer.parseInt((String) qtyObj));
                    }
                } else {
                    variant.setVariantQuantity(0);
                }
                
                if (variantMap.containsKey("variantImage")) {
                    variant.setVariantImage(String.valueOf(variantMap.get("variantImage")));
                } else if (variantMap.containsKey("image")) {
                    variant.setVariantImage(String.valueOf(variantMap.get("image")));
                }
                
                // Store attributes as JSON string
                try {
                    // Remove system fields and keep only attribute fields
                    Map<String, Object> attributesMap = variantMap.entrySet().stream()
                        .filter(entry -> !entry.getKey().startsWith("variant") 
                            && !entry.getKey().equals("price") 
                            && !entry.getKey().equals("quantity")
                            && !entry.getKey().equals("image"))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    
                    if (!attributesMap.isEmpty()) {
                        variant.setAttributes(objectMapper.writeValueAsString(attributesMap));
                    }
                } catch (Exception e) {
                    // If JSON serialization fails, store empty string
                    variant.setAttributes("{}");
                }
                
                product.getVariants().add(variant);
            }
        }

        product = productRepository.save(product);
        return ResponseEntity.ok(ApiResponse.success(productServiceImpl.convertToDTO(product)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (userId != null && product.getSeller() != null && 
            !userId.equals(product.getSeller().getUser().getId())) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        productRepository.delete(product);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/featured")
    @Transactional
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateFeatured(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody UpdateFeaturedRequestDTO request) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (!seller.getId().equals(product.getSeller().getId())) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        boolean enabled = request.featured != null ? request.featured : true;
        product.setIsFeatured(enabled);

        product = productRepository.save(product);
        return ResponseEntity.ok(ApiResponse.success(productServiceImpl.convertToDTO(product)));
    }

    @PostMapping("/{id}/flash-sale")
    @Transactional
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateFlashSale(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody UpdateFlashSaleRequestDTO request) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (!seller.getId().equals(product.getSeller().getId())) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        boolean enabled = request.enabled != null ? request.enabled : true;
        
        // Note: Product entity doesn't have flash sale fields yet
        // For now, we'll use comparePrice to store original price when enabling flash sale
        // and set price to flashPrice. This is a temporary solution.
        // In production, you should add flash sale fields to Product entity or create a separate FlashSale entity.
        
        if (enabled) {
            if (request.flashPrice == null || request.flashPrice <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("flashPrice is required when enabling flash sale"));
            }
            
            // Store original price in comparePrice if not already set
            if (product.getComparePrice() == null) {
                product.setComparePrice(product.getPrice());
            }
            
            // Set flash sale price
            product.setPrice(BigDecimal.valueOf(request.flashPrice));
        } else {
            // Restore original price from comparePrice if available
            if (product.getComparePrice() != null && product.getComparePrice().compareTo(product.getPrice()) > 0) {
                product.setPrice(product.getComparePrice());
            }
        }

        product = productRepository.save(product);
        return ResponseEntity.ok(ApiResponse.success(productServiceImpl.convertToDTO(product)));
    }
}

