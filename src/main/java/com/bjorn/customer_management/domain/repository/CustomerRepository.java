package com.bjorn.customer_management.domain.repository;

import java.util.List;
import java.util.Optional;

import com.bjorn.customer_management.domain.model.Customer;
import com.bjorn.customer_management.domain.model.CustomerId;

public interface CustomerRepository {
	Customer save(Customer customer);
	Optional<Customer> findById(CustomerId id);
	List<Customer> findAll();
}