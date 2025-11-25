package com.shopcuathuy.dto.request;

import java.util.List;

public class CheckoutRequestDTO {
    public List<CheckoutItemRequestDTO> items;
    public String shippingAddressId;
    public String paymentMethod;
    public String voucherCode;
    public String notes;
}


