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
