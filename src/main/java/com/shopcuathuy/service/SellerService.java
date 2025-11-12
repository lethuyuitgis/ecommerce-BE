package com.shopcuathuy.service;

import com.shopcuathuy.dto.ProductDTO;
import com.shopcuathuy.dto.SellerDTO;
import com.shopcuathuy.entity.Category;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductImage;
import com.shopcuathuy.entity.ProductVariant;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.BadRequestException;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.CategoryRepository;
import com.shopcuathuy.repository.ProductImageRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.ProductVariantRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.service.MinIOService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional

public class SellerService {
    
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final MinIOService minIOService;
    
    public SellerDTO getSellerProfile(String userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        return toDTO(seller);
    }
    
    public SellerDTO createSeller(String userId, CreateSellerRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (sellerRepository.existsByUserId(userId)) {
            throw new BadRequestException("Seller profile already exists");
        }
        
        Seller seller = new Seller();
        seller.setUser(user);
        seller.setShopName(request.getShopName());
        seller.setShopDescription(request.getShopDescription());
        seller.setShopPhone(request.getShopPhone());
        seller.setShopEmail(request.getShopEmail());
        seller.setProvince(request.getProvince());
        seller.setDistrict(request.getDistrict());
        seller.setVerificationStatus(Seller.VerificationStatus.PENDING);
        
        seller = sellerRepository.save(seller);
        
        // Update user type to SELLER
        user.setUserType(User.UserType.SELLER);
        userRepository.save(user);
        
        return toDTO(seller);
    }
    
    public SellerDTO updateSellerProfile(String userId, UpdateSellerRequest request) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        
        if (request.getShopName() != null) {
            seller.setShopName(request.getShopName());
        }
        if (request.getShopDescription() != null) {
            seller.setShopDescription(request.getShopDescription());
        }
        if (request.getShopPhone() != null) {
            seller.setShopPhone(request.getShopPhone());
        }
        if (request.getShopEmail() != null) {
            seller.setShopEmail(request.getShopEmail());
        }
        if (request.getProvince() != null) {
            seller.setProvince(request.getProvince());
        }
        if (request.getDistrict() != null) {
            seller.setDistrict(request.getDistrict());
        }
        
