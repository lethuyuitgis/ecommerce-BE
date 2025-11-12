# Cách xem data đã crawl từ Shopee

## 1. Xem qua API (Khuyến nghị)

### Xem thống kê
```bash
GET http://localhost:8080/api/admin/crawler/stats
```

Response:
```json
{
  "success": true,
  "data": {
    "totalCategories": 50,
    "totalProducts": 200,
    "activeCategories": 50,
    "activeProducts": 180
  }
}
```

### Xem danh sách categories
```bash
GET http://localhost:8080/api/admin/crawler/categories?page=0&size=20
```

### Xem danh sách products
```bash
GET http://localhost:8080/api/admin/crawler/products?page=0&size=20
```

### Xem products theo category
```bash
GET http://localhost:8080/api/admin/crawler/products/category/thoi-trang-nam?page=0&size=20
```

## 2. Xem trực tiếp trong Database

### MySQL
```sql
-- Xem số lượng categories
SELECT COUNT(*) FROM categories;

-- Xem số lượng products
SELECT COUNT(*) FROM products;

-- Xem danh sách categories
SELECT id, name, slug, cover_image, display_order, is_active 
FROM categories 
ORDER BY display_order 
LIMIT 20;

-- Xem danh sách products
SELECT p.id, p.name, p.sku, p.price, p.quantity, p.rating, p.total_sold
FROM products p
ORDER BY p.created_at DESC
LIMIT 20;

-- Xem products theo category
SELECT p.id, p.name, p.price, p.quantity
FROM products p
JOIN categories c ON p.category_id = c.id
WHERE c.slug = 'thoi-trang-nam'
LIMIT 20;

-- Xem ảnh products
SELECT pi.id, pi.image_url, pi.is_primary, pi.display_order
FROM product_images pi
JOIN products p ON pi.product_id = p.id
WHERE p.id = 'your-product-id'
ORDER BY pi.display_order;
```

## 3. Xem ảnh trong MinIO

### Qua MinIO Console
1. Truy cập: `http://localhost:9000`
2. Login với credentials trong `application.yml`
3. Chọn bucket `shopcuathuy`
4. Xem các folder:
   - `categories/` - Ảnh categories
   - `products/` - Ảnh products

### Qua API
```bash
# URL ảnh sẽ có format:
http://localhost:9000/shopcuathuy/categories/{filename}
http://localhost:9000/shopcuathuy/products/{filename}
```

## 4. Xem logs

### Trong console
Khi crawler chạy, bạn sẽ thấy logs:
```
Starting to crawl categories from Shopee...
Saved category: Thời Trang Nam
Saved category: Điện Thoại & Phụ Kiện
...
Categories crawling completed.
Starting to crawl products from Shopee for category: thoi-trang-nam
Saved product: Áo Thun Nam...
Products crawling completed for category: thoi-trang-nam
```

### Trong file log
```bash
tail -f logs/application.log | grep -i "crawl\|shopee"
```

## 5. Kiểm tra qua Frontend

### Xem categories
Truy cập: `http://localhost:3000/categories`

### Xem products
Truy cập: `http://localhost:3000/products`

### Xem products theo category
Truy cập: `http://localhost:3000/category/thoi-trang-nam`

## 6. Sử dụng Postman hoặc cURL

### Xem stats
```bash
curl http://localhost:8080/api/admin/crawler/stats
```

### Xem categories
```bash
curl http://localhost:8080/api/admin/crawler/categories
```

### Xem products
```bash
curl http://localhost:8080/api/admin/crawler/products
```

## 7. Kiểm tra nhanh

### Kiểm tra có data chưa
```sql
-- Kiểm tra categories
SELECT COUNT(*) as total_categories FROM categories;

-- Kiểm tra products
SELECT COUNT(*) as total_products FROM products;

-- Kiểm tra products có ảnh
SELECT COUNT(DISTINCT pi.product_id) as products_with_images
FROM product_images pi;
```

### Kiểm tra data mới nhất
```sql
-- Categories mới nhất
SELECT name, slug, created_at 
FROM categories 
ORDER BY created_at DESC 
LIMIT 10;

-- Products mới nhất
SELECT name, sku, price, created_at 
FROM products 
ORDER BY created_at DESC 
LIMIT 10;
```

## 8. Troubleshooting

### Không thấy data
1. Kiểm tra crawler đã chạy chưa:
   ```bash
   # Xem logs
   tail -f logs/application.log
   ```

2. Kiểm tra database connection:
   ```sql
   SELECT 1;
   ```

3. Kiểm tra MinIO connection:
   ```bash
   curl http://localhost:9000/minio/health/live
   ```

### Data không đầy đủ
1. Kiểm tra logs để xem có lỗi không
2. Chạy lại crawler cho category cụ thể:
   ```bash
   POST http://localhost:8080/api/admin/crawler/products/thoi-trang-nam?limit=50
   ```

### Ảnh không hiển thị
1. Kiểm tra MinIO đang chạy
2. Kiểm tra bucket `shopcuathuy` đã được tạo
3. Kiểm tra URL ảnh trong database:
   ```sql
   SELECT image_url FROM product_images LIMIT 5;
   ```







