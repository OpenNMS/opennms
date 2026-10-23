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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import java.net.SocketException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.RestHealthClient;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;

/**
 * Measures how long a running Core takes to stop and to come back after a container restart.
 *
 * The first start of the stack installs the database and warms the Karaf cache, so it is not
 * representative. This test stops and starts the same container, which is what a pod restart
 * in Kubernetes or a service restart on a host does.
 *
 * The measured phases are logged on every run. The hard limit fails the test on a clear
 * regression. The target is the goal for the restart time; a miss is logged as a warning so
 * the limit can be adjusted from CI logs.
 */
public class CoreRestartTimeIT {

    private static final Logger LOG = LoggerFactory.getLogger(CoreRestartTimeIT.class);

    /** Goal for the time from container start to a passing health check. */
    private static final Duration RESTART_TARGET = Duration.ofSeconds(60);

    /** The test fails when the restart takes longer than this. */
    private static final Duration RESTART_LIMIT = Duration.ofMinutes(3);

    /** The test fails when the container does not stop within this time after SIGTERM. */
    private static final Duration STOP_LIMIT = Duration.ofSeconds(30);

    /** Docker sends SIGKILL when the container does not exit within this time after SIGTERM. */
    private static final Duration KILL_TIMEOUT = STOP_LIMIT.plusSeconds(30);

    /** Exit code of a container that Docker killed with SIGKILL. */
    private static final long SIGKILL_EXIT_CODE = 137;

    private static final Pattern BEGIN_STARTUP = Pattern.compile("^(\\S+ \\S+) .*Starter: Beginning startup", Pattern.MULTILINE);
    private static final Pattern STARTUP_COMPLETE = Pattern.compile("^(\\S+ \\S+) .*Starter: Startup complete", Pattern.MULTILINE);
    private static final DateTimeFormatter LOG_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Rule
    public Timeout timeout = new Timeout(15, TimeUnit.MINUTES);

    @Test
    public void restartsWithinLimit() {
        final OpenNMSContainer opennms = stack.opennms();
        final DockerClient docker = opennms.getDockerClient();
        final String containerId = opennms.getContainerId();
        final Path managerLog = OpenNMSContainer.CONTAINER_LOG_DIR.resolve("manager.log");

        // The log file survives the restart, so count the markers of the first start.
        final int startsBefore = count(STARTUP_COMPLETE, managerLog(opennms, managerLog));
        assertThat("the first start must be complete", startsBefore, greaterThanOrEqualTo(1));

        // Stop: docker sends SIGTERM and waits up to the given timeout before it sends SIGKILL.
        LOG.info("Stopping container {}", containerId);
        final long stopStart = System.nanoTime();
        docker.stopContainerCmd(containerId).withTimeout((int) KILL_TIMEOUT.getSeconds()).exec();
        await("container stopped").atMost(2, MINUTES).pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> !Boolean.TRUE.equals(docker.inspectContainerCmd(containerId).exec().getState().getRunning()));
        final Duration stopDuration = sinceNanos(stopStart);
        final Long exitCode = docker.inspectContainerCmd(containerId).exec().getState().getExitCodeLong();
        LOG.info("Container stopped in {} ms with exit code {}", stopDuration.toMillis(), exitCode);
        assertThat("Core did not exit on SIGTERM within " + KILL_TIMEOUT.getSeconds() + " s and Docker killed it (exit code)",
                exitCode, not(equalTo(SIGKILL_EXIT_CODE)));

        // Start and wait for the markers of the second start.
        LOG.info("Starting container {}", containerId);
        final long startStart = System.nanoTime();
        docker.startContainerCmd(containerId).exec();
        // Docker assigns new host ports on restart and testcontainers caches the old ones.
        opennms.refreshContainerInfo();

        await("second Beginning startup").atMost(RESTART_LIMIT.getSeconds(), SECONDS).pollInterval(500, TimeUnit.MILLISECONDS)
                .failFast("container is no longer running", () -> !Boolean.TRUE.equals(docker.inspectContainerCmd(containerId).exec().getState().getRunning()))
                .until(() -> count(BEGIN_STARTUP, managerLog(opennms, managerLog)), greaterThanOrEqualTo(startsBefore + 1));
        final Duration toBeginStartup = sinceNanos(startStart);

        await("second Startup complete").atMost(RESTART_LIMIT.getSeconds(), SECONDS).pollInterval(500, TimeUnit.MILLISECONDS)
                .failFast("container is no longer running", () -> !Boolean.TRUE.equals(docker.inspectContainerCmd(containerId).exec().getState().getRunning()))
                .until(() -> count(STARTUP_COMPLETE, managerLog(opennms, managerLog)), greaterThanOrEqualTo(startsBefore + 1));
        final Duration toStartupComplete = sinceNanos(startStart);

        final RestHealthClient health = new RestHealthClient(opennms.getWebUrl(), Optional.of(OpenNMSContainer.ALIAS));
        await("health check after restart").atMost(RESTART_LIMIT.getSeconds(), SECONDS).pollInterval(1, SECONDS)
                .ignoreExceptionsMatching(e -> e.getCause() instanceof SocketException)
                .until(health::getProbeHealthResponse, containsString(health.getProbeSuccessMessage()));
        final Duration toHealthy = sinceNanos(startStart);

        // Time spent inside the JVM, from the log timestamps of the second start.
        final String log = managerLog(opennms, managerLog);
        final Duration jvmStartup = Duration.between(
                lastTimestamp(BEGIN_STARTUP, log), lastTimestamp(STARTUP_COMPLETE, log));

        LOG.info("Core restart measurement (ms): stop={} (exit code {}), entrypoint={}, daemons={}, health={}, total={}",
                stopDuration.toMillis(),
                exitCode,
                toBeginStartup.toMillis(),
                jvmStartup.toMillis(),
                toHealthy.minus(toStartupComplete).toMillis(),
                toHealthy.toMillis());
        if (toHealthy.compareTo(RESTART_TARGET) > 0) {
            LOG.warn("Core restart took {} ms, target is {} ms", toHealthy.toMillis(), RESTART_TARGET.toMillis());
        }

        assertThat("stop time (ms)", stopDuration.toMillis(), lessThanOrEqualTo(STOP_LIMIT.toMillis()));
        assertThat("restart time (ms)", toHealthy.toMillis(), lessThanOrEqualTo(RESTART_LIMIT.toMillis()));
    }

    private static String managerLog(final OpenNMSContainer opennms, final Path managerLog) {
        return TestContainerUtils.getFileFromContainerAsString(opennms, managerLog);
    }

    private static int count(final Pattern pattern, final String text) {
        int n = 0;
        final Matcher m = pattern.matcher(text);
        while (m.find()) {
            n++;
        }
        return n;
    }

    private static LocalDateTime lastTimestamp(final Pattern pattern, final String text) {
        String last = null;
        final Matcher m = pattern.matcher(text);
        while (m.find()) {
            last = m.group(1);
        }
        if (last == null) {
            throw new IllegalStateException("No match for " + pattern + " in manager.log");
        }
        return LocalDateTime.parse(last, LOG_TIMESTAMP);
    }

    private static Duration sinceNanos(final long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}
