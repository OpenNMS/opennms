/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.KarafShellUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.junit.experimental.categories.Category(org.opennms.smoketest.junit.FlakyTests.class)
public class CortexTssTimeseriesPluginIT {

    private static final Logger LOG = LoggerFactory.getLogger(CortexTssTimeseriesPluginIT.class);

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;

    private KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());

    @Before
    public void setUp() throws IOException, InterruptedException {
        if (!CortexTssPluginIT.CORTEX_PLUGIN_KAR.toFile().exists()) {
            FileUtils.copyURLToFile(new URL(CortexTssPluginIT.CORTEX_PLUGIN_RELEASE), CortexTssPluginIT.CORTEX_PLUGIN_KAR.toFile());
        }

        // Make sure the Karaf shell is healthy before we start
        KarafShellUtils.awaitHealthCheckSucceeded(stack.opennms());
    }

    @Test
    public void canLoadTimeseriesFeatureWithCortex() throws Exception {
        stack.opennms().installFeature("opennms-plugins-cortex-tss", CortexTssPluginIT.CORTEX_PLUGIN_KAR);
        assertTrue(karafShell.runCommandOnce("feature:install opennms-timeseries-api", output -> !output.toLowerCase().contains("error"), false));

        KarafShellUtils.testHealthCheckSucceeded(stack.opennms().getSshAddress());
    }
}