        seller = sellerRepository.save(seller);
        return toDTO(seller);
    }
    
    public Page<ProductDTO> getSellerProducts(String userId, Pageable pageable) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        
        return productRepository.findBySellerId(seller.getId(), pageable)
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setId(product.getId());
                    dto.setName(product.getName());
                    dto.setDescription(product.getDescription());
                    dto.setSku(product.getSku());
                    dto.setPrice(product.getPrice());
                    dto.setComparePrice(product.getComparePrice());
                    dto.setQuantity(product.getQuantity());
                    dto.setMinOrder(product.getMinOrder());
                    dto.setStatus(product.getStatus().name());
                    dto.setRating(product.getRating());
                    dto.setTotalReviews(product.getTotalReviews());
                    dto.setTotalSold(product.getTotalSold());
                    dto.setTotalViews(product.getTotalViews());
                    dto.setIsFeatured(product.getIsFeatured());
                    
                    if (product.getCategory() != null) {
                        dto.setCategoryId(product.getCategory().getId());
                        dto.setCategoryName(product.getCategory().getName());
                    }
                    
                    if (product.getSeller() != null) {
                        dto.setSellerId(product.getSeller().getId());
                        dto.setSellerName(product.getSeller().getShopName());
                    }
                    
                    // Load images từ product_images table
                    List<ProductImage> productImages = productImageRepository
                            .findByProductIdOrderByDisplayOrderAsc(product.getId());
                    
                    if (!productImages.isEmpty()) {
                        // Lấy ảnh primary (isPrimary = true) hoặc ảnh đầu tiên
                        ProductImage primaryImg = productImages.stream()
                                .filter(ProductImage::getIsPrimary)
                                .findFirst()
                                .orElse(productImages.get(0));
                        
                        // Convert imageUrl thành presigned URL
                        String primaryImageUrl = convertToPresignedUrl(primaryImg.getImageUrl());
                        dto.setPrimaryImage(primaryImageUrl);
                        
                        // Set tất cả ảnh với presigned URLs
                        List<String> imageUrls = productImages.stream()
                                .map(ProductImage::getImageUrl)
                                .map(this::convertToPresignedUrl)
                                .collect(Collectors.toList());
                        dto.setImages(imageUrls);
                    }
                    
                    return dto;
                });
    }
    
    public ProductDTO createProduct(String userId, CreateProductRequest request) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Product name is required");
        }
        if (request.getPrice() == null) {
            throw new BadRequestException("Product price is required");
        }
        // Resolve category: either by id or slug or name (best-effort)
        Category category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        } else if (request.getCategory() != null && !request.getCategory().isBlank()) {
            // Try find by slug then by name
            category = categoryRepository.findBySlug(request.getCategory())
                    .orElseGet(() -> categoryRepository.findByNameIgnoreCase(request.getCategory()).orElse(null));
        }
        if (category == null) {
            throw new BadRequestException("Category is required");
        }
        
        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku() != null && !request.getSku().isBlank() ? request.getSku() : generateSku());
        product.setPrice(request.getPrice());
        product.setComparePrice(request.getComparePrice());
        product.setQuantity(request.getQuantity() != null ? request.getQuantity() : 0);
        product.setStatus(Product.ProductStatus.ACTIVE);
        
        // Images
        List<ProductImage> images = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (int i = 0; i < request.getImages().size(); i++) {
                String url = request.getImages().get(i);
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageUrl(url);
                img.setIsPrimary(i == 0);
                img.setDisplayOrder(i + 1);
                images.add(img);
            }
        }
        product.setImages(images);
        
        // Variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            final Product productRef = product; // đảm bảo effectively final

            List<ProductVariant> variants = request.getVariants().stream().map(v -> {
                ProductVariant pv = new ProductVariant();

                pv.setProduct(productRef);

                String color = v.getColor();
                String size = v.getSize();
                String nameParts =
                        (color != null && !color.isBlank() ? color : "") +
                                (size != null && !size.isBlank() ? (" - " + size) : "");
                pv.setVariantName(nameParts.isBlank() ? "Default" : nameParts);

                pv.setVariantPrice(v.getPrice() != null ? v.getPrice() : productRef.getPrice());
                pv.setVariantQuantity(v.getStock() != null ? v.getStock() : 0);

                // simple attributes JSON (escape dấu ")
                String safeColor = color != null ? color.replace("\"", "\\\"") : "";
                String safeSize  = size  != null ? size.replace("\"", "\\\"")  : "";
                String attributesJson = "{"
                        + "\"color\":\"" + safeColor + "\","
                        + "\"size\":\""  + safeSize  + "\""
                        + "}";
                pv.setAttributes(attributesJson);

                return pv;
            }).collect(Collectors.toList());

            product.setVariants(variants);
        }
        
        product = productRepository.save(product);
        
        // Map to DTO
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setComparePrice(product.getComparePrice());
        dto.setQuantity(product.getQuantity());
        dto.setStatus(product.getStatus().name());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerName(product.getSeller().getShopName());
        
        // Convert images to presigned URLs
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> imageUrls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .map(this::convertToPresignedUrl)
                    .collect(Collectors.toList());
            dto.setImages(imageUrls);
            
            product.getImages().stream()
                    .filter(ProductImage::getIsPrimary)
                    .findFirst()
                    .ifPresent(img -> dto.setPrimaryImage(convertToPresignedUrl(img.getImageUrl())));
        }
        
        return dto;
    }
    
    public ProductDTO updateProduct(String userId, String productId, UpdateProductRequest request) {
        // Verify seller owns this product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new BadRequestException("You don't have permission to update this product");
        }
        
        // Update basic fields
        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getSku() != null && !request.getSku().isBlank()) {
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
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                product.setStatus(Product.ProductStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Invalid status, keep current
            }
        }
        
        // Update category
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
        
        // Update images
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Clear existing images collection first (let Hibernate handle cascade delete orphan)
            if (product.getImages() != null) {
                product.getImages().clear();
            } else {
                product.setImages(new ArrayList<>());
            }
            
            // Add new images to the existing collection
            for (int i = 0; i < request.getImages().size(); i++) {
                String imageUrl = request.getImages().get(i);
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageUrl(imageUrl);
                img.setDisplayOrder(i + 1);
                img.setIsPrimary(i == 0);
                product.getImages().add(img);
            }
        }
        
        // Update variants
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            // Clear existing variants collection first (let Hibernate handle cascade delete orphan)
            if (product.getVariants() != null) {
                product.getVariants().clear();
            } else {
                product.setVariants(new ArrayList<>());
            }
            
            // Add new variants to the existing collection
            final Product productRef = product; // ensure effectively final
            request.getVariants().forEach(v -> {
                ProductVariant pv = new ProductVariant();
                pv.setProduct(productRef);
                
                String color = v.getColor();
                String size = v.getSize();
                String nameParts = (color != null && !color.isBlank() ? color : "") 
                        + (size != null && !size.isBlank() ? (" - " + size) : "");
                pv.setVariantName(nameParts.isBlank() ? "Default" : nameParts);
                pv.setVariantPrice(v.getPrice() != null ? v.getPrice() : productRef.getPrice());
                pv.setVariantQuantity(v.getStock() != null ? v.getStock() : 0);
                
                // Simple attributes JSON
                String safeColor = color != null ? color.replace("\"", "\\\"") : "";
                String safeSize = size != null ? size.replace("\"", "\\\"") : "";
                String attributesJson = "{"
                        + "\"color\":\"" + safeColor + "\","
                        + "\"size\":\"" + safeSize + "\""
                        + "}";
                pv.setAttributes(attributesJson);
                
                productRef.getVariants().add(pv);
            });
        }
        
        product = productRepository.save(product);
        
        // Convert to DTO
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setComparePrice(product.getComparePrice());
        dto.setQuantity(product.getQuantity());
        dto.setStatus(product.getStatus().name());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setSellerId(product.getSeller().getId());
        dto.setSellerName(product.getSeller().getShopName());
        
        // Convert images to presigned URLs
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> imageUrls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .map(this::convertToPresignedUrl)
                    .collect(Collectors.toList());
            dto.setImages(imageUrls);
            
            product.getImages().stream()
                    .filter(ProductImage::getIsPrimary)
                    .findFirst()
                    .ifPresent(img -> dto.setPrimaryImage(convertToPresignedUrl(img.getImageUrl())));
        }
        
        return dto;
    }
    
    private String generateSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Convert imageUrl thành presigned URL
     * Hỗ trợ cả object name (products/xxx.webp) và full URL
     */
    private String convertToPresignedUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }
        
        try {
            String objectName = imageUrl;
            
            // Nếu là full URL (chứa http:// hoặc https://), extract object name
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // Tìm phần sau bucket name
                String[] parts = imageUrl.split("/");
                int bucketIndex = -1;
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("shopcuathuy")) {
                        bucketIndex = i;
                        break;
                    }
                }
                if (bucketIndex >= 0 && bucketIndex < parts.length - 1) {
                    // Lấy phần sau bucket name
                    StringBuilder objName = new StringBuilder();
                    for (int i = bucketIndex + 1; i < parts.length; i++) {
                        if (objName.length() > 0) objName.append("/");
                        objName.append(parts[i]);
                    }
                    objectName = objName.toString();
                } else {
                    // Nếu không tìm thấy bucket name, có thể URL không đúng format
                    // Thử lấy phần cuối cùng
                    objectName = parts[parts.length - 1];
                }
            }
            // Nếu không phải full URL, giả sử nó đã là object name
            
            // Tạo presigned URL từ object name
            return minIOService.getFileUrl(objectName);
        } catch (Exception e) {
            // Nếu có lỗi, trả về URL gốc
            return imageUrl;
        }
    }
    
    private SellerDTO toDTO(Seller seller) {
        SellerDTO dto = new SellerDTO();
        dto.setId(seller.getId());
        dto.setShopName(seller.getShopName());
        dto.setShopDescription(seller.getShopDescription());
        dto.setShopAvatar(seller.getShopAvatar());
        dto.setShopCover(seller.getShopCover());
        dto.setShopPhone(seller.getShopPhone());
        dto.setShopEmail(seller.getShopEmail());
        dto.setProvince(seller.getProvince());
        dto.setDistrict(seller.getDistrict());
        dto.setRating(seller.getRating());
        dto.setTotalProducts(seller.getTotalProducts());
        dto.setTotalFollowers(seller.getTotalFollowers());
        dto.setTotalOrders(seller.getTotalOrders());
        dto.setVerificationStatus(seller.getVerificationStatus().name());
        return dto;
    }
    
    public static class CreateSellerRequest {
        private String shopName;
        private String shopDescription;
        private String shopPhone;
        private String shopEmail;
        private String province;
        private String district;
        
        // Getters and setters
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getShopDescription() { return shopDescription; }
        public void setShopDescription(String shopDescription) { this.shopDescription = shopDescription; }
        public String getShopPhone() { return shopPhone; }
        public void setShopPhone(String shopPhone) { this.shopPhone = shopPhone; }
        public String getShopEmail() { return shopEmail; }
        public void setShopEmail(String shopEmail) { this.shopEmail = shopEmail; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
    }
    
    public static class UpdateSellerRequest {
        private String shopName;
        private String shopDescription;
        private String shopPhone;
        private String shopEmail;
        private String province;
        private String district;
        
        // Getters and setters
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getShopDescription() { return shopDescription; }
        public void setShopDescription(String shopDescription) { this.shopDescription = shopDescription; }
        public String getShopPhone() { return shopPhone; }
        public void setShopPhone(String shopPhone) { this.shopPhone = shopPhone; }
        public String getShopEmail() { return shopEmail; }
        public void setShopEmail(String shopEmail) { this.shopEmail = shopEmail; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
    }
    
    public static class CreateProductRequest {
        private String name;
        private String description;
        private String sku;
        private BigDecimal price;
        private BigDecimal comparePrice;
        private Integer quantity;
        private String categoryId;
        private String category; // slug or name
        private List<String> images;
        private List<CreateVariantRequest> variants;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getComparePrice() { return comparePrice; }
        public void setComparePrice(BigDecimal comparePrice) { this.comparePrice = comparePrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
        public List<CreateVariantRequest> getVariants() { return variants; }
        public void setVariants(List<CreateVariantRequest> variants) { this.variants = variants; }
    }
    
    public static class CreateVariantRequest {
        private String size;
        private String color;
        private BigDecimal price;
        private Integer stock;
        
        // Getters and setters
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }
    
    public static class UpdateProductRequest {
        private String name;
        private String description;
        private String sku;
        private BigDecimal price;
        private BigDecimal comparePrice;
        private Integer quantity;
        private String status;
        private String categoryId;
        private List<String> images;
        private List<UpdateVariantRequest> variants;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getComparePrice() { return comparePrice; }
        public void setComparePrice(BigDecimal comparePrice) { this.comparePrice = comparePrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
        public List<UpdateVariantRequest> getVariants() { return variants; }
        public void setVariants(List<UpdateVariantRequest> variants) { this.variants = variants; }
    }
    
    public static class UpdateVariantRequest {
        private String size;
        private String color;
        private BigDecimal price;
        private Integer stock;
        
        // Getters and setters
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }
}

