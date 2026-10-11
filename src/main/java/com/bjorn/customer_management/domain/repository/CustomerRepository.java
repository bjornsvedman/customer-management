package com.bjorn.customer_management.domain.repository;

import com.bjorn.customer_management.domain.model.Customer;

public interface CustomerRepository {
	Customer save(Customer customer);
}