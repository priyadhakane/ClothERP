package com.clotherp.backend.modules.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SalesOrderService {
    SalesOrderDTO createSalesOrder(CreateSalesOrderRequest request);
    SalesOrderDTO getSalesOrderById(UUID id);
    Page<SalesOrderDTO> getSalesOrdersByBranch(UUID branchId, Pageable pageable);
    Page<SalesOrderDTO> getAllSalesOrders(Pageable pageable);
}
