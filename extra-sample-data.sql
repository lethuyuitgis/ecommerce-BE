USE shopcuathuy;

-- Insert an extra category
INSERT INTO categories (id, name, slug, description, display_order, is_active, created_at, updated_at) VALUES
('extra-cat-1', 'Đồ Chơi Sáng Tạo', 'do-choi-sang-tao', 'Đồ chơi phát triển trí tuệ', 30, TRUE, NOW(), NOW());

-- Insert extra products
INSERT INTO products (id, seller_id, category_id, name, description, sku, price, compare_price, quantity, status, rating, total_reviews, total_sold, total_views, is_featured, created_at, updated_at) VALUES
('extra-pd-1', '22222222-1111-1111-1111-111111111111', 'extra-cat-1', 'Bộ Xếp Hình LEGO Technic', 'Lego Technic 42151 Bugatti Bolide', 'LEGO42151', 1590000, 1990000, 20, 'ACTIVE', 5.0, 10, 5, 100, TRUE, NOW(), NOW()),
('extra-pd-2', '33333333-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Áo Polo Nam Premium', 'Áo thun polo nam cao cấp, thoáng mát', 'POLO001', 450000, 590000, 30, 'ACTIVE', 4.9, 15, 8, 150, TRUE, NOW(), NOW());

-- Update seller total_products count
UPDATE sellers SET total_products = (SELECT COUNT(*) FROM products WHERE seller_id = sellers.id);
