package org.opennms.netmgt.alarmd.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smoke test for the Alarmd Spring Boot application.
 *
 * <p>This is a prototype-level integration test. The full flow (send event,
 * verify alarm created) cannot work yet because:
 * <ul>
 *   <li>DAO beans (AlarmDao, NodeDao, etc.) have no JPA implementations</li>
 *   <li>The database schema (Liquibase/db-init) is not available in tests</li>
 *   <li>EventUtilDaoImpl requires DAOs that lack JPA backing</li>
 * </ul>
 *
 * <p>A {@code @SpringBootTest} variant is included but {@code @Disabled} until
 * the DAO layer is migrated. See the inline comments for what is needed.
 */
class AlarmdApplicationIT {

    @Test
    void applicationClassExists() {
        assertThat(AlarmdApplication.class).isNotNull();
    }

    @Test
    void hasSpringBootApplicationAnnotation() {
        assertThat(AlarmdApplication.class.isAnnotationPresent(SpringBootApplication.class))
                .as("AlarmdApplication must be annotated with @SpringBootApplication")
                .isTrue();
    }

    @Test
    void scanBasePackagesIncludeAlarmd() {
        SpringBootApplication annotation =
                AlarmdApplication.class.getAnnotation(SpringBootApplication.class);
        assertThat(annotation.scanBasePackages())
                .as("Scan packages must include the alarmd package")
                .contains("org.opennms.netmgt.alarmd");
    }

    @Test
    void scanBasePackagesIncludeDaemonCommon() {
        SpringBootApplication annotation =
                AlarmdApplication.class.getAnnotation(SpringBootApplication.class);
        assertThat(annotation.scanBasePackages())
                .as("Scan packages must include daemon-common infrastructure")
                .contains("org.opennms.core.daemon.common");
    }

    @Test
    void mainMethodExists() throws NoSuchMethodException {
        var method = AlarmdApplication.class.getMethod("main", String[].class);
        assertThat(method).isNotNull();
        assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                .as("main() must be static")
                .isTrue();
    }

    // -----------------------------------------------------------------------
    // Full context-load test — @Disabled until DAO layer is migrated.
    //
    // To enable this test, the following must be in place:
    //   1. JPA-backed implementations of AlarmDao, NodeDao, EventDao, etc.
    //   2. A Liquibase changelog or Flyway migration that creates the OpenNMS
    //      schema inside the Testcontainers PostgreSQL instance.
    //   3. spring-boot-starter-data-jpa on the classpath.
    //   4. Testcontainers PostgreSQL + Kafka dependencies in test scope.
    //
    // Once those are available, uncomment and adapt:
    //
    // @org.springframework.boot.test.context.SpringBootTest
    // @org.testcontainers.junit.jupiter.Testcontainers
    // class FullContextTest {
    //
    //     @org.testcontainers.junit.jupiter.Container
    //     static PostgreSQLContainer<?> postgres =
    //             new PostgreSQLContainer<>("postgres:16-alpine");
    //
    //     @org.testcontainers.junit.jupiter.Container
    //     static KafkaContainer kafka =
    //             new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));
    //
    //     @org.springframework.test.context.DynamicPropertySource
    //     static void configureProperties(DynamicPropertyRegistry registry) {
    //         registry.add("spring.datasource.url", postgres::getJdbcUrl);
    //         registry.add("spring.datasource.username", postgres::getUsername);
    //         registry.add("spring.datasource.password", postgres::getPassword);
    //         registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    //     }
    //
    //     @Test
    //     void contextLoads() {
    //         // If we get here, the Spring Boot context started successfully
    //     }
    // }
    // -----------------------------------------------------------------------
}
