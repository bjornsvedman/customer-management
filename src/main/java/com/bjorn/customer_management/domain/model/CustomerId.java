package com.bjorn.customer_management.domain.model;

public record CustomerId(long value) {
	public CustomerId {
		if (value <= 0) {
			throw new IllegalArgumentException("Customer ID must be positive");
		}
	}
}