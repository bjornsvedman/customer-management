package com.bjorn.customer_management.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.bjorn.customer_management.domain.exception.DomainValidationException;

class CustomerIdTest {
	@Test
	void acceptsPositiveValue() {
		assertEquals(1L, new CustomerId(1L).value());
	}

	@Test
	void rejectsZeroAndNegativeValues() {
		assertThrows(DomainValidationException.class, () -> new CustomerId(0L));
		assertThrows(DomainValidationException.class, () -> new CustomerId(-1L));
	}
}