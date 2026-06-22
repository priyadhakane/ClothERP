package com.clotherp.backend.modules.sales;

import com.clotherp.backend.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping
    public ResponseEntity<ApiResponse<SalesOrderDTO>> createSalesOrder(@Valid @RequestBody CreateSalesOrderRequest request) {
        SalesOrderDTO created = salesOrderService.createSalesOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Billing transaction completed successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SalesOrderDTO>> getSalesOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(salesOrderService.getSalesOrderById(id)));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<Page<SalesOrderDTO>>> getSalesOrdersByBranch(
            @PathVariable UUID branchId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(salesOrderService.getSalesOrdersByBranch(branchId, pageable)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SalesOrderDTO>>> getAllSalesOrders(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(salesOrderService.getAllSalesOrders(pageable)));
    }
}
