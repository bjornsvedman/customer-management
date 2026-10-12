package com.bjorn.customer_management.domain.model;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

import com.bjorn.customer_management.domain.exception.DomainValidationException;

public record BirthDate(LocalDate value) {
	public BirthDate {
		Objects.requireNonNull(value, "Birth date must not be null");
		if (!value.isBefore(LocalDate.now(ZoneOffset.UTC))) {
			throw new DomainValidationException("Birth date must be in the past");
		}
	}
}