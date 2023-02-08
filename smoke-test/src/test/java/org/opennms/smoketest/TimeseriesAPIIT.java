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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.KarafShellUtils;
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
        assertTrue(karafShell.runCommandOnce("feature:install opennms-timeseries-api", output -> !output.toLowerCase().contains("error"), false));

        KarafShellUtils.testHealthCheckSucceeded(stack.opennms().getSshAddress());
    }

    @Test
    public void testDualWrites() throws Exception {
        assertTrue(karafShell.runCommandOnce("feature:install opennms-timeseries-api", output -> !output.toLowerCase().contains("error"), false));
        assertTrue(karafShell.runCommandOnce("feature:install inmemory-timeseries-plugin", output -> !output.toLowerCase().contains("error"), false));
        KarafShellUtils.testHealthCheckSucceeded(stack.opennms().getSshAddress());
        printSummary();
        long start = System.currentTimeMillis();
        await().atMost(10, TimeUnit.MINUTES)
                .pollInterval(5, TimeUnit.SECONDS)
                .until(() -> karafShell.runCommandOnce("opennms:get-tss-plugin-metrics", output -> output.contains("data points"), false));
        String summaryUrl = String.format(
                "http://0.0.0.0:%d/opennms/summary/results.htm?filterRule=ipaddr+iplike+*.*.*.*&startTime=%d&endTime=%d&attributeSieve=.*",
                stack.opennms().getWebPort(),
                0,
                System.currentTimeMillis());
        Thread.sleep(120000);
        System.out.println("<KARAF OUT>");
        karafShell.runCommandOnce("opennms:get-tss-plugin-metrics", output -> {
            System.out.println(output);
            return !output.isEmpty();
        }
        , false);
        System.out.println("</KARAF OUT>");
        System.out.println("SUMMARY URL: " + summaryUrl);
        URL summary = new URL(summaryUrl);
        URLConnection connection = summary.openConnection();
        String userpass = String.format("%s:%s", OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
        String basicAuth = new String(Base64.getEncoder().encode(userpass.getBytes()));
        connection.setRequestProperty("Authorization", "Basic " + basicAuth);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String input;
        System.out.println("<SUMMARY CONTENTS>");
        while ((input = reader.readLine()) != null) {
            System.out.println(input);
        }
        System.out.println("</SUMMARY CONTENTS>");
    }

    private void printSummary() throws Exception {

        String summaryUrl = String.format(
                "http://0.0.0.0:%d/opennms/summary/results.htm?filterRule=ipaddr+iplike+*.*.*.*&startTime=%d&endTime=%d&attributeSieve=.*",
                stack.opennms().getWebPort(),
                0,
                System.currentTimeMillis());
        URL summary = new URL(summaryUrl);
        URLConnection connection = summary.openConnection();
        String userpass = String.format("%s:%s", OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD);
        String basicAuth = new String(Base64.getEncoder().encode(userpass.getBytes()));
        connection.setRequestProperty("Authorization", "Basic " + basicAuth);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String input;
        System.out.println("<SUMMARY CONTENTS>");
        while ((input = reader.readLine()) != null) {
            System.out.println(input);
        }
        System.out.println("</SUMMARY CONTENTS>");

    }

}