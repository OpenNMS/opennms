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

package org.opennms.smoketest.health;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class HealthCheckIT {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withSentinel()
            .withElasticsearch()
            .withIpcStrategy(IpcStrategy.JMS)
            // This adds extra health checks that our test counts
            .withTelemetryProcessing()
            .build());

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);


    @Test
    public void verifyOpenNMSHealth() {
        final InetSocketAddress opennmsShellAddr = stack.opennms().getSshAddress();
        verifyHealthCheck(2, opennmsShellAddr);
        verifyMetrics(opennmsShellAddr);
    }

    @Test
    public void verifyMinionHealth() {
        final InetSocketAddress minionShellAddr = stack.minion().getSshAddress();
        verifyHealthCheck(4, minionShellAddr);
        verifyMetrics(minionShellAddr);
    }

    @Test
    public void verifySentinelHealth() {
        final InetSocketAddress sentinelShellAddr = stack.sentinel().getSshAddress();
        verifyHealthCheck(9, sentinelShellAddr);
        verifyMetrics(sentinelShellAddr);
    }

    private void verifyHealthCheck(final int expectedHealthCheckServices, final InetSocketAddress sshAddress) {
        // Ensure we are actually started the sink and are ready to listen for messages
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sshAddress, OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("health:check");
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        // each health check times out after 5 seconds, so we wait at least that long
                        int maxWaitTime = expectedHealthCheckServices * 5;
                        await().atMost(maxWaitTime, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();

                        // Log what was read, to help debugging issues
                        LOG.info("log:display");
                        LOG.info("{}", shellOutput);

                        final int count = StringUtils.countOccurrencesOf(shellOutput, "Success");
                        final String overallStatus = getOverallStatus(shellOutput);
                        LOG.info("{} checks are successful and overall status is {}, expected >= {} and \"Everything is awesome\"", count, overallStatus, expectedHealthCheckServices);

                        // We check if at least the number of expected health
                        // checks succeeded and overall status is "AWESOME". This way we avoid updating this test each time a new health check is added
                        return count >= expectedHealthCheckServices && overallStatus.contains("awesome");
                    } catch (Exception ex) {
                        LOG.error("Error while trying to verify health:check: {}", ex.getMessage());
                        return false;
                    }
                });
    }

    private void verifyMetrics(final InetSocketAddress sshAddress) {
        await().atMost(2, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sshAddress, OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("health:metrics-display");
                        pipe.println("logout");

                        await().atMost(15, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final int count = StringUtils.countOccurrencesOf(shellOutput, "Metric set:");

                        LOG.info("log:display");
                        LOG.info("{}", shellOutput);
                        return count;
                    } catch (Exception ex) {
                        LOG.error("Error while trying to verify health:check: {}", ex.getMessage());
                        return 0;
                    }
                }, greaterThanOrEqualTo(1));
    }

    private static String getOverallStatus(String input) {
        // Returns the text starting from the success line, but also contains other content
        final String tempStatus = input.substring(input.indexOf("=> "));
        // Here we remove the leading => and also anything at the end of the status line
        final String overallStatus = tempStatus.substring("=> ".length(), tempStatus.indexOf("\n")).trim();
        return overallStatus;
    }
}
