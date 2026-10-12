package com.bjorn.customer_management.application.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A validation error associated with a request field")
public record ApiFieldError(
		@Schema(description = "Name of the invalid request field", example = "dateOfBirth") String field,
		@Schema(description = "Reason the field value was rejected", example = "must be a past date") String message) {
}