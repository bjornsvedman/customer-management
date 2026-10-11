package com.bjorn.customer_management.application.rest;

import java.time.Clock;
import java.time.ZoneOffset;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfiguration {
	@Bean
	public LocalValidatorFactoryBean defaultValidator() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.setConfigurationInitializer(configuration ->
				configuration.clockProvider(() -> Clock.system(ZoneOffset.UTC)));
		return validator;
	}
}