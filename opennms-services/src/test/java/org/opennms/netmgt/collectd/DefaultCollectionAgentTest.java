/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.easymock.EasyMock;
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

        IpInterfaceDao ifaceDao = EasyMock.createMock(IpInterfaceDao.class);
        EasyMock.expect(ifaceDao.load(iface.getId())).andReturn(iface).times(5);
        EasyMock.replay(ifaceDao);

        PlatformTransactionManager transMgr = new MockPlatformTransactionManager();

        CollectionAgent agent = DefaultCollectionAgent.create(iface.getId(), ifaceDao, transMgr);

        EasyMock.verify(ifaceDao);

        assertEquals(iface.getIpAddress(), agent.getAddress());
        assertEquals(node.getId().intValue(), agent.getNodeId());
    }
}
