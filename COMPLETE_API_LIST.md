# Complete API List - ShopCuaThuy Backend

## Authentication APIs
- `POST /api/auth/register` - Đăng ký tài khoản
- `POST /api/auth/login` - Đăng nhập

## Product APIs
- `GET /api/products` - Lấy danh sách sản phẩm (paginated)
- `GET /api/products/{id}` - Lấy chi tiết sản phẩm
- `GET /api/products/featured` - Lấy sản phẩm nổi bật
- `GET /api/products/category/{slug}` - Lấy sản phẩm theo danh mục
- `GET /api/products/search?keyword=...` - Tìm kiếm sản phẩm

## Category APIs
- `GET /api/categories` - Lấy tất cả danh mục
- `GET /api/categories/{slug}` - Lấy danh mục theo slug

## Cart APIs
- `GET /api/cart` - Lấy giỏ hàng
- `POST /api/cart/add` - Thêm sản phẩm vào giỏ hàng
- `PUT /api/cart/{cartItemId}` - Cập nhật số lượng sản phẩm
- `DELETE /api/cart/{cartItemId}` - Xóa sản phẩm khỏi giỏ hàng
- `DELETE /api/cart` - Xóa toàn bộ giỏ hàng

## Order APIs
- `POST /api/orders` - Tạo đơn hàng mới
- `GET /api/orders` - Lấy danh sách đơn hàng của user
- `GET /api/orders/{orderId}` - Lấy chi tiết đơn hàng

## Wishlist APIs
- `GET /api/wishlist` - Lấy danh sách yêu thích
- `POST /api/wishlist/add` - Thêm sản phẩm vào yêu thích
- `DELETE /api/wishlist/{productId}` - Xóa sản phẩm khỏi yêu thích
- `GET /api/wishlist/{productId}/check` - Kiểm tra sản phẩm có trong yêu thích

## User APIs
- `GET /api/users/profile` - Lấy thông tin profile
- `PUT /api/users/profile` - Cập nhật profile
- `GET /api/users/addresses` - Lấy danh sách địa chỉ
- `POST /api/users/addresses` - Thêm địa chỉ mới

## Review APIs
- `GET /api/reviews/product/{productId}` - Lấy đánh giá sản phẩm
- `POST /api/reviews/product/{productId}` - Tạo đánh giá mới

## Notification APIs
- `GET /api/notifications` - Lấy danh sách thông báo
- `GET /api/notifications/unread-count` - Lấy số thông báo chưa đọc
- `PUT /api/notifications/{notificationId}/read` - Đánh dấu đã đọc
- `PUT /api/notifications/read-all` - Đánh dấu tất cả đã đọc

## Seller APIs
- `GET /api/seller/profile` - Lấy thông tin seller
- `POST /api/seller/create` - Tạo seller profile
- `PUT /api/seller/profile` - Cập nhật seller profile
- `GET /api/seller/products` - Lấy danh sách sản phẩm của seller

## Shipping APIs
- `GET /api/shipping/methods` - Lấy danh sách phương thức vận chuyển
- `POST /api/shipping/calculate-fee` - Tính phí vận chuyển
- `POST /api/shipping/shipments` - Tạo đơn vận chuyển
- `GET /api/shipping/track/{trackingNumber}` - Theo dõi đơn hàng
- `GET /api/shipping/orders/{orderId}` - Lấy thông tin vận chuyển theo order
- `PUT /api/shipping/shipments/{shipmentId}/status` - Cập nhật trạng thái vận chuyển
- `GET /api/shipping/shipments/{shipmentId}/tracking` - Lấy lịch sử tracking

## Payment APIs
- `GET /api/payments/methods` - Lấy danh sách phương thức thanh toán
- `POST /api/payments/orders/{orderId}` - Tạo thanh toán
- `GET /api/payments/orders/{orderId}` - Lấy thông tin thanh toán
- `PUT /api/payments/{transactionId}/status` - Cập nhật trạng thái thanh toán

## Promotion APIs
- `GET /api/promotions/seller` - Lấy danh sách khuyến mãi của seller
- `POST /api/promotions` - Tạo khuyến mãi mới
- `PUT /api/promotions/{promotionId}` - Cập nhật khuyến mãi
- `DELETE /api/promotions/{promotionId}` - Xóa khuyến mãi

## File Upload APIs
- `POST /api/upload/image` - Upload ảnh lên MinIO
- `DELETE /api/upload/image` - Xóa ảnh

## Response Format

Tất cả các API đều trả về format chuẩn:

```json
{
  "success": true,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2024-01-01T00:00:00"
}
```

## Authentication

Các API yêu cầu authentication sử dụng header:
- `Authorization: Bearer <jwt-token>`
- `X-User-Id: <user-id>` (optional, có thể lấy từ JWT token)

## Pagination

Các API có pagination sử dụng query parameters:
- `page` - Số trang (default: 0)
- `size` - Số items mỗi trang (default: 20)

## Error Handling

Các lỗi trả về format:
```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "timestamp": "2024-01-01T00:00:00"
}
```

Status codes:
- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Authentication required
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error








