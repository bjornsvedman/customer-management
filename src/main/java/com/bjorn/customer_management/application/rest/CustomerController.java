package com.bjorn.customer_management.application.rest;

import java.net.URI;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bjorn.customer_management.application.CustomerApplicationService;
import com.bjorn.customer_management.application.rest.dto.CreateCustomerRequest;
import com.bjorn.customer_management.application.rest.dto.CustomerResponse;
import com.bjorn.customer_management.domain.model.BirthDate;
import com.bjorn.customer_management.domain.model.Customer;
import com.bjorn.customer_management.domain.model.CustomerId;
import com.bjorn.customer_management.domain.model.PersonName;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
	private final CustomerApplicationService customerApplicationService;

	public CustomerController(CustomerApplicationService customerApplicationService) {
		this.customerApplicationService = customerApplicationService;
	}

	@PostMapping
	public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
		PersonName name = new PersonName(request.firstName(), request.lastName());
		BirthDate birthDate = new BirthDate(request.dateOfBirth());
		Customer customer = customerApplicationService.createCustomer(name, birthDate);
		CustomerResponse response = CustomerResponse.from(customer);
		URI location = Objects.requireNonNull(URI.create("/api/v1/customers/" + response.id()));

		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CustomerResponse> getCustomer(@PathVariable long id) {
		Customer customer = customerApplicationService.getCustomer(new CustomerId(id));
		return ResponseEntity.ok(CustomerResponse.from(customer));
	}
}