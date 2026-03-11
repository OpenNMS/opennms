package org.opennms.core.dbinit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DbInitProperties.class)
public class DbInitApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(
            SpringApplication.run(DbInitApplication.class, args)
        ));
    }
}
