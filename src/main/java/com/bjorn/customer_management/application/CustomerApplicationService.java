package com.bjorn.customer_management.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bjorn.customer_management.domain.model.BirthDate;
import com.bjorn.customer_management.domain.model.Customer;
import com.bjorn.customer_management.domain.model.CustomerId;
import com.bjorn.customer_management.domain.model.PersonName;
import com.bjorn.customer_management.domain.repository.CustomerRepository;

@Service
public class CustomerApplicationService {
	private final CustomerRepository customerRepository;

	public CustomerApplicationService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional
	public Customer createCustomer(PersonName name, BirthDate birthDate) {
		return customerRepository.save(Customer.create(name, birthDate));
	}

	@Transactional(readOnly = true)
	public Customer getCustomer(CustomerId id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new CustomerNotFoundException(id.value()));
	}
}