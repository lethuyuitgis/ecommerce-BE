package com.shopcuathuy.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcuathuy.entity.*;
import com.shopcuathuy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserAddressRepository userAddressRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            // Check if data already exists
            if (userRepository.count() > 0) {
                log.info("Data already exists. Skipping data initialization.");
                return;
            }

            log.info("Starting data initialization from JSON files...");

            // Load Users
            loadUsers();
            
            // Load Categories (must be before products)
            loadCategories();
            
            // Load Sellers
            loadSellers();
            
            // Load Products (must be after categories and sellers)
            loadProducts();
            
            // Load Product Images
            loadProductImages();
            
            // Load Shipping Methods
            loadShippingMethods();
            
            // Load Payment Methods
            loadPaymentMethods();
            
            // Load User Addresses
            loadUserAddresses();
            
            // Update seller total_products
            updateSellerTotalProducts();

            log.info("Data initialization completed successfully!");
        } catch (Exception e) {
            log.error("Error initializing data", e);
        }
    }

    private void loadUsers() throws Exception {
        log.info("Loading users...");
        InputStream inputStream = new ClassPathResource("data/users.json").getInputStream();
        List<Map<String, Object>> usersData = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        for (Map<String, Object> userData : usersData) {
            User user = new User();
            user.setId((String) userData.get("id"));
            user.setEmail((String) userData.get("email"));
            user.setPasswordHash((String) userData.get("passwordHash"));
            user.setFullName((String) userData.get("fullName"));
            user.setPhone((String) userData.get("phone"));
            user.setUserType(User.UserType.valueOf((String) userData.get("userType")));
            user.setStatus(User.UserStatus.valueOf((String) userData.get("status")));
            
            userRepository.save(user);
        }
        log.info("Loaded {} users", usersData.size());
    }

    private void loadCategories() throws Exception {
        log.info("Loading categories...");
        InputStream inputStream = new ClassPathResource("data/categories.json").getInputStream();
        List<Map<String, Object>> categoriesData = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        // First pass: create all categories without parent
        for (Map<String, Object> categoryData : categoriesData) {
            Category category = new Category();
            category.setId((String) categoryData.get("id"));
            category.setName((String) categoryData.get("name"));
            category.setSlug((String) categoryData.get("slug"));
            category.setDescription((String) categoryData.get("description"));
            category.setIcon((String) categoryData.get("icon"));
            category.setCoverImage((String) categoryData.get("coverImage"));
            category.setDisplayOrder(((Number) categoryData.get("displayOrder")).intValue());
            category.setIsActive((Boolean) categoryData.get("isActive"));
            
            categoryRepository.save(category);
        }

        // Second pass: set parent relationships
        for (Map<String, Object> categoryData : categoriesData) {
            String parentId = (String) categoryData.get("parentId");
            if (parentId != null) {
                Category category = categoryRepository.findById((String) categoryData.get("id")).orElse(null);
                Category parent = categoryRepository.findById(parentId).orElse(null);
                if (category != null && parent != null) {
                    category.setParent(parent);
                    categoryRepository.save(category);
                }
            }
        }
        log.info("Loaded {} categories", categoriesData.size());
    }

    private void loadSellers() throws Exception {
        log.info("Loading sellers...");
        InputStream inputStream = new ClassPathResource("data/sellers.json").getInputStream();
        List<Map<String, Object>> sellersData = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        for (Map<String, Object> sellerData : sellersData) {
            Seller seller = new Seller();
            seller.setId((String) sellerData.get("id"));
            
            String userId = (String) sellerData.get("userId");
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found for seller: {}", userId);
                continue;
            }
            seller.setUser(user);
            
            seller.setShopName((String) sellerData.get("shopName"));
            seller.setShopDescription((String) sellerData.get("shopDescription"));
            seller.setShopPhone((String) sellerData.get("shopPhone"));
            seller.setShopEmail((String) sellerData.get("shopEmail"));
            seller.setProvince((String) sellerData.get("province"));
            seller.setDistrict((String) sellerData.get("district"));
            seller.setVerificationStatus(Seller.VerificationStatus.valueOf((String) sellerData.get("verificationStatus")));
            
            if (sellerData.get("rating") != null) {
                seller.setRating(BigDecimal.valueOf(((Number) sellerData.get("rating")).doubleValue()));
            }
            seller.setTotalProducts(((Number) sellerData.get("totalProducts")).intValue());
            seller.setTotalFollowers(((Number) sellerData.get("totalFollowers")).intValue());
            seller.setTotalOrders(((Number) sellerData.get("totalOrders")).intValue());
            
            sellerRepository.save(seller);
        }
        log.info("Loaded {} sellers", sellersData.size());
    }

    private void loadProducts() throws Exception {
        log.info("Loading products...");
        InputStream inputStream = new ClassPathResource("data/products.json").getInputStream();
        List<Map<String, Object>> productsData = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        for (Map<String, Object> productData : productsData) {
            Product product = new Product();
            product.setId((String) productData.get("id"));
            
            String sellerId = (String) productData.get("sellerId");
            Seller seller = sellerRepository.findById(sellerId).orElse(null);
            if (seller == null) {
                log.warn("Seller not found for product: {}", sellerId);
                continue;
            }
            product.setSeller(seller);
            
            String categoryId = (String) productData.get("categoryId");
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                log.warn("Category not found for product: {}", categoryId);
                continue;
            }
            product.setCategory(category);
            
            product.setName((String) productData.get("name"));
            product.setDescription((String) productData.get("description"));
            product.setSku((String) productData.get("sku"));
            product.setPrice(BigDecimal.valueOf(((Number) productData.get("price")).doubleValue()));
            
            if (productData.get("comparePrice") != null) {
                product.setComparePrice(BigDecimal.valueOf(((Number) productData.get("comparePrice")).doubleValue()));
            }
            
            product.setQuantity(((Number) productData.get("quantity")).intValue());
            product.setStatus(Product.ProductStatus.valueOf((String) productData.get("status")));
            
            if (productData.get("rating") != null) {
                product.setRating(BigDecimal.valueOf(((Number) productData.get("rating")).doubleValue()));
            }
            product.setTotalReviews(((Number) productData.get("totalReviews")).intValue());
            product.setTotalSold(((Number) productData.get("totalSold")).intValue());
            product.setTotalViews(((Number) productData.get("totalViews")).intValue());
            product.setIsFeatured((Boolean) productData.get("isFeatured"));
            
            productRepository.save(product);
        }
        log.info("Loaded {} products", productsData.size());
    }

    private void loadProductImages() throws Exception {
        log.info("Loading product images...");
        InputStream inputStream = new ClassPathResource("data/product-images.json").getInputStream();
        List<Map<String, Object>> imagesData = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        for (Map<String, Object> imageData : imagesData) {
            ProductImage image = new ProductImage();
            image.setId((String) imageData.get("id"));
            
            String productId = (String) imageData.get("productId");
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.warn("Product not found for image: {}", productId);
                continue;
            }
            image.setProduct(product);
            
            image.setImageUrl((String) imageData.get("imageUrl"));
            image.setAltText((String) imageData.get("altText"));
            image.setDisplayOrder(((Number) imageData.get("displayOrder")).intValue());
            image.setIsPrimary((Boolean) imageData.get("isPrimary"));
            
            productImageRepository.save(image);
        }
        log.info("Loaded {} product images", imagesData.size());
    }

    private void loadShippingMethods() throws Exception {
        log.info("Loading shipping methods...");
        InputStream inputStream = new ClassPathResource("data/shipping-methods.json").getInputStream();
        List<Map<String, Object>> methodsData = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        for (Map<String, Object> methodData : methodsData) {
            ShippingMethod method = new ShippingMethod();
            method.setId((String) methodData.get("id"));
            method.setCode((String) methodData.get("code"));
            method.setName((String) methodData.get("name"));
            method.setDescription((String) methodData.get("description"));
            method.setIsActive((Boolean) methodData.get("isActive"));
            
            if (methodData.get("minDeliveryDays") != null) {
                method.setMinDeliveryDays(((Number) methodData.get("minDeliveryDays")).intValue());
            }
            if (methodData.get("maxDeliveryDays") != null) {
                method.setMaxDeliveryDays(((Number) methodData.get("maxDeliveryDays")).intValue());
            }
            
            shippingMethodRepository.save(method);
        }
        log.info("Loaded {} shipping methods", methodsData.size());
    }

    private void loadPaymentMethods() throws Exception {
        log.info("Loading payment methods...");
        InputStream inputStream = new ClassPathResource("data/payment-methods.json").getInputStream();
        List<Map<String, Object>> methodsData = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        for (Map<String, Object> methodData : methodsData) {
            PaymentMethod method = new PaymentMethod();
            method.setId((String) methodData.get("id"));
            method.setMethodCode((String) methodData.get("methodCode"));
            method.setMethodName((String) methodData.get("methodName"));
            method.setDescription((String) methodData.get("description"));
            method.setIsActive((Boolean) methodData.get("isActive"));
            
            paymentMethodRepository.save(method);
        }
        log.info("Loaded {} payment methods", methodsData.size());
    }

    private void loadUserAddresses() throws Exception {
        log.info("Loading user addresses...");
        InputStream inputStream = new ClassPathResource("data/user-addresses.json").getInputStream();
        List<Map<String, Object>> addressesData = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

        for (Map<String, Object> addressData : addressesData) {
            UserAddress address = new UserAddress();
            address.setId((String) addressData.get("id"));
            
            String userId = (String) addressData.get("userId");
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User not found for address: {}", userId);
                continue;
            }
            address.setUser(user);
            
            address.setAddressType(UserAddress.AddressType.valueOf((String) addressData.get("addressType")));
            address.setFullName((String) addressData.get("fullName"));
            address.setPhone((String) addressData.get("phone"));
            address.setProvince((String) addressData.get("province"));
            address.setDistrict((String) addressData.get("district"));
            address.setWard((String) addressData.get("ward"));
            address.setStreet((String) addressData.get("street"));
            address.setIsDefault((Boolean) addressData.get("isDefault"));
            
            userAddressRepository.save(address);
        }
        log.info("Loaded {} user addresses", addressesData.size());
    }

    private void updateSellerTotalProducts() {
        log.info("Updating seller total products...");
        List<Seller> sellers = sellerRepository.findAll();
        for (Seller seller : sellers) {
            long count = productRepository.countBySellerId(seller.getId());
            seller.setTotalProducts((int) count);
            sellerRepository.save(seller);
        }
        log.info("Updated total products for {} sellers", sellers.size());
    }
}

