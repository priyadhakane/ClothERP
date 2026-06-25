package com.clotherp.backend.modules.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clotherp.backend.modules.branch.BranchRepository;
import com.clotherp.backend.modules.product.ProductRepository;
import com.clotherp.backend.modules.sales.SalesOrderRepository;
import com.clotherp.backend.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardStats() {
        return AdminDashboardDTO.builder()
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .totalSalesOrders(salesOrderRepository.count())
                .totalBranches(branchRepository.count())
                .build();
    }
}
