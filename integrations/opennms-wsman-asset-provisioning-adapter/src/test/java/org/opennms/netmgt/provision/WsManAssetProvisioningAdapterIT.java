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
package org.opennms.netmgt.provision;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;
import org.w3c.dom.Node;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class WsManAssetProvisioningAdapterIT {

    @Autowired
    private WsManAssetProvisioningAdapter adapter;

    @Autowired
    private TransactionTemplate template;

    @Autowired
    private NodeDao nodeDao;

    private OnmsNode node;

    @Before
    public void setUp() {
        // Delete any existing nodes
        nodeDao.findAll().forEach(n -> nodeDao.delete(n));

        // Create a new node
        final NetworkBuilder nb = new NetworkBuilder();
        nb.addNode("R1").setForeignSource("Microsoft").setForeignId("1").setSysObjectId(".1.3.6.1.4.1.9.1.222");
        nb.addInterface("192.168.0.1").setIsSnmpPrimary("P").setIsManaged("P");
        node = nb.getCurrentNode();
        // WS-Man lookups are vendor dependent, use a known one from the default config.
        node.getAssetRecord().setVendor("Microsoft Corporation");
        nodeDao.save(node);
    }

    @Test
    public void canPopulateAssetFieldsOnNodeUpdate() {
        triggerAdapterAndVerifyAssetPopulation(nodeId -> adapter.doUpdateNode(nodeId));
    }

    @Test
    public void canPopulateAssetFieldsOnNodeAdd() {
        triggerAdapterAndVerifyAssetPopulation(nodeId -> adapter.doAddNode(nodeId));
    }

    private void triggerAdapterAndVerifyAssetPopulation(Consumer<Integer> triggerAdapterForNodeId) {
        final WSManClientFactory clientFactory = mock(WSManClientFactory.class);
        // Use our mock client factory
        adapter.setWsmanClientFactory(clientFactory);

        // Return a mock client
        final WSManClient client = mock(WSManClient.class);
        when(clientFactory.getClient(any(WSManEndpoint.class))).thenReturn(client);

        when(client.enumerateAndPullUsingFilter(any(), any(), any(), any(), anyBoolean())).then((Answer) invocation -> {
            final String query = invocation.getArgument(2);
            @SuppressWarnings("unchecked")
            final List<Node> nodes = (List<Node>)invocation.getArgument(3);

            Node node = mock(Node.class);
            when(node.getTextContent()).thenReturn("Wesley");
            nodes.add(node);

            // Add another node if the query contains the given substring
            if (query.contains("caption")) {
                node = mock(Node.class);
                when(node.getTextContent()).thenReturn("Snipes");
                nodes.add(node);
            }

            return null;
        });

        // Now trigger the adapter
        triggerAdapterForNodeId.accept(node.getId());

        // Retrieve our node from the database again
        node = template.execute(status -> {
            final OnmsNode newNode = nodeDao.get(node.getId());
            // Trigger a lazy load of the asset record
            //noinspection ResultOfMethodCallIgnored
            newNode.getAssetRecord().getVendor();
            return newNode;
        });

        // Verify a field that contains concatenated results
        assertThat(node.getAssetRecord().getOperatingSystem(), equalTo("Wesley\nSnipes"));
        // Verify a field that contains a single result
        assertThat(node.getAssetRecord().getRam(), equalTo("Wesley"));
    }

}
