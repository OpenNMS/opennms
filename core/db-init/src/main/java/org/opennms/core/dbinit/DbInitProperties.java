package org.opennms.core.dbinit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opennms.dbinit")
public record DbInitProperties(
    String databaseName,
    String databaseUser,
    String databasePassword,
    String adminUser,
    String adminPassword,
    boolean createUser,
    boolean createDatabase,
    boolean iplike,
    boolean timescaleDb,
    boolean vacuum,
    boolean fullVacuum
) {}
