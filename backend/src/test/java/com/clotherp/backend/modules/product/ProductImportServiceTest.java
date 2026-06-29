package com.clotherp.backend.modules.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.UUID;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class ProductImportServiceTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductImportService productImportService;

    private UUID defaultBranchId;

    @BeforeEach
    void setUp() {
        defaultBranchId = UUID.randomUUID();
    }

    @Test
    void testImportProductsFromExcel_WithDefaultBranchId() throws Exception {
        // Create an in-memory Excel workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "SKU", "Category", "Size", "Color", "Price (₹)", "Cost (₹)", "Material", "Description"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Add 1 test product row
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Test Product");
        dataRow.createCell(1).setCellValue("TST-PROD-001");
        dataRow.createCell(2).setCellValue("TestCategory");
        dataRow.createCell(3).setCellValue("M");
        dataRow.createCell(4).setCellValue("Red");
        dataRow.createCell(5).setCellValue(299.99);
        dataRow.createCell(6).setCellValue(150.00);
        dataRow.createCell(7).setCellValue("Cotton");
        dataRow.createCell(8).setCellValue("A cool test product");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.xlsx", 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
            bos.toByteArray()
        );

        // When productService.getProductBySku check happens, it should throw exception (SKU doesn't exist)
        when(productService.getProductBySku("TST-PROD-001")).thenThrow(new RuntimeException("Not found"));

        // Run the import
        ProductImportService.ImportResult result = productImportService.importProductsFromExcel(file, defaultBranchId);

        // Verify the result
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getErrors().size());

        // Verify productService.createProduct was called with defaultBranchId set
        ArgumentCaptor<ProductDTO> productCaptor = ArgumentCaptor.forClass(ProductDTO.class);
        verify(productService).createProduct(productCaptor.capture());
        
        ProductDTO savedProduct = productCaptor.getValue();
        assertEquals("Test Product", savedProduct.getName());
        assertEquals("TST-PROD-001", savedProduct.getSku());
        assertEquals(defaultBranchId, savedProduct.getBranchId());
        assertTrue(new BigDecimal("299.99").compareTo(savedProduct.getPrice()) == 0);
        assertTrue(new BigDecimal("150.00").compareTo(savedProduct.getCost()) == 0);
    }
}
