/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.containers;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.opennms.smoketest.utils.OpenNMSRestClient;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.SelinuxContext;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.InternetProtocol;

public class MinionContainer extends GenericContainer {
    private static final Logger LOG = LoggerFactory.getLogger(MinionContainer.class);
    private static final int MINION_SYSLOG_PORT = 1514;
    private static final int MINION_SSH_PORT = 8201;
    static final String ALIAS = "minion";

    public MinionContainer() {
        super("minion");
        withExposedPorts(MINION_SSH_PORT, MINION_SYSLOG_PORT)
                .withEnv("MINION_LOCATION", "Minion")
                .withEnv("MINION_ID", "00000000-0000-0000-0000-000000ddba11")
                .withEnv("OPENNMS_BROKER_URL", "failover:tcp://" + OpenNMSContainer.ALIAS + ":61616")
                .withEnv("OPENNMS_HTTP_URL", "http://" + OpenNMSContainer.ALIAS + ":8980/opennms")
                .withEnv("OPENNMS_HTTP_USER", "admin")
                .withEnv("OPENNMS_HTTP_PASS", "admin")
                .withEnv("OPENNMS_BROKER_USER", "admin")
                .withEnv("OPENNMS_BROKER_PASS", "admin")
                //.withClasspathResourceMapping("minion-overlay", "/minion-docker-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE)
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand("-c")
                .waitingFor(new WaitForMinion(this));
    }

    public InetSocketAddress getSyslogAddress() {
        // Workaround for UDP ports -- see https://github.com/testcontainers/testcontainers-java/issues/554
        final String hostPortSpec = getContainerInfo().getNetworkSettings().getPorts().getBindings().get(new ExposedPort(MINION_SYSLOG_PORT, InternetProtocol.UDP))[0].getHostPortSpec();
        final int hostPort = Integer.parseInt(hostPortSpec);
        return new InetSocketAddress(getContainerIpAddress(), hostPort);
    }

    public InetSocketAddress getSshAddress() {
        return new InetSocketAddress(getContainerIpAddress(), getMappedPort(MINION_SSH_PORT));
    }

    private static class WaitForMinion extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final MinionContainer container;

        public WaitForMinion(MinionContainer container) {
            this.container = Objects.requireNonNull(container);
        }

        @Override
        protected void waitUntilReady() {
            final InetSocketAddress sshAddr = container.getSshAddress();
            LOG.info("Waiting for Minion...");
            await().atMost(5, MINUTES).pollInterval(5, SECONDS).until(() -> canMinionConnectToOpenNMS(sshAddr));
        }

        public boolean canMinionConnectToOpenNMS(InetSocketAddress sshAddr) {
            try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
                // Issue the 'health:check' command
                PrintStream pipe = sshClient.openShell();
                pipe.println("health:check");
                pipe.println("logout");

                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());

                // Grab the output
                String shellOutput = sshClient.getStdout();
                LOG.info("health:check output: {}", shellOutput);

                // Verify
                return shellOutput.contains("awesome");
            } catch (Exception e) {
                LOG.error("Failed to reach OpenNMS from Minion.", e);
            }
            return false;
        }
    }
}
