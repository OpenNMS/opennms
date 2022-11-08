/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.opennms.core.health.api.Status;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.junit.SentinelTests;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.KarafShellUtils;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

@Category(SentinelTests.class)
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
        final long timeoutMins = 5;
        try {
            await().atMost(timeoutMins, MINUTES)
                    .pollInterval(5, SECONDS)
                    .ignoreExceptions()
                    .untilAsserted(() -> {
                        var result = KarafShellUtils.executeHealthCheck(sshAddress);
                        final var reason = "health check result: " + result;
                        assertThat(reason, result.isSuccess());
                        // the health check may output the some success messages repeatedly
                        // -> the health check output moves the terminal's cursor and overwrites its former output
                        // -> count only distinct success messages
                        var count = (int)result.stdout.stream().filter(s -> s.contains(Status.Success.name())).distinct().count();
                        assertThat(reason, count, Matchers.greaterThanOrEqualTo(expectedHealthCheckServices));
                    });
        } catch (ConditionTimeoutException e) {
            throw new RuntimeException("health check did not complete as expected after " + timeoutMins + " minutes", e);
        }
    }

    private void verifyMetrics(final InetSocketAddress sshAddress) {
        await().atMost(2, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sshAddress, OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("opennms:metrics-display");
                        pipe.println("logout");

                        await().atMost(15, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final int count = StringUtils.countOccurrencesOf(shellOutput, "Metric set:");

                        LOG.info("log:display");
                        LOG.info("{}", shellOutput);
                        return count;
                    } catch (Exception ex) {
                        LOG.error("Error while trying to verify opennms:health-check: {}", ex.getMessage());
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
