package com.shopcuathuy.dto.request;

import java.util.List;

public class CreateOrderRequestDTO {
    public List<CreateOrderItemRequestDTO> items;
    public String shippingAddressId;
    public String paymentMethod;
    public String voucherCode;
    public String notes;
}


