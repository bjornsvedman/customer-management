package com.bjorn.customer_management.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class BirthDateTest {
	@Test
	void acceptsDateInThePast() {
		LocalDate value = LocalDate.now(ZoneOffset.UTC).minusDays(1);

		assertEquals(value, new BirthDate(value).value());
	}

	@Test
	void rejectsTodayAndFutureDates() {
		LocalDate today = LocalDate.now(ZoneOffset.UTC);

		assertThrows(IllegalArgumentException.class, () -> new BirthDate(today));
		assertThrows(IllegalArgumentException.class, () -> new BirthDate(today.plusDays(1)));
	}

	@Test
	void doesNotImposeAnUnspecifiedMaximumAge() {
		LocalDate value = LocalDate.now(ZoneOffset.UTC).minusYears(200);

		assertEquals(value, new BirthDate(value).value());
	}
}