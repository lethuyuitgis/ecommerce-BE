# Shopee Crawler Service

Service để crawl data danh mục (categories) và sản phẩm (products) từ Shopee, upload ảnh vào MinIO và insert vào database.

## Cách sử dụng

### 1. Cấu hình MinIO

Đảm bảo MinIO đang chạy và cấu hình trong `application.yml`:

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: shopcuathuy
```

### 2. Chạy crawler

Có 2 cách để chạy crawler:

#### Cách 1: Sử dụng CommandLineRunner (Tự động)

Thêm vào `application.yml`:

```yaml
shopee:
  crawl:
    enabled: true
```

Sau đó chạy ứng dụng:

```bash
mvn spring-boot:run
```

Crawler sẽ tự động chạy khi ứng dụng khởi động.

#### Cách 2: Sử dụng REST API (Thủ công)

Tạo một controller để gọi service:

```java
@RestController
@RequestMapping("/api/admin/crawler")
public class CrawlerController {
    
    @Autowired
    private ShopeeCrawlerService shopeeCrawlerService;
    
    @PostMapping("/categories")
    public ResponseEntity<String> crawlCategories() {
        shopeeCrawlerService.crawlCategories();
        return ResponseEntity.ok("Categories crawling started");
    }
    
    @PostMapping("/products/{categorySlug}")
    public ResponseEntity<String> crawlProducts(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "20") int limit) {
        shopeeCrawlerService.crawlProducts(categorySlug, limit);
        return ResponseEntity.ok("Products crawling started");
    }
}
```

### 3. Các tính năng

#### Crawl Categories

- Crawl tất cả categories từ Shopee
- Tự động tạo cấu trúc parent-child
- Download và upload ảnh cover vào MinIO
- Tạo slug tự động từ tên category

#### Crawl Products

- Crawl products theo category
- Download và upload ảnh sản phẩm vào MinIO
- Lưu thông tin: tên, mô tả, giá, số lượng, rating, số lượng đã bán
- Tự động tạo SKU từ Shopee item ID

### 4. Lưu ý

- **Rate Limiting**: Service có delay 1 giây giữa mỗi product và 5 giây giữa mỗi category để tránh bị block
- **Duplicate Check**: Service kiểm tra SKU trước khi insert để tránh duplicate
- **Error Handling**: Nếu có lỗi khi download/upload ảnh, service sẽ sử dụng URL gốc từ Shopee
- **Default Seller**: Nếu không có seller nào, service sẽ tạo một seller mặc định

### 5. Cấu trúc dữ liệu

#### Category
- Tên, slug, mô tả
- Ảnh cover (upload vào MinIO)
- Cấu trúc parent-child
- Display order

#### Product
- Thông tin cơ bản: tên, mô tả, SKU
- Giá: price, comparePrice
- Số lượng: quantity
- Rating: rating, totalReviews
- Thống kê: totalSold, totalViews
- Ảnh sản phẩm (upload vào MinIO)

### 6. Troubleshooting

#### Lỗi: "Category not found"
- Đảm bảo đã crawl categories trước khi crawl products
- Kiểm tra slug category có đúng không

#### Lỗi: "MinIO connection failed"
- Kiểm tra MinIO đang chạy
- Kiểm tra cấu hình trong `application.yml`
- Kiểm tra bucket đã được tạo chưa

#### Lỗi: "Rate limit exceeded"
- Tăng delay giữa các request
- Giảm số lượng products crawl mỗi lần

### 7. Tùy chỉnh

Bạn có thể tùy chỉnh các tham số trong `ShopeeCrawlerRunner`:

```java
// Số lượng products crawl mỗi category
shopeeCrawlerService.crawlProducts(categorySlug, 20);

// Delay giữa các categories (milliseconds)
Thread.sleep(5000);
```

## API Endpoints (nếu tạo controller)

- `POST /api/admin/crawler/categories` - Crawl categories
- `POST /api/admin/crawler/products/{categorySlug}?limit=20` - Crawl products cho một category







