-- Script để xóa dữ liệu bảng products và các bảng liên quan
-- GIỮ LẠI bảng categories

-- Tắt kiểm tra foreign key tạm thời
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa dữ liệu các bảng liên quan đến Product (theo thứ tự dependency)

-- 1. Xóa review_images (liên quan qua product_reviews)
DELETE FROM review_images;

-- 2. Xóa product_reviews (có foreign key đến products)
DELETE FROM product_reviews;

-- 3. Xóa promotion_items (có foreign key đến products)
DELETE FROM promotion_items;

-- 4. Xóa product_images (có foreign key đến products)
DELETE FROM product_images;

-- 5. Xóa product_variants (có foreign key đến products)
DELETE FROM product_variants;

-- 6. Xóa order_items (có foreign key đến products)
-- LƯU Ý: Nếu bạn muốn giữ lại orders, có thể comment dòng này
DELETE FROM order_items;

-- 7. Xóa cart_items (có foreign key đến products)
DELETE FROM cart_items;

-- 8. Xóa wishlist (có foreign key đến products)
DELETE FROM wishlist;

-- 9. Xóa complaints có product_id (không phải foreign key nhưng liên quan)
DELETE FROM complaints WHERE product_id IS NOT NULL;

-- 10. Xóa products (bảng chính)
DELETE FROM products;

-- Bật lại kiểm tra foreign key
SET FOREIGN_KEY_CHECKS = 1;

-- Reset AUTO_INCREMENT cho bảng products (nếu cần)
ALTER TABLE products AUTO_INCREMENT = 1;

-- Hiển thị thông báo
SELECT 'Đã xóa tất cả dữ liệu products và các bảng liên quan. Categories được giữ lại.' AS message;

