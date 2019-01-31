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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.function.Function;

import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.utils.TargetRoot;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

/**
 * Verifies that sflow packets containing samples are also persisted if set up correctly
 */
public class SFlowTelemetryAdapterIT extends AbstractAdapterIT {

    @Override
    protected void customizeTestEnvironment(TestEnvironmentBuilder builder) {
        builder.es6();

        // Enable SFlow Adapters
        final Path opennmsSourceEtcDirectory = new TargetRoot(getClass()).getPath("system-test-resources", "etc");
        builder.withSentinelEnvironment()
                .addFile(getClass().getResource("/sentinel/features-newts-sflow.xml"), "deploy/features.xml")
                .addFile(opennmsSourceEtcDirectory.resolve("telemetryd-adapters/sflow-host.groovy"), "etc/sflow-host.groovy");

        // Enable SFlow Listener
        builder.withMinionEnvironment()
                .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50003.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-sflow.cfg")
        ;
    }

    @Override
    protected void sendTelemetryMessage() throws IOException {
        final InetSocketAddress minionListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, 50003, "udp");
        new Packet("/flows/sflow2.dat", minionListenerAddress).send();
    }

    @Override
    protected String getResourceId() {
        return "node[telemetry-sflow:dummy-node].nodeSnmp[]/load_avg_5min";
    }

    @Override
    protected Function<String, Boolean> getSentinelReadyVerificationFunction() {
        return (output) -> output.contains("Route: Sink.Server.Telemetry-SFlow started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-SFlow");
    }

    @Override
    protected RequisitionCreateInfo getRequisitionToCreate() {
        RequisitionCreateInfo createInfo = new RequisitionCreateInfo();
        createInfo.foreignSource = "telemetry-sflow";
        createInfo.foreignId = "dummy-node";
        createInfo.nodeLabel = "Dummy Node";
        createInfo.ipAddress = "172.18.45.116";
        return createInfo;
    }
}
