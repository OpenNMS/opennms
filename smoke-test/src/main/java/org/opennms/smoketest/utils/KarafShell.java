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

package org.opennms.smoketest.utils;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to run commands in the Karaf Shell as well as verify the log.
 *
 * @author mvrueden
 */
public class KarafShell {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final InetSocketAddress sshAddress;
    private final String username;
    private final String password;

    public KarafShell(InetSocketAddress sshAddress) {
        this(sshAddress, "admin", "admin");
    }

    public KarafShell(InetSocketAddress sshAddress, String username, String password) {
        this.sshAddress = Objects.requireNonNull(sshAddress);
        this.username = username;
        this.password = password;
    }

    public KarafShell runCommand(final String command, Function<String, Boolean> verifyOutputFunction) {
        return runCommand(command, verifyOutputFunction, true);
    }

    /**
     * Runs the given command in the karaf shell.
     * The optional <code>function</code> verifies the output.
     *
     * @param command the command to run, e.g. "features:list"
     * @param verifyOutputFunction An optional function to verify the output, e.g. to check for certain log messages
     * @param displayLogs include log:display in command output
     * @return The shell itself, to run further commands.
     */
    public KarafShell runCommand(final String command, Function<String, Boolean> verifyOutputFunction, boolean displayLogs) {
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> runCommandOnce(command, verifyOutputFunction, displayLogs));
        return this;
    }

    public Boolean runCommandOnce(String command, Function<String, Boolean> verifyOutputFunction, boolean displayLogs) {
        try (final SshClient sshClient = new SshClient(sshAddress, username, password)) {
            final PrintStream pipe = sshClient.openShell();

            List<String> foo = new LinkedList<String>();
            if (command != null) {
                foo.add(command);
            }
            if (displayLogs) {
                foo.add("log:display");
            }

            var output = run(foo.toArray(new String[0]));

            // Optionally Verify Output
            boolean result = true;
            if (verifyOutputFunction != null) {
                result = verifyOutputFunction.apply(output.getLeft());
            }

            // Log output
            if (command != null) {
                logger.info(command);
            }
            if (displayLogs) {
                logger.info("log:display");
            }
            logger.info("{}", output.getLeft());

            return result;
        } catch (Exception ex) {
            logger.error("Error while executing command '{}': {}", command, ex.getMessage());
            return false;
        }
    }

    public Pair<String, String> run(String... command) throws Exception {
        try (final SshClient sshClient = new SshClient(sshAddress, username, password)) {
            final PrintStream pipe = sshClient.openShell();
            for (String c : command) {
                pipe.println(c);
            }
            pipe.println("logout");

            // Wait for karaf to process the commands
            await().atMost(30, SECONDS).until(sshClient.isShellClosedCallable());

            return Pair.of(sshClient.getStdout(), sshClient.getStderr());
        }
    }

    /**
     * Is the same as <code>runCommand(command, null</code>.
     *
     * @see #runCommand(String, Function)
     */
    public KarafShell runCommand(String command) {
        runCommand(command, null);
        return this;
    }

    /**
     * Is the same as <code>runCommand(null, function)</code>.
     *
     * @see #runCommand(String, Function)
     */
    public KarafShell verifyLog(Function<String, Boolean> function) {
        Objects.requireNonNull(function);
        runCommand(null, function);
        return this;
    }

    public void checkFeature(String feature, String regex, Duration wait) {
        await(String.format("waiting for feature %s state to match regex '%s'", feature, regex))
                .atMost(wait)
                .until(() ->
                        runCommandOnce("feature:list | grep " + feature,
                                output -> output.matches("(?ms).*?\\|\\s*(" + regex + ")\\s*\\|.*"), false)
                );
    }
}
