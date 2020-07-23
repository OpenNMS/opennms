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

import static org.opennms.smoketest.minion.JtiTelemetryIT.sendJtiTelemetryMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Function;

import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.smoketest.stacks.NetworkProtocol;

/**
 * Verifies that JTI messages are persisted to newts if set up correctly.
 */
public class JtiTelemetryAdapterIT extends AbstractAdapterIT {

    @Override
    protected void sendTelemetryMessage() throws IOException {
        final InetSocketAddress minionListenerAddress = stack.minion().getNetworkProtocolAddress(NetworkProtocol.JTI);
        sendJtiTelemetryMessage(minionListenerAddress);
    }

    @Override
    protected String getResourceId() {
        return "node[telemetry-jti:dummy-node].interfaceSnmp[eth0_system_test]/ifOutOctets";
    }

    @Override
    protected Function<String, Boolean> getSentinelReadyVerificationFunction() {
        return (output) -> output.contains("Route: Sink.Server.Telemetry-JTI started and consuming from: queuingservice://" + SystemInfoUtils.getInstanceId() + ".Sink.Telemetry-JTI");
    }

    @Override
    protected RequisitionCreateInfo getRequisitionToCreate() {
        final RequisitionCreateInfo requisitionCreateInfo = new RequisitionCreateInfo();
        requisitionCreateInfo.ipAddress = "192.168.1.1";
        requisitionCreateInfo.foreignSource = "telemetry-jti";
        requisitionCreateInfo.foreignId = "dummy-node";
        requisitionCreateInfo.nodeLabel = "Dummy Node";
        return requisitionCreateInfo;
    }
}
