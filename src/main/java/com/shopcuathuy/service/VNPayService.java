package com.shopcuathuy.service;

import java.util.Map;

public interface VNPayService {
    String createPaymentUrl(String orderId, double amount, String orderInfo, String returnUrl);
    Map<String, String> verifyPayment(Map<String, String> vnpParams);
}


