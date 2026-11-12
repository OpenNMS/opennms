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
package org.opennms.smoketest.health;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.InetSocketAddress;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Sender;
import org.opennms.smoketest.utils.RestClient;

/**
 * Verifies that Telemetryd runs, the health check succeeds and telemetry data is persisted
 * when Pollerd, Collectd and Provisiond are disabled.
 */
public class DisabledServicesHealthCheckIT extends AbstractDisabledServicesHealthCheckIT {

    private static final List<String> DISABLED_DAEMONS = List.of("Pollerd", "Collectd", "Provisiond");

    private static final String FOREIGN_SOURCE = "telemetry-test";

    // The system ID in the JTI payload
    private static final String JTI_SYSTEM_ID = "192.168.1.1";

    @ClassRule
    public static final OpenNMSStack stack = stackWithDisabledServices(DISABLED_DAEMONS);

    @Override
    protected OpenNMSStack stack() {
        return stack;
    }

    @Override
    protected List<String> disabledDaemons() {
        return DISABLED_DAEMONS;
    }

    @Override
    protected List<String> requiredDaemons() {
        return List.of("Telemetryd");
    }

    @Test
    public void verifyTelemetryIsPersisted() {
        final RestClient restClient = stack.opennms().getRestClient();
        final String nodeCriteria = FOREIGN_SOURCE + ":" + JTI_SYSTEM_ID;

        // Provisiond is disabled, so create the node through the REST API
        OnmsNode node = new OnmsNode();
        node.setLabel(JTI_SYSTEM_ID);
        node.setType(OnmsNode.NodeType.ACTIVE);
        node.setForeignSource(FOREIGN_SOURCE);
        node.setForeignId(JTI_SYSTEM_ID);
        Response response = restClient.addNode(node);
        assertThat(response.getStatus(), is(201));
        node = restClient.getNode(nodeCriteria);

        final OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setNode(node);
        ipInterface.setIpAddress(InetAddressUtils.getInetAddress(JTI_SYSTEM_ID));
        ipInterface.setIpHostName(JTI_SYSTEM_ID);
        response = restClient.addInterface(nodeCriteria, ipInterface);
        assertThat(response.getStatus(), is(201));

        final InetSocketAddress jtiAddress = stack.opennms().getNetworkProtocolAddress(NetworkProtocol.JTI);
        final String nodeId = Integer.toString(node.getId());
        await().atMost(2, MINUTES).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .ignoreExceptions()
                .until(() -> {
                    new Packet(Packets.JTI.getPayload()).send(Sender.udp(jtiAddress));
                    return hasInterfaceMetrics(restClient.getResourcesForNode(nodeId));
                });
    }

    private static boolean hasInterfaceMetrics(final ResourceDTO resources) {
        return resources.getChildren().getObjects().stream()
                .flatMap(r -> r.getRrdGraphAttributes().values().stream())
                .anyMatch(a -> a.getRrdFile().startsWith("ifOutOctets"));
    }
}
