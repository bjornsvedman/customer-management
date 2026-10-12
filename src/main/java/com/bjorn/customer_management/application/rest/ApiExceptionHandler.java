package com.bjorn.customer_management.application.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.bjorn.customer_management.application.CustomerNotFoundException;
import com.bjorn.customer_management.domain.exception.DomainValidationException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {
		List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> Map.of(
						"field", error.getField(),
						"message", error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()))
				.toList();
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"One or more request fields are invalid");
		problem.setTitle("Validation failed");
		problem.setProperty("errors", errors);

		return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"The request body is missing or malformed");
		problem.setTitle("Malformed request");

		return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(
			TypeMismatchException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {
		if (!(exception instanceof MethodArgumentTypeMismatchException)) {
			return super.handleTypeMismatch(exception, headers, status, request);
		}
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
			"Customer ID must be a valid integer");
		problem.setTitle("Invalid path parameter");

		return handleExceptionInternal(exception, problem, headers, HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(DomainValidationException.class)
	public ResponseEntity<ProblemDetail> handleDomainValidation(DomainValidationException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
			"One or more customer values are invalid");
		problem.setTitle("Invalid customer");

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred");
		problem.setTitle("Internal server error");

		return ResponseEntity.internalServerError().body(problem);
	}

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleCustomerNotFound(CustomerNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setTitle("Customer not found");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}
}