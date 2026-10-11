package com.bjorn.customer_management.application.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.bjorn.customer_management.domain.model.BirthDate;
import com.bjorn.customer_management.domain.model.Customer;
import com.bjorn.customer_management.domain.model.PersonName;
import com.bjorn.customer_management.domain.repository.CustomerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {
	@NonNull
	private static final MediaType JSON = Objects.requireNonNull(MediaType.APPLICATION_JSON);

	@NonNull
	private static final MediaType PROBLEM_JSON = Objects.requireNonNull(MediaType.APPLICATION_PROBLEM_JSON);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CustomerRepository customerRepository;

	@BeforeEach
	void clearCustomers() {
		jdbcTemplate.update("DELETE FROM customers");
	}

	@Test
	void rejectsAlreadyPersistedCustomerWithoutInsertingAnotherRow() {
		Customer customer = Customer.create(
				new PersonName("Viper", "Napuatzi"),
				new BirthDate(LocalDate.of(1985, 5, 20)));
		Customer persistedCustomer = customerRepository.save(customer);
		assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));

		InvalidDataAccessApiUsageException exception = assertThrows(
				InvalidDataAccessApiUsageException.class, () -> customerRepository.save(persistedCustomer));
		assertInstanceOf(IllegalStateException.class, exception.getCause());

		assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@Test
	void createsCustomerAndPersistsItInH2() throws Exception {
		String requestBody = """
				{
				  "firstName": " Viper ",
				  "lastName": " Napuatzi ",
				  "dateOfBirth": "1985-05-20"
				}
				""";

		MvcResult result = mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.firstName").value("Viper"))
				.andExpect(jsonPath("$.lastName").value("Napuatzi"))
				.andExpect(jsonPath("$.dateOfBirth").value("1985-05-20"))
				.andReturn();
		JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
		long customerId = response.get("id").asLong();
		String location = result.getResponse().getHeader("Location");

		assertTrue(customerId > 0);
		assertEquals("/api/v1/customers/" + customerId, location);
		assertEquals("Viper", jdbcTemplate.queryForObject(
				"SELECT first_name FROM customers WHERE id = ?", String.class, customerId));
		assertEquals("Napuatzi", jdbcTemplate.queryForObject(
				"SELECT last_name FROM customers WHERE id = ?", String.class, customerId));
		assertEquals(LocalDate.of(1985, 5, 20), jdbcTemplate.queryForObject(
				"SELECT date_of_birth FROM customers WHERE id = ?", LocalDate.class, customerId));

		mockMvc.perform(get(Objects.requireNonNull(location)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(customerId))
				.andExpect(jsonPath("$.firstName").value("Viper"))
				.andExpect(jsonPath("$.lastName").value("Napuatzi"))
				.andExpect(jsonPath("$.dateOfBirth").value("1985-05-20"));
	}

	@Test
	void getsPersistedCustomerById() throws Exception {
		Customer persistedCustomer = customerRepository.save(Customer.create(
				new PersonName("Viper", "Napuatzi"),
				new BirthDate(LocalDate.of(1985, 5, 20))));
		long customerId = persistedCustomer.id().orElseThrow().value();

		mockMvc.perform(get("/api/v1/customers/{id}", customerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(customerId))
				.andExpect(jsonPath("$.firstName").value("Viper"))
				.andExpect(jsonPath("$.lastName").value("Napuatzi"))
				.andExpect(jsonPath("$.dateOfBirth").value("1985-05-20"));
	}

	@Test
	void returnsEmptyCustomerList() throws Exception {
		mockMvc.perform(get("/api/v1/customers"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(JSON))
				.andExpect(content().string("[]"));
	}

	@Test
	void listsPostCreatedCustomersNewestFirstWithoutChangingStorage() throws Exception {
		ObjectNode request = objectMapper.createObjectNode()
				.put("firstName", " Viper ")
				.put("lastName", " Napuatzi ")
				.put("dateOfBirth", "1985-05-20");
		MvcResult firstCreation = mockMvc.perform(post("/api/v1/customers")
				.contentType(JSON)
				.content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
				.andExpect(status().isCreated())
				.andReturn();
		long firstId = objectMapper.readTree(firstCreation.getResponse().getContentAsString()).get("id").asLong();

		request.put("firstName", "Deckard").put("lastName", "Cain").put("dateOfBirth", "1970-01-02");
		MvcResult secondCreation = mockMvc.perform(post("/api/v1/customers")
				.contentType(JSON)
				.content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
				.andExpect(status().isCreated())
				.andReturn();
		long secondId = objectMapper.readTree(secondCreation.getResponse().getContentAsString()).get("id").asLong();
		assertTrue(secondId > firstId);
		var storedCustomers = jdbcTemplate.queryForList("SELECT * FROM customers ORDER BY id");
		assertEquals(2, storedCustomers.size());

		MvcResult firstList = mockMvc.perform(get("/api/v1/customers"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(JSON))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(secondId))
				.andExpect(jsonPath("$[0].firstName").value("Deckard"))
				.andExpect(jsonPath("$[0].lastName").value("Cain"))
				.andExpect(jsonPath("$[0].dateOfBirth").value("1970-01-02"))
				.andExpect(jsonPath("$[1].id").value(firstId))
				.andExpect(jsonPath("$[1].firstName").value("Viper"))
				.andExpect(jsonPath("$[1].lastName").value("Napuatzi"))
				.andExpect(jsonPath("$[1].dateOfBirth").value("1985-05-20"))
				.andReturn();
		MvcResult secondList = mockMvc.perform(get("/api/v1/customers"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(JSON))
				.andReturn();

		assertEquals(firstList.getResponse().getContentAsString(), secondList.getResponse().getContentAsString());
		assertEquals(storedCustomers, jdbcTemplate.queryForList("SELECT * FROM customers ORDER BY id"));
	}

	@Test
	void returnsProblemDetailWhenCustomerIdDoesNotExist() throws Exception {
		mockMvc.perform(get("/api/v1/customers/{id}", 999L))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Customer not found"));
	}

	@ParameterizedTest
	@CsvSource({"abc", "9223372036854775808"})
	void returnsProblemDetailWhenCustomerIdIsNotALong(String id) throws Exception {
		mockMvc.perform(get("/api/v1/customers/{id}", id))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Invalid path parameter"));
	}

	@ParameterizedTest
	@CsvSource({"0", "-1"})
	void returnsProblemDetailWhenCustomerIdIsNotPositive(long id) throws Exception {
		mockMvc.perform(get("/api/v1/customers/{id}", id))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void rejectsBlankNameWithFieldErrorAndDoesNotPersist() throws Exception {
		String requestBody = """
				{
				  "firstName": " ",
				  "lastName": "Napuatzi",
				  "dateOfBirth": "1985-05-20"
				}
				""";

		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[0].field").value("firstName"));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@Test
	void rejectsBlankLastNameWithFieldErrorAndDoesNotPersist() throws Exception {
		String requestBody = """
				{
				  "firstName": "Viper",
				  "lastName": " ",
				  "dateOfBirth": "1985-05-20"
				}
				""";

		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[0].field").value("lastName"));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@Test
	void rejectsNameLongerThanFiftyCharactersAndDoesNotPersist() throws Exception {
		String requestBody = Objects.requireNonNull("""
				{
				  "firstName": "%s",
				  "lastName": "Napuatzi",
				  "dateOfBirth": "1985-05-20"
				}
				""".formatted("v".repeat(51)));

		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[0].field").value("firstName"));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@ParameterizedTest(name = "Rejects missing date when explicit null: {0}")
	@CsvSource({"false", "true"})
	void rejectsMissingOrNullRequiredDateAndDoesNotPersist(boolean explicitNull) throws Exception {
		ObjectNode request = objectMapper.createObjectNode()
				.put("firstName", "Viper")
				.put("lastName", "Napuatzi");
		if (explicitNull) {
			request.putNull("dateOfBirth");
		}

		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[0].field").value("dateOfBirth"));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@Test
	void rejectsBirthDateTodayAndDoesNotPersist() throws Exception {
		String today = LocalDate.now(ZoneOffset.UTC).toString();
		String requestBody = Objects.requireNonNull("""
				{
				  "firstName": "Viper",
				  "lastName": "Napuatzi",
				  "dateOfBirth": "%s"
				}
				""".formatted(today));

		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[0].field").value("dateOfBirth"));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@Test
	void rejectsFutureBirthDateWithFieldErrorAndDoesNotPersist() throws Exception {
		String futureDate = LocalDate.now(ZoneOffset.UTC).plusDays(1).toString();
		String requestBody = Objects.requireNonNull("""
				{
				  "firstName": "Viper",
				  "lastName": "Napuatzi",
				  "dateOfBirth": "%s"
				}
				""".formatted(futureDate));

		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.errors[0].field").value("dateOfBirth"));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@Test
	void returnsProblemDetailForMalformedDate() throws Exception {
		String requestBody = """
				{
				  "firstName": "Viper",
				  "lastName": "Napuatzi",
				  "dateOfBirth": "not-a-date"
				}
				""";

		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Malformed request"));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@Test
	void returnsProblemDetailForMalformedJson() throws Exception {
		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content("{ malformed json"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Malformed request"));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}

	@ParameterizedTest(name = "Rejects {0} with explicit null: {1}")
	@CsvSource({"firstName, false", "firstName, true", "lastName, false", "lastName, true"})
	void rejectsMissingOrNullNameAndDoesNotPersist(String field, boolean explicitNull) throws Exception {
		ObjectNode request = objectMapper.createObjectNode()
				.put("firstName", "Viper")
				.put("lastName", "Napuatzi")
				.put("dateOfBirth", "1985-05-20");
		if (explicitNull) {
			request.putNull(field);
		} else {
			request.remove(field);
		}

		mockMvc.perform(post("/api/v1/customers")
					.contentType(JSON)
					.content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors[0].field").value(field));

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class));
	}
}