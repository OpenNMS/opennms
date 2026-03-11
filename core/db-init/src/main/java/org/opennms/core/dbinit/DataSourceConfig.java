package org.opennms.core.dbinit;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.admin")
    public DataSourceProperties adminDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource adminDataSource() {
        return adminDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties appDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return appDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }
}
