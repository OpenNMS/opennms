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
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.KarafShellUtils;
import org.opennms.smoketest.utils.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeseriesAPIIT {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesAPIIT.class);

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL_WITH_DEFAULT_LOCALHOST;

    private KarafShell karafShell = new KarafShell(stack.opennms().getSshAddress());

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Make sure the Karaf shell is healthy before we start
        KarafShellUtils.awaitHealthCheckSucceeded(stack.opennms());
    }

    @Test
    public void canLoadTimeseriesFeature() throws Exception {
        assertTrue("no errors installing opennms-timeseries-api", karafShell.runCommandOnce("feature:install opennms-timeseries-api", output -> !output.toLowerCase().contains("error"), false));

        KarafShellUtils.testHealthCheckSucceeded(stack.opennms().getSshAddress());
    }

    @Test
    @org.junit.Ignore
    public void testDualWrites() throws Exception {
        assertTrue("no errors installing opennms-timeseries-api", karafShell.runCommandOnce("feature:install opennms-timeseries-api", output -> !output.toLowerCase().contains("error"), false));
        assertTrue("no errors installing inmemory-timeseries-plugin", karafShell.runCommandOnce("feature:install inmemory-timeseries-plugin", output -> !output.toLowerCase().contains("error"), false));
        KarafShellUtils.testHealthCheckSucceeded(stack.opennms().getSshAddress());

        final Set<String> originalRrdFiles = getRrdFiles();

        // Take a peek
        System.out.println("<RRD FILES>");
        originalRrdFiles.stream().forEach(r -> System.out.println(r));
        System.out.println("</RRD FILES>");

        //Wait for data
        await().atMost(5, TimeUnit.MINUTES)
                .pollInterval(15, TimeUnit.SECONDS)
                .until(() -> karafShell.runCommandOnce("opennms:get-tss-plugin-metrics",
                        output -> Arrays.stream(output.split("\n"))
                                   .filter(line -> line.contains("data points"))
                                   .count() >= originalRrdFiles.size(),
                        false));

        // Collect metrics from plugin
        System.out.println("<KARAF OUT>");
        List<String> karafMetrics = new ArrayList<>();
        karafShell.runCommandOnce(
                "opennms:get-tss-plugin-metrics",
                output -> {
                    Arrays.stream(output.split("\n"))
                           .filter(line -> line.contains("data points"))
                           .forEach(line -> {
                               System.out.println(line);
                               karafMetrics.add(line);
                           });
                    return !karafMetrics.isEmpty();
                }
                , false);
        System.out.println("</KARAF OUT>");

        Set<String> rrdFiles = getRrdFiles();

        // Take a peek
        System.out.println("<RRD FILES>");
        rrdFiles.stream().forEach(r -> System.out.println(r));
        System.out.println("</RRD FILES>");

        // Make sure all RRD files have corresponding entries stored in the plugin
        rrdFiles.stream().forEach(rrdFile -> {
            String metricName = rrdFile.substring(0, rrdFile.length() - 4);
            assertTrue(metricName + " must contain a match in the kafka stream", karafMetrics.stream().anyMatch(x -> x.contains(metricName)));
        });
    }

    Set<String> getRrdFiles() {
        // Use resources to get RRD files
        final RestClient client = stack.opennms().getRestClient();
        final ResourceDTO resources = client.getResourcesForNode("selfmonitor:1");
        return resources.getChildren().getObjects().stream()
                .flatMap(r -> r.getRrdGraphAttributes().values().stream())
                .map(e -> e.getRrdFile())
                .collect(Collectors.toSet());
    }
}
