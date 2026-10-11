package com.bjorn.customer_management.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.bjorn.customer_management.domain.model.BirthDate;
import com.bjorn.customer_management.domain.model.Customer;
import com.bjorn.customer_management.domain.model.CustomerId;
import com.bjorn.customer_management.domain.model.PersonName;
import com.bjorn.customer_management.domain.repository.CustomerRepository;

@Repository
public class CustomerPersistenceAdapter implements CustomerRepository {
	private final SpringDataCustomerRepository springDataCustomerRepository;

	public CustomerPersistenceAdapter(SpringDataCustomerRepository springDataCustomerRepository) {
		this.springDataCustomerRepository = springDataCustomerRepository;
	}

	@Override
	public Customer save(Customer customer) {
		if (customer.id().isPresent()) {
			throw new IllegalStateException("Customer ID has already been assigned");
		}

		CustomerJpaEntity entity = new CustomerJpaEntity(
				customer.name().firstName(),
				customer.name().lastName(),
				customer.birthDate().value());
		CustomerJpaEntity savedEntity = springDataCustomerRepository.save(entity);

		return customer.withId(new CustomerId(savedEntity.getId()));
	}

	@Override
	public Optional<Customer> findById(CustomerId id) {
		return springDataCustomerRepository.findById(id.value())
				.map(this::toDomain);
	}

	@Override
	public List<Customer> findAll() {
		return springDataCustomerRepository.findAll(Sort.by("id").descending()).stream()
				.map(this::toDomain)
				.toList();
	}

	private Customer toDomain(CustomerJpaEntity entity) {
		return Customer.reconstitute(
				new CustomerId(entity.getId()),
				new PersonName(entity.getFirstName(), entity.getLastName()),
				new BirthDate(entity.getDateOfBirth()));
	}
}