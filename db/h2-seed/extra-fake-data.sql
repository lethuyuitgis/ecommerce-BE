-- =================================================================
-- Fake data cho các bảng còn trống (H2 MODE=MySQL)
-- =================================================================

-- ===================== SHIPPING PARTNERS =====================
INSERT INTO shipping_partners (id, partner_code, partner_name, partner_type, contact_phone, contact_email, api_key, api_secret, is_active, created_at, updated_at) VALUES
('sp-ghn',   'GHN',   'Giao Hàng Nhanh',     'GHN',          '1900545445', 'support@ghn.vn',     'GHN_API_KEY_DEMO',  'GHN_API_SECRET_DEMO',  TRUE, NOW(), NOW()),
('sp-ghtk',  'GHTK',  'Giao Hàng Tiết Kiệm', 'GHTK',         '1900636677', 'support@ghtk.vn',    'GHTK_API_KEY_DEMO', 'GHTK_API_SECRET_DEMO', TRUE, NOW(), NOW()),
('sp-jnt',   'JNT',   'J&T Express',         'STANDARD',     '1900099269', 'support@jtexpress.vn','JNT_API_KEY_DEMO', 'JNT_API_SECRET_DEMO',  TRUE, NOW(), NOW()),
('sp-shopee','SPX',   'Shopee Express',      'IN_HOUSE',     '19001221',   'support@shopee.vn',  'SPX_API_KEY_DEMO',  'SPX_API_SECRET_DEMO',  TRUE, NOW(), NOW()),
('sp-vnpost','VNP',   'Vietnam Post',        'VIETTEL_POST', '18001096',   'support@vnpost.vn',  'VNP_API_KEY_DEMO',  'VNP_API_SECRET_DEMO',  TRUE, NOW(), NOW());

-- ===================== SHIPPING HUBS =====================
INSERT INTO shipping_hubs (id, code, name, hub_type, province, district, address, contact_phone, is_active, created_at, updated_at) VALUES
('hub-hcm-1', 'HCM-Q1',  'Kho Trung Tâm HCM - Quận 1',   'WAREHOUSE',      'Hồ Chí Minh', 'Quận 1',         '123 Lê Lợi, Phường Bến Nghé',         '0281234567', TRUE, NOW(), NOW()),
('hub-hcm-2', 'HCM-Q9',  'Kho Phía Đông HCM - Quận 9',   'SORTING_CENTER', 'Hồ Chí Minh', 'Quận 9',         '456 Đỗ Xuân Hợp, Phường Phước Long B', '0282345678', TRUE, NOW(), NOW()),
('hub-hcm-3', 'HCM-TB',  'Kho Phía Tây HCM - Tân Bình',  'LOCAL_STATION',  'Hồ Chí Minh', 'Quận Tân Bình',  '789 Cộng Hòa, Phường 12',             '0283456789', TRUE, NOW(), NOW()),
('hub-hn-1',  'HN-CG',   'Kho Trung Tâm Hà Nội',         'WAREHOUSE',      'Hà Nội',      'Cầu Giấy',       '88 Trần Duy Hưng, Trung Hoà',         '0241234567', TRUE, NOW(), NOW()),
('hub-hn-2',  'HN-LB',   'Kho Phía Đông Hà Nội',         'SORTING_CENTER', 'Hà Nội',      'Long Biên',      '12 Nguyễn Văn Linh, Sài Đồng',        '0242345678', TRUE, NOW(), NOW()),
('hub-dn-1',  'DN-HC',   'Kho Đà Nẵng',                  'WAREHOUSE',      'Đà Nẵng',     'Hải Châu',       '99 Bạch Đằng, Hải Châu 1',            '0236123456', TRUE, NOW(), NOW()),
('hub-ct-1',  'CT-NK',   'Kho Cần Thơ',                  'LOCAL_STATION',  'Cần Thơ',     'Ninh Kiều',      '55 Nguyễn Trãi, Tân An',              '0292123456', TRUE, NOW(), NOW());

