package com.bjorn.customer_management.application;

public class CustomerNotFoundException extends RuntimeException {
	public CustomerNotFoundException(long customerId) {
		super("Customer with ID " + customerId + " was not found");
	}
}