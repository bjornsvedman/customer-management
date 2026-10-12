package com.bjorn.customer_management.application.rest.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "RFC 7807 problem response; errors is present for field-validation failures")
public record ApiProblemDetail(
		@Schema(description = "Problem type URI", example = "about:blank") String type,
		@Schema(description = "Short summary of the problem", example = "Validation failed") String title,
		@Schema(description = "HTTP status code", example = "400") Integer status,
		@Schema(description = "General explanation of the problem", example = "One or more request fields are invalid") String detail,
		@Schema(description = "Request path that caused the problem", example = "/api/v1/customers") String instance,
		@Schema(description = "Field-level validation errors, omitted when the request has no field errors") List<ApiFieldError> errors) {
}