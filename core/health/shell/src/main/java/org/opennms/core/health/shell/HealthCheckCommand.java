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

package org.opennms.core.health.shell;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.Health;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.HealthCheckService;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "opennms", name = "health-check", description="Verifies that the container is healthy.")
@Service
public class HealthCheckCommand implements Action {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckCommand.class);
    public static final String SUCCESS_MESSAGE = "=> Everything is awesome";
    public static final String FAILURE_MESSAGE = "=> Oh no, something is wrong";

    public static boolean isEndMessage(String s) {
        // a newline is appended to the string -> do a startsWith check
        return s.startsWith(SUCCESS_MESSAGE) || s.startsWith(FAILURE_MESSAGE);
    }

    private static final String DESCRIPTION_FORMAT = "%%-%ds   ";
    private static final String STATUS_FORMAT = "[ %%-%ds ]";

    @Option(name = "-t", description = "Maximum number of milliseconds to wait before failing when waiting for a check to complete (e.g. try to establish a JMS session.")
    public long timeout = 5L * 1000L;

    @Option(name = "--maxAge", description = "Maximum age of cached health check responses in milliseconds. Defaults to 90,000.")
    public long maxAgeMs = 90000;

    @Reference
    private BundleContext bundleContext;

    @Reference
    private HealthCheckService healthCheckService;

    @Override
    public Object execute() throws Exception {
        // Print header
        System.out.println("Verifying the health of the container");
        System.out.println();
        LOG.debug("Verifying the health of the container");

        // Perform check
        final Context context = new Context();
        context.setTimeout(timeout);
        context.setMaxAge(Duration.ofMillis(maxAgeMs));

        final var listener = new Listener();

        healthCheckService.performAsyncHealthCheck(context, listener, null).fold(
                errorMessage -> {
                    System.out.println(Colorizer.colorize("Error: " + errorMessage, Color.Red));
                    System.out.println("=> Oh no, something is wrong");
                    LOG.debug("Error: {}", errorMessage);
                    LOG.debug("Oh no, something is wrong");
                    return null;
                },
                completionStage -> {
                    // the concurrent execution of health checks is started
                    // -> the listener is informed about progress and creates corresponding ouput
                    // -> consume that output and print it to the console
                    String next;
                    while ((next = listener.read()) != null) {
                        System.out.print(next);
                        System.out.flush();
                        LOG.debug(next);
                    }
                    return null;
                });

        return null;
    }

    private static Color determineColor(Status status) {
        switch (status) {
            case Failure: return Color.Red;
            case Timeout: return Color.Yellow;
            case Starting: return Color.Blue;
            case Success: return Color.Green;
            case Unknown: return Color.Yellow;
            default:      return Color.NoColor;
        }
    }

    /**
     * Listens for progress during the health check executions.
     * <p>
     * The listener must not output to the console directly when its callbacks are called. There is some multiplexing
     * logic that takes care that System.out is multiplexed to the Karaf shell session that invoked a command.
     * When a callback is called the listener just stores a corresponding messages in its {@code out} queue.
     * That queue is consumed by the thread that called the command. The final success or failure messages are
     * used like a poison pill to indicate the end of messages.
     */
    private static class Listener implements HealthCheckService.ProgressListener {

        private List<HealthCheck> checks;
        private String descFormat, statusFormat;
        private final Map<HealthCheck, Response> responses = new IdentityHashMap<>();
        private final BlockingQueue<String> out = new LinkedBlockingQueue<>();
        private boolean completed = false;

        private void print(String string) {
            out.add(string);
        }

        private void println() {
            print("\n");
        }

        private void println(String string) {
            print(string + "\n");
        }

        @Override
        public synchronized void onHealthChecksFound(List<HealthCheck> healthChecks) {
            this.checks = healthChecks;
            final int maxColorLength = Arrays.stream(Color.values()).map(c -> c.toAnsi()).max(Comparator.comparingInt(String::length)).get().length();
            final int maxDescriptionLength = healthChecks.stream().map(check -> check.getDescription()).max(Comparator.comparingInt(String::length)).orElse("").length();
            final int maxStatusLength = Arrays.stream(Status.values()).map(v -> v.name()).max(Comparator.comparingInt(String::length)).get().length() + maxColorLength + "\033[m".length() * 2 + Color.NoColor.toAnsi().length();
            descFormat = String.format(DESCRIPTION_FORMAT, maxDescriptionLength);
            statusFormat = String.format(STATUS_FORMAT, maxStatusLength);
            printCurrentExecutionState();
        }

        @Override
        public synchronized void onResponse(HealthCheck check, Response response) {
            responses.put(check, response);
            print(String.format("\033[%dA", checks.size())); // move cursor up
            printCurrentExecutionState();
        }

        @Override
        public synchronized void onAllHealthChecksCompleted(Health health) {
            println();
            if (health.isSuccess()) {
                println(SUCCESS_MESSAGE);
            } else {
                println(FAILURE_MESSAGE);
            }
        }

        private void printCurrentExecutionState() {
            for (var healthCheck : checks) {
                String str;
                str = String.format(descFormat, healthCheck.getDescription());
                var response = responses.get(healthCheck);
                if (response != null) {
                    final Status status = response.getStatus();
                    final Color statusColor = determineColor(status);
                    final String statusText = String.format(statusFormat, Colorizer.colorize(status.name(), statusColor));
                    str = str + statusText;
                    if (response.getMessage() != null) {
                        str = str + " => " + response.getMessage();
                    }
                } else {
                    str = str + "[ ]";
                }
                println(str);
            }
        }

        public String read() {
            if (completed) {
                return null;
            }
            try {
                var string = out.take();
                completed = isEndMessage(string);
                return string;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