-- ===================== BANNERS =====================
INSERT INTO banners (id, title, image_url, link_url, position, display_order, is_active, created_at, updated_at) VALUES
('bnr-001', 'Sale Hè Cực Đỉnh - Giảm tới 70%',         'https://cdn.shopcuathuy.com/banners/summer-sale.jpg',    '/promotions/summer-sale',    'HOME_TOP',     1, TRUE, NOW(), NOW()),
('bnr-002', 'iPhone 15 Pro Max - Giá tốt chỉ hôm nay', 'https://cdn.shopcuathuy.com/banners/iphone15.jpg',       '/products/pd-iphone15pm',    'HOME_TOP',     2, TRUE, NOW(), NOW()),
('bnr-003', 'Thời Trang Hè - Mua 2 tặng 1',           'https://cdn.shopcuathuy.com/banners/fashion-summer.jpg', '/categories/cat-thoitrangnu','HOME_MIDDLE',  1, TRUE, NOW(), NOW()),
('bnr-004', 'Mỹ Phẩm Hàn Quốc Chính Hãng',           'https://cdn.shopcuathuy.com/banners/k-beauty.jpg',       '/categories/cat-mytham',     'HOME_MIDDLE',  2, TRUE, NOW(), NOW()),
('bnr-005', 'Free Ship Toàn Quốc Đơn Từ 199K',        'https://cdn.shopcuathuy.com/banners/freeship.jpg',       '/promotions/freeship',       'HOME_BOTTOM',  1, TRUE, NOW(), NOW()),
('bnr-006', 'MacBook Pro M3 - Trả Góp 0%',            'https://cdn.shopcuathuy.com/banners/macbook.jpg',        '/products/pd-macbook-pro14', 'CATEGORY_TOP', 1, TRUE, NOW(), NOW());

-- ===================== CART ITEMS =====================
INSERT INTO cart_items (id, user_id, product_id, variant_id, quantity, created_at, updated_at) VALUES
('cart-001', 'u-cust-001', 'pd-samsung-s24u',    NULL,             1, NOW(), NOW()),
('cart-002', 'u-cust-001', 'pd-polo-lacoste',    'v-polo-m',       2, NOW(), NOW()),
('cart-003', 'u-cust-002', 'pd-giay-nike-af1',   'v-af1-42',       1, NOW(), NOW()),
('cart-004', 'u-cust-002', 'pd-serum-skii',      NULL,             1, NOW(), NOW()),
('cart-005', 'u-cust-003', 'pd-dam-zara',        NULL,             1, NOW(), NOW()),
('cart-006', 'u-cust-003', 'pd-kem-laneige',     NULL,             2, NOW(), NOW()),
('cart-007', 'u-cust-004', 'pd-iphone15',        NULL,             1, NOW(), NOW()),
('cart-008', 'u-cust-004', 'pd-noicom-zojirushi',NULL,             1, NOW(), NOW()),
('cart-009', 'u-cust-005', 'pd-tui-coach',       NULL,             1, NOW(), NOW()),
('cart-010', 'u-cust-005', 'pd-quan-jean-levis', NULL,             1, NOW(), NOW());

-- ===================== WISHLIST =====================
INSERT INTO wishlist (id, user_id, product_id, created_at, updated_at) VALUES
('wl-001', 'u-cust-001', 'pd-iphone15pm',       NOW(), NOW()),
('wl-002', 'u-cust-001', 'pd-macbook-pro14',    NOW(), NOW()),
('wl-003', 'u-cust-002', 'pd-dam-zara',         NOW(), NOW()),
('wl-004', 'u-cust-002', 'pd-tui-coach',        NOW(), NOW()),
('wl-005', 'u-cust-003', 'pd-serum-skii',       NOW(), NOW()),
('wl-006', 'u-cust-003', 'pd-kem-laneige',      NOW(), NOW()),
('wl-007', 'u-cust-004', 'pd-asus-zenbook',     NOW(), NOW()),
('wl-008', 'u-cust-004', 'pd-dell-xps15',       NOW(), NOW()),
('wl-009', 'u-cust-005', 'pd-robot-huttbui',    NOW(), NOW()),
('wl-010', 'u-cust-005', 'pd-maylocnuoc-karofi',NOW(), NOW());

