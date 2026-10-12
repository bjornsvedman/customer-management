package com.bjorn.customer_management.application.rest;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(
		title = "Customer Management API",
		version = "1.0.0",
		description = "API for creating and retrieving customer records."))
public class OpenApiConfiguration {
}