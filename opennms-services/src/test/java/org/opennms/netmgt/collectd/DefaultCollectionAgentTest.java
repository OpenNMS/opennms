/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import org.junit.After;
import org.junit.Test;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.springframework.transaction.PlatformTransactionManager;

import static org.mockito.Mockito.*;

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
        SnmpCollectionAgent agent = DefaultCollectionAgent.create(ifaceId, ifaceDao, transMgr);
        agent.getAgentConfig();

        // Verify
        verify(snmpPeerFactory, times(1)).getAgentConfig(any(), eq("Ocracoke"));
    }
}
