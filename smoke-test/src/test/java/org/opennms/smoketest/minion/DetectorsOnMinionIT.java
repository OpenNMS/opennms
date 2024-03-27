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
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.collect.Iterables;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.ClientProtocolException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(MinionTests.class)
public class DetectorsOnMinionIT {

    private static final Logger LOG = LoggerFactory.getLogger(DetectorsOnMinionIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    private static final String LOCALHOST = "127.0.0.1";

    @Test
    public void checkServicesDetectedOnMinion() throws ClientProtocolException, IOException, InterruptedException {
        RestClient client = stack.opennms().getRestClient();
        addRequisition(client, "MINION", LOCALHOST);
        await().atMost(5, MINUTES).pollDelay(0, SECONDS).pollInterval(30, SECONDS)
                .until(getnumberOfServicesDetected(client), greaterThan(0));
    }
    
    public static void addRequisition(RestClient client, String location, String ipAddress) {

        Requisition requisition = new Requisition("foreignSource");
        List<RequisitionInterface> interfaces = new ArrayList<>();
        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr(ipAddress);
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);
        RequisitionNode node = new RequisitionNode();
        node.setNodeLabel(ipAddress);
        node.setLocation(location);
        node.setInterfaces(interfaces);
        node.setForeignId("foreignId");
        requisition.insertNode(node);

        client.addOrReplaceRequisition(requisition);
        client.importRequisition("foreignSource");

    }

    public static Callable<Integer> getnumberOfServicesDetected(RestClient client) {
        return new Callable<Integer>() {
            public Integer call() throws Exception {
                List<OnmsNode> nodes = client.getNodes();
                Integer number = null;
                if (Iterables.any(nodes, (node) -> "foreignSource".equals(node.getForeignSource()))) {
                    List<OnmsMonitoredService> services = client.getServicesForANode("foreignSource:foreignId",
                            LOCALHOST);
                    if (!CollectionUtils.isEmpty(services)) {
                        number = services.size();
                        LOG.info("The services detected  are \n");
                        for (OnmsMonitoredService service : services) {
                            LOG.info("   {}  ", service.getServiceName());
                        }
                    }
                }
                return number;
            }
        };
    }
}
