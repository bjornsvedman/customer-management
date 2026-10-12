package com.bjorn.customer_management.domain.model;

import java.util.Objects;

import com.bjorn.customer_management.domain.exception.DomainValidationException;

public record PersonName(String firstName, String lastName) {
	public PersonName {
		firstName = normalize(firstName, "First name");
		lastName = normalize(lastName, "Last name");
	}

	private static String normalize(String name, String fieldName) {
		String normalized = Objects.requireNonNull(name, fieldName + " must not be null").strip();
		if (normalized.isBlank()) {
			throw new DomainValidationException(fieldName + " must not be blank");
		}
		if (normalized.length() > 50) {
			throw new DomainValidationException(fieldName + " must be at most 50 characters");
		}
		return normalized;
	}
}