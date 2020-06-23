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

import static org.awaitility.Awaitility.await;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.smoketest.containers.OpenNMSContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KarafShellUtils {
    private static final Logger LOG = LoggerFactory.getLogger(KarafShellUtils.class);

    public static boolean testHealthCheck(InetSocketAddress sshAddr) {
        return testHealthCheck(sshAddr, new AtomicReference<>());
    }

    public static boolean testHealthCheck(InetSocketAddress sshAddr, AtomicReference<String> lastOutput) {
        try (final SshClient sshClient = new SshClient(sshAddr, OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)) {
            // Issue the 'health:check' command
            PrintStream pipe = sshClient.openShell();
            pipe.println("health:check");
            pipe.println("logout");

            await().atMost(2, TimeUnit.MINUTES).until(sshClient.isShellClosedCallable());

            // Grab the output
            String shellOutput = sshClient.getStdout();
            LOG.info("health:check output: {}", shellOutput);
            lastOutput.set(shellOutput);

            // Verify
            return shellOutput.contains("awesome");
        } catch (Exception e) {
            LOG.error("Health check did not pass on: {}", sshAddr, e);
        }
        return false;
    }
}
