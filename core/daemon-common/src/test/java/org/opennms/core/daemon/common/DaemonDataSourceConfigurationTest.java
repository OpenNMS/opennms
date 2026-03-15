package org.opennms.core.daemon.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

class DaemonDataSourceConfigurationTest {

    @Test
    void hasRequiredAnnotations() {
        assertThat(DaemonDataSourceConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
        assertThat(DaemonDataSourceConfiguration.class.isAnnotationPresent(EnableTransactionManagement.class)).isTrue();
        assertThat(DaemonDataSourceConfiguration.class.isAnnotationPresent(EntityScan.class)).isTrue();
    }

    @Test
    void entityScanPointsToModelPackage() {
        EntityScan entityScan = DaemonDataSourceConfiguration.class.getAnnotation(EntityScan.class);
        assertThat(entityScan.basePackages()).containsExactly("org.opennms.netmgt.model");
    }
}
