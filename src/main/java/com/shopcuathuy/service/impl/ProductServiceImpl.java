package com.shopcuathuy.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcuathuy.dto.request.UpdateProductRequestDTO;
import com.shopcuathuy.dto.response.ProductPageResponseDTO;
import com.shopcuathuy.dto.response.ProductResponseDTO;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductVariant;
import com.shopcuathuy.entity.ProductImage;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.exception.ForbiddenException;
import com.shopcuathuy.repository.*;
import com.shopcuathuy.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CategoryRepository categoryRepository; // Added
    private final SellerRepository sellerRepository; // Added
    private final ProductImageRepository productImageRepository; // Added
    private final ProductVariantRepository productVariantRepository; // Added

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              OrderItemRepository orderItemRepository,
                              CategoryRepository categoryRepository, // Added
                              SellerRepository sellerRepository, // Added
                              ProductImageRepository productImageRepository, // Added
                              ProductVariantRepository productVariantRepository) { // Added
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.categoryRepository = categoryRepository; // Added
        this.sellerRepository = sellerRepository; // Added
        this.productImageRepository = productImageRepository; // Added
        this.productVariantRepository = productVariantRepository; // Added
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = "products:search",
        key = "'k=' + #keyword + ':p=' + #page + ':s=' + #size + ':c=' + #categoryId + ':min=' + #minPrice + ':max=' + #maxPrice + ':r=' + #minRating + ':sb=' + #sortBy + ':d=' + #direction",
        unless = "#result == null || #result.content == null"
    )
    public ProductPageResponseDTO searchProducts(String keyword, int page, int size, 
                                                String categoryId, Double minPrice, Double maxPrice, 
                                                Double minRating, String sortBy, String direction) {
        Sort sort = Sort.by("ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        BigDecimal rating = minRating != null ? BigDecimal.valueOf(minRating) : null;
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        Page<Product> products = productRepository.searchProductsWithFilters(
            Product.ProductStatus.ACTIVE, searchKeyword, categoryId, min, max, rating, pageable
        );
        
        return convertToPageResponseDTO(products.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponseDTO getProductsByCategory(String categoryId, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by("ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        Page<ProductResponseDTO> productPage = products.map(this::convertToDTO);
        return convertToPageResponseDTO(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponseDTO getProductsByCategorySlug(String slug, int page, int size, 
                                                           Double minPrice, Double maxPrice, 
                                                           Double minRating, String subcategory,
                                                           String sortBy, String direction) {
        Sort sort = Sort.by("ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        BigDecimal rating = minRating != null ? BigDecimal.valueOf(minRating) : null;

        Page<Product> products = productRepository.findByCategorySlugWithFilters(
            slug, Product.ProductStatus.ACTIVE, min, max, rating, pageable
        );
        
        // Final subcategory text filter if needed (rarely used, most filtering is via slug)
        if (subcategory != null && !subcategory.isEmpty()) {
            List<ProductResponseDTO> filtered = products.getContent().stream()
                .map(this::convertToDTO)
                .filter(p -> p.categoryName != null && p.categoryName.toLowerCase().contains(subcategory.toLowerCase()))
                .collect(Collectors.toList());
            
            return new ProductPageResponseDTO(
                filtered,
                (int) products.getTotalElements(),
                products.getTotalPages(),
                size,
                page
            );
        }

        return convertToPageResponseDTO(products.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = "products:featured",
        key = "'page=' + #page + ':size=' + #size",
        unless = "#result == null || #result.content == null || #result.content.isEmpty()"
    )
    public ProductPageResponseDTO getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productRepository.findFeaturedProducts(Product.ProductStatus.ACTIVE, pageable);
        return convertToPageResponseDTO(products.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = "products:flash-sale",
        key = "'page=' + #page + ':size=' + #size",
        unless = "#result == null || #result.content == null || #result.content.isEmpty()"
    )
    public ProductPageResponseDTO getFlashSaleProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productRepository.findFlashSaleProducts(Product.ProductStatus.ACTIVE, pageable);
        return convertToPageResponseDTO(products.map(this::convertToDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponseDTO getAllProducts(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by("ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable);
        Page<ProductResponseDTO> productPage = products.map(this::convertToDTO);
        return convertToPageResponseDTO(productPage);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(String sellerId, com.shopcuathuy.dto.request.CreateProductRequestDTO request) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));
            
        com.shopcuathuy.entity.Category category = categoryRepository.findById(request.categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId));
            
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setSeller(seller);
        product.setCategory(category);
        product.setName(request.name);
        product.setDescription(request.description);
        product.setPrice(BigDecimal.valueOf(request.price));
        if (request.comparePrice != null) product.setComparePrice(BigDecimal.valueOf(request.comparePrice));
        product.setQuantity(request.quantity != null ? request.quantity : 0);
        product.setSku(request.sku != null ? request.sku : "PROD-" + UUID.randomUUID().toString().substring(0, 8));
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setIsFeatured(false);
        product.setRating(BigDecimal.valueOf(5.0));
        product.setTotalReviews(0);
        product.setTotalSold(0);
        product.setTotalViews(0);
        
        product = productRepository.save(product);
        
        // Handle images if provided
        if (request.images != null && !request.images.isEmpty()) {
            for (String imageUrl : request.images) {
                ProductImage img = new ProductImage();
                img.setId(UUID.randomUUID().toString());
                img.setProduct(product);
                img.setImageUrl(imageUrl);
                productImageRepository.save(img);
                product.getImages().add(img);
            }
        }
        
        return convertToDTO(product);
    }

    /**
     * Convert Page to ProductPageResponseDTO
     */
    private ProductPageResponseDTO convertToPageResponseDTO(Page<ProductResponseDTO> productPage) {
        return new ProductPageResponseDTO(
            productPage.getContent(),
            (int) productPage.getTotalElements(),
            productPage.getTotalPages(),
            productPage.getSize(),
            productPage.getNumber()
        );
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(String id, UpdateProductRequestDTO request, String userId) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
            
        // Security check: must be owner
        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new com.shopcuathuy.exception.ForbiddenException("User is not a seller"));
        
        if (!existing.getSeller().getId().equals(seller.getId())) {
            throw new com.shopcuathuy.exception.ForbiddenException("You do not have permission to update this product");
        }
        
        if (request.name != null) existing.setName(request.name);
        if (request.description != null) existing.setDescription(request.description);
        if (request.price != null) existing.setPrice(java.math.BigDecimal.valueOf(request.price));
        if (request.quantity != null) existing.setQuantity(request.quantity);
        if (request.status != null) {
            try {
                existing.setStatus(Product.ProductStatus.valueOf(request.status));
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        existing = productRepository.save(existing);
        return convertToDTO(existing);
    }

    @Override
    @Transactional
    public void deleteProduct(String id, String userId) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
            
        // Security check
        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new com.shopcuathuy.exception.ForbiddenException("User is not a seller"));
            
        if (!existing.getSeller().getId().equals(seller.getId())) {
            throw new com.shopcuathuy.exception.ForbiddenException("You do not have permission to delete this product");
        }
        
        productRepository.delete(existing);
    }

    // Make this method public so it can be used by other controllers
    public ProductResponseDTO convertToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.description = product.getDescription();
        dto.sku = product.getSku();
        dto.price = product.getPrice() != null ? product.getPrice().doubleValue() : null;
        dto.comparePrice = product.getComparePrice() != null ? product.getComparePrice().doubleValue() : null;
        dto.quantity = product.getQuantity();
        dto.status = product.getStatus() != null ? product.getStatus().name() : null;
        dto.rating = product.getRating() != null ? product.getRating().doubleValue() : null;
        dto.totalReviews = product.getTotalReviews();
        dto.totalSold = product.getTotalSold();
        dto.totalViews = product.getTotalViews();
        dto.isFeatured = product.getIsFeatured();
        
        // Set flash sale fields
        // Flash sale is enabled if comparePrice > price (discounted)
        if (product.getComparePrice() != null && product.getPrice() != null 
            && product.getComparePrice().compareTo(product.getPrice()) > 0) {
            dto.flashSaleEnabled = true;
            dto.flashSalePrice = product.getPrice().doubleValue();
            // Note: flashSaleStart, flashSaleEnd, flashSaleStock, flashSaleSold 
            // would need to be stored in Product entity or separate table
            // For now, set defaults
            dto.flashSaleStock = product.getQuantity();
            dto.flashSaleSold = product.getTotalSold();
        } else {
            dto.flashSaleEnabled = false;
            dto.flashSalePrice = null;
        }
        
        // Handle null category gracefully (in case of orphaned foreign keys)
        try {
            dto.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
            dto.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // Category was deleted but product still references it
            log.warn("Product {} has invalid category reference, setting to null", product.getId());
            dto.categoryId = null;
            dto.categoryName = null;
        }
        dto.sellerId = product.getSeller() != null ? product.getSeller().getId() : null;
        dto.sellerName = product.getSeller() != null ? product.getSeller().getShopName() : null;
        dto.images = product.getImages() != null ? 
            product.getImages().stream()
                .map(img -> convertImageUrlToProxy(img.getImageUrl()))
                .collect(Collectors.toList()) : 
            java.util.Collections.emptyList();
        dto.primaryImage = dto.images.isEmpty() ? null : dto.images.get(0);
        
        // Convert variants to Map format for frontend
        dto.variants = convertVariantsToMap(product.getVariants());
        
        dto.productVariantDtos = product.getVariants() != null ?
            product.getVariants().stream()
                .map(this::convertVariantToDTO)
                .collect(Collectors.toList()) : null;
        
        dto.createdAt = product.getCreatedAt() != null ? 
            product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        
        return dto;
    }

    /**
     * Convert MinIO URL to proxy URL for frontend
     * Example: http://localhost:9000/shopcuathuy/images/xxx.webp -> /api/upload/image/images/xxx.webp
     */
    private String convertImageUrlToProxy(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // If it's an absolute URL that is not from MinIO, return as-is
        if ((imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))
                && !imageUrl.contains("localhost:9000")
                && !imageUrl.contains("minio")) {
            return imageUrl;
        }
        
        // If already a proxy URL, return as is
        if (imageUrl.startsWith("/api/upload/image/")) {
            return imageUrl;
        }
        
        // If it's a MinIO URL, extract object path and convert to proxy URL
        if (imageUrl.contains("localhost:9000") || imageUrl.contains("minio")) {
            try {
                java.net.URL url = java.net.URI.create(imageUrl).toURL();
                String path = url.getPath();
                // Remove leading slash and bucket name
                // Example: /shopcuathuy/images/xxx.webp -> images/xxx.webp
                String[] parts = path.split("/");
                if (parts.length > 2) {
                    // Skip empty first part and bucket name, join the rest
                    StringBuilder objectPath = new StringBuilder();
                    for (int i = 2; i < parts.length; i++) {
                        if (objectPath.length() > 0) {
                            objectPath.append("/");
                        }
                        objectPath.append(parts[i]);
                    }
                    return "/api/upload/image/" + objectPath.toString();
                }
            } catch (Exception e) {
                // If URL parsing fails, try simple string replacement
                if (imageUrl.contains("/shopcuathuy/")) {
                    String objectPath = imageUrl.substring(imageUrl.indexOf("/shopcuathuy/") + "/shopcuathuy/".length());
                    return "/api/upload/image/" + objectPath;
                }
            }
        }
        
        // If it's already a relative path, ensure it has the proxy prefix
        if (imageUrl.startsWith("/") && !imageUrl.startsWith("/api/")) {
            // If it starts with / but not /api/, it might be a direct path
            // Check if it looks like an image path
            if (imageUrl.contains("images/") || imageUrl.contains("products/") || imageUrl.contains("videos/")) {
                // Remove leading slash and add proxy prefix
                return "/api/upload/image" + imageUrl;
            }
        }
        
        // Default: assume it's an object name and add proxy prefix
        return "/api/upload/image/" + imageUrl;
    }

    /**
     * Convert ProductVariant list to Map format for frontend
     * Extract unique sizes and colors from variants
     */
    private Map<String, Object> convertVariantsToMap(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }

        Set<String> sizes = new LinkedHashSet<>();
        Set<String> colors = new LinkedHashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (ProductVariant variant : variants) {
            // Parse attributes JSON to extract size and color
            if (variant.getAttributes() != null && !variant.getAttributes().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> attrs = objectMapper.readValue(variant.getAttributes(), Map.class);
                    if (attrs.containsKey("size")) {
                        String size = String.valueOf(attrs.get("size"));
                        if (size != null && !size.isEmpty() && !"null".equals(size)) {
                            sizes.add(size);
                        }
                    }
                    if (attrs.containsKey("color")) {
                        String color = String.valueOf(attrs.get("color"));
                        if (color != null && !color.isEmpty() && !"null".equals(color)) {
                            colors.add(color);
                        }
                    }
                } catch (Exception e) {
                    // If JSON parsing fails, try to extract from variant name
                    String variantName = variant.getVariantName();
                    if (variantName != null) {
                        // Try to parse "Color - Size" format
                        String[] parts = variantName.split("\\s*-\\s*");
                        for (String part : parts) {
                            part = part.trim();
                            // Common size patterns
                            if (part.matches("(?i)^(XS|S|M|L|XL|XXL|XXXL|\\d+)$")) {
                                sizes.add(part);
                            } else if (part.length() > 0) {
                                // Assume it's a color
                                colors.add(part);
                            }
                        }
                    }
                }
            } else if (variant.getVariantName() != null) {
                // Fallback: parse variant name
                String variantName = variant.getVariantName();
                String[] parts = variantName.split("\\s*-\\s*");
                for (String part : parts) {
                    part = part.trim();
                    if (part.matches("(?i)^(XS|S|M|L|XL|XXL|XXXL|\\d+)$")) {
                        sizes.add(part);
                    } else if (part.length() > 0) {
                        colors.add(part);
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (!sizes.isEmpty()) {
            result.put("sizes", new ArrayList<>(sizes));
        }
        if (!colors.isEmpty()) {
            result.put("colors", new ArrayList<>(colors));
        }

        return result.isEmpty() ? null : result;
    }

    private com.shopcuathuy.dto.response.ProductVariantResponseDTO convertVariantToDTO(com.shopcuathuy.entity.ProductVariant variant) {
        com.shopcuathuy.dto.response.ProductVariantResponseDTO dto = new com.shopcuathuy.dto.response.ProductVariantResponseDTO();
        dto.id = variant.getId();
        dto.variantName = variant.getVariantName();
        dto.variantSku = variant.getVariantSku();
        dto.variantPrice = variant.getVariantPrice() != null ? variant.getVariantPrice().doubleValue() : null;
        dto.variantQuantity = variant.getVariantQuantity();
        dto.variantImage = convertImageUrlToProxy(variant.getVariantImage());
        dto.attributes = variant.getAttributes();
        return dto;
    }

    @Override
    public Map<String, Object> getProductStats(String productId, int days) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        // Get sales data from order items
        List<Object[]> salesData = orderItemRepository.findSalesByProductIdAndDateRange(
            productId, startDate, endDate);

        // Get views data (if stored in product entity or separate table)
        // For now, use totalViews from product entity
        
        List<Map<String, Object>> salesDataList = new ArrayList<>();
        List<Map<String, Object>> viewsDataList = new ArrayList<>();

        // Group sales by date
        Map<String, Double> salesByDate = new HashMap<>();
        Map<String, Integer> quantityByDate = new HashMap<>();
        
        for (Object[] row : salesData) {
            java.sql.Date orderDate = (java.sql.Date) row[0];
            Double revenue = ((BigDecimal) row[1]).doubleValue();
            Integer quantity = ((Number) row[2]).intValue();
            
            String dateKey = orderDate.toLocalDate().toString();
            salesByDate.put(dateKey, salesByDate.getOrDefault(dateKey, 0.0) + revenue);
            quantityByDate.put(dateKey, quantityByDate.getOrDefault(dateKey, 0) + quantity);
        }

        // Convert to list format
        for (String date : salesByDate.keySet()) {
            Map<String, Object> salesPoint = new HashMap<>();
            salesPoint.put("date", date);
            salesPoint.put("sales", quantityByDate.getOrDefault(date, 0));
            salesPoint.put("revenue", salesByDate.get(date));
            salesDataList.add(salesPoint);
        }

        // Views data - simplified (would need view tracking table in production)
        Map<String, Object> viewsPoint = new HashMap<>();
        viewsPoint.put("date", endDate.toLocalDate().toString());
        viewsPoint.put("views", product.getTotalViews() != null ? product.getTotalViews() : 0);
        viewsDataList.add(viewsPoint);

        Map<String, Object> result = new HashMap<>();
        result.put("salesData", salesDataList);
        result.put("viewsData", viewsDataList);

        return result;
    }
}

