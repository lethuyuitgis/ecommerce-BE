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
-- Password: 123456 (BCrypt)
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
('s-001','u-seller-01','Thế Giới Di Động Official',    'Shop điện thoại, laptop chính hãng uy tín số 1 Việt Nam',         '0901234567','thegioididong@gmail.com', 'Hồ Chí Minh','Quận 10',      'VERIFIED', 4.90, 0, 15230, 8920, 98.50, 99.10, NOW(), NOW()),
('s-002','u-seller-02','Fashion Việt Nam',              'Thời trang nam nữ cao cấp, hàng nhập khẩu chính hãng',            '0902345678','fashionvn@gmail.com',     'Hà Nội',     'Quận Đống Đa','VERIFIED', 4.80, 0, 9870,  5430, 97.20, 98.50, NOW(), NOW()),
('s-003','u-seller-03','Nhà Đẹp Store',                'Đồ gia dụng, nội thất, trang trí nhà cửa chất lượng cao',         '0903456789','homegarden@gmail.com',    'Hồ Chí Minh','Quận 7',       'VERIFIED', 4.75, 0, 6540,  3210, 96.80, 97.90, NOW(), NOW()),
('s-004','u-seller-04','Beauty & Cosmetic VIP',         'Mỹ phẩm chính hãng Korea, Nhật, Pháp - cam kết hàng auth 100%',  '0904567890','cosmeticvip@gmail.com',   'Hà Nội',     'Quận Ba Đình','VERIFIED', 4.85, 0, 12100, 6780, 98.90, 99.50, NOW(), NOW());

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

-- ===================== SHIPPING & PAYMENT METHODS =====================
INSERT INTO shipping_methods (id, code, name, description, is_active, min_delivery_days, max_delivery_days, created_at, updated_at) VALUES
('ship-001','ghn',           'Giao Hàng Nhanh',       'Giao hàng trong 1-2 ngày', TRUE, 1, 2, NOW(), NOW()),
('ship-002','ghtk',          'Giao Hàng Tiết Kiệm',   'Giao hàng 2-4 ngày giá rẻ', TRUE, 2, 4, NOW(), NOW()),
('ship-003','jnt',           'J&T Express',            'Giao hàng toàn quốc', TRUE, 2, 5, NOW(), NOW()),
('ship-004','shopee_express','Shopee Express',          'Giao hàng siêu tốc', TRUE, 1, 3, NOW(), NOW());

INSERT INTO payment_methods (id, method_code, method_name, description, is_active, created_at, updated_at) VALUES
('pay-001','cod',      'Thanh toán khi nhận hàng','Trả tiền mặt khi nhận',                          TRUE, NOW(), NOW()),
('pay-002','transfer', 'Chuyển khoản ngân hàng',  'Chuyển khoản qua Internet Banking/ATM',           TRUE, NOW(), NOW()),
('pay-003','momo',     'Ví MoMo',                 'Thanh toán qua ví điện tử MoMo',                  TRUE, NOW(), NOW()),
('pay-004','zalopay',  'ZaloPay',                 'Thanh toán qua ZaloPay',                           TRUE, NOW(), NOW()),
('pay-005','vnpay',    'VNPay',                   'Thanh toán qua cổng VNPay',                        TRUE, NOW(), NOW());

-- ===================== USER ADDRESSES =====================
INSERT INTO user_addresses (id, user_id, address_type, full_name, phone, province, district, ward, street, is_default, created_at) VALUES
('addr-001','u-cust-001','HOME',   'An Nguyễn','0905678901','Hồ Chí Minh','Quận 1',      'Phường Bến Nghé', '12 Nguyễn Huệ',         TRUE,  NOW()),
('addr-002','u-cust-002','HOME',   'Bình Lê',  '0906789012','Hà Nội',     'Cầu Giấy',    'Phường Dịch Vọng','45 Trần Duy Hưng',      TRUE,  NOW()),
('addr-003','u-cust-003','HOME',   'Chi Trần', '0907890123','Đà Nẵng',    'Hải Châu',    'Phường Hải Châu 1','88 Trần Phú',          TRUE,  NOW()),
('addr-004','u-cust-004','HOME',   'Dũng Phạm','0908901234','Hồ Chí Minh','Quận Bình Thạnh','Phường 25',   '99 Đinh Bộ Lĩnh',       TRUE,  NOW()),
('addr-005','u-cust-005','HOME',   'Em Hoàng', '0909012345','Hà Nội',     'Đống Đa',     'Phường Láng Hạ',  '35 Láng Hạ',           TRUE,  NOW());
