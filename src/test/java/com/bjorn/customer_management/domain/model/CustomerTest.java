package com.bjorn.customer_management.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CustomerTest {
	@Test
	void createsAnUnpersistedCustomer() {
		PersonName name = new PersonName("Viper", "Napuatzi");
		BirthDate birthDate = new BirthDate(LocalDate.of(1815, 12, 10));

		Customer customer = Customer.create(name, birthDate);

		assertFalse(customer.id().isPresent());
		assertEquals(name, customer.name());
		assertEquals(birthDate, customer.birthDate());
	}

	@Test
	void assigningAnIdReturnsANewCustomerWithoutChangingTheOriginal() {
		Customer customer = Customer.create(
				new PersonName("Viper", "Napuatzi"),
				new BirthDate(LocalDate.of(1815, 12, 10)));
		CustomerId id = new CustomerId(1L);

		Customer persistedCustomer = customer.withId(id);

		assertFalse(customer.id().isPresent());
		assertEquals(id, persistedCustomer.id().orElseThrow());
	}

	@Test
	void rejectsReassigningAnId() {
		Customer customer = Customer.reconstitute(
				new CustomerId(1L),
				new PersonName("Viper", "Napuatzi"),
				new BirthDate(LocalDate.of(1815, 12, 10)));

		assertThrows(IllegalStateException.class, () -> customer.withId(new CustomerId(2L)));
	}
}