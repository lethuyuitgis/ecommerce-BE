# ShopCuaThuy E-Commerce Backend API

Backend API cho nền tảng thương mại điện tử ShopCuaThuy được xây dựng bằng Java Spring Boot.

## Công nghệ sử dụng

- **Spring Boot 3.2.0**
- **Spring Data JPA** - ORM và database access
- **Spring Security** - Authentication & Authorization
- **MySQL** - Database
- **MinIO** - Object storage cho files
- **JWT** - Token-based authentication
- **Lombok** - Giảm boilerplate code
- **MapStruct** - Object mapping
- **Maven** - Build tool

## Cấu trúc dự án

```
src/main/java/com/shopcuathuy/
├── common/              # Base classes
├── config/              # Configuration classes
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA Entities
├── exception/           # Exception handling
├── repository/          # Data access layer
└── service/             # Business logic layer
```

## Cài đặt và chạy

### Yêu cầu

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- MinIO Server

### Cấu hình

1. **Database**: Tạo database MySQL tên `shopcuathuy`
2. **MinIO**: Cài đặt và chạy MinIO server
3. **Environment Variables**: Cấu hình trong `application.yml` hoặc environment variables:
   - `MINIO_ENDPOINT`
   - `MINIO_ACCESS_KEY`
   - `MINIO_SECRET_KEY`
   - `JWT_SECRET`

### Chạy ứng dụng

```bash
# Build project
mvn clean install

# Chạy ứng dụng
mvn spring-boot:run
```

API sẽ chạy tại: `http://localhost:8080`

## API Endpoints

### Products
- `GET /api/products` - Lấy danh sách sản phẩm
- `GET /api/products/{id}` - Lấy chi tiết sản phẩm
- `GET /api/products/featured` - Lấy sản phẩm nổi bật
- `GET /api/products/category/{slug}` - Lấy sản phẩm theo danh mục
- `GET /api/products/search?keyword=...` - Tìm kiếm sản phẩm

### Categories
- `GET /api/categories` - Lấy danh sách danh mục
- `GET /api/categories/{slug}` - Lấy chi tiết danh mục

### Authentication
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập

## Database Schema

Tất cả các bảng sử dụng UUID (String) làm primary key. Xem file `docs/DATABASE_SCHEMA.md` trong project frontend để biết chi tiết schema.

## Lưu ý

- Tất cả ID của các bảng đều là String (UUID)
- File storage sử dụng MinIO
- JWT được sử dụng cho authentication
- API hỗ trợ CORS cho frontend

## Phát triển tiếp

Các tính năng cần phát triển thêm:
- [ ] Authentication & Authorization đầy đủ
- [ ] Order management
- [ ] Payment processing
- [ ] Shipping integration
- [ ] Review & Rating system
- [ ] Notification system
- [ ] Admin APIs
- [ ] Seller dashboard APIs



# ecommerce-backend
# ecommerce-BE
