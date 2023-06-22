/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

import static org.awaitility.Awaitility.await;
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

@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.MinionTests.class)
public class IntegrationAPIIT {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationAPIIT.class);

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINION;

    @Test
    public void canLoadSampleProject() throws Exception {
        // Install the sample plugin on OpenNMS
        final InetSocketAddress opennmsSsh = stack.opennms().getSshAddress();
        try (final SshClient sshClient = new SshClient(opennmsSsh, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("feature:install opennms-plugin-sample");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        // Install the sample plugin on Minion
        final InetSocketAddress minionSsh = stack.minion().getSshAddress();
        try (final SshClient sshClient = new SshClient(minionSsh, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("feature:install minion-plugin-sample");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        // Now wait until the health check passes on OpenNMS
        verifyHealthCheckWithDescription(opennmsSsh, "Sample Project :: Health Check");

        // Now do the same on Minion
        verifyHealthCheckWithDescription(minionSsh, "Sample Project :: Minion");

        // Now ensure that we're able to invoke the extensions on Minion from OpenNMS
        verifyHealthCheckWithDescription(opennmsSsh, "Sample Project :: Service Extensions on Minion");
    }

    private static void verifyHealthCheckWithDescription(final InetSocketAddress sshAddress, String healthCheckDescription) {
        // Now wait until the health check passes
        await().atMost(2, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sshAddress, "admin", "admin")) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("opennms:health-check");
                        pipe.println("logout");
                        await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
                        shellOutput = org.apache.commons.lang.StringUtils.substringAfter(shellOutput, "opennms:health-check");

                        boolean healthCheckSuccess = false;
                        for (String line : shellOutput.split("\\r?\\n")) {
                            if (line.contains(healthCheckDescription)) {
                                LOG.info("Health check result: {}", line);
                                if (line.contains("Success")) {
                                    healthCheckSuccess = true;
                                    break;
                                }
                            }
                        }
                        return healthCheckSuccess;
                    } catch (Exception ex) {
                        LOG.error("Error while trying to verify opennms:health-check: {}", ex.getMessage());
                        return false;
                    }
                });
    }
}