-- ===================== PROMOTION ITEMS =====================
INSERT INTO promotion_items (id, promotion_id, product_id, variant_id, created_at, updated_at) VALUES
('pi-001', 'promo-001', 'pd-iphone15pm',     NULL,            NOW(), NOW()),
('pi-002', 'promo-001', 'pd-iphone15',       NULL,            NOW(), NOW()),
('pi-003', 'promo-001', 'pd-macbook-pro14',  NULL,            NOW(), NOW()),
('pi-004', 'promo-002', 'pd-polo-lacoste',   'v-polo-m',      NOW(), NOW()),
('pi-005', 'promo-002', 'pd-dam-zara',       NULL,            NOW(), NOW()),
('pi-006', 'promo-002', 'pd-quan-jean-levis',NULL,            NOW(), NOW());

-- ===================== VOUCHER USAGES =====================
INSERT INTO voucher_usages (id, voucher_id, customer_id, order_id, discount_amount, used_at, created_at, updated_at) VALUES
('vu-001', 'vc-001', 'u-cust-001', 'ord-001', 100000.00, DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW())),
('vu-002', 'vc-002', 'u-cust-002', 'ord-002', 200000.00, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()));

-- ===================== REVIEW IMAGES =====================
INSERT INTO review_images (id, review_id, image_url, created_at, updated_at) VALUES
('ri-001', 'rev-001', 'https://cdn.shopcuathuy.com/reviews/rev-001-img1.jpg', NOW(), NOW()),
('ri-002', 'rev-001', 'https://cdn.shopcuathuy.com/reviews/rev-001-img2.jpg', NOW(), NOW()),
('ri-003', 'rev-002', 'https://cdn.shopcuathuy.com/reviews/rev-002-img1.jpg', NOW(), NOW()),
('ri-004', 'rev-003', 'https://cdn.shopcuathuy.com/reviews/rev-003-img1.jpg', NOW(), NOW()),
('ri-005', 'rev-003', 'https://cdn.shopcuathuy.com/reviews/rev-003-img2.jpg', NOW(), NOW());

-- ===================== PAYMENT TRANSACTIONS =====================
INSERT INTO payment_transactions (id, order_id, payment_method_id, transaction_code, amount, status, bank_code, bank_transaction_id, error_message, created_at, updated_at) VALUES
('pt-001', 'ord-001', 'pay-001', 'TXN-COD-ORD001',    29940000.00, 'COMPLETED', NULL,    NULL,                NULL, DATEADD('DAY', -5, NOW()), DATEADD('DAY', -3, NOW())),
('pt-002', 'ord-002', 'pay-005', 'TXN-VNP-ORD002',     2220000.00, 'COMPLETED', 'NCB',  'VNP1700001234',     NULL, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW())),
('pt-003', 'ord-003', 'pay-003', 'TXN-MOMO-ORD003',    3330000.00, 'COMPLETED', 'MOMO', 'MOMO9876543210',    NULL, DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW()));

-- ===================== SHIPMENTS =====================
INSERT INTO shipments (id, order_id, shipping_method_id, shipping_partner_id, current_hub_id, shipper_id, tracking_number, status, cod_status, cod_amount, shipping_fee, insurance_fee, weight, package_size, sender_name, sender_phone, sender_address, sender_province, sender_district, sender_ward, recipient_name, recipient_phone, recipient_address, recipient_province, recipient_district, recipient_ward, expected_delivery_date, actual_delivery_date, delivery_proof_url, notes, created_at, updated_at) VALUES
('shp-001', 'ord-001', 'ship-001', 'sp-ghn',   'hub-hcm-1', NULL, 'GHN1700000001', 'DELIVERED',   'COLLECTED', 29940000.00, 30000.00, 5000.00, 1.5, 'M', 'Thế Giới Di Động Official', '0901234567', '123 Lê Lợi, Phường Bến Nghé', 'Hồ Chí Minh', 'Quận 10',     'Phường 12',     'An Nguyễn', '0905678901', '12 Nguyễn Huệ',     'Hồ Chí Minh', 'Quận 1',         'Phường Bến Nghé', DATEADD('DAY', -3, CURRENT_DATE), DATEADD('DAY', -3, CURRENT_DATE), 'https://cdn.shopcuathuy.com/shipments/shp-001-proof.jpg', 'Đã giao thành công', DATEADD('DAY', -5, NOW()), DATEADD('DAY', -3, NOW())),
('shp-002', 'ord-002', 'ship-002', 'sp-ghtk',  'hub-hn-1',  NULL, 'GHTK1700000002','IN_TRANSIT',     'NOT_COLLECTED', 2220000.00,  25000.00, 3000.00, 0.8, 'S', 'Fashion Việt Nam',           '0902345678', '45 Trần Duy Hưng',          'Hà Nội',      'Đống Đa',     'Phường Láng Hạ', 'Bình Lê',   '0906789012', '45 Trần Duy Hưng', 'Hà Nội',      'Cầu Giấy',       'Phường Dịch Vọng', DATEADD('DAY', 2, CURRENT_DATE), NULL,                              NULL, 'Đang vận chuyển',     DATEADD('DAY', -2, NOW()), DATEADD('DAY', -1, NOW())),
('shp-003', 'ord-003', 'ship-001', 'sp-ghn',   'hub-dn-1',  NULL, 'GHN1700000003', 'OUT_FOR_DELIVERY','NOT_COLLECTED',3330000.00, 30000.00, 4000.00, 0.5, 'S', 'Cosmetic VIP Store',         '0904567890', '11 Trần Phú',               'Đà Nẵng',     'Hải Châu',    'Hải Châu 1',      'Chi Trần',  '0907890123', '88 Trần Phú',       'Đà Nẵng',     'Hải Châu',       'Phường Hải Châu 1', DATEADD('DAY', 1, CURRENT_DATE), NULL,                              NULL, 'Đang đi giao',         DATEADD('DAY', -1, NOW()), DATEADD('HOUR', -2, NOW()));

