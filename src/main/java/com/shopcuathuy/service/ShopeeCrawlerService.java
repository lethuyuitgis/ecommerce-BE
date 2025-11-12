package com.shopcuathuy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcuathuy.entity.Category;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductImage;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.repository.CategoryRepository;
import com.shopcuathuy.repository.ProductImageRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import io.minio.MinioClient;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


/**
 * Service để crawl data từ Shopee
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopeeCrawlerService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name:shopcuathuy}")
    private String bucketName;
    
    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;
    
    /**
     * Crawl categories từ Shopee
     */
    @Transactional
    public void crawlCategories() {
        log.info("Starting to crawl categories from Shopee...");
        
        try {
            // Shopee categories API endpoint
            String url = "https://shopee.vn/api/v4/pages/get_category_tree";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.get("data");
                
                if (data != null && data.has("category_list")) {
                    JsonNode categoryList = data.get("category_list");
                    int order = 1;
                    
                    for (JsonNode categoryNode : categoryList) {
                        processCategory(categoryNode, null, order++);
                    }
                }
            }
            
            log.info("Categories crawling completed.");
        } catch (Exception e) {
            log.error("Error crawling categories from Shopee", e);
            throw new RuntimeException("Failed to crawl categories", e);
        }
    }
    
    private void processCategory(JsonNode categoryNode, Category parent, int displayOrder) {
        try {
            String categoryId = categoryNode.get("catid").asText();
            String name = categoryNode.get("display_name").asText();
            String slug = generateSlug(name);
            
            // Check if category already exists
            Category category = categoryRepository.findBySlug(slug).orElse(null);
            
            if (category == null) {
                category = new Category();
                category.setId(UUID.randomUUID().toString());
                category.setName(name);
                category.setSlug(slug);
                category.setDescription(categoryNode.has("description") ? 
                    categoryNode.get("description").asText() : null);
                category.setDisplayOrder(displayOrder);
                category.setIsActive(true);
                category.setParent(parent);
                
                // Download and upload cover image
                if (categoryNode.has("image")) {
                    String imageUrl = categoryNode.get("image").asText();
                    String uploadedImageUrl = downloadAndUploadImage(imageUrl, "categories");
                    category.setCoverImage(uploadedImageUrl);
                }
                
                category = categoryRepository.save(category);
                log.info("Saved category: {}", name);
            }
            
            // Process children categories
            if (categoryNode.has("children") && categoryNode.get("children").isArray()) {
                int childOrder = 1;
                for (JsonNode childNode : categoryNode.get("children")) {
                    processCategory(childNode, category, childOrder++);
                }
            }
        } catch (Exception e) {
            log.error("Error processing category", e);
        }
    }
    
    /**
     * Crawl products từ Shopee theo category
     */
    @Transactional
    public void crawlProducts(String categorySlug, int limit) {
        log.info("Starting to crawl products from Shopee for category: {}", categorySlug);
        
        try {
            Category category = categoryRepository.findBySlug(categorySlug)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categorySlug));
            
            log.info("Found category: {} (ID: {})", category.getName(), category.getId());
            
            // Get default seller or create one
            Seller seller = getDefaultSeller();
            if (seller == null) {
                log.error("Failed to get or create seller");
                throw new RuntimeException("Failed to get or create seller");
            }
            log.info("Using seller: {} (ID: {})", seller.getShopName(), seller.getId());
            
            // Shopee search API - sử dụng category ID hoặc tìm kiếm theo keyword
            // Thử nhiều cách để lấy products
            String[] searchKeywords = {
                category.getName(),
                categorySlug.replace("-", " ")
            };
            
            int totalSaved = 0;
            
            for (String keyword : searchKeywords) {
                try {
                    // Shopee search API với keyword
                    String url = String.format(
                        "https://shopee.vn/api/v4/search/search_items?by=relevancy&keyword=%s&limit=%d&newest=0&order=desc&page_type=search&scenario=PAGE_GLOBAL_SEARCH&version=2",
                        java.net.URLEncoder.encode(keyword, "UTF-8"), limit
                    );
                    
                    log.info("Trying to crawl with keyword: {} - URL: {}", keyword, url);
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                    headers.set("Accept", "application/json");
                    headers.set("Referer", "https://shopee.vn/");
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    
                    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                    
                    log.info("Response status: {}", response.getStatusCode());
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        log.info("Response body length: {}", response.getBody().length());
                        JsonNode root = objectMapper.readTree(response.getBody());
                        
                        // Log response structure for debugging
                        log.info("Response keys: {}", root.fieldNames().hasNext() ? root.fieldNames().next() : "empty");
                        
                        JsonNode items = root.path("items");
                        if (items.isArray() && items.size() > 0) {
                            log.info("Found {} items in response", items.size());
                            
                            for (JsonNode item : items) {
                                try {
                                    processProduct(item, category, seller);
                                    totalSaved++;
                                } catch (Exception e) {
                                    log.error("Error processing individual product", e);
                                }
                            }
                            
                            // Nếu đã lấy được products, không cần thử keyword khác
                            if (totalSaved > 0) {
                                break;
                            }
                        } else {
                            log.warn("No items found in response for keyword: {}", keyword);
                        }
                    } else {
                        log.warn("Failed to get response for keyword: {}", keyword);
                    }
                    
                    // Delay giữa các request
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("Error crawling with keyword: {}", keyword, e);
                }
            }
            
            log.info("Products crawling completed for category: {}. Total saved: {}", categorySlug, totalSaved);
        } catch (Exception e) {
            log.error("Error crawling products from Shopee", e);
            e.printStackTrace();
            throw new RuntimeException("Failed to crawl products: " + e.getMessage(), e);
        }
    }
    
    private void processProduct(JsonNode itemNode, Category category, Seller seller) {
        try {
            JsonNode itemBasic = itemNode.get("item_basic");
            if (itemBasic == null) {
                log.warn("item_basic is null in itemNode");
                return;
            }
            
            if (!itemBasic.has("itemid")) {
                log.warn("itemid not found in item_basic");
                return;
            }
            
            String itemId = itemBasic.get("itemid").asText();
            String shopId = itemBasic.has("shopid") ? itemBasic.get("shopid").asText() : "0";
            
            log.debug("Processing product itemId: {}, shopId: {}", itemId, shopId);
            
            // Check if product already exists
            String sku = "SHOPEE_" + itemId;
            if (productRepository.findBySku(sku).isPresent()) {
                log.debug("Product already exists: {}", sku);
                return;
            }
            
            if (!itemBasic.has("name")) {
                log.warn("Product name not found for itemId: {}", itemId);
                return;
            }
            
            Product product = new Product();
            product.setId(UUID.randomUUID().toString());
            product.setSeller(seller);
            product.setCategory(category);
            product.setName(itemBasic.get("name").asText());
            product.setDescription(itemBasic.has("description") ? 
                itemBasic.get("description").asText() : null);
            product.setSku(sku);
            
            // Price - Shopee price is in smallest currency unit (VND * 100000)
            if (itemBasic.has("price")) {
                long priceValue = itemBasic.get("price").asLong();
                long price = priceValue / 100000; // Convert to VND
                product.setPrice(BigDecimal.valueOf(price));
                log.debug("Product price: {} (raw: {})", price, priceValue);
            } else {
                product.setPrice(BigDecimal.ZERO);
                log.warn("Product price not found for itemId: {}", itemId);
            }
            
            if (itemBasic.has("price_before_discount")) {
                long comparePriceValue = itemBasic.get("price_before_discount").asLong();
                long comparePrice = comparePriceValue / 100000;
                product.setComparePrice(BigDecimal.valueOf(comparePrice));
            }
            
            // Stock
            int stock = itemBasic.has("stock") ? itemBasic.get("stock").asInt() : 0;
            product.setQuantity(stock);
            
            // Rating
            if (itemBasic.has("item_rating")) {
                JsonNode rating = itemBasic.get("item_rating");
                if (rating.has("rating_star")) {
                    product.setRating(BigDecimal.valueOf(rating.get("rating_star").asDouble()));
                }
                if (rating.has("rating_count")) {
                    product.setTotalReviews(rating.get("rating_count").asInt());
                }
            }
            
            // Sold
            if (itemBasic.has("historical_sold")) {
                product.setTotalSold(itemBasic.get("historical_sold").asInt());
            }
            
            // Views
            if (itemBasic.has("view_count")) {
                product.setTotalViews(itemBasic.get("view_count").asInt());
            }
            
            product.setStatus(Product.ProductStatus.ACTIVE);
            product.setIsFeatured(false);
            
            product = productRepository.save(product);
            log.info("✅ Saved product: {} (SKU: {}, Price: {})", product.getName(), product.getSku(), product.getPrice());
            
            // Process images
            if (itemBasic.has("images")) {
                List<String> imageUrls = new ArrayList<>();
                JsonNode images = itemBasic.get("images");
                
                for (JsonNode imageId : images) {
                    String imageUrl = String.format("https://cf.shopee.vn/file/%s", imageId.asText());
                    imageUrls.add(imageUrl);
                }
                
                // Download and upload images
                int displayOrder = 1;
                for (String imageUrl : imageUrls) {
                    try {
                        String uploadedImageUrl = downloadAndUploadImage(imageUrl, "products");
                        
                        ProductImage productImage = new ProductImage();
                        productImage.setId(UUID.randomUUID().toString());
                        productImage.setProduct(product);
                        productImage.setImageUrl(uploadedImageUrl);
                        productImage.setAltText(product.getName());
                        productImage.setDisplayOrder(displayOrder++);
                        productImage.setIsPrimary(displayOrder == 2); // First image is primary
                        
                        productImageRepository.save(productImage);
                    } catch (Exception e) {
                        log.error("Error processing product image: {}", imageUrl, e);
                    }
                }
            }
            
            // Add delay to avoid rate limiting
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error("Error processing product", e);
        }
    }
    
    private Seller getDefaultSeller() {
        // Get first seller or create default one
        return sellerRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    // Create default seller if none exists
                    User sellerUser = userRepository.findAll().stream()
                            .filter(u -> u.getUserType() == User.UserType.SELLER)
                            .findFirst()
                            .orElseGet(() -> {
                                // Create default seller user
                                User user = new User();
                                user.setId(UUID.randomUUID().toString());
                                user.setEmail("shopee-crawler@shopcuathuy.com");
                                user.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // 123456
                                user.setFullName("Shopee Crawler");
                                user.setUserType(User.UserType.SELLER);
                                user.setStatus(User.UserStatus.ACTIVE);
                                return userRepository.save(user);
                            });
                    
                    Seller seller = new Seller();
                    seller.setId(UUID.randomUUID().toString());
                    seller.setUser(sellerUser);
                    seller.setShopName("Shopee Products");
                    seller.setShopDescription("Products crawled from Shopee");
                    seller.setVerificationStatus(Seller.VerificationStatus.VERIFIED);
                    seller.setRating(BigDecimal.valueOf(4.5));
                    seller.setTotalProducts(0);
                    seller.setTotalFollowers(0);
                    seller.setTotalOrders(0);
                    return sellerRepository.save(seller);
                });
    }
    
    /**
     * Download image from URL and upload to MinIO
     */
    private String downloadAndUploadImage(String imageUrl, String folder) {
        try {
            log.info("Downloading image: {}", imageUrl);
            
            // Download image
            URL url = new URL(imageUrl);
            InputStream imageStream = url.openStream();
            
            // Generate filename
            String extension = imageUrl.substring(imageUrl.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String objectName = folder + "/" + fileName;
            
            // Upload to MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(imageStream, -1, 10485760) // 10MB max
                    .contentType("image/jpeg")
                    .build()
            );
            
            imageStream.close();
            
            // Return MinIO URL
            String minioUrl = minioEndpoint + "/" + bucketName + "/" + objectName;
            log.info("Uploaded image to MinIO: {}", minioUrl);
            
            return minioUrl;
        } catch (Exception e) {
            log.error("Error downloading/uploading image: {}", imageUrl, e);
            // Return original URL if upload fails
            return imageUrl;
        }
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}

