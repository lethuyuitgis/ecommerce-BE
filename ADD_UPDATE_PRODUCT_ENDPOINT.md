# Thêm endpoint PUT để cập nhật sản phẩm

## Vấn đề
Frontend đang gọi `PUT /api/seller/products/{id}` nhưng backend chưa có endpoint này, dẫn đến lỗi 404.

## Giải pháp
Thêm method `updateProduct` vào `SellerController.java`:

```java
@PutMapping("/products/{productId}")
public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
    @RequestHeader("X-User-Id") String userId,
    @PathVariable String productId,
    @RequestBody SellerService.UpdateProductRequest request
) {
    ProductDTO product = sellerService.updateProduct(userId, productId, request);
    return ResponseEntity.ok(ApiResponse.success("Product updated", product));
}
```

## Thêm method vào SellerService

### 1. Tạo UpdateProductRequest class trong SellerService:

```java
@Data
public static class UpdateProductRequest {
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private Integer quantity;
    private String status;
    private String categoryId;
    private String categoryName;
    private List<String> images;
    private String shippingMethodId;
    private List<VariantRequest> variants;
    
    @Data
    public static class VariantRequest {
        private String size;
        private String color;
        private BigDecimal price;
        private Integer stock;
    }
}
```

### 2. Thêm method updateProduct vào SellerService:

```java
public ProductDTO updateProduct(String userId, String productId, UpdateProductRequest request) {
    // Verify seller owns this product
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    
    if (!product.getSellerId().equals(userId)) {
        throw new RuntimeException("You don't have permission to update this product");
    }
    
    // Update basic fields
    if (request.getName() != null) {
        product.setName(request.getName());
    }
    if (request.getDescription() != null) {
        product.setDescription(request.getDescription());
    }
    if (request.getSku() != null) {
        product.setSku(request.getSku());
    }
    if (request.getPrice() != null) {
        product.setPrice(request.getPrice());
    }
    if (request.getComparePrice() != null) {
        product.setComparePrice(request.getComparePrice());
    }
    if (request.getQuantity() != null) {
        product.setQuantity(request.getQuantity());
    }
    if (request.getStatus() != null) {
        product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));
    }
    
    // Update category
    if (request.getCategoryId() != null) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategoryId(category.getId());
    }
    
    // Update shipping method
    if (request.getShippingMethodId() != null) {
        product.setShippingMethodId(request.getShippingMethodId());
    }
    
    // Update images
    if (request.getImages() != null && !request.getImages().isEmpty()) {
        // Delete old images
        productImageRepository.deleteByProductId(productId);
        
        // Add new images
        for (int i = 0; i < request.getImages().size(); i++) {
            String imageUrl = request.getImages().get(i);
            ProductImage productImage = new ProductImage();
            productImage.setProductId(productId);
            productImage.setImageUrl(imageUrl);
            productImage.setDisplayOrder(i);
            productImage.setIsPrimary(i == 0);
            productImageRepository.save(productImage);
        }
    }
    
    // Update variants
    if (request.getVariants() != null && !request.getVariants().isEmpty()) {
        // Delete old variants
        productVariantRepository.deleteByProductId(productId);
        
        // Add new variants
        for (UpdateProductRequest.VariantRequest v : request.getVariants()) {
            ProductVariant pv = new ProductVariant();
            pv.setProduct(product);
            
            String nameParts = (v.getColor() != null && !v.getColor().isBlank() ? v.getColor() : "") 
                + (v.getSize() != null && !v.getSize().isBlank() ? (" - " + v.getSize()) : "");
            pv.setVariantName(nameParts.isBlank() ? "Default" : nameParts);
            pv.setVariantPrice(v.getPrice() != null ? v.getPrice() : product.getPrice());
            pv.setVariantQuantity(v.getStock() != null ? v.getStock() : 0);
            
            // Simple attributes JSON
            String attributesJson = "{" 
                + "\"color\":\"" + (v.getColor() != null ? v.getColor().replace("\"", "\\\"") : "") + "\","
                + "\"size\":\"" + (v.getSize() != null ? v.getSize().replace("\"", "\\\"") : "") + "\""
                + "}";
            pv.setAttributes(attributesJson);
            
            productVariantRepository.save(pv);
        }
    }
    
    product = productRepository.save(product);
    
    // Convert to DTO with images
    ProductDTO dto = convertToDTO(product);
    
    // Load and convert images to presigned URLs
    List<ProductImage> productImages = productImageRepository
        .findByProductIdOrderByDisplayOrderAsc(product.getId());
    
    if (!productImages.isEmpty()) {
        ProductImage primaryImg = productImages.stream()
            .filter(ProductImage::getIsPrimary)
            .findFirst()
            .orElse(productImages.get(0));
        
        String primaryImageUrl = convertToPresignedUrl(primaryImg.getImageUrl());
        dto.setPrimaryImage(primaryImageUrl);
        
        List<String> imageUrls = productImages.stream()
            .map(ProductImage::getImageUrl)
            .map(this::convertToPresignedUrl)
            .collect(Collectors.toList());
        dto.setImages(imageUrls);
    }
    
    return dto;
}
```

## Lưu ý

1. Đảm bảo có các repository methods:
   - `productImageRepository.deleteByProductId(String productId)`
   - `productVariantRepository.deleteByProductId(String productId)`

2. Nếu chưa có, thêm vào repository interfaces:

```java
// ProductImageRepository.java
void deleteByProductId(String productId);

// ProductVariantRepository.java
void deleteByProductId(String productId);
```

3. Sau khi thêm code, rebuild và restart backend.

4. Test endpoint:
```bash
curl -X PUT 'http://localhost:8080/api/seller/products/{productId}' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {token}' \
  -H 'X-User-Id: {userId}' \
  -d '{
    "name": "Updated Product Name",
    "price": 100000,
    "quantity": 50,
    "status": "active"
  }'
```


