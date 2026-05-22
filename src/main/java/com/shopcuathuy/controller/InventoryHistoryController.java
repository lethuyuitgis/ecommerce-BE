package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.InventoryHistory;
import com.shopcuathuy.repository.InventoryHistoryRepository;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class InventoryHistoryController {

    private final InventoryHistoryRepository repository;

    public InventoryHistoryController(InventoryHistoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{productId}/inventory/history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistory(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<InventoryHistory> result = repository.findByProductIdOrderByCreatedAtDesc(
            productId, PageRequest.of(page, size));

        List<Map<String, Object>> content = result.getContent().stream()
            .map(this::toMap)
            .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", content);
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        body.put("size", result.getSize());
        body.put("number", result.getNumber());
        return ResponseEntity.ok(ApiResponse.success(body));
    }

    private Map<String, Object> toMap(InventoryHistory h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId());
        m.put("productId", h.getProductId());
        m.put("variantId", h.getVariantId());
        m.put("quantityChange", h.getQuantityChange());
        m.put("reason", h.getReason());
        m.put("referenceId", h.getReferenceId());
        m.put("note", h.getNote());
        m.put("user", h.getUserLabel());
        m.put("createdAt", h.getCreatedAt() != null
            ? h.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        return m;
    }
}
