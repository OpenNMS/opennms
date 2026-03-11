package org.opennms.core.dbinit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DbInitIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("template1")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("opennms.dbinit.admin-url", postgres::getJdbcUrl);
        registry.add("opennms.dbinit.admin-user", postgres::getUsername);
        registry.add("opennms.dbinit.admin-password", postgres::getPassword);
        registry.add("opennms.dbinit.database-name", () -> "opennms");
        registry.add("opennms.dbinit.database-user", () -> "opennms");
        registry.add("opennms.dbinit.database-password", () -> "opennms");
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DbInitRunner runner;

    @Test
    void migrationCreatesAlarmTable() throws Exception {
        try (var conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, "public", "alarms", null)) {
            assertThat(rs.next())
                    .as("alarms table should exist after migration")
                    .isTrue();
        }
    }

    @Test
    void migrationCreatesNodeTable() throws Exception {
        try (var conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, "public", "node", null)) {
            assertThat(rs.next())
                    .as("node table should exist after migration")
                    .isTrue();
        }
    }

    @Test
    void eventsTableDoesNotExist() throws Exception {
        try (var conn = dataSource.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, "public", "events", null)) {
            assertThat(rs.next())
                    .as("events table should NOT exist (dropped by 36.0.0)")
                    .isFalse();
        }
    }

    @Test
    void migrationIsIdempotent() {
        assertThatNoException().isThrownBy(() -> runner.run());
    }
}
