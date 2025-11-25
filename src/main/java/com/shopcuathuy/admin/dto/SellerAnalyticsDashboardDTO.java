package com.shopcuathuy.admin.dto;

import java.util.List;

public class SellerAnalyticsDashboardDTO {
    private SellerAnalyticsOverviewDTO overview;
    private List<RevenuePointDTO> revenueSeries;
    private List<CategoryRevenueDTO> categorySeries;
    private List<CustomerTypeDTO> customerTypes;
    private List<CustomerLocationDTO> customerLocations;
    private List<TrafficPointDTO> trafficSeries;
    private List<TrafficSourceDTO> trafficSources;
    private List<TopProductDTO> topProducts;
    private List<LowStockProductDTO> lowStockProducts;

    public SellerAnalyticsOverviewDTO getOverview() {
        return overview;
    }

    public void setOverview(SellerAnalyticsOverviewDTO overview) {
        this.overview = overview;
    }

    public List<RevenuePointDTO> getRevenueSeries() {
        return revenueSeries;
    }

    public void setRevenueSeries(List<RevenuePointDTO> revenueSeries) {
        this.revenueSeries = revenueSeries;
    }

    public List<CategoryRevenueDTO> getCategorySeries() {
        return categorySeries;
    }

    public void setCategorySeries(List<CategoryRevenueDTO> categorySeries) {
        this.categorySeries = categorySeries;
    }

    public List<CustomerTypeDTO> getCustomerTypes() {
        return customerTypes;
    }

    public void setCustomerTypes(List<CustomerTypeDTO> customerTypes) {
        this.customerTypes = customerTypes;
    }

    public List<CustomerLocationDTO> getCustomerLocations() {
        return customerLocations;
    }

    public void setCustomerLocations(List<CustomerLocationDTO> customerLocations) {
        this.customerLocations = customerLocations;
    }

    public List<TrafficPointDTO> getTrafficSeries() {
        return trafficSeries;
    }

    public void setTrafficSeries(List<TrafficPointDTO> trafficSeries) {
        this.trafficSeries = trafficSeries;
    }

    public List<TrafficSourceDTO> getTrafficSources() {
        return trafficSources;
    }

    public void setTrafficSources(List<TrafficSourceDTO> trafficSources) {
        this.trafficSources = trafficSources;
    }

    public List<TopProductDTO> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductDTO> topProducts) {
        this.topProducts = topProducts;
    }

    public List<LowStockProductDTO> getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(List<LowStockProductDTO> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }
}




