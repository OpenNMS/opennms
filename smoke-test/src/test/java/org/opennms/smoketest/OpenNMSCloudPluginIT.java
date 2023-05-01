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

import static org.awaitility.Awaitility.await;

import java.nio.file.Path;
import java.time.Duration;

import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.smoketest.containers.MockCloudContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShellUtils;

/*
 * Enforce ordering based on name so that the OpenNMS test runs before Sentinel.
 * The sentinel plugin requires a successful installation of the core plugin first.
 * Reason: The core plugin does the configuration and puts the certificates into the database via KV Store.
 * The Sentinel Plugin needs those certificates. The reason it was implemented that way:
 * The user needs to configure only one plugin.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(org.opennms.smoketest.junit.SentinelTests.class)
public class OpenNMSCloudPluginIT {

    private static final Path CLOUD_KAR_PATH_HOST = Path.of("target/plugin-cloud-assembly/org.opennms.plugins.cloud-assembly.kar");

    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.SENTINEL;

    @ClassRule
    public static MockCloudContainer mockCloudContainer = new MockCloudContainer();

    @Test
    public void installCloudPluginInOpennmsMustBeSuccessful() throws Exception {
        // Given
        stack.opennms().installFeature("opennms-plugin-cloud-core", CLOUD_KAR_PATH_HOST, MockCloudContainer.createConfig());

        // When
        KarafShellUtils.withKarafShell(stack.opennms().getSshAddress(), Duration.ofMinutes(3), streams -> {
            streams.stdin.println("feature:list | grep opennms-plugin-cloud-core");
            await().atMost(Duration.ofMinutes(1)).until(() -> streams.stdout.getLines().stream().anyMatch(line -> line.contains("Started")));
            streams.stdin.println("opennms-cloud:init key");
            await().atMost(Duration.ofMinutes(1)).until(() ->
                    String.join("\n", streams.stdout.getLines()).contains("Initialization of cloud plugin in OPENNMS was successful"));
            return true;
        });
    }

    @Test
    public void installCloudPluginInSentinelMustBeSuccessful() throws Exception {
        // Given
        stack.sentinel().installFeature("opennms-plugin-cloud-sentinel", CLOUD_KAR_PATH_HOST, MockCloudContainer.createConfig());

        // When
        KarafShellUtils.withKarafShell(stack.sentinel().getSshAddress(), Duration.ofMinutes(3), streams -> {
            streams.stdin.println("feature:list | grep opennms-plugin-cloud-sentinel");
            await().atMost(Duration.ofMinutes(1)).until(() -> streams.stdout.getLines().stream().anyMatch(line -> line.contains("Started")));
            await().atMost(Duration.ofMinutes(1)).until(() ->
            {
                streams.stdin.println("opennms-cloud:init key");
                return streams.stdout.getLines().stream().anyMatch(line -> line.contains("Initialization of cloud plugin in SENTINEL was successful."));
            });
            await().atMost(Duration.ofMinutes(1)).until(() ->
                    String.join("\n", streams.stdout.getLines()).contains("Initialization of cloud plugin in SENTINEL was successful."));
            return true;
        });
    }
}
