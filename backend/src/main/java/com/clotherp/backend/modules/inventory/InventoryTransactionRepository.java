package com.clotherp.backend.modules.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {

    Page<InventoryTransaction> findByInventoryItemId(UUID inventoryItemId, Pageable pageable);

    Page<InventoryTransaction> findByInventoryItemBranchId(UUID branchId, Pageable pageable);

    Page<InventoryTransaction> findByInventoryItemProductId(UUID productId, Pageable pageable);
}
