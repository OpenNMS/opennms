/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.PrintStream;
import java.net.InetSocketAddress;

import org.junit.ClassRule;
import org.junit.Ignore;
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
