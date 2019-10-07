/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.PrintStream;
import java.net.InetSocketAddress;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.CommandTestUtils;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationAPIIT {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationAPIIT.class);

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Test
    public void canLoadSampleProject() throws Exception {
        final InetSocketAddress karafSsh = stack.opennms().getSshAddress();
        // Install the sample project
        try (final SshClient sshClient = new SshClient(karafSsh, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("feature:install opennms-integration-api-sample-project");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        // Now wait until the health check passes
        verifyHealthCheckWithDescription(karafSsh, "Sample Project :: Health Check");
    }

    private static void verifyHealthCheckWithDescription(final InetSocketAddress sshAddress, String healthCheckDescription) {
        // Now wait until the health check passes
        await().atMost(2, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sshAddress, "admin", "admin")) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("health:check");
                        pipe.println("logout");
                        await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
                        shellOutput = org.apache.commons.lang.StringUtils.substringAfter(shellOutput, "health:check");

                        boolean healthCheckSuccess = false;
                        for (String line : shellOutput.split("\\r?\\n")) {
                            if (line.contains(healthCheckDescription)) {
                                LOG.info("Health check result: {}", line);
                            }
                            if (line.contains("Success")) {
                                healthCheckSuccess = true;
                            }
                        }
                        return healthCheckSuccess;
                    } catch (Exception ex) {
                        LOG.error("Error while trying to verify health:check: {}", ex.getMessage());
                        return false;
                    }
                });
    }
}
