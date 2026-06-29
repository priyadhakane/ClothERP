package com.clotherp.backend.modules.product;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.clotherp.backend.common.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImportService {

    private final ProductService productService;

    public ImportResult importProductsFromExcel(MultipartFile file, UUID defaultBranchId) throws Exception {
        List<ProductDTO> productsToSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();
            
            log.info("=== Starting Excel Import ===");
            
            // Get the header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add("No header row found in Excel file");
                return new ImportResult(0, errors);
            }
            
            // Find column indices
            int nameCol = -1, skuCol = -1, categoryCol = -1, sizeCol = -1;
            int colorCol = -1, priceCol = -1, costCol = -1, materialCol = -1, descCol = -1;
            int branchIdCol = -1; // NEW: optional branch column
            
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String header = getCellValue(headerRow.getCell(i), dataFormatter).toLowerCase().trim();
                log.info("Column {}: '{}'", i, header);
                
                if (header.contains("name") || header.equals("name")) nameCol = i;
                else if (header.contains("sku")) skuCol = i;
                else if (header.contains("category")) categoryCol = i;
                else if (header.contains("size")) sizeCol = i;
                else if (header.contains("color")) colorCol = i;
                else if (header.contains("price")) priceCol = i;
                else if (header.contains("cost")) costCol = i;
                else if (header.contains("material")) materialCol = i;
                else if (header.contains("description")) descCol = i;
                else if (header.contains("branch") || header.contains("branchid")) branchIdCol = i;
            }
            
            log.info("Column mapping - Name: {}, SKU: {}, Category: {}, Size: {}, Color: {}, Price: {}, Cost: {}, Material: {}, Description: {}, Branch: {}", 
                nameCol, skuCol, categoryCol, sizeCol, colorCol, priceCol, costCol, materialCol, descCol, branchIdCol);
            
            // Process rows
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                if (isRowEmpty(row)) continue;
                
                int rowNum = row.getRowNum() + 1;
                log.info("Processing row {}...", rowNum);
                
                try {
                    ProductDTO product = ProductDTO.builder().build();
                    
                    // Name
                    String name = nameCol >= 0 ? getCellValue(row.getCell(nameCol), dataFormatter) : "";
                    if (name.isEmpty()) {
                        errors.add("Row " + rowNum + ": Product Name is required");
                        continue;
                    }
                    product.setName(name);
                    
                    // SKU
                    String sku = skuCol >= 0 ? getCellValue(row.getCell(skuCol), dataFormatter) : "";
                    if (sku.isEmpty()) {
                        errors.add("Row " + rowNum + ": SKU is required");
                        continue;
                    }
                    
                    try {
                        productService.getProductBySku(sku);
                        errors.add("Row " + rowNum + ": SKU '" + sku + "' already exists");
                        continue;
                    } catch (Exception e) {
                        // SKU doesn't exist - good to proceed
                    }
                    product.setSku(sku);
                    
                    // Category
                    product.setCategory(categoryCol >= 0 ? getCellValue(row.getCell(categoryCol), dataFormatter) : "");
                    
                    // Size
                    product.setSize(sizeCol >= 0 ? getCellValue(row.getCell(sizeCol), dataFormatter) : "");
                    
                    // Color
                    product.setColor(colorCol >= 0 ? getCellValue(row.getCell(colorCol), dataFormatter) : "");
                    
                    // Material
                    product.setMaterial(materialCol >= 0 ? getCellValue(row.getCell(materialCol), dataFormatter) : "");
                    
                    // Price
                    String priceStr = priceCol >= 0 ? getCellValue(row.getCell(priceCol), dataFormatter) : "";
                    if (priceStr.isEmpty()) {
                        errors.add("Row " + rowNum + ": Price is required");
                        continue;
                    }
                    try {
                        product.setPrice(new BigDecimal(priceStr));
                    } catch (NumberFormatException e) {
                        errors.add("Row " + rowNum + ": Invalid price format '" + priceStr + "'");
                        continue;
                    }
                    
                    // Cost
                    String costStr = costCol >= 0 ? getCellValue(row.getCell(costCol), dataFormatter) : "";
                    if (!costStr.isEmpty()) {
                        try {
                            product.setCost(new BigDecimal(costStr));
                        } catch (NumberFormatException e) {
                            errors.add("Row " + rowNum + ": Invalid cost format '" + costStr + "'");
                            continue;
                        }
                    } else {
                        product.setCost(product.getPrice());
                    }
                    
                    // Description
                    product.setDescription(descCol >= 0 ? getCellValue(row.getCell(descCol), dataFormatter) : "");
                    
                    // ✅ NEW: Get branch ID - if not in Excel, use a default or ask user
                    String branchIdStr = branchIdCol >= 0 ? getCellValue(row.getCell(branchIdCol), dataFormatter) : "";
                    UUID branchId = null;
                    
                    if (!branchIdStr.isEmpty()) {
                        try {
                            branchId = UUID.fromString(branchIdStr);
                        } catch (IllegalArgumentException e) {
                            errors.add("Row " + rowNum + ": Invalid branch ID format '" + branchIdStr + "'");
                            continue;
                        }
                    }
                    
                    // If branch ID is not provided, use a default
                    if (branchId == null) {
                        branchId = defaultBranchId;
                    }
                    
                    if (branchId == null) {
                        errors.add("Row " + rowNum + ": Branch ID is required. Please select a branch or add a 'Branch' column to your Excel file.");
                        continue;
                    }
                    product.setBranchId(branchId);
                    
                    log.info("Row {}: Valid product - Name: {}, SKU: {}, Price: {}, Branch: {}", 
                        rowNum, product.getName(), product.getSku(), product.getPrice(), branchId);
                    
                    productsToSave.add(product);
                    
                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                    log.error("Error processing row {}: {}", rowNum, e.getMessage(), e);
                }
            }
            
            log.info("Total valid products to save: {}", productsToSave.size());
            
            // Save each product
            for (ProductDTO product : productsToSave) {
                try {
                    log.info("Saving product: {} ({})", product.getName(), product.getSku());
                    productService.createProduct(product);
                    successCount++;
                    log.info("✅ Successfully saved product: {}", product.getSku());
                } catch (BusinessException e) {
                    errors.add("SKU '" + product.getSku() + "' already exists (skipped)");
                    log.warn("⚠️ SKU already exists: {}", product.getSku());
                } catch (Exception e) {
                    errors.add("Failed to save SKU '" + product.getSku() + "': " + e.getMessage());
                    log.error("Error saving product with SKU {}: {}", product.getSku(), e.getMessage(), e);
                }
            }
            
            log.info("=== IMPORT COMPLETED: {} products imported, {} errors ===", successCount, errors.size());
            return new ImportResult(successCount, errors);
            
        } catch (Exception e) {
            log.error("Error importing products: {}", e.getMessage(), e);
            throw new Exception("Failed to import products: " + e.getMessage());
        }
    }

    private String getCellValue(Cell cell, DataFormatter dataFormatter) {
        if (cell == null) return "";
        return dataFormatter.formatCellValue(cell).trim();
    }
    
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public static class ImportResult {
        private final int successCount;
        private final List<String> errors;
        
        public ImportResult(int successCount, List<String> errors) {
            this.successCount = successCount;
            this.errors = errors;
        }
        
        public int getSuccessCount() { return successCount; }
        public List<String> getErrors() { return errors; }
        public boolean hasErrors() { return !errors.isEmpty(); }
    }
}