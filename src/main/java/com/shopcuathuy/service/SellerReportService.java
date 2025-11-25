package com.shopcuathuy.service;

import com.shopcuathuy.admin.dto.RevenuePointDTO;
import com.shopcuathuy.dto.SellerReportDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ReportAudit;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.ReportAuditRepository;
import com.shopcuathuy.repository.SellerRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SellerReportService {

    private static final Logger log = LoggerFactory.getLogger(SellerReportService.class);
    private static final Locale VI_LOCALE = Locale.forLanguageTag("vi-VN");

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ReportAuditRepository reportAuditRepository;

    public SellerReportService(SellerRepository sellerRepository,
                               OrderRepository orderRepository,
                               ProductRepository productRepository,
                               ReportAuditRepository reportAuditRepository) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.reportAuditRepository = reportAuditRepository;
    }

    public SellerReportDTO getReportSummary(String userId, LocalDate startDate, LocalDate endDate, String reportType) {
        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
        return generateReportData(seller.getId(), startDate, endDate, reportType);
    }

    public Resource exportReport(String userId, String type, LocalDate startDate, LocalDate endDate, String reportType) {
        long startedAt = System.currentTimeMillis();
        ReportAudit audit = new ReportAudit();
        audit.setUserId(userId);
        audit.setReportType(reportType != null ? reportType : "all");
        audit.setExportFormat(type != null ? type.toUpperCase(Locale.ROOT) : "EXCEL");
        try {
            Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
            audit.setSellerId(seller.getId());

            SellerReportDTO reportData = generateReportData(seller.getId(), startDate, endDate, reportType);
            audit.setPeriodStart(reportData.getStartDate());
            audit.setPeriodEnd(reportData.getEndDate());

            Resource resource = "PDF".equalsIgnoreCase(type)
                ? generatePDFReport(reportData)
                : generateExcelReport(reportData);

            audit.setStatus("SUCCESS");
            audit.setDurationMs(System.currentTimeMillis() - startedAt);
            reportAuditRepository.save(audit);
            return resource;
        } catch (Exception e) {
            audit.setStatus("FAILED");
            audit.setNotes(e.getMessage());
            audit.setDurationMs(System.currentTimeMillis() - startedAt);
            reportAuditRepository.save(audit);
            log.error("Error generating report", e);
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    private SellerReportDTO generateReportData(String sellerId,
                                               LocalDate startDate,
                                               LocalDate endDate,
                                               String reportType) {
        SellerReportDTO report = new SellerReportDTO();
        LocalDate effectiveStart = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate effectiveEnd = endDate != null ? endDate : LocalDate.now();
        if (effectiveEnd.isBefore(effectiveStart)) {
            LocalDate tmp = effectiveStart;
            effectiveStart = effectiveEnd;
            effectiveEnd = tmp;
        }

        LocalDateTime startDateTime = effectiveStart.atStartOfDay();
        LocalDateTime endDateTime = effectiveEnd.plusDays(1).atStartOfDay().minusSeconds(1);

        boolean includeOrders = includeSection(reportType, "orders");
        boolean includeProducts = includeSection(reportType, "inventory") || includeSection(reportType, "products");

        // Use optimized query with JOIN FETCH to avoid N+1 problem
        // Limit to 1000 most recent orders to avoid loading too much data
        List<Order> orders = includeOrders
            ? orderRepository.findBySellerIdAndCreatedAtBetweenWithCustomer(sellerId, startDateTime, endDateTime)
                .stream()
                .limit(1000)
                .collect(Collectors.toList())
            : Collections.emptyList();

        // Use aggregation queries for statistics instead of filtering in memory
        double totalRevenue = includeOrders
            ? toDouble(orderRepository.sumRevenueBySellerIdAndDateRangeAndStatus(
                sellerId, startDateTime, endDateTime, Order.OrderStatus.DELIVERED))
            : 0.0;
        
        long completedOrders = includeOrders
            ? orderRepository.countBySellerIdAndDateRangeAndStatus(
                sellerId, startDateTime, endDateTime, Order.OrderStatus.DELIVERED)
            : 0L;

        long totalOrders = includeOrders
            ? orderRepository.countBySellerIdAndDateRange(sellerId, startDateTime, endDateTime)
            : 0L;

        // Build order summaries (limit to 500 for display)
        List<SellerReportDTO.OrderSummary> orderSummaries = includeOrders
            ? orders.stream()
                .limit(500)
                .map(this::toOrderSummary)
                .collect(Collectors.toList())
            : Collections.emptyList();

        // Use optimized query with JOIN FETCH and limit products
        // Limit to 1000 products to avoid loading too much data
        Pageable productPageable = PageRequest.of(0, 1000);
        List<Product> products = includeProducts
            ? productRepository.findBySellerIdWithCategory(sellerId, productPageable)
            : Collections.emptyList();
        
        List<SellerReportDTO.ProductSummary> productSummaries = includeProducts
            ? products.stream()
                .limit(500) // Limit for display
                .map(this::toProductSummary)
                .collect(Collectors.toList())
            : Collections.emptyList();
        
        // Use aggregation query for active products count
        long activeProducts = includeProducts
            ? productRepository.countBySellerIdAndStatus(sellerId, Product.ProductStatus.ACTIVE)
            : 0L;
        
        long totalProducts = includeProducts
            ? productRepository.countBySellerId(sellerId)
            : 0L;

        List<SellerReportDTO.CustomerSummary> customerSummaries = includeOrders
            ? buildTopCustomers(orders)
            : Collections.emptyList();

        // Use aggregation query for status breakdown
        Map<String, Long> statusBreakdown = includeOrders
            ? orderRepository.countBySellerIdAndDateRangeGroupByStatus(sellerId, startDateTime, endDateTime)
                .stream()
                .collect(Collectors.toMap(
                    obj -> obj[0] != null ? obj[0].toString() : "UNKNOWN",
                    obj -> ((Number) obj[1]).longValue()
                ))
            : Collections.emptyMap();

        List<RevenuePointDTO> revenueSeries = includeOrders
            ? buildRevenueSeries(orders, effectiveStart, effectiveEnd)
            : Collections.emptyList();

        report.setTotalRevenue(totalRevenue);
        report.setTotalOrders((int) totalOrders);
        report.setCompletedOrders((int) completedOrders);
        report.setTotalProducts((int) totalProducts);
        report.setActiveProducts((int) activeProducts);
        report.setOrders(orderSummaries);
        report.setProducts(productSummaries);
        report.setRevenueSeries(revenueSeries);
        report.setStatusBreakdown(statusBreakdown);
        report.setTopCustomers(customerSummaries);
        report.setStartDate(effectiveStart);
        report.setEndDate(effectiveEnd);

        return report;
    }

    private SellerReportDTO.OrderSummary toOrderSummary(Order order) {
        SellerReportDTO.OrderSummary summary = new SellerReportDTO.OrderSummary();
        summary.setOrderNumber(order.getOrderNumber());
        summary.setCustomerName(order.getCustomer() != null ? order.getCustomer().getFullName() : "Khách lẻ");
        summary.setStatus(order.getStatus() != null ? order.getStatus().name() : "UNKNOWN");
        summary.setFinalTotal(toDouble(order.getFinalTotal()));
        summary.setCreatedAt(order.getCreatedAt());
        return summary;
    }

    private List<RevenuePointDTO> buildRevenueSeries(List<Order> orders,
                                                     LocalDate start,
                                                     LocalDate end) {
        Map<LocalDate, Double> revenueByDay = new HashMap<>();
        Map<LocalDate, Long> orderCountByDay = new HashMap<>();
        for (Order order : orders) {
            LocalDate date = order.getCreatedAt() != null
                ? order.getCreatedAt().toLocalDate()
                : start;
            if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                revenueByDay.merge(date, toDouble(order.getFinalTotal()), Double::sum);
            }
            orderCountByDay.merge(date, 1L, Long::sum);
        }
        List<RevenuePointDTO> series = new ArrayList<>();
        for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
            double revenueValue = revenueByDay.getOrDefault(cursor, 0.0);
            int orderCount = orderCountByDay.getOrDefault(cursor, 0L).intValue();
            series.add(new RevenuePointDTO(
                cursor.toString(),
                revenueValue,
                revenueValue * 0.2,
                orderCount
            ));
        }
        return series;
    }

    private SellerReportDTO.ProductSummary toProductSummary(Product product) {
        SellerReportDTO.ProductSummary summary = new SellerReportDTO.ProductSummary();
        summary.setName(product.getName());
        summary.setPrice(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
        summary.setQuantity(product.getQuantity() != null ? product.getQuantity() : 0);
        summary.setStatus(product.getStatus() != null ? product.getStatus().name() : "UNKNOWN");
        summary.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
        return summary;
    }

    private List<SellerReportDTO.CustomerSummary> buildTopCustomers(List<Order> orders) {
        Map<String, CustomerStats> statsMap = new HashMap<>();
        for (Order order : orders) {
            if (order.getCustomer() == null) {
                continue;
            }
            String customerId = order.getCustomer().getId();
            CustomerStats stats = statsMap.computeIfAbsent(customerId, id -> new CustomerStats());
            stats.customerId = customerId;
            stats.customerName = resolveCustomerName(order);
            stats.orderCount++;
            stats.totalSpent += toDouble(order.getFinalTotal());
            if (order.getCreatedAt() != null &&
                (stats.lastOrderAt == null || order.getCreatedAt().isAfter(stats.lastOrderAt))) {
                stats.lastOrderAt = order.getCreatedAt();
            }
        }

        return statsMap.values().stream()
            .sorted(Comparator.comparing(CustomerStats::getTotalSpent).reversed())
            .limit(5)
            .map(stats -> {
                SellerReportDTO.CustomerSummary dto = new SellerReportDTO.CustomerSummary();
                dto.setCustomerId(stats.customerId);
                dto.setCustomerName(stats.customerName);
                dto.setOrderCount(stats.orderCount);
                dto.setTotalSpent(stats.totalSpent);
                dto.setLastOrderAt(stats.lastOrderAt);
                return dto;
            })
            .collect(Collectors.toList());
    }

    private String resolveCustomerName(Order order) {
        if (order.getCustomer() == null) {
            return "Khách lẻ";
        }
        if (order.getCustomer().getFullName() != null && !order.getCustomer().getFullName().isBlank()) {
            return order.getCustomer().getFullName();
        }
        if (order.getCustomer().getEmail() != null) {
            return order.getCustomer().getEmail();
        }
        return "Khách lẻ";
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private boolean includeSection(String reportType, String section) {
        if (reportType == null || reportType.isBlank()) {
            return true;
        }
        if ("all".equalsIgnoreCase(reportType)) {
            return true;
        }
        return reportType.equalsIgnoreCase(section);
    }

    private Resource generateExcelReport(SellerReportDTO report) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);

        Sheet overviewSheet = workbook.createSheet("Tổng quan");
        int rowNum = 0;

        Row titleRow = overviewSheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO TỔNG QUAN");
        titleCell.setCellStyle(titleStyle);

        rowNum++;
        createRow(overviewSheet, rowNum++, "Kỳ báo cáo",
                report.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " - " + report.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        rowNum++;

        Row headerRow = overviewSheet.createRow(rowNum++);
        String[] headers = {"Chỉ số", "Giá trị"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        createRow(overviewSheet, rowNum++, "Doanh thu", formatCurrency(report.getTotalRevenue()));
        createRow(overviewSheet, rowNum++, "Tổng đơn hàng", String.valueOf(report.getTotalOrders()));
        createRow(overviewSheet, rowNum++, "Đơn hàng hoàn thành", String.valueOf(report.getCompletedOrders()));
        createRow(overviewSheet, rowNum++, "Tổng sản phẩm", String.valueOf(report.getTotalProducts()));
        createRow(overviewSheet, rowNum++, "Sản phẩm đang bán", String.valueOf(report.getActiveProducts()));

        for (int i = 0; i < headers.length; i++) {
            overviewSheet.autoSizeColumn(i);
        }

        if (report.getOrders() != null && !report.getOrders().isEmpty()) {
            Sheet ordersSheet = workbook.createSheet("Đơn hàng");
            rowNum = 0;

            Row ordersHeaderRow = ordersSheet.createRow(rowNum++);
            String[] orderHeaders = {"Mã đơn", "Khách hàng", "Trạng thái", "Tổng tiền", "Ngày tạo"};
            for (int i = 0; i < orderHeaders.length; i++) {
                Cell cell = ordersHeaderRow.createCell(i);
                cell.setCellValue(orderHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (SellerReportDTO.OrderSummary order : report.getOrders()) {
                Row row = ordersSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(order.getOrderNumber());
                row.createCell(1).setCellValue(order.getCustomerName());
                row.createCell(2).setCellValue(order.getStatus());
                row.createCell(3).setCellValue(order.getFinalTotal());
                row.createCell(4).setCellValue(order.getCreatedAt().format(formatter));
            }

            for (int i = 0; i < orderHeaders.length; i++) {
                ordersSheet.autoSizeColumn(i);
            }
        }

        if (report.getProducts() != null && !report.getProducts().isEmpty()) {
            Sheet productsSheet = workbook.createSheet("Sản phẩm");
            rowNum = 0;

            Row productsHeaderRow = productsSheet.createRow(rowNum++);
            String[] productHeaders = {"Tên sản phẩm", "Giá", "Số lượng", "Trạng thái", "Danh mục"};
            for (int i = 0; i < productHeaders.length; i++) {
                Cell cell = productsHeaderRow.createCell(i);
                cell.setCellValue(productHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            for (SellerReportDTO.ProductSummary product : report.getProducts()) {
                Row row = productsSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getName());
                row.createCell(1).setCellValue(product.getPrice());
                row.createCell(2).setCellValue(product.getQuantity());
                row.createCell(3).setCellValue(product.getStatus());
                row.createCell(4).setCellValue(product.getCategory());
            }

            for (int i = 0; i < productHeaders.length; i++) {
                productsSheet.autoSizeColumn(i);
            }
        }

        if (report.getTopCustomers() != null && !report.getTopCustomers().isEmpty()) {
            Sheet customersSheet = workbook.createSheet("Khách hàng");
            rowNum = 0;

            Row customersHeaderRow = customersSheet.createRow(rowNum++);
            String[] customerHeaders = {"Khách hàng", "Số đơn", "Tổng chi tiêu", "Đơn gần nhất"};
            for (int i = 0; i < customerHeaders.length; i++) {
                Cell cell = customersHeaderRow.createCell(i);
                cell.setCellValue(customerHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (SellerReportDTO.CustomerSummary customer : report.getTopCustomers()) {
                Row row = customersSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer.getCustomerName());
                row.createCell(1).setCellValue(customer.getOrderCount());
                row.createCell(2).setCellValue(formatCurrency(customer.getTotalSpent()));
                row.createCell(3).setCellValue(
                    customer.getLastOrderAt() != null ? customer.getLastOrderAt().format(formatter) : "-"
                );
            }

            for (int i = 0; i < customerHeaders.length; i++) {
                customersSheet.autoSizeColumn(i);
            }
        }

        if (report.getRevenueSeries() != null && !report.getRevenueSeries().isEmpty()) {
            Sheet revenueSheet = workbook.createSheet("Doanh thu ngày");
            rowNum = 0;
            Row revenueHeader = revenueSheet.createRow(rowNum++);
            String[] revenueHeaders = {"Ngày", "Doanh thu", "Số đơn"};
            for (int i = 0; i < revenueHeaders.length; i++) {
                Cell cell = revenueHeader.createCell(i);
                cell.setCellValue(revenueHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            for (RevenuePointDTO point : report.getRevenueSeries()) {
                Row row = revenueSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(point.getDate());
                row.createCell(1).setCellValue(point.getRevenue());
                row.createCell(2).setCellValue(point.getOrders());
            }
            for (int i = 0; i < revenueHeaders.length; i++) {
                revenueSheet.autoSizeColumn(i);
            }
        }

        if (report.getStatusBreakdown() != null && !report.getStatusBreakdown().isEmpty()) {
            Sheet statusSheet = workbook.createSheet("Trạng thái đơn");
            rowNum = 0;
            Row statusHeader = statusSheet.createRow(rowNum++);
            String[] statusHeaders = {"Trạng thái", "Số lượng"};
            for (int i = 0; i < statusHeaders.length; i++) {
                Cell cell = statusHeader.createCell(i);
                cell.setCellValue(statusHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            report.getStatusBreakdown().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    Row row = statusSheet.createRow(statusSheet.getPhysicalNumberOfRows());
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                });
            for (int i = 0; i < statusHeaders.length; i++) {
                statusSheet.autoSizeColumn(i);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }

    private Resource generatePDFReport(SellerReportDTO report) {
        try {
            return generateExcelReport(report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void createRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private String formatCurrency(double value) {
        return String.format(VI_LOCALE, "%,.0f ₫", value);
    }

    private static class CustomerStats {
        private String customerId;
        private String customerName;
        private long orderCount;
        private double totalSpent;
        private LocalDateTime lastOrderAt;

        public double getTotalSpent() {
            return totalSpent;
        }
    }
}