-- ===================== TRACKING UPDATES =====================
INSERT INTO tracking_updates (id, shipment_id, status, description, location, timestamp, created_at, updated_at) VALUES
('tu-001', 'shp-001', 'PICKED_UP',    'Đã lấy hàng tại shop',          'Kho HCM - Quận 10',  DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW()), NOW()),
('tu-002', 'shp-001', 'IN_TRANSIT',   'Đang vận chuyển đến bưu cục',   'Kho Trung Tâm HCM',  DATEADD('DAY', -4, NOW()), DATEADD('DAY', -4, NOW()), NOW()),
('tu-003', 'shp-001', 'OUT_FOR_DELIVERY','Shipper đang giao hàng',     'Quận 1, HCM',        DATEADD('DAY', -3, NOW()), DATEADD('DAY', -3, NOW()), NOW()),
('tu-004', 'shp-001', 'DELIVERED',    'Đã giao thành công',            'Quận 1, HCM',        DATEADD('DAY', -3, NOW()), DATEADD('DAY', -3, NOW()), NOW()),
('tu-005', 'shp-002', 'PICKED_UP',    'Đã lấy hàng tại shop',          'Kho HN - Đống Đa',   DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()), NOW()),
('tu-006', 'shp-002', 'IN_TRANSIT',   'Đang trung chuyển',             'Kho Trung Tâm HN',   DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW()), NOW()),
('tu-007', 'shp-003', 'PICKED_UP',    'Đã lấy hàng tại shop',          'Kho ĐN - Hải Châu',  DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW()), NOW()),
('tu-008', 'shp-003', 'OUT_FOR_DELIVERY','Shipper đang giao hàng',     'Hải Châu, ĐN',       DATEADD('HOUR', -2, NOW()), DATEADD('HOUR', -2, NOW()), NOW());

