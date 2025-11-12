# Hướng dẫn Test Crawler

## 1. Kiểm tra Categories đã có chưa

```bash
# Xem stats
curl http://localhost:8080/api/admin/crawler/stats

# Xem categories
curl http://localhost:8080/api/admin/crawler/categories
```

## 2. Crawl Categories (nếu chưa có)

```bash
POST http://localhost:8080/api/admin/crawler/categories
```

Hoặc dùng cURL:
```bash
curl -X POST http://localhost:8080/api/admin/crawler/categories
```

## 3. Crawl Products cho một category

```bash
# Crawl products cho category "thoi-trang-nam"
POST http://localhost:8080/api/admin/crawler/products/thoi-trang-nam?limit=10
```

Hoặc dùng cURL:
```bash
curl -X POST "http://localhost:8080/api/admin/crawler/products/thoi-trang-nam?limit=10"
```

## 4. Kiểm tra Products đã crawl

```bash
# Xem stats
curl http://localhost:8080/api/admin/crawler/stats

# Xem products
curl http://localhost:8080/api/admin/crawler/products?page=0&size=10

# Xem products theo category
curl http://localhost:8080/api/admin/crawler/products/category/thoi-trang-nam
```

## 5. Xem logs

```bash
# Xem logs trong console hoặc
tail -f logs/application.log | grep -i "crawl\|product\|shopee"
```

## 6. Troubleshooting

### Không có products được lưu

1. **Kiểm tra categories đã có chưa:**
   ```bash
   curl http://localhost:8080/api/admin/crawler/categories
   ```
   Nếu không có, crawl categories trước:
   ```bash
   curl -X POST http://localhost:8080/api/admin/crawler/categories
   ```

2. **Kiểm tra logs:**
   - Xem có lỗi gì không
   - Xem có "Saved product" không
   - Xem có "No items found" không

3. **Kiểm tra category slug:**
   - Đảm bảo category slug đúng
   - Thử với category slug khác

4. **Kiểm tra Shopee API:**
   - Có thể Shopee đã thay đổi API
   - Có thể bị rate limit
   - Thử với limit nhỏ hơn (5-10)

5. **Kiểm tra database:**
   ```sql
   SELECT COUNT(*) FROM products;
   SELECT * FROM products ORDER BY created_at DESC LIMIT 5;
   ```

6. **Kiểm tra seller:**
   ```sql
   SELECT * FROM sellers LIMIT 1;
   ```
   Nếu không có seller, crawler sẽ tự tạo một seller mặc định.

## 7. Test thủ công

### Test với Postman

1. **Crawl categories:**
   - Method: POST
   - URL: `http://localhost:8080/api/admin/crawler/categories`
   - Headers: None

2. **Crawl products:**
   - Method: POST
   - URL: `http://localhost:8080/api/admin/crawler/products/thoi-trang-nam?limit=5`
   - Headers: None

3. **Xem stats:**
   - Method: GET
   - URL: `http://localhost:8080/api/admin/crawler/stats`

## 8. Lưu ý

- Crawler có delay 1-2 giây giữa các request để tránh rate limit
- Nếu Shopee API không hoạt động, có thể cần sửa lại URL hoặc cách gọi API
- Kiểm tra logs để xem chi tiết lỗi
- Nếu không có products, thử với category khác hoặc keyword khác







