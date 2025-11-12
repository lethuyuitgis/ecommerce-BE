# Frontend Integration Guide

## API Base URL
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Authentication Flow

### 1. Register
```typescript
const register = async (email: string, password: string, fullName: string, phone?: string) => {
  const response = await fetch(`${API_BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, fullName, phone })
  });
  const data = await response.json();
  if (data.success) {
    // Store token
    localStorage.setItem('token', data.data.token);
    localStorage.setItem('userId', data.data.userId);
  }
  return data;
};
```

### 2. Login
```typescript
const login = async (email: string, password: string) => {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await response.json();
  if (data.success) {
    localStorage.setItem('token', data.data.token);
    localStorage.setItem('userId', data.data.userId);
  }
  return data;
};
```

### 3. API Client with Auth
```typescript
const apiClient = async (url: string, options: RequestInit = {}) => {
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');
  
  return fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      'X-User-Id': userId || '',
      ...options.headers,
    },
  });
};
```

## Product APIs

### Get Products
```typescript
const getProducts = async (page = 0, size = 20) => {
  const response = await apiClient(`/products?page=${page}&size=${size}`);
  return response.json();
};

// Featured products
const getFeaturedProducts = async () => {
  const response = await apiClient('/products/featured');
  return response.json();
};

// Get product by ID
const getProduct = async (id: string) => {
  const response = await apiClient(`/products/${id}`);
  return response.json();
};

// Search products
const searchProducts = async (keyword: string) => {
  const response = await apiClient(`/products/search?keyword=${encodeURIComponent(keyword)}`);
  return response.json();
};
```

## Cart APIs

### Get Cart
```typescript
const getCart = async () => {
  const response = await apiClient('/cart');
  return response.json();
};

// Add to cart
const addToCart = async (productId: string, variantId?: string, quantity = 1) => {
  const response = await apiClient('/cart/add', {
    method: 'POST',
    body: JSON.stringify({ productId, variantId, quantity })
  });
  return response.json();
};

// Update cart item
const updateCartItem = async (cartItemId: string, quantity: number) => {
  const response = await apiClient(`/cart/${cartItemId}`, {
    method: 'PUT',
    body: JSON.stringify({ quantity })
  });
  return response.json();
};

// Remove from cart
const removeFromCart = async (cartItemId: string) => {
  const response = await apiClient(`/cart/${cartItemId}`, {
    method: 'DELETE'
  });
  return response.json();
};
```

## Order APIs

### Create Order
```typescript
const createOrder = async (orderData: {
  items: Array<{ productId: string; variantId?: string; quantity: number }>;
  shippingAddressId: string;
  paymentMethod: string;
  voucherCode?: string;
  notes?: string;
}) => {
  const response = await apiClient('/orders', {
    method: 'POST',
    body: JSON.stringify(orderData)
  });
  return response.json();
};

// Get user orders
const getOrders = async (page = 0, size = 20) => {
  const response = await apiClient(`/orders?page=${page}&size=${size}`);
  return response.json();
};

// Get order by ID
const getOrder = async (orderId: string) => {
  const response = await apiClient(`/orders/${orderId}`);
  return response.json();
};
```

## Wishlist APIs

```typescript
const getWishlist = async () => {
  const response = await apiClient('/wishlist');
  return response.json();
};

const addToWishlist = async (productId: string) => {
  const response = await apiClient('/wishlist/add', {
    method: 'POST',
    body: JSON.stringify({ productId })
  });
  return response.json();
};

const removeFromWishlist = async (productId: string) => {
  const response = await apiClient(`/wishlist/${productId}`, {
    method: 'DELETE'
  });
  return response.json();
};

const checkWishlist = async (productId: string) => {
  const response = await apiClient(`/wishlist/${productId}/check`);
  return response.json();
};
```

## User Profile APIs

```typescript
const getProfile = async () => {
  const response = await apiClient('/users/profile');
  return response.json();
};

const updateProfile = async (profileData: {
  fullName?: string;
  phone?: string;
  avatarUrl?: string;
}) => {
  const response = await apiClient('/users/profile', {
    method: 'PUT',
    body: JSON.stringify(profileData)
  });
  return response.json();
};

// Addresses
const getAddresses = async () => {
  const response = await apiClient('/users/addresses');
  return response.json();
};

const addAddress = async (addressData: {
  addressType: 'HOME' | 'OFFICE' | 'OTHER';
  fullName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  street: string;
  isDefault: boolean;
}) => {
  const response = await apiClient('/users/addresses', {
    method: 'POST',
    body: JSON.stringify(addressData)
  });
  return response.json();
};
```

## Reviews APIs

```typescript
const getProductReviews = async (productId: string, page = 0, size = 20) => {
  const response = await fetch(`${API_BASE_URL}/reviews/product/${productId}?page=${page}&size=${size}`);
  return response.json();
};

const createReview = async (productId: string, reviewData: {
  orderItemId?: string;
  rating: number;
  title: string;
  comment: string;
}) => {
  const response = await apiClient(`/reviews/product/${productId}`, {
    method: 'POST',
    body: JSON.stringify(reviewData)
  });
  return response.json();
};
```

## Notifications APIs

```typescript
const getNotifications = async (page = 0, size = 20) => {
  const response = await apiClient(`/notifications?page=${page}&size=${size}`);
  return response.json();
};

const getUnreadCount = async () => {
  const response = await apiClient('/notifications/unread-count');
  return response.json();
};

const markAsRead = async (notificationId: string) => {
  const response = await apiClient(`/notifications/${notificationId}/read`, {
    method: 'PUT'
  });
  return response.json();
};
```

## File Upload

```typescript
const uploadImage = async (file: File, folder = 'images') => {
  const formData = new FormData();
  formData.append('file', file);
  
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_BASE_URL}/upload/image?folder=${folder}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  return response.json();
};
```

## Error Handling

```typescript
const handleApiError = (error: any) => {
  if (error.response?.status === 401) {
    // Unauthorized - redirect to login
    localStorage.removeItem('token');
    window.location.href = '/login';
  } else if (error.response?.status === 404) {
    // Not found
    console.error('Resource not found');
  } else {
    // Other errors
    console.error('API Error:', error);
  }
};
```

## Example Usage in React

```typescript
// hooks/useProducts.ts
import { useState, useEffect } from 'react';

export const useProducts = (page = 0, size = 20) => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const data = await getProducts(page, size);
        if (data.success) {
          setProducts(data.data.content);
        }
      } catch (error) {
        handleApiError(error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchProducts();
  }, [page, size]);
  
  return { products, loading };
};
```

## Next.js API Routes (Optional)

Nếu bạn muốn sử dụng Next.js API routes như proxy:

```typescript
// pages/api/products.ts
import type { NextApiRequest, NextApiResponse } from 'next';

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
  const response = await fetch(`${process.env.API_BASE_URL}/api/products`, {
    headers: {
      'Authorization': req.headers.authorization || '',
      'X-User-Id': req.headers['x-user-id'] || '',
    },
  });
  
  const data = await response.json();
  res.status(response.status).json(data);
}
```








