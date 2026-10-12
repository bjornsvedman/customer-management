package com.bjorn.customer_management.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.bjorn.customer_management.domain.exception.DomainValidationException;

class PersonNameTest {
	@Test
	void trimsAndAcceptsValidNames() {
		PersonName name = new PersonName("  Viper ", " Napuatzi  ");

		assertEquals("Viper", name.firstName());
		assertEquals("Napuatzi", name.lastName());
	}

	@Test
	void acceptsNamesAtMaximumLength() {
		String name = "a".repeat(50);

		assertEquals(name, new PersonName(name, name).firstName());
	}

	@Test
	void rejectsBlankNames() {
		assertThrows(DomainValidationException.class, () -> new PersonName(" ", "Napuatzi"));
		assertThrows(DomainValidationException.class, () -> new PersonName("Viper", "\t"));
	}

	@Test
	void rejectsNamesLongerThanMaximumLength() {
		assertThrows(DomainValidationException.class, () -> new PersonName("a".repeat(51), "Napuatzi"));
		assertThrows(DomainValidationException.class, () -> new PersonName("Viper", "n".repeat(51)));
	}
}