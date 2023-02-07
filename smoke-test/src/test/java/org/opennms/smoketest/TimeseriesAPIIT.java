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

import static org.junit.Assert.assertTrue;
import static com.jayway.awaitility.Awaitility.await;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.KarafShellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeseriesAPIIT {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesAPIIT.class);

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    private KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Make sure the Karaf shell is healthy before we start
        KarafShellUtils.awaitHealthCheckSucceeded(stack.opennms());
    }

    @Test
    public void canLoadTimeseriesFeature() throws Exception {
        assertTrue(karafShell.runCommandOnce("feature:install opennms-timeseries-api", output -> !output.toLowerCase().contains("error"), false));

        KarafShellUtils.testHealthCheckSucceeded(stack.opennms().getSshAddress());
    }

    @Test
    public void testDualWrites() throws Exception {
        assertTrue(karafShell.runCommandOnce("feature:install opennms-timeseries-api", output -> !output.toLowerCase().contains("error"), false));
        assertTrue(karafShell.runCommandOnce("feature:install inmemory-timeseries-plugin", output -> !output.toLowerCase().contains("error"), false));
        KarafShellUtils.testHealthCheckSucceeded(stack.opennms().getSshAddress());
        await().atMost(5, TimeUnit.MINUTES)
                .pollInterval(5, TimeUnit.SECONDS)
                .until(() -> karafShell.runCommandOnce("opennms:get-tss-plugin-metrics", output -> !output.isEmpty(), false));
    }
}