-- ===================== NOTIFICATIONS =====================
INSERT INTO notifications (id, recipient_id, type, title, message, related_id, image_url, link_url, data, is_read, read_at, created_at, updated_at) VALUES
('ntf-001', 'u-cust-001', 'ORDER_STATUS', 'Đơn hàng đã giao thành công', 'Đơn ord-001 (iPhone 15 Pro Max) đã được giao. Mời bạn đánh giá sản phẩm.', 'ord-001', NULL, '/orders/ord-001', NULL, TRUE,  DATEADD('DAY', -2, NOW()), DATEADD('DAY', -3, NOW()), DATEADD('DAY', -2, NOW())),
('ntf-002', 'u-cust-002', 'ORDER_STATUS', 'Đơn hàng đang vận chuyển',    'Đơn ord-002 (Polo Lacoste) đang được vận chuyển. Mã vận đơn: GHTK1700000002', 'ord-002', NULL, '/orders/ord-002', NULL, FALSE, NULL,                       DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),
('ntf-003', 'u-cust-003', 'ORDER_STATUS', 'Đơn hàng sắp giao',           'Shipper sẽ giao đơn ord-003 trong hôm nay. Vui lòng giữ máy điện thoại.', 'ord-003', NULL, '/orders/ord-003', NULL, FALSE, NULL,                       DATEADD('HOUR', -2, NOW()), DATEADD('HOUR', -2, NOW())),
('ntf-004', 'u-cust-001', 'PROMOTION',    'Voucher mới dành cho bạn',    'Sử dụng mã TECH100 để giảm 100k cho đơn từ 2 triệu.', 'vc-001', NULL, '/vouchers', NULL, TRUE, DATEADD('DAY', -4, NOW()), DATEADD('DAY', -5, NOW()), DATEADD('DAY', -4, NOW())),
('ntf-005', 'u-cust-002', 'PROMOTION',    'Sale Thời Trang Hè',          'Giảm tới 15% cho tất cả sản phẩm thời trang.', 'promo-002', NULL, '/promotions/promo-002', NULL, FALSE, NULL, DATEADD('DAY', -3, NOW()), DATEADD('DAY', -3, NOW())),
('ntf-006', 'u-seller-01', 'ORDER_NEW',   'Bạn có đơn hàng mới',         'Khách An Nguyễn vừa đặt đơn ord-001 trị giá 29,940,000đ.', 'ord-001', NULL, '/seller/orders/ord-001', NULL, TRUE,  DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW())),
('ntf-007', 'u-seller-02', 'ORDER_NEW',   'Bạn có đơn hàng mới',         'Khách Bình Lê vừa đặt đơn ord-002 trị giá 2,220,000đ.', 'ord-002', NULL, '/seller/orders/ord-002', NULL, TRUE,  DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW())),
('ntf-008', 'u-seller-04', 'ORDER_NEW',   'Bạn có đơn hàng mới',         'Khách Chi Trần vừa đặt đơn ord-003 trị giá 3,330,000đ.', 'ord-003', NULL, '/seller/orders/ord-003', NULL, FALSE, NULL,                       DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),
('ntf-009', 'u-cust-004', 'PROMOTION',    'Sản phẩm trong wishlist giảm giá', 'ASUS ZenBook trong wishlist của bạn đang sale 15%.', 'pd-asus-zenbook', NULL, '/products/pd-asus-zenbook', NULL, FALSE, NULL, DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),
('ntf-010', 'u-cust-005', 'SYSTEM',       'Bạn có 2 sản phẩm trong giỏ', 'Hoàn tất đặt hàng để không bỏ lỡ các sản phẩm yêu thích.', NULL, NULL, '/cart', NULL, FALSE, NULL, DATEADD('HOUR', -6, NOW()), DATEADD('HOUR', -6, NOW()));

-- ===================== CONVERSATIONS =====================
INSERT INTO conversations (id, customer_id, seller_id, last_message, last_message_at, seller_unread_count, created_at, updated_at) VALUES
('conv-001', 'u-cust-001', 's-001', 'Cảm ơn shop, sản phẩm đúng mô tả!',     DATEADD('DAY', -2, NOW()), 0, DATEADD('DAY', -6, NOW()), DATEADD('DAY', -2, NOW())),
('conv-002', 'u-cust-002', 's-002', 'Khi nào hàng tới shop?',                DATEADD('DAY', -1, NOW()), 1, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -1, NOW())),
('conv-003', 'u-cust-003', 's-004', 'Sản phẩm còn hàng không shop?',         DATEADD('HOUR', -3, NOW()), 2, DATEADD('DAY', -1, NOW()), DATEADD('HOUR', -3, NOW())),
('conv-004', 'u-cust-004', 's-001', 'Cho xin trợ giá MacBook Pro M3 nhé.',   DATEADD('HOUR', -5, NOW()), 1, DATEADD('HOUR', -8, NOW()), DATEADD('HOUR', -5, NOW()));

