package com.bjorn.customer_management.domain.model;

import com.bjorn.customer_management.domain.exception.DomainValidationException;

public record CustomerId(long value) {
	public CustomerId {
		if (value <= 0) {
			throw new DomainValidationException("Customer ID must be positive");
		}
	}
}