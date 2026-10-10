package com.bjorn.customer_management.domain.model;

import java.util.Objects;
import java.util.Optional;

public final class Customer {
	private final Optional<CustomerId> id;
	private final PersonName name;
	private final BirthDate birthDate;

	private Customer(Optional<CustomerId> id, PersonName name, BirthDate birthDate) {
		this.id = Objects.requireNonNull(id, "Customer ID state must not be null");
		this.name = Objects.requireNonNull(name, "Customer name must not be null");
		this.birthDate = Objects.requireNonNull(birthDate, "Birth date must not be null");
	}

	public static Customer create(PersonName name, BirthDate birthDate) {
		return new Customer(Optional.empty(), name, birthDate);
	}

	public static Customer reconstitute(CustomerId id, PersonName name, BirthDate birthDate) {
		return new Customer(Optional.of(Objects.requireNonNull(id, "Customer ID must not be null")), name, birthDate);
	}

	public Customer withId(CustomerId id) {
		if (this.id.isPresent()) {
			throw new IllegalStateException("Customer ID has already been assigned");
		}
		return reconstitute(id, name, birthDate);
	}

	public Optional<CustomerId> id() {
		return id;
	}

	public PersonName name() {
		return name;
	}

	public BirthDate birthDate() {
		return birthDate;
	}
}