-- ===================== MESSAGES =====================
INSERT INTO messages (id, conversation_id, sender_id, content, attachments, read_at, created_at, updated_at) VALUES
('msg-001', 'conv-001', 'u-cust-001', 'Shop ơi, iPhone 15 Pro Max còn màu xanh không ạ?', NULL, DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW())),
('msg-002', 'conv-001', 'u-seller-01','Dạ còn ạ, em vừa cập nhật hàng mới chiều nay.',    NULL, DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW())),
('msg-003', 'conv-001', 'u-cust-001', 'Vậy đặt giúp em 1 cái nhé. Bao giờ giao?',         NULL, DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW())),
('msg-004', 'conv-001', 'u-seller-01','Em sẽ giao trong 1-2 ngày tới ạ.',                 NULL, DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW())),
('msg-005', 'conv-001', 'u-cust-001', 'Cảm ơn shop, sản phẩm đúng mô tả!',                NULL, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW())),

('msg-006', 'conv-002', 'u-cust-002', 'Em đã đặt đơn Polo Lacoste hôm qua ạ.',            NULL, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW())),
('msg-007', 'conv-002', 'u-seller-02','Dạ em đã đóng gói và gửi GHTK ạ.',                 NULL, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW())),
('msg-008', 'conv-002', 'u-cust-002', 'Khi nào hàng tới shop?',                           NULL, NULL,                       DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),

('msg-009', 'conv-003', 'u-cust-003', 'Sản phẩm còn hàng không shop?',                    NULL, NULL,                       DATEADD('HOUR', -3, NOW()), DATEADD('HOUR', -3, NOW())),
('msg-010', 'conv-003', 'u-cust-003', 'Em quan tâm Serum SK-II.',                         NULL, NULL,                       DATEADD('HOUR', -3, NOW()), DATEADD('HOUR', -3, NOW())),

('msg-011', 'conv-004', 'u-cust-004', 'Em định mua MacBook Pro M3 trả góp.',              NULL, NULL,                       DATEADD('HOUR', -8, NOW()), DATEADD('HOUR', -8, NOW())),
('msg-012', 'conv-004', 'u-cust-004', 'Cho xin trợ giá MacBook Pro M3 nhé.',              NULL, NULL,                       DATEADD('HOUR', -5, NOW()), DATEADD('HOUR', -5, NOW()));

-- ===================== COMPLAINTS =====================
INSERT INTO complaints (id, reporter_id, target_id, order_id, product_id, category, title, content, desired_resolution, attachments, status, due_at, first_response_at, resolved_at, created_at, updated_at) VALUES
('cmp-001', 'u-cust-002', 'u-seller-02', 'ord-002', 'pd-polo-lacoste',  'PRODUCT_QUALITY','Áo Polo có lỗi đường may', 'Áo nhận về có vài đường may không đều ở cổ.',          'REFUND_PARTIAL', NULL, 'OPEN',     DATEADD('DAY', 5, NOW()),  DATEADD('HOUR', -8, NOW()), NULL,                       DATEADD('DAY', -1, NOW()), DATEADD('HOUR', -8, NOW())),
('cmp-002', 'u-cust-003', 'u-seller-04', 'ord-003', 'pd-serum-skii',    'SHIPPING_DELAY', 'Đơn hàng giao quá chậm',    'Em đặt 3 ngày rồi mà chưa thấy giao.',                  'SPEED_UP',       NULL, 'IN_PROGRESS', DATEADD('DAY', 3, NOW()),  DATEADD('HOUR', -4, NOW()), NULL,                       DATEADD('DAY', -1, NOW()), DATEADD('HOUR', -4, NOW())),
('cmp-003', 'u-cust-001', 'u-seller-01', 'ord-001', 'pd-iphone15pm',    'OTHER',          'Hỏi về bảo hành',           'Em muốn hỏi shop bảo hành chính hãng bao lâu ạ.',       'INFO',           NULL, 'RESOLVED', DATEADD('DAY', -1, NOW()), DATEADD('DAY', -3, NOW()),  DATEADD('DAY', -2, NOW()),  DATEADD('DAY', -4, NOW()), DATEADD('DAY', -2, NOW()));

