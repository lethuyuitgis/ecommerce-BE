package com.shopcuathuy.service;

import com.shopcuathuy.admin.dto.CategoryRevenueDTO;
import com.shopcuathuy.admin.dto.CustomerLocationDTO;
import com.shopcuathuy.admin.dto.CustomerTypeDTO;
import com.shopcuathuy.admin.dto.LowStockProductDTO;
import com.shopcuathuy.admin.dto.RevenuePointDTO;
import com.shopcuathuy.admin.dto.SellerAnalyticsDashboardDTO;
import com.shopcuathuy.admin.dto.SellerAnalyticsOverviewDTO;
import com.shopcuathuy.admin.dto.TopProductDTO;
import com.shopcuathuy.admin.dto.TrafficPointDTO;
import com.shopcuathuy.admin.dto.TrafficSourceDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.UserAddress;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderItemRepository;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserAddressRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerAnalyticsService {

    private static final Locale VI_LOCALE = Locale.forLanguageTag("vi-VN");

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;

    public SellerAnalyticsService(SellerRepository sellerRepository,
                                  OrderRepository orderRepository,
                                  OrderItemRepository orderItemRepository,
                                  ProductRepository productRepository,
                                  UserAddressRepository userAddressRepository) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userAddressRepository = userAddressRepository;
    }

    @Transactional(readOnly = true)
    public SellerAnalyticsDashboardDTO getDashboard(String userId, String period) {
        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        PeriodWindow window = resolvePeriod(period);
        List<Order> currentOrders = orderRepository.findBySellerIdAndCreatedAtBetween(
            seller.getId(), window.start(), window.end());
        List<Order> previousOrders = orderRepository.findBySellerIdAndCreatedAtBetween(
            seller.getId(), window.previousStart(), window.previousEnd());

        SellerAnalyticsDashboardDTO dto = new SellerAnalyticsDashboardDTO();
        dto.setOverview(buildOverview(currentOrders, previousOrders));
        dto.setRevenueSeries(buildRevenueSeries(currentOrders, window.startDate(), window.endDate()));
        dto.setCategorySeries(buildCategorySeries(seller.getId(), window));
        dto.setCustomerTypes(buildCustomerTypes(currentOrders));
        dto.setCustomerLocations(buildCustomerLocations(currentOrders));
        dto.setTrafficSeries(buildTrafficSeries(currentOrders, window));
        dto.setTrafficSources(buildTrafficSources(currentOrders));
        dto.setTopProducts(buildTopProducts(seller.getId(), window));
        dto.setLowStockProducts(buildLowStockProducts(seller.getId()));
        return dto;
    }

    private SellerAnalyticsOverviewDTO buildOverview(List<Order> currentOrders, List<Order> previousOrders) {
        double revenue = sumRevenue(currentOrders);
        double previousRevenue = sumRevenue(previousOrders);

        long orderCount = currentOrders.size();
        long previousOrderCount = previousOrders.size();

        double avgOrderValue = orderCount > 0 ? revenue / orderCount : 0.0;
        double previousAvgOrder = previousOrderCount > 0 ? sumRevenue(previousOrders) / previousOrderCount : 0.0;

        long uniqueCustomers = countUniqueCustomers(currentOrders);
        long previousUniqueCustomers = countUniqueCustomers(previousOrders);

        double conversionRate = uniqueCustomers > 0
            ? (double) orderCount / uniqueCustomers * 100
            : 0.0;
        double previousConversionRate = previousUniqueCustomers > 0
            ? (double) previousOrderCount / previousUniqueCustomers * 100
            : 0.0;

        SellerAnalyticsOverviewDTO overview = new SellerAnalyticsOverviewDTO();
        overview.setRevenue(revenue);
        overview.setRevenueChange(calculateChange(revenue, previousRevenue));
        overview.setOrders(orderCount);
        overview.setOrdersChange(calculateChange(orderCount, previousOrderCount));
        overview.setAverageOrderValue(avgOrderValue);
        overview.setAverageOrderValueChange(calculateChange(avgOrderValue, previousAvgOrder));
        overview.setConversionRate(conversionRate);
        overview.setConversionRateChange(calculateChange(conversionRate, previousConversionRate));
        return overview;
    }

    private List<RevenuePointDTO> buildRevenueSeries(List<Order> orders,
                                                     LocalDate start,
                                                     LocalDate end) {
        Map<LocalDate, RevenueAggregate> aggregates = new HashMap<>();
        for (Order order : orders) {
            LocalDate date = order.getCreatedAt() != null
                ? order.getCreatedAt().toLocalDate()
                : start;
            RevenueAggregate aggregate = aggregates.computeIfAbsent(date, key -> new RevenueAggregate());
            aggregate.orders++;
            aggregate.revenue += toDouble(order.getFinalTotal());
        }

        List<RevenuePointDTO> series = new ArrayList<>();
        for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
            RevenueAggregate aggregate = aggregates.getOrDefault(cursor, new RevenueAggregate());
            double profit = aggregate.revenue * 0.22;
            series.add(new RevenuePointDTO(cursor.toString(), aggregate.revenue, profit, aggregate.orders));
        }
        return series;
    }

    private List<CategoryRevenueDTO> buildCategorySeries(String sellerId, PeriodWindow window) {
        List<Object[]> rows = orderItemRepository.findCategoryBreakdown(
            sellerId, window.start(), window.end());
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream()
            .map(row -> new CategoryRevenueDTO(
                Objects.toString(row[0], "Khác"),
                toDouble((BigDecimal) row[1])
            ))
            .sorted(Comparator.comparing(CategoryRevenueDTO::getRevenue).reversed())
            .collect(Collectors.toList());
    }

    private List<CustomerTypeDTO> buildCustomerTypes(List<Order> orders) {
        Map<String, Long> orderCountByCustomer = orders.stream()
            .map(Order::getCustomer)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(User::getId, Collectors.counting()));

        long returning = orderCountByCustomer.values().stream()
            .filter(count -> count > 1)
            .count();
        long newCustomers = orderCountByCustomer.size() - returning;

        List<CustomerTypeDTO> types = new ArrayList<>();
        types.add(new CustomerTypeDTO("Khách mới", newCustomers, "#22c55e"));
        types.add(new CustomerTypeDTO("Khách trung thành", returning, "#fb923c"));
        return types;
    }

    private List<CustomerLocationDTO> buildCustomerLocations(List<Order> orders) {
        Set<String> customerIds = orders.stream()
            .map(Order::getCustomer)
            .filter(Objects::nonNull)
            .map(User::getId)
            .collect(Collectors.toSet());
        if (customerIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserAddress> addresses = userAddressRepository.findByUserIdIn(customerIds);
        Map<String, UserAddress> defaultAddressMap = new HashMap<>();
        for (UserAddress address : addresses) {
            if (address.getUser() == null) {
                continue;
            }
            String id = address.getUser().getId();
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                defaultAddressMap.put(id, address);
                continue;
            }
            defaultAddressMap.putIfAbsent(id, address);
        }

        Map<String, Integer> counts = new HashMap<>();
        for (String customerId : customerIds) {
            UserAddress address = defaultAddressMap.get(customerId);
            String province = address != null && address.getProvince() != null
                ? address.getProvince()
                : "Khác";
            counts.merge(province, 1, Integer::sum);
        }

        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .map(entry -> new CustomerLocationDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private List<TrafficPointDTO> buildTrafficSeries(List<Order> orders, PeriodWindow window) {
        Map<LocalDate, List<Order>> grouped = orders.stream()
            .collect(Collectors.groupingBy(order -> order.getCreatedAt() != null
                ? order.getCreatedAt().toLocalDate()
                : window.startDate()));

        List<TrafficPointDTO> points = new ArrayList<>();
        for (LocalDate cursor = window.startDate(); !cursor.isAfter(window.endDate()); cursor = cursor.plusDays(1)) {
            List<Order> dayOrders = grouped.getOrDefault(cursor, Collections.emptyList());
            int visitors = (int) dayOrders.stream()
                .map(Order::getCustomer)
                .filter(Objects::nonNull)
                .map(User::getId)
                .distinct()
                .count();
            int views = dayOrders.size() * 20 + visitors * 5;
            double bounceRate = visitors == 0
                ? 60.0
                : Math.max(5.0, 100.0 - (views / Math.max(1.0, visitors)) * 5.0);
            points.add(new TrafficPointDTO(cursor.toString(), views, visitors, roundOneDecimal(bounceRate)));
        }
        return points;
    }

    private List<TrafficSourceDTO> buildTrafficSources(List<Order> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Long> counts = orders.stream()
            .map(order -> order.getPaymentMethod() != null
                ? order.getPaymentMethod().toUpperCase(VI_LOCALE)
                : "KHÁC")
            .collect(Collectors.groupingBy(method -> method, LinkedHashMap::new, Collectors.counting()));

        return counts.entrySet().stream()
            .map(entry -> new TrafficSourceDTO(entry.getKey(), entry.getValue().intValue()))
            .collect(Collectors.toList());
    }

    private List<TopProductDTO> buildTopProducts(String sellerId, PeriodWindow window) {
        List<Object[]> rows = orderItemRepository.findProductPerformance(
            sellerId, window.start(), window.end());
        Map<String, Double> previousRevenue = orderItemRepository.findProductPerformance(
                sellerId, window.previousStart(), window.previousEnd())
            .stream()
            .collect(Collectors.toMap(
                row -> ((Product) row[0]).getId(),
                row -> toDouble((BigDecimal) row[2]),
                Double::sum));

        List<TopProductDTO> result = rows.stream()
            .limit(5)
            .map(row -> {
                Product product = (Product) row[0];
                long sold = ((Number) row[1]).longValue();
                double revenue = toDouble((BigDecimal) row[2]);
                double change = calculateChange(revenue, previousRevenue.getOrDefault(product.getId(), 0.0));

                TopProductDTO dto = new TopProductDTO();
                dto.setId(product.getId());
                dto.setName(product.getName());
                dto.setImage(resolveProductImage(product));
                dto.setSold((int) sold);
                dto.setRevenue(revenue);
                dto.setTrend(String.format(VI_LOCALE, "%+.1f%%", change));
                dto.setTrendUp(change >= 0);
                return dto;
            })
            .collect(Collectors.toList());

        if (result.isEmpty()) {
            productRepository.findTop5BySellerIdOrderByTotalSoldDesc(sellerId).forEach(product -> {
                TopProductDTO dto = new TopProductDTO();
                dto.setId(product.getId());
                dto.setName(product.getName());
                dto.setImage(resolveProductImage(product));
                dto.setSold(product.getTotalSold() != null ? product.getTotalSold() : 0);
                dto.setRevenue(product.getPrice() != null ? product.getPrice().doubleValue() * dto.getSold() : 0.0);
                dto.setTrend("+0.0%");
                dto.setTrendUp(true);
                result.add(dto);
            });
        }

        return result;
    }

    private List<LowStockProductDTO> buildLowStockProducts(String sellerId) {
        return productRepository.findTop5BySellerIdOrderByQuantityAsc(sellerId).stream()
            .map(product -> {
                int stock = product.getQuantity() != null ? product.getQuantity() : 0;
                String severity = stock <= 5 ? "critical" : "warning";
                return new LowStockProductDTO(
                    product.getName(),
                    stock,
                    severity
                );
            })
            .collect(Collectors.toList());
    }

    private double sumRevenue(Collection<Order> orders) {
        return orders.stream()
            .map(order -> toDouble(order.getFinalTotal()))
            .reduce(0.0, Double::sum);
    }

    private long countUniqueCustomers(Collection<Order> orders) {
        return orders.stream()
            .map(Order::getCustomer)
            .filter(Objects::nonNull)
            .map(User::getId)
            .distinct()
            .count();
    }

    private double calculateChange(double current, double previous) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    private PeriodWindow resolvePeriod(String period) {
        long days;
        if ("7days".equalsIgnoreCase(period)) {
            days = 7;
        } else if ("90days".equalsIgnoreCase(period)) {
            days = 90;
        } else if ("year".equalsIgnoreCase(period)) {
            days = 365;
        } else {
            days = 30;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        LocalDateTime previousEnd = startDateTime.minusSeconds(1);
        LocalDateTime previousStart = previousEnd.minusDays(days - 1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        return new PeriodWindow(startDateTime, endDateTime, previousStart, previousEnd, startDate, endDate);
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String resolveProductImage(Product product) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return null;
        }
        return product.getImages().stream()
            .sorted(Comparator.comparing(
                    (com.shopcuathuy.entity.ProductImage image) ->
                        Boolean.TRUE.equals(image.getIsPrimary()) ? 0 : 1)
                .thenComparing(image -> image.getDisplayOrder() != null ? image.getDisplayOrder() : 0))
            .map(com.shopcuathuy.entity.ProductImage::getImageUrl)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private static class RevenueAggregate {
        private int orders = 0;
        private double revenue = 0.0;
    }

    private record PeriodWindow(LocalDateTime start,
                                LocalDateTime end,
                                LocalDateTime previousStart,
                                LocalDateTime previousEnd,
                                LocalDate startDate,
                                LocalDate endDate) {
    }
}

