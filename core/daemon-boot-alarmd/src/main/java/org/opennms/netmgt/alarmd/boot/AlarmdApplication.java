package org.opennms.netmgt.alarmd.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "org.opennms.core.daemon.common",
    "org.opennms.netmgt.alarmd"
})
public class AlarmdApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlarmdApplication.class, args);
    }
}