-- ===================== COMPLAINT MESSAGES =====================
INSERT INTO complaint_messages (id, complaint_id, sender_id, sender_type, content, attachments, created_at, updated_at) VALUES
('cm-001', 'cmp-001', 'u-cust-002', 'CUSTOMER', 'Em gửi shop ảnh đường may bị lỗi.',                NULL, DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),
('cm-002', 'cmp-001', 'u-seller-02','SELLER',   'Dạ em đã nhận, shop xin lỗi và sẽ hoàn 30% giá trị.', NULL, DATEADD('HOUR', -8, NOW()), DATEADD('HOUR', -8, NOW())),
('cm-003', 'cmp-002', 'u-cust-003', 'CUSTOMER', 'Em đặt đã 3 ngày mà chưa thấy giao.',              NULL, DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),
('cm-004', 'cmp-002', 'u-seller-04','SELLER',   'Dạ em liên hệ shipper đẩy nhanh đơn cho anh chị.', NULL, DATEADD('HOUR', -4, NOW()), DATEADD('HOUR', -4, NOW())),
('cm-005', 'cmp-002', 'u-admin-001','ADMIN',    'Đội hỗ trợ đã ghi nhận và đang đôn đốc đơn vị vận chuyển.', NULL, DATEADD('HOUR', -2, NOW()), DATEADD('HOUR', -2, NOW())),
('cm-006', 'cmp-003', 'u-cust-001', 'CUSTOMER', 'Cho em hỏi bảo hành chính hãng bao lâu ạ?',        NULL, DATEADD('DAY', -4, NOW()), DATEADD('DAY', -4, NOW())),
('cm-007', 'cmp-003', 'u-seller-01','SELLER',   'Dạ bảo hành chính hãng 12 tháng tại Apple Việt Nam.', NULL, DATEADD('DAY', -3, NOW()), DATEADD('DAY', -3, NOW())),
('cm-008', 'cmp-003', 'u-cust-001', 'CUSTOMER', 'Cảm ơn shop, em đã rõ.',                           NULL, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW()));

-- ===================== ADMIN METRICS =====================
INSERT INTO admin_metrics (id, request_count, success_count, avg_response_ms, created_at, updated_at) VALUES
('metric-2026-05-12', 12450, 12380, 145, DATEADD('DAY', -6, NOW()), DATEADD('DAY', -6, NOW())),
('metric-2026-05-13', 13820, 13750, 138, DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW())),
('metric-2026-05-14', 14200, 14090, 152, DATEADD('DAY', -4, NOW()), DATEADD('DAY', -4, NOW())),
('metric-2026-05-15', 15100, 14990, 141, DATEADD('DAY', -3, NOW()), DATEADD('DAY', -3, NOW())),
('metric-2026-05-16', 16780, 16650, 158, DATEADD('DAY', -2, NOW()), DATEADD('DAY', -2, NOW())),
('metric-2026-05-17', 18900, 18820, 134, DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),
('metric-2026-05-18', 9230,  9190,  142, NOW(),                     NOW());

-- ===================== REPORT AUDITS =====================
INSERT INTO report_audits (id, user_id, seller_id, report_type, period_start, period_end, export_format, status, duration_ms, notes, created_at) VALUES
('ra-001', 'u-admin-001', NULL,    'SALES_OVERVIEW',   DATEADD('DAY', -30, CURRENT_DATE), CURRENT_DATE, 'PDF',   'COMPLETED', 1820, 'Báo cáo tổng quan doanh số 30 ngày', DATEADD('DAY', -1, NOW())),
('ra-002', 'u-admin-001', NULL,    'USER_ACTIVITY',    DATEADD('DAY', -7, CURRENT_DATE),  CURRENT_DATE, 'EXCEL', 'COMPLETED', 2450, 'Báo cáo hoạt động user tuần này',     DATEADD('HOUR', -12, NOW())),
('ra-003', 'u-seller-01', 's-001', 'SELLER_REVENUE',   DATEADD('DAY', -30, CURRENT_DATE), CURRENT_DATE, 'PDF',   'COMPLETED', 980,  'Doanh thu shop tháng',                DATEADD('DAY', -2, NOW())),
('ra-004', 'u-seller-02', 's-002', 'SELLER_REVENUE',   DATEADD('DAY', -30, CURRENT_DATE), CURRENT_DATE, 'EXCEL', 'COMPLETED', 1120, 'Doanh thu shop tháng',                DATEADD('DAY', -2, NOW())),
('ra-005', 'u-admin-001', NULL,    'COMPLAINT_REPORT', DATEADD('DAY', -30, CURRENT_DATE), CURRENT_DATE, 'PDF',   'PENDING',   NULL, 'Báo cáo khiếu nại tháng',             DATEADD('HOUR', -1, NOW()));
