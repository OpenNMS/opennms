package org.opennms.core.daemon.common;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "org.opennms.netmgt.model")
public class DaemonDataSourceConfiguration {
    // Spring Boot auto-configuration handles:
    // - HikariCP DataSource from spring.datasource.* properties
    // - Hibernate SessionFactory from spring.jpa.* properties
    // - JpaTransactionManager
    //
    // Entity scanning finds @Entity classes in opennms-model.
    // Schema management is "none" (Liquibase manages via db-init container).
}
