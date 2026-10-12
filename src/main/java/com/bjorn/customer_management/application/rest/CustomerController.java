package com.bjorn.customer_management.application.rest;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bjorn.customer_management.application.CustomerApplicationService;
import com.bjorn.customer_management.application.rest.dto.ApiProblemDetail;
import com.bjorn.customer_management.application.rest.dto.CreateCustomerRequest;
import com.bjorn.customer_management.application.rest.dto.CustomerResponse;
import com.bjorn.customer_management.domain.model.BirthDate;
import com.bjorn.customer_management.domain.model.Customer;
import com.bjorn.customer_management.domain.model.CustomerId;
import com.bjorn.customer_management.domain.model.PersonName;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.headers.Header;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Create and retrieve customer records")
public class CustomerController {
	private final CustomerApplicationService customerApplicationService;

	public CustomerController(CustomerApplicationService customerApplicationService) {
		this.customerApplicationService = customerApplicationService;
	}

	@PostMapping
	@Operation(summary = "Create a customer", description = "Creates a customer after validating and trimming the supplied names.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Customer created",
					headers = @Header(name = "Location", description = "URI of the created customer", schema = @Schema(type = "string", example = "/api/v1/customers/1")),
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomerResponse.class))),
			@ApiResponse(responseCode = "400", description = "Request is malformed or contains invalid customer fields",
					content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ApiProblemDetail.class)))
	})
	public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
		PersonName name = new PersonName(request.firstName(), request.lastName());
		BirthDate birthDate = new BirthDate(request.dateOfBirth());
		Customer customer = customerApplicationService.createCustomer(name, birthDate);
		CustomerResponse response = CustomerResponse.from(customer);
		URI location = Objects.requireNonNull(URI.create("/api/v1/customers/" + response.id()));

		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a customer", description = "Retrieves one customer by its positive identifier.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Customer found",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomerResponse.class))),
			@ApiResponse(responseCode = "400", description = "Customer ID is invalid",
					content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ApiProblemDetail.class))),
			@ApiResponse(responseCode = "404", description = "Customer not found",
					content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ApiProblemDetail.class)))
	})
	public ResponseEntity<CustomerResponse> getCustomer(
			@Parameter(description = "Positive generated customer ID", example = "1", schema = @Schema(minimum = "1"))
			@PathVariable long id) {
		Customer customer = customerApplicationService.getCustomer(new CustomerId(id));
		return ResponseEntity.ok(CustomerResponse.from(customer));
	}

	@GetMapping
	@Operation(summary = "List customers", description = "Returns all customers in descending ID order, newest first.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Customers returned, possibly an empty list",
					content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class))))
	})
	public ResponseEntity<List<CustomerResponse>> getCustomers() {
		List<CustomerResponse> responses = customerApplicationService.getCustomers().stream()
				.map(CustomerResponse::from)
				.toList();
		return ResponseEntity.ok(responses);
	}
}