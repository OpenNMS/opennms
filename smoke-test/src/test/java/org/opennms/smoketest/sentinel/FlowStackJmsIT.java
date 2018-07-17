/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.sentinel;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.PrintStream;
import java.net.InetSocketAddress;

import org.junit.Test;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.springframework.util.StringUtils;

// Verifies that flows can be processed by a sentinel and are persisted to Elastic communicating via activemq (jms)
public class FlowStackJmsIT extends AbstractFlowIT {

    @Override
    protected void customizeTestEnvironment(TestEnvironmentBuilder builder) {
            builder
                    .opennms()
                    .minion()
                    .es6()
                    .sentinel();

            // Enable Netflow 5 Adapter
            builder.withSentinelEnvironment()
                    .addFile(getClass().getResource("/sentinel/features-jms.xml"), "deploy/features.xml");

            // Enable Netflow 5 Listener
            builder.withMinionEnvironment()
                    .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50000.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-50000.cfg");
    }

    @Test
    public void verifyHealthCheck() {
        final InetSocketAddress sentinelSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);

        // Ensure we are actually started the sink and are ready to listen for messages
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sentinelSshAddress, "admin", "admin")) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("health:check");
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final int count = StringUtils.countOccurrencesOf(shellOutput, "Success");

                        logger.info("log:display");
                        logger.info("{}", shellOutput);
                        return count == 6;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify health:check: {}", ex.getMessage());
                        return false;
                    }
                });
    }

    @Override
    protected String getSentinelReadyString() {
        return "Route: Sink.Server.Telemetry-Netflow-5 started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-Netflow-5";
    }
}
