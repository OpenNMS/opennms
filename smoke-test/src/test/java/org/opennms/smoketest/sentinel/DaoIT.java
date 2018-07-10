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
import java.nio.file.Paths;
import java.util.Arrays;
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

// Verifies that all exposed service DAOs can be loaded in a sentinel container
public class DaoIT {

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
            final TestEnvironmentBuilder builder = TestEnvironment.builder().opennms().sentinel();

            // Auto-Deploy sentinel-persistence feature and the dao-test bundle, which verifies the DAOs afterwards
            builder.withSentinelEnvironment()
                    .addFile(getClass().getResource("/sentinel/features-dao.xml"), "deploy/features.xml")
                    .addFile(Paths.get("target/deploy-artifacts/org.opennms.features.distributed.dao-test.jar").toUri().toURL(), "deploy/org.opennms.features.distributed.dao-test.jar");
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
    public void verifyDaos() {
        final InetSocketAddress sentinelSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);

        // Ensure we are actually started the sink and are ready to listen for messages
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sentinelSshAddress, "admin", "admin")) {
                        final PrintStream pipe = sshClient.openShell();
                        final String command ="bundle:list";
                        pipe.println(command);
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final boolean bundleActive = Arrays.stream(shellOutput.split("\n"))
                                            .filter(row -> row.contains("OpenNMS :: Features :: Distributed :: DAO :: Test"))
                                            .findFirst().filter(bundle -> bundle.contains("Active"))
                                            .isPresent();
                        logger.info(command);
                        logger.info("{}", shellOutput);
                        return bundleActive;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify sentinel startup: {}", ex.getMessage());
                        return false;
                    }
                });

    }
}
