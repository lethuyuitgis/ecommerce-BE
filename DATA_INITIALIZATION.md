# Data Initialization Guide

## Tổng quan

Script `DataInitializer` sẽ tự động đọc các file JSON từ `src/main/resources/data/` và insert vào database khi Spring Boot application khởi động.

## Cách sử dụng

### Bước 1: Đảm bảo các file JSON đã được copy vào resources

Các file JSON đã được copy vào `src/main/resources/data/`:
- `users.json`
- `categories.json`
- `sellers.json`
- `products.json`
- `product-images.json`
- `shipping-methods.json`
- `payment-methods.json`
- `user-addresses.json`

### Bước 2: Xóa database (nếu cần)

Nếu muốn reset database, chạy script SQL:
```bash
mysql -u root -p123456 < drop-all-tables.sql
```

### Bước 3: Khởi động Spring Boot application

```bash
cd e-commerce-backend
mvn spring-boot:run
```

Script `DataInitializer` sẽ tự động:
1. Kiểm tra xem database đã có dữ liệu chưa
2. Nếu chưa có, đọc các file JSON và insert vào database
3. Xử lý các foreign keys đúng cách
4. Cập nhật `total_products` cho sellers

## Thứ tự insert

Script sẽ insert dữ liệu theo thứ tự:
1. **Users** - Phải insert trước vì sellers và addresses cần user
2. **Categories** - Phải insert trước vì products cần category
3. **Sellers** - Phải insert sau users, trước products
4. **Products** - Phải insert sau categories và sellers
5. **Product Images** - Phải insert sau products
6. **Shipping Methods** - Không phụ thuộc
7. **Payment Methods** - Không phụ thuộc
8. **User Addresses** - Phải insert sau users
9. **Update seller total_products** - Cập nhật số lượng sản phẩm

## Lưu ý

- Script chỉ chạy một lần khi database trống (kiểm tra `userRepository.count() > 0`)
- Nếu muốn chạy lại, cần xóa dữ liệu trong database trước
- Tất cả password đều là `123456` (đã được hash bằng BCrypt)
- Tất cả UUID đều dùng định dạng `CHAR(36)`

## Thông tin đăng nhập mẫu

- **Admin**: `admin@shopcuathuy.com` / `123456`
- **Seller 1**: `seller1@shopcuathuy.com` / `123456`
- **Seller 2**: `seller2@shopcuathuy.com` / `123456`
- **Customer 1**: `customer1@shopcuathuy.com` / `123456`
- **Customer 2**: `customer2@shopcuathuy.com` / `123456`

## Troubleshooting

### Lỗi: "Data already exists"
- Script đã phát hiện dữ liệu trong database
- Nếu muốn chạy lại, xóa dữ liệu trong database trước

### Lỗi: "Foreign key constraint"
- Đảm bảo đã chạy `drop-all-tables.sql` trước
- Kiểm tra xem các entity đã được định nghĩa đúng với `CHAR(36)` cho foreign keys

### Lỗi: "File not found"
- Đảm bảo các file JSON đã được copy vào `src/main/resources/data/`
- Kiểm tra đường dẫn file trong code







