-- ===================== PROMOTIONS & VOUCHERS =====================
INSERT INTO promotions (id, seller_id, name, description, promotion_type, discount_value, start_date, end_date, status, created_at, updated_at) VALUES
('promo-001', 's-001', 'Sale Hè Điện Tử', 'Giảm giá 10% toàn bộ sản phẩm Apple', 'PERCENTAGE', 10.00, DATEADD('DAY', -1, NOW()), DATEADD('DAY', 30, NOW()), 'ACTIVE', NOW(), NOW()),
('promo-002', 's-002', 'Thời Trang Hè', 'Mua nhiều giảm sâu', 'PERCENTAGE', 15.00, DATEADD('DAY', -1, NOW()), DATEADD('DAY', 30, NOW()), 'ACTIVE', NOW(), NOW());

INSERT INTO vouchers (id, seller_id, code, description, discount_type, discount_value, start_date, end_date, min_purchase_amount, max_discount, total_uses_limit, total_uses, status, created_at, updated_at) VALUES
('vc-001', 's-001', 'TECH100', 'Giảm 100k cho đơn từ 2 triệu', 'FIXED_AMOUNT', 100000.00, DATEADD('DAY', -1, NOW()), DATEADD('DAY', 30, NOW()), 2000000.00, 100000.00, 100, 0, 'ACTIVE', NOW(), NOW()),
('vc-002', 's-002', 'FASHION20', 'Giảm 20% cho đơn từ 500k', 'PERCENTAGE', 20.00, DATEADD('DAY', -1, NOW()), DATEADD('DAY', 30, NOW()), 500000.00, 200000.00, 50, 0, 'ACTIVE', NOW(), NOW());

-- ===================== REVIEWS =====================
INSERT INTO product_reviews (id, product_id, customer_id, rating, title, comment, status, helpful_count, created_at, updated_at) VALUES
('rev-001', 'pd-iphone15pm', 'u-cust-001', 5, 'Quá tuyệt vời', 'Sản phẩm chính hãng, giao hàng nhanh, dùng cực kỳ mượt.', 'APPROVED', 10, NOW(), NOW()),
('rev-002', 'pd-iphone15pm', 'u-cust-002', 4, 'Hơi đắt nhưng đáng tiền', 'Màu Titan tự nhiên rất đẹp, cầm nhẹ hơn đời cũ.', 'APPROVED', 5, NOW(), NOW()),
('rev-003', 'pd-giay-nike-af1', 'u-cust-003', 5, 'Giày đẹp', 'Form chuẩn, đi êm chân, đúng hàng chính hãng.', 'APPROVED', 8, NOW(), NOW());
