/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;

import java.io.PrintStream;
import java.net.InetSocketAddress;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeseriesAPIIT {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesAPIIT.class);

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    @Test
    public void canLoadTimeseriesFeature() throws Exception {
        final InetSocketAddress opennmsSsh = stack.opennms().getSshAddress();
        try (final SshClient sshClient = new SshClient(opennmsSsh, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("feature:install opennms-timeseries-api");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
            assertEquals(Boolean.FALSE, sshClient.getStderr().toLowerCase().contains("error"));
        }
    }
}