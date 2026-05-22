package com.shopcuathuy.util;

import com.shopcuathuy.entity.*;
import com.shopcuathuy.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.ArrayList;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShippingHubRepository shippingHubRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final BannerRepository bannerRepository;

    @Value("${seed.admin.password:admin@ShopCuaThuy2024!}")
    private String adminPassword;

    @Value("${seed.seller.password:seller@ShopCuaThuy2024!}")
    private String sellerPassword;

    public DataInitializer(UserRepository userRepository,
                           SellerRepository sellerRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder,
                           ShippingHubRepository shippingHubRepository,
                           ShippingMethodRepository shippingMethodRepository,
                           BannerRepository bannerRepository) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
        this.shippingHubRepository = shippingHubRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.bannerRepository = bannerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Create Admin
        User admin = new User();
        admin.setId(UUID.randomUUID().toString());
        admin.setEmail("admin@shopcuathuy.com");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setFullName("System Admin");
        admin.setUserType(User.UserType.ADMIN);
        admin.setApprovalStatus(User.ApprovalStatus.APPROVED);
        userRepository.save(admin);

        // 2. Create Seller
        User sellerUser = new User();
        sellerUser.setId(UUID.randomUUID().toString());
        sellerUser.setEmail("seller@shopcuathuy.com");
        sellerUser.setPasswordHash(passwordEncoder.encode(sellerPassword));
        sellerUser.setFullName("Thuy Seller");
        sellerUser.setUserType(User.UserType.SELLER);
        sellerUser.setApprovalStatus(User.ApprovalStatus.APPROVED);
        userRepository.save(sellerUser);

        System.out.println("[DataInitializer] Seed data created. Admin: admin@shopcuathuy.com");

        Seller seller = new Seller();
        seller.setId(UUID.randomUUID().toString());
        seller.setUser(sellerUser);
        seller.setShopName("Thuy's Modern Shop");
        seller.setShopDescription("Premium electronics and fashion");
        seller.setVerificationStatus(Seller.VerificationStatus.VERIFIED);
        sellerRepository.save(seller);

        // 3. Create Categories
        Category electronics = createCategory("Electronics", "electronics", null);
        Category phones = createCategory("Mobile Phones", "phones", electronics);
        Category laptops = createCategory("Laptops", "laptops", electronics);
        Category fashion = createCategory("Fashion", "fashion", null);
        createCategory("Men Fashion", "men-fashion", fashion);

        // 4. Create Products
        createProduct(seller, phones, "iPhone 15 Pro", "The latest iPhone with titanium design.", "IP-15-TI", 1200.0, 50, true, true, 999.0);
        createProduct(seller, laptops, "MacBook Pro M3", "Supercharged by M3 chip.", "MBP-M3-14", 2500.0, 20, true, false, 0);
        createProduct(seller, phones, "Samsung Galaxy S24", "AI-powered smartphone.", "S24-ULTRA", 1100.0, 30, true, true, 899.0);

        // 5. Create Shipping Hubs
        createHub("Kho Tổng miền Bắc", "HB-001", "Hà Nội", "Giải Phóng", "123 Giải Phóng", ShippingHub.HubType.WAREHOUSE);
        createHub("Bưu cục Quận 1", "HCM-001", "Hồ Chí Minh", "Quận 1", "10 Hàm Nghi", ShippingHub.HubType.LOCAL_STATION);

        // 6. Create Shipping Methods
        createShippingMethod("Giao hàng tiêu chuẩn", "STANDARD", 30000, 5000, 1000);
        createShippingMethod("Giao hàng hỏa tốc", "EXPRESS", 50000, 8000, 2000);

        // 7. Create Banners
        createBanner("Siêu sale 4.4", "https://img.freepik.com/free-vector/shopping-day-banner-template_23-2148881263.jpg", "HOME_MAIN", 1);
        createBanner("Hàng hiệu giảm 50%", "https://img.freepik.com/free-vector/modern-sale-banner-with-product-description_23-2148859169.jpg", "HOME_MAIN", 2);

        // 8. Create Approved Shippers
        createShipper("shipper1@shopcuathuy.com", "Shipper Nguyễn Văn A");
        createShipper("shipper2@shopcuathuy.com", "Shipper Trần Văn B");

        System.out.println("Data initialization completed.");
    }

    private void createHub(String name, String code, String prov, String dist, String addr, ShippingHub.HubType type) {
        ShippingHub hub = new ShippingHub();
        hub.setId(UUID.randomUUID().toString());
        hub.setName(name);
        hub.setCode(code);
        hub.setProvince(prov);
        hub.setDistrict(dist);
        hub.setAddress(addr);
        hub.setHubType(type);
        shippingHubRepository.save(hub);
    }

    private void createShippingMethod(String name, String code, double base, double perKg, double perKm) {
        ShippingMethod sm = new ShippingMethod();
        sm.setId(UUID.randomUUID().toString());
        sm.setName(name);
        sm.setCode(code);
        sm.setBaseFee(BigDecimal.valueOf(base));
        sm.setFeePerKg(BigDecimal.valueOf(perKg));
        sm.setFeePerKm(BigDecimal.valueOf(perKm));
        sm.setIsActive(true);
        shippingMethodRepository.save(sm);
    }

    private void createBanner(String title, String url, String pos, int order) {
        Banner b = new Banner();
        b.setId(UUID.randomUUID().toString());
        b.setTitle(title);
        b.setImageUrl(url);
        b.setPosition(pos);
        b.setDisplayOrder(order);
        b.setIsActive(true);
        bannerRepository.save(b);
    }

    private void createShipper(String email, String name) {
        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setEmail(email);
        u.setFullName(name);
        u.setPasswordHash(passwordEncoder.encode(sellerPassword));
        u.setUserType(User.UserType.SHIPPER);
        u.setApprovalStatus(User.ApprovalStatus.APPROVED);
        u.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(u);
    }

    private Category createCategory(String name, String slug, Category parent) {
        Category category = new Category();
        category.setId(UUID.randomUUID().toString());
        category.setName(name);
        category.setSlug(slug);
        category.setIsActive(true);
        if (parent != null) {
            category.setParent(parent);
        }
        return categoryRepository.save(category);
    }

    private void createProduct(Seller seller, Category category, String name, String description, String sku, double price, int qty, boolean featured, boolean flashSale, double flashPrice) {
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setSeller(seller);
        product.setCategory(category);
        product.setName(name);
        product.setDescription(description);
        product.setSku(sku);
        product.setPrice(BigDecimal.valueOf(price));
        product.setQuantity(qty);
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setIsFeatured(featured);
        
        if (flashSale) {
            product.setComparePrice(BigDecimal.valueOf(price));
            product.setPrice(BigDecimal.valueOf(flashPrice));
            product.setFlashSaleEnabled(true);
            product.setFlashSalePrice(BigDecimal.valueOf(flashPrice));
            product.setFlashSaleStock(qty);
            product.setFlashSaleStart(java.time.LocalDateTime.now());
            product.setFlashSaleEnd(java.time.LocalDateTime.now().plusHours(24));
        }
        
        product.setImages(new ArrayList<>());
        product.setVariants(new ArrayList<>());
        productRepository.save(product);
    }
}
