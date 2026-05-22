USE shopcuathuy;
SET NAMES 'utf8mb4';

-- ===================== PRODUCT IMAGES =====================
INSERT INTO product_images (id, product_id, image_url, alt_text, display_order, is_primary, created_at, updated_at) VALUES
-- iPhone 15 Pro Max
('img-ip15pm-1', 'pd-iphone15pm', 'https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-max-blue-thumb-600x600.jpg', 'iPhone 15 Pro Max Blue', 1, TRUE, NOW(), NOW()),
('img-ip15pm-2', 'pd-iphone15pm', 'https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-max-titan-tu-nhien-1-600x600.jpg', 'iPhone 15 Pro Max Natural Titanium', 2, FALSE, NOW(), NOW()),

-- Samsung S24 Ultra
('img-s24u-1', 'pd-samsung-s24u', 'https://cdn.tgdd.vn/Products/Images/42/307174/samsung-galaxy-s24-ultra-grey-600x600.jpg', 'Samsung S24 Ultra Grey', 1, TRUE, NOW(), NOW()),

-- MacBook Pro
('img-mbp-1', 'pd-macbook-pro14', 'https://cdn.tgdd.vn/Products/Images/44/318231/apple-macbook-pro-14-inch-m3-pro-600x600.jpg', 'MacBook Pro 14 M3 Pro', 1, TRUE, NOW(), NOW()),

-- Polo Lacoste
('img-polo-1', 'pd-polo-lacoste', 'https://images.lacoste.com/is/image/Lacoste/L1212_001_24?wid=1250&hei=1250&fmt=jpeg&qlt=85,0&resMode=sharp2&op_usm=0.8,1,5,0', 'Polo Lacoste White', 1, TRUE, NOW(), NOW()),

-- Nike AF1
('img-af1-1', 'pd-giay-nike-af1', 'https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/3396ee3c-08cc-4ada-baa9-655af12e3120/air-force-1-07-shoes-Wr0Q19.png', 'Nike Air Force 1 All White', 1, TRUE, NOW(), NOW()),

-- Dam Zara
('img-dam-1', 'pd-dam-zara', 'https://static.zara.net/photos///2024/V/0/1/p/2183/040/052/2/w/850/2183040052_6_1_1.jpg', 'Zara Dress', 1, TRUE, NOW(), NOW()),

-- SK-II
('img-skii-1', 'pd-serum-skii', 'https://www.sk-ii.com.vn/wp-content/uploads/2021/08/nuoc-than-sk-ii-facial-treatment-essence-230ml-1.jpg', 'SK-II FTE', 1, TRUE, NOW(), NOW()),

-- Laneige
('img-laneige-1', 'pd-kem-laneige', 'https://www.laneige.com/vn/vi/resource/img/product/sleeping-care/water-sleeping-mask_ex_01.png', 'Laneige Water Sleeping Mask', 1, TRUE, NOW(), NOW());

-- ===================== PRODUCT VARIANTS =====================
INSERT INTO product_variants (id, product_id, variant_name, variant_sku, variant_price, variant_quantity, created_at, updated_at) VALUES
-- iPhone 15 Pro Max Colors
('v-ip15pm-blue', 'pd-iphone15pm', 'Titan Xanh', 'IP15PM-256-BLUE', 29990000, 20, NOW(), NOW()),
('v-ip15pm-nat',  'pd-iphone15pm', 'Titan Tự Nhiên', 'IP15PM-256-NAT', 29990000, 30, NOW(), NOW()),

-- Polo Sizes
('v-polo-s', 'pd-polo-lacoste', 'Size S', 'LAC-POLO-S', 1890000, 30, NOW(), NOW()),
('v-polo-m', 'pd-polo-lacoste', 'Size M', 'LAC-POLO-M', 1890000, 40, NOW(), NOW()),
('v-polo-l', 'pd-polo-lacoste', 'Size L', 'LAC-POLO-L', 1890000, 50, NOW(), NOW()),

-- Nike AF1 Sizes
('v-af1-40', 'pd-giay-nike-af1', 'Size 40', 'NIKE-AF1-40', 2190000, 50, NOW(), NOW()),
('v-af1-41', 'pd-giay-nike-af1', 'Size 41', 'NIKE-AF1-41', 2190000, 50, NOW(), NOW()),
('v-af1-42', 'pd-giay-nike-af1', 'Size 42', 'NIKE-AF1-42', 2190000, 50, NOW(), NOW()),
('v-af1-43', 'pd-giay-nike-af1', 'Size 43', 'NIKE-AF1-43', 2190000, 50, NOW(), NOW());

-- ===================== PROMOTIONS & VOUCHERS =====================
INSERT INTO promotions (id, seller_id, name, description, promotion_type, discount_value, start_date, end_date, status, created_at, updated_at) VALUES
('promo-001', 's-001', 'Sale Hè Điện Tử', 'Giảm giá 10% toàn bộ sản phẩm Apple', 'PERCENTAGE', 10.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW()),
('promo-002', 's-002', 'Thời Trang Hè', 'Mua nhiều giảm sâu', 'PERCENTAGE', 15.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW());

INSERT INTO vouchers (id, seller_id, code, description, discount_type, discount_value, start_date, end_date, min_purchase_amount, max_discount, total_uses_limit, total_uses, status, created_at, updated_at) VALUES
('vc-001', 's-001', 'TECH100', 'Giảm 100k cho đơn từ 2 triệu', 'FIXED_AMOUNT', 100000.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 2000000.00, 100000.00, 100, 0, 'ACTIVE', NOW(), NOW()),
('vc-002', 's-002', 'FASHION20', 'Giảm 20% cho đơn từ 500k', 'PERCENTAGE', 20.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 500000.00, 200000.00, 50, 0, 'ACTIVE', NOW(), NOW());

-- ===================== REVIEWS =====================
INSERT INTO product_reviews (id, product_id, customer_id, rating, title, comment, status, helpful_count, created_at, updated_at) VALUES
('rev-001', 'pd-iphone15pm', 'u-cust-001', 5, 'Quá tuyệt vời', 'Sản phẩm chính hãng, giao hàng nhanh, dùng cực kỳ mượt.', 'APPROVED', 10, NOW(), NOW()),
('rev-002', 'pd-iphone15pm', 'u-cust-002', 4, 'Hơi đắt nhưng đáng tiền', 'Màu Titan tự nhiên rất đẹp, cầm nhẹ hơn đời cũ.', 'APPROVED', 5, NOW(), NOW()),
('rev-003', 'pd-giay-nike-af1', 'u-cust-003', 5, 'Giày đẹp', 'Form chuẩn, đi êm chân, đúng hàng chính hãng.', 'APPROVED', 8, NOW(), NOW());
