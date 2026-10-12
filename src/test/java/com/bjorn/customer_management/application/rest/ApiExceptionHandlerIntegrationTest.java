package com.bjorn.customer_management.application.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bjorn.customer_management.application.CustomerApplicationService;
import com.bjorn.customer_management.domain.model.CustomerId;

@SpringBootTest
@AutoConfigureMockMvc
class ApiExceptionHandlerIntegrationTest {
	private static final String SENSITIVE_MESSAGE = "private-customer-error-canary";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CustomerApplicationService customerApplicationService;

	@Test
	void returnsSafeProblemDetailForUnexpectedFailure() throws Exception {
		doThrow(new IllegalStateException(SENSITIVE_MESSAGE))
				.when(customerApplicationService).getCustomer(new CustomerId(1L));

		mockMvc.perform(get("/api/v1/customers/{id}", 1L))
				.andExpect(status().isInternalServerError())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.title").value("Internal server error"))
				.andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
				.andExpect(content().string(not(containsString(SENSITIVE_MESSAGE))));
	}

	@Test
	void mapsDomainValidationToBadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/customers/{id}", 0L))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid customer"));
	}

	@Test
	void preservesNotFoundForUnknownRoutes() throws Exception {
		mockMvc.perform(get("/api/v1/unknown"))
				.andExpect(status().isNotFound());
	}

	@Test
	void preservesMethodNotAllowedAndAllowHeader() throws Exception {
		mockMvc.perform(post("/api/v1/customers/{id}", 1L))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(header().string("Allow", containsString("GET")));
	}

	@Test
	void preservesUnsupportedMediaType() throws Exception {
		mockMvc.perform(post("/api/v1/customers")
					.contentType(MediaType.TEXT_PLAIN)
					.content("customer"))
				.andExpect(status().isUnsupportedMediaType());
	}
}