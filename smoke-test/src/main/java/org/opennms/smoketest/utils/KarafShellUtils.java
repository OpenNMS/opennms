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
package org.opennms.smoketest.utils;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.opennms.core.health.shell.HealthCheckCommand;
import org.opennms.smoketest.containers.KarafContainer;
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
     * Awaits that the health check succeeds within 3 minutes.
     *
     * @throws RuntimeException if the health check does not succeed.
     */
    public static void awaitHealthCheckSucceeded(KarafContainer container) {
        InetSocketAddress addr = container.getSshAddress();
        Objects.requireNonNull(addr);
        await(container.getDockerImageName() + " health check").atMost(3, MINUTES)
                .failFast("container is no longer running", () -> !container.isRunning())
                .pollDelay(Duration.ZERO) // Poll immediately in case it's already up so we can move on
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
        assertThat("health check result: " + healthCheckResult
                        + "\nMost recent exception from karaf:\n"
                        + new KarafShell(sshAddr).run("log:exception-display").getLeft(),
                healthCheckResult.isSuccess());
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
     * Trigger a dump and collect it.
     */
    @SuppressWarnings("java:S5443")
    public static void saveCoverage(final KarafContainer container, final String prefix, final String type) {
        if (!container.isRunning()) {
            LOG.warn("Container [{}] isn't running, cannot save coverage data", container.getDockerImageName());
            return;
        }
        try {
            LOG.info("Triggering code coverage data file dump...");
            KarafShellUtils.triggerCoverageDump(container, "/tmp/jacoco.exec");

            LOG.info("Gathering coverage files...");
            DevDebugUtils.copyLogs(container,
                    // dest
                    Paths.get("target", "coverage", prefix, type),
                    // source folder
                    Paths.get("/tmp"),
                    // coverage file
                    Arrays.asList("jacoco.exec"));
        } catch (final Exception e) {
            LOG.error("I been hacked. all my dumps gone. this just failed please help me", e);
        }
    }

    /**
     * triggers jacoco:dump on the Karaf shell
     */
    protected static void triggerCoverageDump(final KarafContainer container, final String outputFile) throws Exception {
        Objects.requireNonNull(container);
        try (var sshClient = await()
                .pollDelay(5, SECONDS)
                .atMost(Duration.ofMinutes(1))
                .ignoreExceptions()
                // wait until a shell could be opened
                .until(() -> {
                    var client = container.ssh();
                    client.openShell();
                    return client;
                }, Matchers.notNullValue())) {
            var streams = sshClient.getStreams();
            streams.stdin.println("jacoco:dump " + (outputFile == null? "jacoco.exec":outputFile));
            await().atMost(Duration.ofMinutes(1))
                .until(() -> streams.stdout.getLines()
                    .stream()
                    .anyMatch(line -> line.contains("Wrote") && line.contains("bytes")));
            streams.stdin.println("logout");
            streams.stdout.interrupt();
            streams.stderr.interrupt();
        }
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
