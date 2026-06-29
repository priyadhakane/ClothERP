package com.clotherp.backend.modules.branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByActiveTrue();
    boolean existsByCode(String code);
    Optional<Branch> findByCode(String code);
}
