package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.SellerNotificationPreference;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.SellerNotificationPreferenceRepository;
import com.shopcuathuy.repository.SellerRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/notification/preferences")
public class SellerNotificationPreferenceController {

    private static final Map<String, String[]> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put("ORDER_NEW",     new String[]{"Đơn hàng mới",          "Khi có đơn hàng mới được đặt"});
        DEFAULTS.put("ORDER_STATUS",  new String[]{"Cập nhật đơn hàng",     "Khi trạng thái đơn hàng thay đổi"});
        DEFAULTS.put("COMPLAINT",     new String[]{"Khiếu nại",             "Khi có khiếu nại mới hoặc cập nhật"});
        DEFAULTS.put("CHAT_MESSAGE",  new String[]{"Tin nhắn",              "Khi khách hàng gửi tin nhắn mới"});
        DEFAULTS.put("REVIEW_NEW",    new String[]{"Đánh giá sản phẩm",     "Khi có đánh giá mới cho sản phẩm"});
        DEFAULTS.put("PROMOTION",     new String[]{"Khuyến mãi & Voucher",  "Cập nhật về khuyến mãi và voucher"});
        DEFAULTS.put("SYSTEM",        new String[]{"Hệ thống",              "Thông báo từ hệ thống Shop Của Thuỳ"});
    }

    private final SellerRepository sellerRepository;
    private final SellerNotificationPreferenceRepository preferenceRepository;

    public SellerNotificationPreferenceController(SellerRepository sellerRepository,
                                                  SellerNotificationPreferenceRepository preferenceRepository) {
        this.sellerRepository = sellerRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @GetMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreferences(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Seller seller = requireSeller(userId);
        List<SellerNotificationPreference> rows = preferenceRepository.findBySellerId(seller.getId());
        if (rows.isEmpty()) {
            rows = seedDefaults(seller.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(wrap(rows)));
    }

    @PutMapping("/{preferenceId}")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateOne(
            @PathVariable String preferenceId,
            @RequestBody Map<String, Boolean> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Seller seller = requireSeller(userId);
        SellerNotificationPreference pref = preferenceRepository.findById(preferenceId)
            .orElseThrow(() -> new ResourceNotFoundException("Preference not found"));
        if (!pref.getSellerId().equals(seller.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Forbidden"));
        }
        if (body.get("emailEnabled") != null) pref.setEmailEnabled(body.get("emailEnabled"));
        if (body.get("pushEnabled") != null)  pref.setPushEnabled(body.get("pushEnabled"));
        preferenceRepository.save(pref);
        return ResponseEntity.ok(ApiResponse.success(toMap(pref)));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateAll(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Seller seller = requireSeller(userId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> incoming = (List<Map<String, Object>>) body.getOrDefault("preferences", new ArrayList<>());
        for (Map<String, Object> item : incoming) {
            String id = (String) item.get("id");
            if (id == null) continue;
            preferenceRepository.findById(id).ifPresent(pref -> {
                if (!pref.getSellerId().equals(seller.getId())) return;
                if (item.get("emailEnabled") instanceof Boolean b) pref.setEmailEnabled(b);
                if (item.get("pushEnabled") instanceof Boolean b) pref.setPushEnabled(b);
                preferenceRepository.save(pref);
            });
        }
        return ResponseEntity.ok(ApiResponse.success(wrap(preferenceRepository.findBySellerId(seller.getId()))));
    }

    private Seller requireSeller(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
    }

    private List<SellerNotificationPreference> seedDefaults(String sellerId) {
        List<SellerNotificationPreference> created = new ArrayList<>();
        for (Map.Entry<String, String[]> e : DEFAULTS.entrySet()) {
            SellerNotificationPreference pref = new SellerNotificationPreference();
            pref.setSellerId(sellerId);
            pref.setType(e.getKey());
            pref.setLabel(e.getValue()[0]);
            pref.setDescription(e.getValue()[1]);
            pref.setEmailEnabled(true);
            pref.setPushEnabled(true);
            created.add(preferenceRepository.save(pref));
        }
        return created;
    }

    private Map<String, Object> wrap(List<SellerNotificationPreference> rows) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (SellerNotificationPreference p : rows) list.add(toMap(p));
        out.put("preferences", list);
        return out;
    }

    private Map<String, Object> toMap(SellerNotificationPreference p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("type", p.getType());
        m.put("label", p.getLabel());
        m.put("description", p.getDescription());
        m.put("emailEnabled", p.isEmailEnabled());
        m.put("pushEnabled", p.isPushEnabled());
        return m;
    }
}
