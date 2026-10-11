package com.bjorn.customer_management.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, Long> {
}