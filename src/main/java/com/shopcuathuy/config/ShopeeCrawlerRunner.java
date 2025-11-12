package com.shopcuathuy.config;

import com.shopcuathuy.service.ShopeeCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * CommandLineRunner để crawl data từ Shopee
 * Chạy với: java -jar app.jar --shopee.crawl.enabled=true
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "shopee.crawl.enabled", havingValue = "true")
public class ShopeeCrawlerRunner implements CommandLineRunner {
    
    private final ShopeeCrawlerService shopeeCrawlerService;
    
    @Override
    public void run(String... args) {
        log.info("Starting Shopee crawler...");
        
        try {
            // Crawl categories
            log.info("Step 1: Crawling categories...");
            shopeeCrawlerService.crawlCategories();
            
            // Crawl products for specific categories
            log.info("Step 2: Crawling products...");
            String[] categoriesToCrawl = {
                "thoi-trang-nam",
                "dien-thoai-phu-kien",
                "may-tinh-laptop"
            };
            
            for (String categorySlug : categoriesToCrawl) {
                try {
                    log.info("Crawling products for category: {}", categorySlug);
                    shopeeCrawlerService.crawlProducts(categorySlug, 25); // Limit 20 products per category
                    Thread.sleep(5000); // Delay between categories
                } catch (Exception e) {
                    log.error("Error crawling products for category: {}", categorySlug, e);
                }
            }
            
            log.info("Shopee crawler completed successfully!");
        } catch (Exception e) {
            log.error("Error running Shopee crawler", e);
        }
    }
}

