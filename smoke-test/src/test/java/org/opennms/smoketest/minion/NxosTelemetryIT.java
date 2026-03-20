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
package org.opennms.smoketest.minion;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Sender;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(MinionTests.class)
public class NxosTelemetryIT {

    private static final Logger LOG = LoggerFactory.getLogger(NxosTelemetryIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    @Test
    public void verifyNxosTelemetryOnOpenNMS() throws Exception {
        final Date startOfTest = new Date();
        final OnmsNode onmsNode = addRequisition(stack, false, startOfTest);
        final InetSocketAddress opennmsNxosPort = stack.opennms().getNetworkProtocolAddress(NetworkProtocol.NXOS);
        await().atMost(1, MINUTES).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(() -> {
                    sendNxosTelemetryMessage(opennmsNxosPort);
                    return matchRrdFileFromNodeResource(onmsNode.getId());
                });
    }

    @Test
    public void verifyNxosTelemetryOnMinion() {
        final Date startOfTest = new Date();
        final OnmsNode onmsNode = addRequisition(stack, true, startOfTest);
        final InetSocketAddress minionNxosPort = stack.minion().getNetworkProtocolAddress(NetworkProtocol.NXOS);
        await().atMost(2, MINUTES).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(() -> {
                    sendNxosTelemetryMessage(minionNxosPort);
                    return matchRrdFileFromNodeResource(onmsNode.getId());
                });
    }

    public static void sendNxosTelemetryMessage(InetSocketAddress udpAddress) {
        try {
            new Packet(Packets.NXOS.getPayload()).send(Sender.udp(udpAddress));
        } catch (IOException e) {
            LOG.error("Exception while sending NXOS packets", e);
        }
    }

    public static boolean matchRrdFileFromNodeResource(Integer id) {
        final RestClient client = stack.opennms().getRestClient();
        final ResourceDTO resources = client.getResourcesForNode(Integer.toString(id));
        return resources.getChildren().getObjects().stream()
                .flatMap(r -> r.getRrdGraphAttributes().values().stream())
                .anyMatch(a -> a.getRrdFile().startsWith("load_avg_1min"));
    }

    public static OnmsNode addRequisition(OpenNMSStack stack, boolean isMinion, Date startOfTest) {

        RestClient client = stack.opennms().getRestClient();
        Requisition requisition = new Requisition("telemetry");
        List<RequisitionInterface> interfaces = new ArrayList<>();
        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr("192.168.0.1");
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);
        RequisitionNode node = new RequisitionNode();
        String label = "nexus9k";
        node.setNodeLabel(label);
        node.setForeignId("nxos");
        node.setInterfaces(interfaces);
        if (isMinion) {
            // For a requisition, foreignId needs to be unique, change foreignId
            node.setLocation("MINION");
            node.setForeignId("nexus9k");
            // Change label so that node matches with foreignId
            label = "nxos";
            node.setNodeLabel(label);
        }
        requisition.insertNode(node);
        client.addOrReplaceRequisition(requisition);
        client.importRequisition("telemetry");

        NodeDao nodeDao = stack.postgres().dao(NodeDaoHibernate.class);

        final OnmsNode onmsNode = await().atMost(3, MINUTES).pollInterval(30, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class)
                        .ge("createTime", startOfTest).eq("label", label).toCriteria()), notNullValue());

        assertNotNull(onmsNode);

        return onmsNode;

    }

}
