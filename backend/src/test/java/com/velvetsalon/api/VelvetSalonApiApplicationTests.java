package com.velvetsalon.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"POSTGRES_DB=testdb",
		"POSTGRES_USER=testuser",
		"POSTGRES_PASSWORD=testpass"
})
@Testcontainers
class VelvetSalonApiApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoadsAndSchemaIsApplied() {
		String[] expectedTables = {
				"admin_users",
				"services",
				"staff_members",
				"staff_services",
				"working_hours",
				"blocked_times",
				"appointments",
				"flyway_schema_history"
		};

		for (String table : expectedTables) {
			Integer count = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
					Integer.class,
					table
			);
			assertTrue(count != null && count > 0, "Table should exist: " + table);
		}

		String dbName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
		assertTrue(dbName != null && dbName.equals("test"), "Test should run against ephemeral 'test' database, but ran against: " + dbName);
	}
}
