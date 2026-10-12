package com.bjorn.customer_management.application.rest.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Customer details to create")
public record CreateCustomerRequest(
		@NotBlank @Size(max = 50) @Schema(description = "Required first name; trimmed before storage, up to 50 characters", example = "Deckard", maxLength = 50) String firstName,
		@NotBlank @Size(max = 50) @Schema(description = "Required last name; trimmed before storage, up to 50 characters", example = "Cain", maxLength = 50) String lastName,
		@NotNull @Past @Schema(description = "Required ISO date strictly before the current UTC date", type = "string", format = "date", example = "1985-05-20") LocalDate dateOfBirth) {
	public CreateCustomerRequest {
		if (firstName != null) {
			firstName = firstName.strip();
		}
		if (lastName != null) {
			lastName = lastName.strip();
		}
	}
}