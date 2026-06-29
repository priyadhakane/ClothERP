package com.clotherp.backend;

import com.clotherp.backend.modules.branch.BranchRepository;
import com.clotherp.backend.modules.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private ProductRepository productRepository;

	@Test
	void contextLoads() {
		System.out.println("=== DIAGNOSTIC SYSTEM BRANCHES ===");
		branchRepository.findAll().forEach(b -> {
			System.out.println("Branch: " + b.getName() + " | Code: " + b.getCode() + " | ID: " + b.getId() + " | Active: " + b.isActive());
		});
		System.out.println("================================");

		System.out.println("=== DIAGNOSTIC SYSTEM PRODUCTS ===");
		productRepository.findAll().forEach(p -> {
			System.out.println("Product: " + p.getName() + " | SKU: " + p.getSku() + " | Branch ID: " + p.getBranchId() + " | Deleted: " + p.isDeleted());
		});
		System.out.println("================================");
	}

}
