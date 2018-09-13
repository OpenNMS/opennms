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

import static org.opennms.smoketest.minion.NxosTelemetryIT.sendNxosTelemetryMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.function.Function;

import org.opennms.smoketest.utils.TargetRoot;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

/**
 * Verifies that NXOS messages are persisted to newts if set up correctly.
 */
public class NxosTelemetryAdapterIT extends AbstractAdapterIT {

    @Override
    protected void customizeTestEnvironment(TestEnvironmentBuilder builder) {
        // Enable NXOS Adapter
        final Path opennmsSourceEtcDirectory = new TargetRoot(getClass()).getPath("system-test-resources", "etc");
        builder.withSentinelEnvironment()
                .addFile(getClass().getResource("/sentinel/features-newts-nxos.xml"), "deploy/features.xml")
                .addFile(opennmsSourceEtcDirectory.resolve("telemetryd-adapters/cisco-nxos-telemetry-interface.groovy"), "etc/cisco-nxos-telemetry-interface.groovy")
                .addFiles(opennmsSourceEtcDirectory.resolve("resource-types.d"), "etc/resource-types.d")
                .addFiles(opennmsSourceEtcDirectory.resolve("datacollection"), "etc/datacollection")
                .addFile(opennmsSourceEtcDirectory.resolve("datacollection-config.xml"), "etc/datacollection-config.xml");

        // Enable NXOS-Listener
        builder.withMinionEnvironment()
                .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50000-nxos.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-nxos.cfg")
        ;
    }

    @Override
    protected void sendTelemetryMessage() throws IOException {
        final InetSocketAddress minionListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, 50000, "udp");
        sendNxosTelemetryMessage(minionListenerAddress);
    }

    @Override
    protected String getResourceId() {
        return "node[telemetry-nxos:nexus9k].nodeSnmp[]/load_avg_1min";
    }

    @Override
    protected Function<String, Boolean> getSentinelReadyVerificationFunction() {
        return (output) -> output.contains("Route: Sink.Server.Telemetry-NXOS started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-NXOS");
    }

    @Override
    protected RequisitionCreateInfo getRequisitionToCreate() {
        RequisitionCreateInfo requisitionCreateInfo = new RequisitionCreateInfo();
        requisitionCreateInfo.foreignSource = "telemetry-nxos";
        requisitionCreateInfo.foreignId = "nexus9k";
        requisitionCreateInfo.nodeLabel = "nexus9k";
        requisitionCreateInfo.ipAddress = "192.168.0.1";
        return requisitionCreateInfo;
    }
}
