USE shopcuathuy;
SET NAMES 'utf8mb4';

-- ===================== ORDER TIMELINE (RETRY) =====================
INSERT INTO order_timeline (id, order_id, status, note, created_at, updated_at) VALUES
('tl-ord1-1', 'ord-001', 'PENDING', 'Đơn hàng đã được đặt', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('tl-ord1-2', 'ord-001', 'CONFIRMED', 'Người bán đã xác nhận đơn hàng', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('tl-ord1-3', 'ord-001', 'DELIVERED', 'Đã giao hàng thành công', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

('tl-ord2-1', 'ord-002', 'PENDING', 'Chờ xác nhận từ người bán', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('tl-ord2-2', 'ord-002', 'CONFIRMED', 'Đang đóng gói hàng', NOW(), NOW());

-- Cập nhật total_orders cho sellers
UPDATE sellers SET total_orders = (SELECT COUNT(*) FROM orders WHERE seller_id = sellers.id);
