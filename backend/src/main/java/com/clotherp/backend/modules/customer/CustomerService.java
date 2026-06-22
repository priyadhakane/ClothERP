package com.clotherp.backend.modules.customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    Customer updateCustomer(UUID id, Customer customer);
    Customer getCustomerById(UUID id);
    List<Customer> searchCustomers(String query);
}
