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

package org.opennms.smoketest.utils;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.opennms.core.health.shell.HealthCheckCommand;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KarafShellUtils {
    private static final Logger LOG = LoggerFactory.getLogger(KarafShellUtils.class);

    public static boolean isSuccessMsg(String s) {
        // a newline is appended to the string -> do a startsWith check
        return s.startsWith(HealthCheckCommand.SUCCESS_MESSAGE);
    }

    public static boolean isFailureMessage(String s) {
        // a newline is appended to the string -> do a startsWith check
        return s.startsWith(HealthCheckCommand.FAILURE_MESSAGE);
    }

    public static boolean isEndMessage(String s) {
        return isSuccessMsg(s) || isFailureMessage(s) || s.contains("Command not found");
    }

    public static class HealthCheckResult {
        public final List<String> stdout;
        public final List<String> stderr;

        public HealthCheckResult(List<String> stdout, List<String> stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public boolean isSuccess() {
            return stdout.stream().anyMatch(KarafShellUtils::isSuccessMsg);
        }

        @Override
        public String toString() {
            return "HealthCheckResult{" +
                   "\n===stdout===\n" + stdout.stream().collect(Collectors.joining("\n")) +
                   "\n===stderr===\n" + stderr.stream().collect(Collectors.joining("\n")) +
                   '}';
        }
    }

    /**
     * Awaits that the health check succeeds within the given amount of minutes.
     *
     * @throws RuntimeException if the health check does not succeed.
     */
    public static void awaitHealthCheckSucceeded(InetSocketAddress addr, int timeoutMinutes, String what) {
        Objects.requireNonNull(addr);
        await(what + " health check").atMost(timeoutMinutes, MINUTES)
                .pollInterval(5, SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> testHealthCheckSucceeded(addr));
    }

    /**
     * Checks if the health check succeeds.
     *
     * @throws Exception Throws if the health check could not be called, did not completed, or completed with a failure.
     */
    public static void testHealthCheckSucceeded(InetSocketAddress sshAddr) throws Exception {
        final var healthCheckResult = executeHealthCheck(sshAddr);
        assertThat("health check result: " + healthCheckResult, healthCheckResult.isSuccess());
    }

    /**
     * Calls the health check on a newly opened Karaf shell and waits for its completion.
     *
     * @throws Exception Throws if the health check command could not be called or did not complete within time.
     */
    public static HealthCheckResult executeHealthCheck(InetSocketAddress sshAddr) throws Exception {
        return withKarafShell(
                sshAddr,
                Duration.ofMinutes(2),
                streams -> {
                    final var healthCheckTimeout = Duration.ofSeconds(5);
                    streams.stdin.println("opennms:health-check -t " + healthCheckTimeout.toMillis());
                    // allow some extra seconds for the result to get detected
                    await().atMost(healthCheckTimeout.plus(Duration.ofSeconds(5)))
                            .until(() -> streams.stdout.getLines().stream().anyMatch(KarafShellUtils::isEndMessage));
                    return new HealthCheckResult(streams.stdout.getLines(), streams.stderr.getLines());
                }
        );
    }

    /**
     * Calls a test function within the context of an open Karaf shell. The test function is called only once, i.e.
     * there are no retries.
     *
     * @throws Exception If no shell could be opened or the the test function raised an exception. The thrown exception
     *                   contains the content of stdout and stderr that was received so far. The thrown exception should
     *                   either be propagated or logged in order to preserve this valuable debugging information.
     */
    public static <T> T withKarafShell(
            InetSocketAddress sshAddr,
            Duration waitForShellTimeout,
            Function<SshClient.Streams, T> withStreams
    ) throws Exception {
        Objects.requireNonNull(sshAddr);
        try (var sshClient = await()
                .pollDelay(5, SECONDS)
                .atMost(waitForShellTimeout)
                .ignoreExceptions()
                // wait until a shell could be opened
                .until(() -> {
                    var client = new SshClient(sshAddr, OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
                    client.openShell();
                    return client;
                }, Matchers.notNullValue())) {
            var streams = sshClient.getStreams();
            try {
                return withStreams.apply(streams);
            } catch (Throwable t) {
                throw new RuntimeException("test function raised an exception" +
                                           "\nstdout:\n" + streams.stdout.getLines().stream().collect(Collectors.joining("\n")) +
                                           "\nstderr:\n" + streams.stderr.getLines().stream().collect(Collectors.joining("\n")), t);
            } finally {
                try {
                    streams.stdin.println("logout");
                    streams.stdout.interrupt();
                    streams.stderr.interrupt();
                } catch (Throwable t) {
                    LOG.error("Karaf shell cleanup failed", t);
                }
            }
        }
    }

}
