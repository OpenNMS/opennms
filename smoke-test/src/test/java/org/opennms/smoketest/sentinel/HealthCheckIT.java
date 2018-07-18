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

package org.opennms.smoketest.sentinel;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class HealthCheckIT {

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder()
                    .opennms()
                    .minion()
                    .es6()
                    .sentinel();

            // Install some features to have health:check process something

            builder.withSentinelEnvironment()
                    .addFile(getClass().getResource("/sentinel/features-jms.xml"), "deploy/features.xml");
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            return builder.build();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Test
    public void verifyHealthCheck() {
        final InetSocketAddress sentinelSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);

        // Ensure we are actually started the sink and are ready to listen for messages
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sentinelSshAddress, "admin", "admin")) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("health:check");
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final int count = StringUtils.countOccurrencesOf(shellOutput, "Success");

                        logger.info("log:display");
                        logger.info("{}", shellOutput);
                        return count == 6;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify health:check: {}", ex.getMessage());
                        return false;
                    }
                });
    }
}
