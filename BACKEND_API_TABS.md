# Hướng dẫn tạo API cho các Tab trong Product Detail

## 1. API Inventory History

### Endpoint: `GET /api/products/{productId}/inventory/history`

**Controller:**
```java
@GetMapping("/products/{productId}/inventory/history")
public ResponseEntity<ApiResponse<Page<InventoryHistoryDTO>>> getInventoryHistory(
    @PathVariable String productId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page, size);
    Page<InventoryHistoryDTO> history = inventoryService.getProductHistory(productId, pageable);
    return ResponseEntity.ok(ApiResponse.success(history));
}
```

**DTO:**
```java
public class InventoryHistoryDTO {
    private String id;
    private String productId;
    private String variantId;
    private Integer quantityChange;
    private String reason; // purchase, return, adjustment, restock, damage
    private String referenceId;
    private String createdAt;
    private String note;
    private String user;
    // getters and setters
}
```

**Service:**
```java
public Page<InventoryHistoryDTO> getProductHistory(String productId, Pageable pageable) {
    Page<InventoryHistory> history = inventoryHistoryRepository
        .findByProductIdOrderByCreatedAtDesc(productId, pageable);
    
    return history.map(item -> {
        InventoryHistoryDTO dto = new InventoryHistoryDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setVariantId(item.getVariantId());
        dto.setQuantityChange(item.getQuantityChange());
        dto.setReason(item.getReason().name());
        dto.setReferenceId(item.getReferenceId());
        dto.setCreatedAt(item.getCreatedAt().toString());
        dto.setNote(item.getNote());
        // Set user if available
        return dto;
    });
}
```

## 2. API Product Stats

### Endpoint: `GET /api/products/{productId}/stats?days=7`

**Controller:**
```java
@GetMapping("/products/{productId}/stats")
public ResponseEntity<ApiResponse<ProductStatsDTO>> getProductStats(
    @PathVariable String productId,
    @RequestParam(defaultValue = "7") int days
) {
    ProductStatsDTO stats = productStatsService.getProductStats(productId, days);
    return ResponseEntity.ok(ApiResponse.success(stats));
}
```

**DTO:**
```java
public class ProductStatsDTO {
    private List<SalesDataPoint> salesData;
    private List<ViewsDataPoint> viewsData;
    
    public static class SalesDataPoint {
        private String date;
        private Integer sales;
        private BigDecimal revenue;
        // getters and setters
    }
    
    public static class ViewsDataPoint {
        private String date;
        private Integer views;
        // getters and setters
    }
}
```

**Service:**
```java
public ProductStatsDTO getProductStats(String productId, int days) {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(days);
    
    // Get sales data from orders
    List<OrderItem> orderItems = orderItemRepository
        .findByProductIdAndCreatedAtBetween(productId, startDate, endDate);
    
    Map<String, SalesDataPoint> salesMap = new HashMap<>();
    Map<String, Integer> viewsMap = new HashMap<>();
    
    // Group by date
    for (OrderItem item : orderItems) {
        String date = item.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM"));
        salesMap.computeIfAbsent(date, k -> new SalesDataPoint()).addSale(item.getQuantity(), item.getPrice());
    }
    
    // Get views data (if you have a product_views table)
    // List<ProductView> views = productViewRepository.findByProductIdAndDateBetween(...)
    
    ProductStatsDTO stats = new ProductStatsDTO();
    stats.setSalesData(new ArrayList<>(salesMap.values()));
    stats.setViewsData(new ArrayList<>(viewsMap.values()));
    
    return stats;
}
```

## 3. API Reviews (đã có sẵn)

Endpoint: `GET /api/reviews/product/{productId}?page=0&size=20`

Component `ProductReviews` đã tích hợp sẵn API này.

## Lưu ý

- Nếu backend chưa có các endpoints này, frontend sẽ hiển thị empty state hoặc loading
- Có thể tạm thời return empty data để test UI
- Cần đảm bảo authentication headers (`Authorization`, `X-User-Id`) được gửi kèm


