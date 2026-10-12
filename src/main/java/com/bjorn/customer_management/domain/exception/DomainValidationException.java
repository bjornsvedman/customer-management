package com.bjorn.customer_management.domain.exception;

public class DomainValidationException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	public DomainValidationException(String message) {
		super(message);
	}
}