
-- ===================== ORDER TIMELINE (RETRY) =====================
INSERT INTO order_timeline (id, order_id, status, note, created_at, updated_at) VALUES
('tl-ord1-1', 'ord-001', 'PENDING', 'Đơn hàng đã được đặt', DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW())),
('tl-ord1-2', 'ord-001', 'CONFIRMED', 'Người bán đã xác nhận đơn hàng', DATEADD('DAY', -4, NOW()), DATEADD('DAY', -4, NOW())),
('tl-ord1-3', 'ord-001', 'DELIVERED', 'Đã giao hàng thành công', DATEADD('DAY', -3, NOW()), DATEADD('DAY', -3, NOW())),

('tl-ord2-1', 'ord-002', 'PENDING', 'Chờ xác nhận từ người bán', DATEADD('DAY', -1, NOW()), DATEADD('DAY', -1, NOW())),
('tl-ord2-2', 'ord-002', 'CONFIRMED', 'Đang đóng gói hàng', NOW(), NOW());

-- Cập nhật total_orders cho sellers
UPDATE sellers SET total_orders = (SELECT COUNT(*) FROM orders WHERE seller_id = sellers.id);
