package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.SellerReportDTO;
import com.shopcuathuy.service.SellerReportService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/reports")
public class SellerReportController {

    private final SellerReportService reportService;

    public SellerReportController(SellerReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SellerReportDTO>> getSummary(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "all") String reportType
    ) {
        LocalDate[] dateRange = resolveDateRange(period, startDate, endDate);
        SellerReportDTO summary = reportService.getReportSummary(userId, dateRange[0], dateRange[1], reportType);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/export")
    public ResponseEntity<Resource> exportReport(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false, defaultValue = "EXCEL") String type,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "all") String reportType
    ) {
        try {
            LocalDate[] dateRange = resolveDateRange(period, startDate, endDate);
            LocalDate start = dateRange[0];
            LocalDate end = dateRange[1];

            Resource file = reportService.exportReport(userId, type, start, end, reportType);

            String contentType = type.equalsIgnoreCase("PDF") 
                ? MediaType.APPLICATION_PDF_VALUE 
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            
            String extension = type.equalsIgnoreCase("PDF") ? "pdf" : "xlsx";
            String filename = String.format("bao-cao-%s-%s.%s", 
                period != null ? period : "custom",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                extension);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export report: " + e.getMessage(), e);
        }
    }

    private LocalDate[] resolveDateRange(String period, String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        if (start != null && end != null) {
            if (end.isBefore(start)) {
                LocalDate tmp = start;
                start = end;
                end = tmp;
            }
            return new LocalDate[]{start, end};
        }

        LocalDate calculatedEnd = LocalDate.now();
        LocalDate calculatedStart;

        if (period == null || period.equals("30days")) {
            calculatedStart = calculatedEnd.minusDays(30);
        } else if (period.equals("7days")) {
            calculatedStart = calculatedEnd.minusDays(7);
        } else if (period.equals("90days")) {
            calculatedStart = calculatedEnd.minusDays(90);
        } else if (period.equals("year")) {
            calculatedStart = calculatedEnd.withDayOfYear(1);
        } else {
            calculatedStart = calculatedEnd.minusDays(30);
        }

        return new LocalDate[]{calculatedStart, calculatedEnd};
    }
}


