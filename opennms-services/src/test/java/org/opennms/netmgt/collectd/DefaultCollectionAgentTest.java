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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Test;
import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.core.DefaultCollectionAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.PlatformTransactionManager;

public class DefaultCollectionAgentTest {

    @After
    public void tearDown() {
        // Always reset the peer factory
        SnmpPeerFactory.setInstance(null);
    }

    /**
     * Verifies that the SNMP agent configuration is retrieved using
     * the location name that is associated with the interface/node.
     */
    @Test
    public void canGetLocationAwareAgentConfig() {
        // Mock the peer factory
        SnmpPeerFactory snmpPeerFactory = mock(SnmpPeerFactory.class);
        SnmpPeerFactory.setInstance(snmpPeerFactory);

        // Mock the other arguments required to create a DefaultCollectionAgent
        Integer ifaceId = 1;
        IpInterfaceDao ifaceDao = mock(IpInterfaceDao.class);
        PlatformTransactionManager transMgr = mock(PlatformTransactionManager.class);

        OnmsIpInterface ipIface = mock(OnmsIpInterface.class, RETURNS_DEEP_STUBS);
        when(ifaceDao.load(ifaceId)).thenReturn(ipIface);
        when(ipIface.getNode().getLocation().getLocationName()).thenReturn("Ocracoke");

        // Retrieve the agent configuration
        SnmpCollectionAgent agent = DefaultSnmpCollectionAgent.create(ifaceId, ifaceDao, transMgr);
        agent.getAgentConfig();

        // Verify
        verify(snmpPeerFactory, times(1)).getAgentConfig(any(), eq("Ocracoke"));
    }

    /**
     * NMS-5105: When processing serviceDeleted and interfaceDeleted events
     * in Collectd we need to match both the Node ID and IP Address of
     * the service that is being collected with the information from the event.
     *
     * Since the entities have been deleted, we not longer be able to reach
     * in the database to fetch the required details. Instead, they
     * should be loaded when the agent is created, and cached for the lifetime
     * of the object.
     */
    @Test
    public void verifyThatTheIpAndNodeIdAreCached() {
        OnmsNode node = new OnmsNode();
        node.setId(11);

        OnmsIpInterface iface = new OnmsIpInterface();
        iface.setId(42);
        iface.setNode(node);
        iface.setIpAddress(InetAddressUtils.ONE_TWENTY_SEVEN);

        IpInterfaceDao ifaceDao = mock(IpInterfaceDao.class);
        when(ifaceDao.load(iface.getId())).thenReturn(iface);

        PlatformTransactionManager transMgr = new MockPlatformTransactionManager();

        CollectionAgent agent = DefaultCollectionAgent.create(iface.getId(), ifaceDao, transMgr);

        assertEquals(iface.getIpAddress(), agent.getAddress());
        assertEquals(node.getId().intValue(), agent.getNodeId());
    }
}
