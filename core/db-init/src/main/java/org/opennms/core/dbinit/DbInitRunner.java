package org.opennms.core.dbinit;

import javax.sql.DataSource;

import org.opennms.core.schema.Migrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DbInitRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DbInitRunner.class);

    private final DataSource adminDataSource;
    private final DataSource dataSource;
    private final ApplicationContext context;
    private final DbInitProperties properties;

    public DbInitRunner(DataSource adminDataSource,
                        DataSource dataSource,
                        ApplicationContext context,
                        DbInitProperties properties) {
        this.adminDataSource = adminDataSource;
        this.dataSource = dataSource;
        this.context = context;
        this.properties = properties;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Starting database initialization...");

        var migrator = new Migrator();
        migrator.setAdminDataSource(adminDataSource);
        migrator.setDataSource(dataSource);
        migrator.setApplicationContext(context);
        migrator.setDatabaseName(properties.databaseName());
        migrator.setDatabaseUser(properties.databaseUser());
        migrator.setDatabasePassword(properties.databasePassword());
        migrator.setAdminUser(properties.adminUser());
        migrator.setAdminPassword(properties.adminPassword());
        migrator.setCreateUser(properties.createUser());
        migrator.setCreateDatabase(properties.createDatabase());

        migrator.setupDatabase(
            true,
            properties.vacuum(),
            properties.fullVacuum(),
            properties.iplike(),
            properties.timescaleDb()
        );

        LOG.info("Database initialization complete.");
    }
}
