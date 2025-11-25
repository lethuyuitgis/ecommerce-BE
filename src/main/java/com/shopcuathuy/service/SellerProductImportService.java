package com.shopcuathuy.service;

import com.shopcuathuy.dto.response.ExportedFileDTO;
import com.shopcuathuy.dto.response.ImportProductResultDTO;
import com.shopcuathuy.entity.Category;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductImage;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.repository.CategoryRepository;
import com.shopcuathuy.repository.ProductRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SellerProductImportService {

    private static final Logger logger = LoggerFactory.getLogger(SellerProductImportService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public SellerProductImportService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ImportProductResultDTO importProducts(Seller seller, MultipartFile file) {
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("File import không chứa sheet dữ liệu");
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                try {
                    Product product = buildProductFromRow(row, seller);
                    productRepository.save(product);
                    success++;
                } catch (Exception ex) {
                    failed++;
                    String message = String.format("Dòng %d: %s", rowIndex + 1, ex.getMessage());
                    errors.add(message);
                    logger.warn("Import sản phẩm thất bại tại dòng {}: {}", rowIndex + 1, ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file import", e);
        }

        return new ImportProductResultDTO(success, failed, errors);
    }

    private Product buildProductFromRow(Row row, Seller seller) {
        String name = getCellString(row, 0);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }

        BigDecimal price = getCellDecimal(row, 2);
        if (price == null) {
            throw new IllegalArgumentException("Giá sản phẩm không hợp lệ");
        }

        String categoryValue = getCellString(row, 4);
        Category category = resolveCategory(categoryValue);

        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setSeller(seller);
        product.setCategory(category);
        product.setName(name.trim());
        product.setDescription(getCellString(row, 1));
        product.setPrice(price);
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }

        BigDecimal comparePrice = getCellDecimal(row, 3);
        if (comparePrice != null) {
            product.setComparePrice(comparePrice);
        }

        product.setQuantity(getCellInteger(row, 10, 0));
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setSku(Optional.ofNullable(getCellString(row, 5)).filter(s -> !s.isBlank()).orElse(UUID.randomUUID().toString()));
        product.setIsFeatured(false);
        product.setMinOrder(1);
        product.setRating(BigDecimal.ZERO);
        product.setTotalReviews(0);
        product.setTotalSold(0);
        product.setTotalViews(0);

        List<String> imageUrls = parseList(getCellString(row, 6));
        if (!imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                ProductImage image = new ProductImage();
                image.setId(UUID.randomUUID().toString());
                image.setProduct(product);
                image.setImageUrl(imageUrls.get(i).trim());
                image.setDisplayOrder(i);
                image.setIsPrimary(i == 0);
                image.setAltText(product.getName());
                product.getImages().add(image);
            }
        }

        return product;
    }

    public ExportedFileDTO exportProducts(Seller seller) {
        List<Product> products = productRepository.findBySellerId(seller.getId());
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");
            createHeaderRow(sheet);
            int rowIndex = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIndex++);
                writeProductRow(product, row);
            }

            for (int i = 0; i < 11; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            String filename = "products_" + LocalDate.now() + ".xlsx";
            Resource resource = new ByteArrayResource(out.toByteArray());
            return new ExportedFileDTO(resource, filename);
        } catch (IOException e) {
            throw new RuntimeException("Không thể xuất danh sách sản phẩm", e);
        }
    }

    private void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Tên sản phẩm");
        header.createCell(1).setCellValue("Mô tả");
        header.createCell(2).setCellValue("Giá");
        header.createCell(3).setCellValue("Giá so sánh");
        header.createCell(4).setCellValue("Danh mục");
        header.createCell(5).setCellValue("SKU");
        header.createCell(6).setCellValue("Hình ảnh");
        header.createCell(7).setCellValue("Số lượng");
        header.createCell(8).setCellValue("Đã bán");
        header.createCell(9).setCellValue("Trạng thái");
        header.createCell(10).setCellValue("Ngày tạo");
    }

    private void writeProductRow(Product product, Row row) {
        row.createCell(0).setCellValue(Optional.ofNullable(product.getName()).orElse(""));
        row.createCell(1).setCellValue(Optional.ofNullable(product.getDescription()).orElse(""));
        row.createCell(2).setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0);
        row.createCell(3).setCellValue(product.getComparePrice() != null ? product.getComparePrice().doubleValue() : 0);
        row.createCell(4).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "");
        row.createCell(5).setCellValue(Optional.ofNullable(product.getSku()).orElse(""));
        row.createCell(6).setCellValue(product.getImages() != null && !product.getImages().isEmpty()
            ? String.join(", ", product.getImages().stream().map(ProductImage::getImageUrl).toList())
            : "");
        row.createCell(7).setCellValue(product.getQuantity());
        row.createCell(8).setCellValue(product.getTotalSold());
        row.createCell(9).setCellValue(product.getStatus() != null ? product.getStatus().name() : "");
        row.createCell(10).setCellValue(product.getCreatedAt() != null ? product.getCreatedAt().toString() : "");
    }

    private Category resolveCategory(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Danh mục không được để trống");
        }
        return categoryRepository.findBySlug(value.trim())
            .or(() -> categoryRepository.findByNameIgnoreCase(value.trim()))
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục: " + value));
    }

    private String getCellString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private BigDecimal getCellDecimal(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return null;
        }
        try {
            return switch (cell.getCellType()) {
                case STRING -> {
                    String value = cell.getStringCellValue();
                    yield value == null || value.isBlank() ? null : new BigDecimal(value.trim());
                }
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                default -> null;
            };
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Giá trị số không hợp lệ ở cột " + (index + 1));
        }
    }

    private int getCellInteger(Row row, int index, int defaultValue) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return defaultValue;
        }
        try {
            return switch (cell.getCellType()) {
                case STRING -> {
                    String value = cell.getStringCellValue();
                    yield value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
                }
                case NUMERIC -> (int) cell.getNumericCellValue();
                default -> defaultValue;
            };
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private List<String> parseList(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        String[] parts = raw.split(",");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                result.add(part.trim());
            }
        }
        return result;
    }
}

