USE shopcuathuy;
SET NAMES 'utf8mb4';

-- ===================== SAMPLE ORDERS =====================
INSERT INTO orders (id, order_number, customer_id, seller_id, status, subtotal, shipping_fee, discount_amount, total_price, final_total, payment_method, payment_status, shipping_status, created_at, updated_at) VALUES
('ord-001', 'ORD-20240320-1001', 'u-cust-001', 's-001', 'DELIVERED', 29990000.00, 50000.00, 100000.00, 29990000.00, 29940000.00, 'momo', 'PAID', 'DELIVERED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('ord-002', 'ORD-20240320-1002', 'u-cust-002', 's-002', 'CONFIRMED', 2190000.00, 30000.00, 0.00, 2190000.00, 2220000.00, 'cod', 'PENDING', 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('ord-003', 'ORD-20240320-1003', 'u-cust-003', 's-004', 'SHIPPED', 3290000.00, 40000.00, 0.00, 3290000.00, 3330000.00, 'transfer', 'PAID', 'IN_TRANSIT', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- ===================== ORDER ITEMS =====================
INSERT INTO order_items (id, order_id, product_id, variant_id, unit_price, quantity, total_price, created_at, updated_at) VALUES
('oi-001', 'ord-001', 'pd-iphone15pm', 'v-ip15pm-blue', 29990000.00, 1, 29990000.00, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('oi-002', 'ord-002', 'pd-giay-nike-af1', 'v-af1-42', 2190000.00, 1, 2190000.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('oi-003', 'ord-003', 'pd-serum-skii', NULL, 3290000.00, 1, 3290000.00, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- ===================== ORDER TIMELINE =====================
INSERT INTO order_timeline (id, order_id, status, note, created_at, updated_at) VALUES
('tl-ord1-1', 'ord-001', 'PENDING', 'Đơn hàng đã được đặt', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('tl-ord1-2', 'ord-001', 'CONFIRMED', 'Người bán đã xác nhận đơn hàng', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('tl-ord1-3', 'ord-001', 'DELIVERED', 'Đã giao hàng thành công', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

('tl-ord2-1', 'ord-002', 'PENDING', 'Chờ xác nhận từ người bán', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('tl-ord2-2', 'ord-002', 'CONFIRMED', 'Đang đóng gói hàng', NOW(), NOW());

-- Cập nhật total_orders cho sellers
UPDATE sellers SET total_orders = (SELECT COUNT(*) FROM orders WHERE seller_id = sellers.id);
