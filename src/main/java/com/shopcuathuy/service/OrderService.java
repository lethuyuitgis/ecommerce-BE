package com.shopcuathuy.service;

import com.shopcuathuy.dto.request.CreateOrderRequestDTO;
import com.shopcuathuy.dto.request.UpdateOrderStatusRequestDTO;
import com.shopcuathuy.dto.response.OrderPageResponseDTO;
import com.shopcuathuy.dto.response.OrderResponseDTO;
import com.shopcuathuy.dto.response.PurchaseStatusResponseDTO;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderPageResponseDTO getOrders(String userId, String userRole, String status, Pageable pageable);
    OrderResponseDTO getOrderById(String id, String userId);
    PurchaseStatusResponseDTO checkPurchase(String productId, String userId);
    OrderResponseDTO createOrder(String userId, CreateOrderRequestDTO request);
    OrderResponseDTO cancelOrder(String id, String userId);
    OrderResponseDTO updateOrderStatus(String id, UpdateOrderStatusRequestDTO request);
}


