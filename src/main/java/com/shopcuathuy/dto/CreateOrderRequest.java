package com.shopcuathuy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;


@Data
public class CreateOrderRequest {
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull(message = "Shipping address is required")
    private String shippingAddressId;
    
    private String paymentMethod;
    private String voucherCode;
    private String notes;
    
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private String productId;
        
        private String variantId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}








