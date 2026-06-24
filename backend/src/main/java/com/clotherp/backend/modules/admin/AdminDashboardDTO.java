package com.clotherp.backend.modules.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private long totalUsers;
    private long totalProducts;
    private long totalSalesOrders;
    private long totalInventoryItems;
    private long totalBranches;
}