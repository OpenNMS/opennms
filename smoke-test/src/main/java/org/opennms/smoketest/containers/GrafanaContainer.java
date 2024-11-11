package org.opennms.smoketest.containers;


import org.junit.Before;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.lifecycle.TestLifecycleAware;
import org.testcontainers.utility.DockerImageName;

public class GrafanaContainer extends GenericContainer<GrafanaContainer> implements TestLifecycleAware {

    public static final int WEB_PORT = 3000;
    public static final String GRAFANA_ALIAS = "grafana";
    public GrafanaContainer() {
        super("grafana/grafana:latest");
            withEnv("GF_SECURITY_ADMIN_PASSWORD", "admin")
                .withNetwork(Network.SHARED)
                    .withExposedPorts(WEB_PORT);


    }
    public int getWebPort() {
        return getMappedPort(WEB_PORT);
    }

}
