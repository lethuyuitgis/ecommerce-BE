USE shopcuathuy;
SET FOREIGN_KEY_CHECKS = 0;
SET NAMES 'utf8mb4';

-- ===================== TRUNCATE ALL =====================
TRUNCATE TABLE tracking_updates;
TRUNCATE TABLE shipments;
TRUNCATE TABLE order_timeline;
TRUNCATE TABLE payment_transactions;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE review_images;
TRUNCATE TABLE product_reviews;
TRUNCATE TABLE promotion_items;
TRUNCATE TABLE promotions;
TRUNCATE TABLE voucher_usages;
TRUNCATE TABLE vouchers;
TRUNCATE TABLE wishlist;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE product_images;
TRUNCATE TABLE product_variants;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE notifications;
TRUNCATE TABLE user_addresses;
TRUNCATE TABLE sellers;
TRUNCATE TABLE users;
TRUNCATE TABLE shipping_methods;
TRUNCATE TABLE payment_methods;

SET FOREIGN_KEY_CHECKS = 1;

-- ===================== USERS =====================
INSERT INTO users (id, email, password_hash, full_name, phone, user_type, status, created_at, updated_at) VALUES
('u-admin-001', 'admin@shopcuathuy.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin Hệ Thống',    '0900000000', 'ADMIN',    'ACTIVE', NOW(), NOW()),
('u-seller-01', 'thegioididong@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Minh Khoa',  '0901234567', 'SELLER',   'ACTIVE', NOW(), NOW()),
('u-seller-02', 'fashionvn@gmail.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị Hương',    '0902345678', 'SELLER',   'ACTIVE', NOW(), NOW()),
('u-seller-03', 'homegarden@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Văn Đức',        '0903456789', 'SELLER',   'ACTIVE', NOW(), NOW()),
('u-seller-04', 'cosmeticvip@gmail.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Thu Ngân',     '0904567890', 'SELLER',   'ACTIVE', NOW(), NOW()),
('u-cust-001',  'annguyen@gmail.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'An Nguyễn',         '0905678901', 'CUSTOMER', 'ACTIVE', NOW(), NOW()),
('u-cust-002',  'binhle@gmail.com',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bình Lê',           '0906789012', 'CUSTOMER', 'ACTIVE', NOW(), NOW()),
('u-cust-003',  'chitran@gmail.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Chi Trần',          '0907890123', 'CUSTOMER', 'ACTIVE', NOW(), NOW()),
('u-cust-004',  'dungpham@gmail.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dũng Phạm',         '0908901234', 'CUSTOMER', 'ACTIVE', NOW(), NOW()),
('u-cust-005',  'emhoang@gmail.com',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Em Hoàng',          '0909012345', 'CUSTOMER', 'ACTIVE', NOW(), NOW());

-- ===================== SELLERS =====================
INSERT INTO sellers (id, user_id, shop_name, shop_description, shop_phone, shop_email, province, district, verification_status, rating, total_products, total_followers, total_orders, response_rate, on_time_delivery_rate, created_at, updated_at) VALUES
('s-001','u-seller-01','Thế Giới Di Động Official',    'Shop điện thoại, laptop chính hãng uy tín số 1 Việt Nam',         '0901234567','thegioididong@gmail.com', 'Hồ Chí Minh','Quận 10',      'VERIFIED', 4.90, 0, 15230, 0, 98.50, 99.10, NOW(), NOW()),
('s-002','u-seller-02','Fashion Việt Nam',              'Thời trang nam nữ cao cấp, hàng nhập khẩu chính hãng',            '0902345678','fashionvn@gmail.com',     'Hà Nội',     'Quận Đống Đa','VERIFIED', 4.80, 0, 9870,  0, 97.20, 98.50, NOW(), NOW()),
('s-003','u-seller-03','Nhà Đẹp Store',                'Đồ gia dụng, nội thất, trang trí nhà cửa chất lượng cao',         '0903456789','homegarden@gmail.com',    'Hồ Chí Minh','Quận 7',       'VERIFIED', 4.75, 0, 6540,  0, 96.80, 97.90, NOW(), NOW()),
('s-004','u-seller-04','Beauty & Cosmetic VIP',         'Mỹ phẩm chính hãng Korea, Nhật, Pháp - cam kết hàng auth 100%',  '0904567890','cosmeticvip@gmail.com',   'Hà Nội',     'Quận Ba Đình','VERIFIED', 4.85, 0, 12100, 0, 98.90, 99.50, NOW(), NOW());

-- ===================== CATEGORIES =====================
INSERT INTO categories (id, name, slug, description, display_order, is_active, created_at, updated_at) VALUES
('cat-dienthoai',  'Điện Thoại & Phụ Kiện',     'dien-thoai-phu-kien',    'Điện thoại thông minh và phụ kiện',              1,  TRUE, NOW(), NOW()),
('cat-laptop',     'Máy Tính & Laptop',           'may-tinh-laptop',        'Laptop, máy tính bảng, phụ kiện máy tính',       2,  TRUE, NOW(), NOW()),
('cat-thoitrangnam','Thời Trang Nam',              'thoi-trang-nam',         'Quần áo, giày dép, phụ kiện nam',                3,  TRUE, NOW(), NOW()),
('cat-thoitrangnu', 'Thời Trang Nữ',              'thoi-trang-nu',          'Quần áo, giày dép, túi xách nữ',                 4,  TRUE, NOW(), NOW()),
('cat-giadung',    'Đồ Gia Dụng & Nội Thất',      'do-gia-dung-noi-that',   'Đồ dùng nhà bếp, nội thất, trang trí',           5,  TRUE, NOW(), NOW()),
('cat-mytham',     'Làm Đẹp & Chăm Sóc Da',       'lam-dep-cham-soc-da',    'Mỹ phẩm, dưỡng da, trang điểm',                  6,  TRUE, NOW(), NOW()),
('cat-theothao',   'Thể Thao & Du Lịch',           'the-thao-du-lich',       'Dụng cụ thể thao, đồ du lịch, outdoor',          7,  TRUE, NOW(), NOW()),
('cat-sachvanphong','Sách & Văn Phòng Phẩm',      'sach-van-phong-pham',    'Sách giáo khoa, kỹ năng, văn phòng phẩm',        8,  TRUE, NOW(), NOW()),
('cat-thucpham',   'Thực Phẩm & Đồ Uống',         'thuc-pham-do-uong',      'Thực phẩm sạch, đồ uống, snack',                 9,  TRUE, NOW(), NOW()),
('cat-dochoi',     'Đồ Chơi & Mẹ & Bé',           'do-choi-me-be',          'Đồ chơi trẻ em, sản phẩm mẹ và bé',             10, TRUE, NOW(), NOW());

-- ===================== PRODUCTS =====================
INSERT INTO products (id, seller_id, category_id, name, description, sku, price, compare_price, quantity, status, rating, total_reviews, total_sold, total_views, is_featured, created_at, updated_at) VALUES
('pd-iphone15pm', 's-001','cat-dienthoai','iPhone 15 Pro Max 256GB', 'iPhone 15 Pro Max chip A17 Pro, camera 48MP...', 'IP15PM-256', 29990000, 32990000, 50, 'ACTIVE', 4.9, 245, 1280, 8900, TRUE, NOW(), NOW()),
('pd-samsung-s24u','s-001','cat-dienthoai','Samsung Galaxy S24 Ultra', 'Samsung S24 Ultra bút S Pen, camera 200MP...', 'SS-S24U-256', 26990000, 29990000, 45, 'ACTIVE', 4.8, 187, 950, 7200, TRUE, NOW(), NOW()),
('pd-giay-nike-af1','s-002','cat-thoitrangnam','Nike Air Force 1', 'Nike Air Force 1 chính hãng Nike Việt Nam...', 'NIKE-AF1-WHT', 2190000, 2590000, 200, 'ACTIVE', 4.9, 342, 1890, 13500, TRUE, NOW(), NOW()),
('pd-serum-skii','s-004','cat-mytham','SK-II Facial Treatment Essence', 'Nước thần SK-II huyền thoại...', 'SKII-FTE-230', 3290000, 3690000, 80, 'ACTIVE', 4.9, 267, 1340, 14500, TRUE, NOW(), NOW());

-- ===================== PRODUCT IMAGES =====================
INSERT INTO product_images (id, product_id, image_url, alt_text, display_order, is_primary, created_at, updated_at) VALUES
('img-ip15pm-1', 'pd-iphone15pm', 'https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-max-blue-thumb-600x600.jpg', 'iPhone 15 Pro Max Blue', 1, TRUE, NOW(), NOW()),
('img-af1-1', 'pd-giay-nike-af1', 'https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/3396ee3c-08cc-4ada-baa9-655af12e3120/air-force-1-07-shoes-Wr0Q19.png', 'Nike Air Force 1', 1, TRUE, NOW(), NOW());

-- ===================== ORDERS & ITEMS =====================
INSERT INTO orders (id, order_number, customer_id, seller_id, status, subtotal, shipping_fee, discount_amount, total_price, final_total, payment_method, payment_status, shipping_status, created_at, updated_at) VALUES
('ord-001', 'ORD-20240320-1001', 'u-cust-001', 's-001', 'DELIVERED', 29990000.00, 50000.00, 100000.00, 29990000.00, 29940000.00, 'momo', 'PAID', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO order_items (id, order_id, product_id, variant_id, unit_price, quantity, total_price, created_at, updated_at) VALUES
('oi-001', 'ord-001', 'pd-iphone15pm', NULL, 29990000.00, 1, 29990000.00, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO order_timeline (id, order_id, status, note, created_at, updated_at) VALUES
('tl-ord1-1', 'ord-001', 'PENDING', 'Đơn hàng đã được đặt', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('tl-ord1-2', 'ord-001', 'CONFIRMED', 'Người bán đã xác nhận', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('tl-ord1-3', 'ord-001', 'DELIVERED', 'Đã giao hàng thành công', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ===================== FINAL UPDATES =====================
UPDATE sellers SET total_products = (SELECT COUNT(*) FROM products WHERE seller_id = sellers.id AND status = 'ACTIVE');
UPDATE sellers SET total_orders = (SELECT COUNT(*) FROM orders WHERE seller_id = sellers.id);
