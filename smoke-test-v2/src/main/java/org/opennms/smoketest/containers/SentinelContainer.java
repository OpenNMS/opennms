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

import org.opennms.smoketest.utils.OpenNMSRestClient;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.SelinuxContext;

public class SentinelContainer extends GenericContainer {
    private static final Logger LOG = LoggerFactory.getLogger(SentinelContainer.class);
    private static final int SENTINEL_SSH_PORT = 8301;
    static final String ALIAS = "sentinel";

    public SentinelContainer() {
        super("sentinel");
        withExposedPorts(SENTINEL_SSH_PORT)
                .withEnv("SENTINEL_LOCATION", "Sentinel")
                .withEnv("SENTINEL_ID", "00000000-0000-0000-0000-000000ddb33f")
                .withEnv("POSTGRES_HOST", OpenNMSContainer.DB_ALIAS)
                .withEnv("POSTGRES_PORT", Integer.toString(PostgreSQLContainer.POSTGRESQL_PORT))
                // User/pass are hardcoded in PostgreSQLContainer but are not exposed
                .withEnv("POSTGRES_USER", "test")
                .withEnv("POSTGRES_PASSWORD", "test")
                .withEnv("OPENNMS_DBNAME", "opennms")
                .withEnv("OPENNMS_DBUSER", "opennms")
                .withEnv("OPENNMS_DBPASS", "opennms")
                .withEnv("OPENNMS_BROKER_URL", "failover:tcp://" + OpenNMSContainer.ALIAS + ":61616")
                .withEnv("OPENNMS_HTTP_URL", "http://" + OpenNMSContainer.ALIAS + ":8980/opennms")
                .withEnv("OPENNMS_HTTP_USER", "admin")
                .withEnv("OPENNMS_HTTP_PASS", "admin")
                .withEnv("OPENNMS_BROKER_USER", "admin")
                .withEnv("OPENNMS_BROKER_PASS", "admin")
                .withClasspathResourceMapping("sentinel-overlay", "/opt/sentinel-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE)
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand("-f")
                .waitingFor(new WaitForSentinel(this));
    }

    public InetSocketAddress getSshAddress() {
        return new InetSocketAddress(getContainerIpAddress(), getMappedPort(SENTINEL_SSH_PORT));
    }

    private static class WaitForSentinel extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final SentinelContainer container;

        public WaitForSentinel(SentinelContainer container) {
            this.container = Objects.requireNonNull(container);
        }

        @Override
        protected void waitUntilReady() {
            final InetSocketAddress sshAddr = container.getSshAddress();
            LOG.info("Waiting for Sentinel...");
            await().atMost(5, MINUTES).pollInterval(5, SECONDS).until(() -> canSentinelConnectToOpenNMS(sshAddr));
        }

        public boolean canSentinelConnectToOpenNMS(InetSocketAddress sshAddr) {
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
                LOG.error("Failed to reach OpenNMS from Sentinel.", e);
            }
            return false;
        }
    }
}
