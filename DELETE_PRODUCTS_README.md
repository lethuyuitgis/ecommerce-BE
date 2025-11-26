# Hướng dẫn xóa dữ liệu Products

## Mô tả
Script này dùng để xóa tất cả dữ liệu trong bảng `products` và các bảng liên quan, **GIỮ LẠI** bảng `categories`.

## Các bảng sẽ bị xóa dữ liệu:

### Bảng trực tiếp liên quan đến Product:
1. `review_images` - Ảnh đánh giá sản phẩm
2. `product_reviews` - Đánh giá sản phẩm
3. `promotion_items` - Sản phẩm trong khuyến mãi
4. `product_images` - Hình ảnh sản phẩm
5. `product_variants` - Biến thể sản phẩm (size, color, etc.)
6. `cart_items` - Sản phẩm trong giỏ hàng
7. `wishlist` - Sản phẩm yêu thích
8. `complaints` - Khiếu nại có product_id
9. `products` - Bảng chính sản phẩm

### Bảng được GIỮ LẠI:
- ✅ `categories` - Danh mục sản phẩm
- ✅ `users` - Người dùng
- ✅ `sellers` - Người bán
- ✅ Tất cả các bảng khác không liên quan đến products

## Có 2 script để chọn:

### 1. `delete-products-data.sql` (Xóa tất cả)
- Xóa **TẤT CẢ** dữ liệu liên quan đến products
- **Bao gồm cả `order_items`** (xóa lịch sử đơn hàng)
- Dùng khi muốn reset hoàn toàn

### 2. `delete-products-data-safe.sql` (An toàn - Khuyến nghị)
- Xóa dữ liệu products và các bảng liên quan
- **GIỮ LẠI `order_items`** (giữ lịch sử đơn hàng)
- Dùng khi muốn giữ lại lịch sử đơn hàng

## Cách sử dụng:

### Cách 1: Sử dụng MySQL Command Line
```bash
mysql -u root -p shopcuathuy < delete-products-data-safe.sql
```

### Cách 2: Sử dụng MySQL Workbench hoặc phpMyAdmin
1. Mở file `delete-products-data-safe.sql`
2. Copy toàn bộ nội dung
3. Paste vào SQL editor
4. Chạy script

### Cách 3: Sử dụng Docker (nếu dùng Docker)
```bash
docker exec -i mysql-container mysql -uroot -p123456 shopcuathuy < delete-products-data-safe.sql
```

## Lưu ý quan trọng:

⚠️ **BACKUP DATABASE TRƯỚC KHI CHẠY!**
```bash
mysqldump -u root -p shopcuathuy > backup_before_delete_products.sql
```

⚠️ Script sẽ:
- Tắt `FOREIGN_KEY_CHECKS` tạm thời để tránh lỗi
- Xóa dữ liệu theo thứ tự đúng (bảng con trước, bảng cha sau)
- Reset `AUTO_INCREMENT` cho bảng `products`
- Bật lại `FOREIGN_KEY_CHECKS` sau khi hoàn thành

## Sau khi xóa:

1. Bạn có thể insert dữ liệu mới vào bảng `products`
2. Categories vẫn được giữ nguyên, có thể sử dụng lại
3. Nếu dùng script safe, lịch sử đơn hàng vẫn còn (nhưng `order_items` sẽ có `product_id` không tồn tại)

## Kiểm tra sau khi xóa:

```sql
-- Kiểm tra số lượng products
SELECT COUNT(*) FROM products; -- Phải = 0

-- Kiểm tra categories vẫn còn
SELECT COUNT(*) FROM categories; -- Phải > 0

-- Kiểm tra các bảng liên quan
SELECT COUNT(*) FROM product_images; -- Phải = 0
SELECT COUNT(*) FROM product_variants; -- Phải = 0
SELECT COUNT(*) FROM cart_items; -- Phải = 0
SELECT COUNT(*) FROM wishlist; -- Phải = 0
```


