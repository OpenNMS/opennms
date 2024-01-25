/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest.sentinel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Function;

import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.telemetry.Payload;
import org.opennms.smoketest.telemetry.Sender;

/**
 * Verifies that sflow packets containing samples are also persisted if set up correctly
 */
public class SFlowTelemetryAdapterIT extends AbstractAdapterIT {

    @Override
    protected void sendTelemetryMessage() throws IOException {
        final InetSocketAddress minionListenerAddress = stack.minion().getNetworkProtocolAddress(NetworkProtocol.FLOWS);
        new Packet(Payload.resource("/payloads/flows/sflow2.dat")).send(Sender.udp(minionListenerAddress));
    }

    @Override
    protected String getResourceId() {
        return "node[telemetry-sflow:dummy-node].nodeSnmp[]/load_avg_5min";
    }

    @Override
    protected Function<String, Boolean> getSentinelReadyVerificationFunction() {
        return (output) -> output.contains("Route: Sink.Server.Telemetry-SFlow started and consuming from: queuingservice://" + SystemInfoUtils.getInstanceId() + ".Sink.Telemetry-SFlow");
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
