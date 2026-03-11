package org.opennms.core.dbinit;

import javax.sql.DataSource;

import org.postgresql.Driver;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource adminDataSource(DbInitProperties props) {
        return DataSourceBuilder.create()
                .type(SimpleDriverDataSource.class)
                .driverClassName(Driver.class.getName())
                .url(props.adminUrl())
                .username(props.adminUser())
                .password(props.adminPassword())
                .build();
    }

    @Bean
    @Primary
    public DataSource dataSource(DbInitProperties props) {
        String host = extractHost(props.adminUrl());
        String appUrl = "jdbc:postgresql://" + host + "/" + props.databaseName();
        return DataSourceBuilder.create()
                .type(SimpleDriverDataSource.class)
                .driverClassName(Driver.class.getName())
                .url(appUrl)
                .username(props.databaseUser())
                .password(props.databasePassword())
                .build();
    }

    private String extractHost(String jdbcUrl) {
        // jdbc:postgresql://host:port/dbname -> host:port
        String withoutPrefix = jdbcUrl.replace("jdbc:postgresql://", "");
        int slashIdx = withoutPrefix.indexOf('/');
        return slashIdx > 0 ? withoutPrefix.substring(0, slashIdx) : withoutPrefix;
    }
}
