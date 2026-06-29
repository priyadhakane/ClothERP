package com.clotherp.backend.modules.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByPhone(String phone);
    List<Customer> findByFullNameContainingIgnoreCaseOrPhoneContaining(String fullName, String phone);
}
