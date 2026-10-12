package com.bjorn.customer_management.application.rest.dto;

import java.time.LocalDate;

import com.bjorn.customer_management.domain.model.Customer;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Customer record")
public record CustomerResponse(long id, String firstName, String lastName, LocalDate dateOfBirth) {
	public static CustomerResponse from(Customer customer) {
		long id = customer.id()
				.orElseThrow(() -> new IllegalStateException("Persisted customer must have an ID"))
				.value();

		return new CustomerResponse(
				id,
				customer.name().firstName(),
				customer.name().lastName(),
				customer.birthDate().value());
	}
}