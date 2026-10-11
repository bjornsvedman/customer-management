package com.bjorn.customer_management.application.rest.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
		@NotBlank @Size(max = 50) String firstName,
		@NotBlank @Size(max = 50) String lastName,
		@NotNull @Past LocalDate dateOfBirth) {
	public CreateCustomerRequest {
		if (firstName != null) {
			firstName = firstName.strip();
		}
		if (lastName != null) {
			lastName = lastName.strip();
		}
	}
}