package com.shopcuathuy.service.impl;

import com.shopcuathuy.dto.request.UpdateProductRequestDTO;
import com.shopcuathuy.dto.response.ProductPageResponseDTO;
import com.shopcuathuy.dto.response.ProductResponseDTO;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductVariant;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderItemRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponseDTO searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        Page<ProductResponseDTO> productPage = products.map(this::convertToDTO);
        return convertToPageResponseDTO(productPage);
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
                                                           Double minRating, String subcategory) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productRepository.findByCategorySlug(slug, Product.ProductStatus.ACTIVE, pageable);
        Page<ProductResponseDTO> productPage = products.map(this::convertToDTO);
        
        // Apply filtering logic
        List<ProductResponseDTO> filtered = productPage.getContent().stream()
            .filter(p -> {
                if (minPrice != null && (p.price == null || p.price < minPrice)) return false;
                if (maxPrice != null && (p.price == null || p.price > maxPrice)) return false;
                if (minRating != null && (p.rating == null || p.rating < minRating)) return false;
                if (subcategory != null && !subcategory.isEmpty()) {
                    return p.categoryName != null && p.categoryName.toLowerCase().contains(subcategory.toLowerCase());
                }
                return true;
            })
            .collect(Collectors.toList());

        return new ProductPageResponseDTO(
            filtered,
            filtered.size(),
            (int) Math.ceil((double) filtered.size() / size),
            size,
            page
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponseDTO getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = null;
        
        // Try multiple query methods to find featured products
        // Method 1: Try method name query
        try {
            products = productRepository.findByStatusAndIsFeatured(Product.ProductStatus.ACTIVE, Boolean.TRUE, pageable);
            log.info("Method name query returned {} featured products", products.getTotalElements());
            if (products.getTotalElements() > 0) {
                Page<ProductResponseDTO> productPage = products.map(this::convertToDTO);
                return convertToPageResponseDTO(productPage);
            }
        } catch (Exception e) {
            log.warn("Method name query failed: {}", e.getMessage());
        }
        
        // Method 2: Try JPQL with IS TRUE
        try {
            products = productRepository.findFeaturedProducts(Product.ProductStatus.ACTIVE, pageable);
            log.info("JPQL IS TRUE query returned {} featured products", products.getTotalElements());
            if (products.getTotalElements() > 0) {
                Page<ProductResponseDTO> productPage = products.map(this::convertToDTO);
                return convertToPageResponseDTO(productPage);
            }
        } catch (Exception e) {
            log.warn("JPQL IS TRUE query failed: {}", e.getMessage());
        }
        
        // Method 3: Try JPQL with parameter
        try {
            products = productRepository.findFeaturedProductsByParam(Product.ProductStatus.ACTIVE, Boolean.TRUE, pageable);
            log.info("JPQL parameter query returned {} featured products", products.getTotalElements());
            if (products.getTotalElements() > 0) {
                Page<ProductResponseDTO> productPage = products.map(this::convertToDTO);
                return convertToPageResponseDTO(productPage);
            }
        } catch (Exception e) {
            log.warn("JPQL parameter query failed: {}", e.getMessage());
        }
        
        // Method 4: Fallback - fetch all active products with images and filter in memory
        // This is less efficient but will work if Boolean mapping is the issue
        log.warn("All queries returned empty. Trying fallback: fetch all active with images and filter in memory...");
        try {
            // Use query that fetches images
            List<Product> allActiveProducts = productRepository.findByStatusWithImages(Product.ProductStatus.ACTIVE);
            
            log.info("Found {} active products total. Checking isFeatured values...", allActiveProducts.size());
            
            // Filter featured products in memory
            List<Product> featuredList = allActiveProducts.stream()
                .filter(p -> {
                    Boolean featured = p.getIsFeatured();
                    boolean isFeatured = featured != null && featured;
                    if (isFeatured) {
                        log.debug("Found featured product: {} - isFeatured={}", p.getId(), featured);
                    }
                    return isFeatured;
                })
                .sorted((p1, p2) -> {
                    // Sort by createdAt descending
                    if (p1.getCreatedAt() == null && p2.getCreatedAt() == null) return 0;
                    if (p1.getCreatedAt() == null) return 1;
                    if (p2.getCreatedAt() == null) return -1;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                })
                .collect(Collectors.toList());
            
            log.info("Filtered {} featured products from {} active products", featuredList.size(), allActiveProducts.size());
            
            if (!featuredList.isEmpty()) {
                // Apply pagination manually
                int start = page * size;
                int end = Math.min(start + size, featuredList.size());
                List<Product> paginatedList = start < featuredList.size() 
                    ? featuredList.subList(start, end)
                    : new ArrayList<>();
                
                // Create a custom page
                Page<Product> featuredPage = new PageImpl<>(
                    paginatedList,
                    pageable,
                    featuredList.size()
                );
                
                Page<ProductResponseDTO> productPage = featuredPage.map(this::convertToDTO);
                return convertToPageResponseDTO(productPage);
            }
        } catch (Exception e) {
            log.error("Fallback method also failed", e);
        }
        
        // If all methods fail, return empty
        log.warn("All methods failed to find featured products");
        products = Page.empty(pageable);
        Page<ProductResponseDTO> productPage = products.map(this::convertToDTO);
        return convertToPageResponseDTO(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponseDTO getFlashSaleProducts(int page, int size) {
        // Tạm thời dùng featured làm dữ liệu flash sale, đều lấy từ DB
        return getFeaturedProducts(page, size);
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
    public ProductResponseDTO updateProduct(String id, UpdateProductRequestDTO request) {
        Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
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
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
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
                java.net.URL url = new java.net.URL(imageUrl);
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

