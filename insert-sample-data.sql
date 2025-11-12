-- Script to insert sample data into database
-- Run this AFTER all tables have been created by Hibernate
-- Make sure to run drop-all-tables.sql first, then restart Spring Boot app, then run this script

USE shopcuathuy;

-- Insert Users
-- Password for all users: 123456 (BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
INSERT INTO users (id, email, password_hash, full_name, phone, user_type, status, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111', 'admin@shopcuathuy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', '0900000000', 'ADMIN', 'ACTIVE', NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'seller1@shopcuathuy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Văn Bán', '0901111111', 'SELLER', 'ACTIVE', NOW(), NOW()),
('33333333-3333-3333-3333-333333333333', 'seller2@shopcuathuy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Thị Bán', '0902222222', 'SELLER', 'ACTIVE', NOW(), NOW()),
('44444444-4444-4444-4444-444444444444', 'customer1@shopcuathuy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Văn Mua', '0903333333', 'CUSTOMER', 'ACTIVE', NOW(), NOW()),
('55555555-5555-5555-5555-555555555555', 'customer2@shopcuathuy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Thị Mua', '0904444444', 'CUSTOMER', 'ACTIVE', NOW(), NOW());

-- Insert Categories (26 categories)
INSERT INTO categories (id, name, slug, description, icon, cover_image, display_order, is_active, created_at, updated_at) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Thời Trang Nam', 'thoi-trang-nam', 'Quần áo, phụ kiện thời trang nam', NULL, 'https://cf.shopee.vn/file/687f3967b7c2fe6a134a2c1189be1264_tn', 1, TRUE, NOW(), NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Điện Thoại & Phụ Kiện', 'dien-thoai-phu-kien', 'Điện thoại thông minh và phụ kiện', NULL, 'https://cf.shopee.vn/file/31234a27876fb89cd522d7e3db1ba5ca_tn', 2, TRUE, NOW(), NOW()),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Thiết Bị Điện Tử', 'thiet-bi-dien-tu', 'Thiết bị điện tử, TV, loa', NULL, 'https://cf.shopee.vn/file/978b9e4e61e5052402e023533bfb8555_tn', 3, TRUE, NOW(), NOW()),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Máy Tính & Laptop', 'may-tinh-laptop', 'Máy tính, laptop, phụ kiện', NULL, 'https://cf.shopee.vn/file/c3f3edfaa9f6dafaf482a645d8d6ac57_tn', 4, TRUE, NOW(), NOW()),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Máy Ảnh & Máy Quay Phim', 'may-anh-may-quay-phim', 'Máy ảnh, máy quay phim, ống kính', NULL, 'https://cf.shopee.vn/file/ec14dd4fc238e676e43be2a911414d4d_tn', 5, TRUE, NOW(), NOW()),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Đồng Hồ', 'dong-ho', 'Đồng hồ nam, nữ, thông minh', NULL, 'https://cf.shopee.vn/file/86c294aae72ca1dc5f74d5d2e6b19226_tn', 6, TRUE, NOW(), NOW()),
('gggggggg-gggg-gggg-gggg-gggggggggggg', 'Giày Dép Nam', 'giay-dep-nam', 'Giày dép nam các loại', NULL, 'https://cf.shopee.vn/file/74ca517e1fa74dc4d974e5d03c3139de_tn', 7, TRUE, NOW(), NOW()),
('hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh', 'Thiết Bị Điện Gia Dụng', 'thiet-bi-dien-gia-dung', 'Đồ điện gia dụng, thiết bị nhà bếp', NULL, 'https://cf.shopee.vn/file/7abfbfee3c4844652b4a8245b47340d4_tn', 8, TRUE, NOW(), NOW()),
('iiiiiiii-iiii-iiii-iiii-iiiiiiiiiiii', 'Thể Thao & Du Lịch', 'the-thao-du-lich', 'Đồ thể thao, du lịch, ngoài trời', NULL, 'https://cf.shopee.vn/file/6cb7e633a8e4e00086e26ba824575a82_tn', 9, TRUE, NOW(), NOW()),
('jjjjjjjj-jjjj-jjjj-jjjj-jjjjjjjjjjjj', 'Ô Tô & Xe Máy & Xe Đạp', 'o-to-xe-may-xe-dap', 'Phụ kiện ô tô, xe máy, xe đạp', NULL, 'https://cf.shopee.vn/file/3fb459e3449905545701b418e8220334_tn', 10, TRUE, NOW(), NOW()),
('kkkkkkkk-kkkk-kkkk-kkkk-kkkkkkkkkkkk', 'Thời Trang Nữ', 'thoi-trang-nu', 'Quần áo, phụ kiện thời trang nữ', NULL, 'https://cf.shopee.vn/file/75ea42f9eca124e9cb3cd453bd6be5f4_tn', 11, TRUE, NOW(), NOW()),
('llllllll-llll-llll-llll-llllllllllll', 'Mẹ & Bé', 'me-be', 'Đồ dùng cho mẹ và bé', NULL, 'https://cf.shopee.vn/file/099edde1ab31df35bc255912bab54b75_tn', 12, TRUE, NOW(), NOW()),
('mmmmmmmm-mmmm-mmmm-mmmm-mmmmmmmmmmmm', 'Nhà Cửa & Đời Sống', 'nha-cua-doi-song', 'Đồ dùng nhà cửa, nội thất', NULL, 'https://cf.shopee.vn/file/24b194a695ea59d384768b7d471b02e8_tn', 13, TRUE, NOW(), NOW()),
('nnnnnnnn-nnnn-nnnn-nnnn-nnnnnnnnnnnn', 'Sắc Đẹp', 'sac-dep', 'Mỹ phẩm, làm đẹp', NULL, 'https://cf.shopee.vn/file/ef1f336ecc6f97b790d5aae9916dcb72_tn', 14, TRUE, NOW(), NOW()),
('oooooooo-oooo-oooo-oooo-oooooooooooo', 'Sức Khỏe', 'suc-khoe', 'Thực phẩm chức năng, chăm sóc sức khỏe', NULL, 'https://cf.shopee.vn/file/49119e892a44f01326fefb5d80e46ef7_tn', 15, TRUE, NOW(), NOW()),
('pppppppp-pppp-pppp-pppp-pppppppppppp', 'Giày Dép Nữ', 'giay-dep-nu', 'Giày dép nữ các loại', NULL, 'https://cf.shopee.vn/file/48630b7c76a2b62ba72a1e4b6a9f6e93_tn', 16, TRUE, NOW(), NOW()),
('qqqqqqqq-qqqq-qqqq-qqqq-qqqqqqqqqqqq', 'Túi Ví Nữ', 'tui-vi-nu', 'Túi xách, ví nữ', NULL, 'https://cf.shopee.vn/file/cc6b6e4c933a77c2b300e58f00a71552_tn', 17, TRUE, NOW(), NOW()),
('rrrrrrrr-rrrr-rrrr-rrrr-rrrrrrrrrrrr', 'Phụ Kiện & Trang Sức Nữ', 'phu-kien-trang-suc-nu', 'Trang sức, phụ kiện nữ', NULL, 'https://cf.shopee.vn/file/8d75e78b9cfe5a91c311b154ddc70d4c_tn', 18, TRUE, NOW(), NOW()),
('ssssssss-ssss-ssss-ssss-ssssssssssss', 'Bách Hóa Online', 'bach-hoa-online', 'Thực phẩm, đồ khô, bách hóa', NULL, 'https://cf.shopee.vn/file/36013311815c55d303b0d5c62e1e92bc_tn', 19, TRUE, NOW(), NOW()),
('tttttttt-tttt-tttt-tttt-tttttttttttt', 'Nhà Sách Online', 'nha-sach-online', 'Sách văn học, sách kỹ năng', NULL, 'https://cf.shopee.vn/file/0c0b2441ee74bdf3a0e4d0c42d23f322_tn', 20, TRUE, NOW(), NOW()),
('uuuuuuuu-uuuu-uuuu-uuuu-uuuuuuuuuuuu', 'Balo & Túi Ví Nam', 'balo-tui-vi-nam', 'Balo, túi xách, ví nam', NULL, 'https://cf.shopee.vn/file/9df57ba80f22581d31a2b3efcf3b99f5_tn', 21, TRUE, NOW(), NOW()),
('vvvvvvvv-vvvv-vvvv-vvvv-vvvvvvvvvvvv', 'Đồ Chơi', 'do-choi', 'Đồ chơi trẻ em, đồ chơi giáo dục', NULL, 'https://cf.shopee.vn/file/86c294aae72ca1dc5f74d5d2e6b19226_tn', 22, TRUE, NOW(), NOW()),
('wwwwwwww-wwww-wwww-wwww-wwwwwwwwwwww', 'Chăm Sóc Thú Cưng', 'cham-soc-thu-cung', 'Thức ăn, phụ kiện cho thú cưng', NULL, 'https://cf.shopee.vn/file/65aed915a3fe1e553fa55874b6db5c7e_tn', 23, TRUE, NOW(), NOW()),
('xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx', 'Thời Trang Trẻ Em', 'thoi-trang-tre-em', 'Quần áo, phụ kiện trẻ em', NULL, 'https://cf.shopee.vn/file/454f532a575d587103a60db616e6f891_tn', 24, TRUE, NOW(), NOW()),
('yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy', 'Giặt Giũ & Chăm Sóc Nhà Cửa', 'giat-giu-cham-soc-nha-cua', 'Nước giặt, chất tẩy rửa, chăm sóc nhà cửa', NULL, 'https://cf.shopee.vn/file/24b194a695ea59d384768b7d471b02e8_tn', 25, TRUE, NOW(), NOW()),
('zzzzzzzz-zzzz-zzzz-zzzz-zzzzzzzzzzzz', 'Voucher & Dịch Vụ', 'voucher-dich-vu', 'Voucher, mã giảm giá, dịch vụ', NULL, 'https://cf.shopee.vn/file/3fb459e3449905545701b418e8220334_tn', 26, TRUE, NOW(), NOW());

-- Insert Sellers
INSERT INTO sellers (id, user_id, shop_name, shop_description, shop_phone, shop_email, province, district, verification_status, rating, total_products, total_followers, total_orders, created_at, updated_at) VALUES
('22222222-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'Shop Điện Tử ABC', 'Chuyên bán điện thoại, laptop chính hãng', '0901111111', 'seller1@shopcuathuy.com', 'Hồ Chí Minh', 'Quận 1', 'VERIFIED', 4.5, 0, 0, 0, NOW(), NOW()),
('33333333-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 'Shop Thời Trang XYZ', 'Quần áo thời trang giá rẻ', '0902222222', 'seller2@shopcuathuy.com', 'Hà Nội', 'Quận Hoàn Kiếm', 'VERIFIED', 4.8, 0, 0, 0, NOW(), NOW());

-- Insert Products
INSERT INTO products (id, seller_id, category_id, name, description, sku, price, compare_price, quantity, status, rating, total_reviews, total_sold, total_views, is_featured, created_at, updated_at) VALUES
('pppppppp-1111-1111-1111-111111111111', '22222222-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'iPhone 15 Pro Max', 'iPhone 15 Pro Max 256GB chính hãng Apple', 'IPHONE15PM256', 29990000, 32990000, 50, 'ACTIVE', 4.8, 120, 500, 2000, TRUE, NOW(), NOW()),
('pppppppp-2222-2222-2222-222222222222', '22222222-1111-1111-1111-111111111111', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'MacBook Pro M3', 'MacBook Pro 14 inch M3 chip, 16GB RAM, 512GB SSD', 'MBP14M3', 45990000, 49990000, 30, 'ACTIVE', 4.9, 85, 200, 1500, TRUE, NOW(), NOW()),
('pppppppp-3333-3333-3333-333333333333', '33333333-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Áo Thun Nam Cổ Tròn', 'Áo thun nam chất liệu cotton 100%, nhiều màu', 'AOTHUN001', 199000, 299000, 100, 'ACTIVE', 4.5, 200, 800, 3000, TRUE, NOW(), NOW()),
('pppppppp-4444-4444-4444-444444444444', '33333333-1111-1111-1111-111111111111', 'gggggggg-gggg-gggg-gggg-gggggggggggg', 'Giày Thể Thao Nike Air Max', 'Giày thể thao Nike Air Max chính hãng', 'NIKEAM001', 2490000, 2990000, 60, 'ACTIVE', 4.7, 150, 400, 2500, TRUE, NOW(), NOW()),
('pppppppp-5555-5555-5555-555555555555', '22222222-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Samsung Galaxy S24 Ultra', 'Samsung Galaxy S24 Ultra 256GB, camera 200MP', 'SGS24U256', 24990000, 27990000, 40, 'ACTIVE', 4.6, 95, 300, 1800, TRUE, NOW(), NOW()),
('pppppppp-6666-6666-6666-666666666666', '22222222-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Xiaomi 14 Pro', 'Xiaomi 14 Pro 256GB, camera Leica', 'XIAOMI14P256', 19990000, 22990000, 35, 'ACTIVE', 4.4, 80, 250, 1200, TRUE, NOW(), NOW()),
('pppppppp-7777-7777-7777-777777777777', '33333333-1111-1111-1111-111111111111', 'kkkkkkkk-kkkk-kkkk-kkkk-kkkkkkkkkkkk', 'Áo Sơ Mi Nam', 'Áo sơ mi nam công sở, chất liệu cotton', 'AOSOMI001', 299000, 399000, 80, 'ACTIVE', 4.6, 150, 600, 2200, TRUE, NOW(), NOW()),
('pppppppp-8888-8888-8888-888888888888', '33333333-1111-1111-1111-111111111111', 'pppppppp-pppp-pppp-pppp-pppppppppppp', 'Giày Adidas Ultraboost', 'Giày thể thao Adidas Ultraboost chính hãng', 'ADIDASUB001', 2990000, 3490000, 45, 'ACTIVE', 4.8, 120, 350, 1800, TRUE, NOW(), NOW()),
('pppppppp-9999-9999-9999-999999999999', '22222222-1111-1111-1111-111111111111', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Dell XPS 15', 'Dell XPS 15 inch, Intel i7, 16GB RAM, 512GB SSD', 'DELLXPS15', 39990000, 44990000, 25, 'ACTIVE', 4.7, 70, 150, 1000, TRUE, NOW(), NOW()),
('pppppppp-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Quần Jean Nam', 'Quần jean nam form slim, nhiều size', 'QUANJEAN001', 399000, 499000, 90, 'ACTIVE', 4.5, 180, 700, 2800, TRUE, NOW(), NOW());

-- Insert Product Images
INSERT INTO product_images (id, product_id, image_url, alt_text, display_order, is_primary, created_at, updated_at) VALUES
('iiiiiiii-1111-1111-1111-111111111111', 'pppppppp-1111-1111-1111-111111111111', 'https://via.placeholder.com/600x600?text=iPhone+15+Pro+Max', 'iPhone 15 Pro Max', 1, TRUE, NOW(), NOW()),
('iiiiiiii-2222-2222-2222-222222222222', 'pppppppp-2222-2222-2222-222222222222', 'https://via.placeholder.com/600x600?text=MacBook+Pro+M3', 'MacBook Pro M3', 1, TRUE, NOW(), NOW()),
('iiiiiiii-3333-3333-3333-333333333333', 'pppppppp-3333-3333-3333-333333333333', 'https://via.placeholder.com/600x600?text=Ao+Thun+Nam', 'Áo Thun Nam', 1, TRUE, NOW(), NOW()),
('iiiiiiii-4444-4444-4444-444444444444', 'pppppppp-4444-4444-4444-444444444444', 'https://via.placeholder.com/600x600?text=Nike+Air+Max', 'Nike Air Max', 1, TRUE, NOW(), NOW()),
('iiiiiiii-5555-5555-5555-555555555555', 'pppppppp-5555-5555-5555-555555555555', 'https://via.placeholder.com/600x600?text=Samsung+S24+Ultra', 'Samsung S24 Ultra', 1, TRUE, NOW(), NOW()),
('iiiiiiii-6666-6666-6666-666666666666', 'pppppppp-6666-6666-6666-666666666666', 'https://via.placeholder.com/600x600?text=Xiaomi+14+Pro', 'Xiaomi 14 Pro', 1, TRUE, NOW(), NOW()),
('iiiiiiii-7777-7777-7777-777777777777', 'pppppppp-7777-7777-7777-777777777777', 'https://via.placeholder.com/600x600?text=Ao+So+Mi+Nam', 'Áo Sơ Mi Nam', 1, TRUE, NOW(), NOW()),
('iiiiiiii-8888-8888-8888-888888888888', 'pppppppp-8888-8888-8888-888888888888', 'https://via.placeholder.com/600x600?text=Adidas+Ultraboost', 'Adidas Ultraboost', 1, TRUE, NOW(), NOW()),
('iiiiiiii-9999-9999-9999-999999999999', 'pppppppp-9999-9999-9999-999999999999', 'https://via.placeholder.com/600x600?text=Dell+XPS+15', 'Dell XPS 15', 1, TRUE, NOW(), NOW()),
('iiiiiiii-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'pppppppp-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'https://via.placeholder.com/600x600?text=Quan+Jean+Nam', 'Quần Jean Nam', 1, TRUE, NOW(), NOW());

-- Insert Shipping Methods
INSERT INTO shipping_methods (id, code, name, description, is_active, min_delivery_days, max_delivery_days, created_at, updated_at) VALUES
('ssssssss-1111-1111-1111-111111111111', 'ghn', 'Giao Hàng Nhanh', 'Giao hàng nhanh trong 24-48 giờ', TRUE, 1, 2, NOW(), NOW()),
('ssssssss-2222-2222-2222-222222222222', 'shopee_express', 'Shopee Express', 'Giao hàng nhanh của Shopee', TRUE, 1, 3, NOW(), NOW()),
('ssssssss-3333-3333-3333-333333333333', 'ahamove', 'Ahamove', 'Giao hàng siêu tốc trong ngày', TRUE, 0, 1, NOW(), NOW()),
('ssssssss-4444-4444-4444-444444444444', 'jnt', 'J&T Express', 'Giao hàng tiết kiệm', TRUE, 2, 5, NOW(), NOW());

-- Insert Payment Methods
INSERT INTO payment_methods (id, method_code, method_name, description, is_active, created_at, updated_at) VALUES
('mmmmmmmm-1111-1111-1111-111111111111', 'cod', 'Thanh toán khi nhận hàng', 'Thanh toán bằng tiền mặt khi nhận hàng', TRUE, NOW(), NOW()),
('mmmmmmmm-2222-2222-2222-222222222222', 'transfer', 'Chuyển khoản ngân hàng', 'Chuyển khoản qua tài khoản ngân hàng', TRUE, NOW(), NOW()),
('mmmmmmmm-3333-3333-3333-333333333333', 'momo', 'Ví MoMo', 'Thanh toán qua ví điện tử MoMo', TRUE, NOW(), NOW()),
('mmmmmmmm-4444-4444-4444-444444444444', 'zalopay', 'ZaloPay', 'Thanh toán qua ví điện tử ZaloPay', TRUE, NOW(), NOW());

-- Insert User Addresses
INSERT INTO user_addresses (id, user_id, address_type, full_name, phone, province, district, ward, street, is_default, created_at) VALUES
('aaaa1111-1111-1111-1111-111111111111', '44444444-4444-4444-4444-444444444444', 'HOME', 'Lê Văn Mua', '0903333333', 'Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '123 Đường Nguyễn Huệ', TRUE, NOW()),
('bbbb1111-1111-1111-1111-111111111111', '55555555-5555-5555-5555-555555555555', 'HOME', 'Phạm Thị Mua', '0904444444', 'Hà Nội', 'Quận Hoàn Kiếm', 'Phường Hàng Bông', '456 Phố Hàng Gai', TRUE, NOW());

-- Update seller total_products count
UPDATE sellers SET total_products = (SELECT COUNT(*) FROM products WHERE seller_id = sellers.id);
