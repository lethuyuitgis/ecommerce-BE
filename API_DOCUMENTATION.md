# API Documentation - ShopCuaThuy Backend

## Base URL
```
http://localhost:8080/api
```

## Authentication

### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phone": "0123456789"
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "jwt-token",
    "refreshToken": "refresh-token",
    "userId": "user-id",
    "email": "user@example.com",
    "fullName": "John Doe",
    "userType": "CUSTOMER"
  }
}
```

## Products

### Get All Products
```http
GET /api/products?page=0&size=20&sortBy=createdAt&direction=DESC
```

### Get Featured Products
```http
GET /api/products/featured?page=0&size=20
```

### Get Product by ID
```http
GET /api/products/{id}
```

### Get Products by Category
```http
GET /api/products/category/{slug}?page=0&size=20
```

### Search Products
```http
GET /api/products/search?keyword=iphone&page=0&size=20
```

## Categories

### Get All Categories
```http
GET /api/categories
```

### Get Category by Slug
```http
GET /api/categories/{slug}
```

## Cart

**Note:** All cart endpoints require `X-User-Id` header or JWT token in `Authorization: Bearer <token>`

### Get Cart Items
```http
GET /api/cart
Headers: X-User-Id: user-id
```

### Add to Cart
```http
POST /api/cart/add
Headers: X-User-Id: user-id
Content-Type: application/json

{
  "productId": "product-id",
  "variantId": "variant-id", // optional
  "quantity": 1
}
```

### Update Cart Item
```http
PUT /api/cart/{cartItemId}
Headers: X-User-Id: user-id
Content-Type: application/json

{
  "quantity": 2
}
```

### Remove from Cart
```http
DELETE /api/cart/{cartItemId}
Headers: X-User-Id: user-id
```

### Clear Cart
```http
DELETE /api/cart
Headers: X-User-Id: user-id
```

## Orders

### Create Order
```http
POST /api/orders
Headers: X-User-Id: user-id
Content-Type: application/json

{
  "items": [
    {
      "productId": "product-id",
      "variantId": "variant-id", // optional
      "quantity": 1
    }
  ],
  "shippingAddressId": "address-id",
  "paymentMethod": "cod",
  "voucherCode": "DISCOUNT10", // optional
  "notes": "Please deliver in the morning"
}
```

### Get User Orders
```http
GET /api/orders?page=0&size=20
Headers: X-User-Id: user-id
```

### Get Order by ID
```http
GET /api/orders/{orderId}
Headers: X-User-Id: user-id
```

## Wishlist

### Get Wishlist
```http
GET /api/wishlist
Headers: X-User-Id: user-id
```

### Add to Wishlist
```http
POST /api/wishlist/add
Headers: X-User-Id: user-id
Content-Type: application/json

{
  "productId": "product-id"
}
```

### Remove from Wishlist
```http
DELETE /api/wishlist/{productId}
Headers: X-User-Id: user-id
```

### Check if in Wishlist
```http
GET /api/wishlist/{productId}/check
Headers: X-User-Id: user-id
```

## User Profile

### Get Profile
```http
GET /api/users/profile
Headers: X-User-Id: user-id
```

### Update Profile
```http
PUT /api/users/profile
Headers: X-User-Id: user-id
Content-Type: application/json

{
  "fullName": "John Doe",
  "phone": "0123456789",
  "avatarUrl": "https://..."
}
```

### Get User Addresses
```http
GET /api/users/addresses
Headers: X-User-Id: user-id
```

### Add Address
```http
POST /api/users/addresses
Headers: X-User-Id: user-id
Content-Type: application/json

{
  "addressType": "HOME",
  "fullName": "John Doe",
  "phone": "0123456789",
  "province": "Ho Chi Minh",
  "district": "District 1",
  "ward": "Ward 1",
  "street": "123 Main Street",
  "isDefault": true
}
```

## Reviews

### Get Product Reviews
```http
GET /api/reviews/product/{productId}?page=0&size=20
```

### Create Review
```http
POST /api/reviews/product/{productId}
Headers: X-User-Id: user-id
Content-Type: application/json

{
  "orderItemId": "order-item-id", // optional
  "rating": 5,
  "title": "Great product!",
  "comment": "Very satisfied with the purchase"
}
```

## Notifications

### Get Notifications
```http
GET /api/notifications?page=0&size=20
Headers: X-User-Id: user-id
```

### Get Unread Count
```http
GET /api/notifications/unread-count
Headers: X-User-Id: user-id
```

### Mark as Read
```http
PUT /api/notifications/{notificationId}/read
Headers: X-User-Id: user-id
```

### Mark All as Read
```http
PUT /api/notifications/read-all
Headers: X-User-Id: user-id
```

## File Upload

### Upload Image
```http
POST /api/upload/image?folder=products
Content-Type: multipart/form-data

file: [binary data]
```

### Delete Image
```http
DELETE /api/upload/image?url=image-url
```

## Response Format

All API responses follow this format:

```json
{
  "success": true,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2024-01-01T00:00:00"
}
```

## Error Response

```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "timestamp": "2024-01-01T00:00:00"
}
```

## Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid request
- `401 Unauthorized` - Authentication required
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error
















