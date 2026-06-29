package com.clotherp.backend.modules.accounting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JournalItemRepository extends JpaRepository<JournalItem, UUID> {
    // Additional query methods can be added here if needed
}
