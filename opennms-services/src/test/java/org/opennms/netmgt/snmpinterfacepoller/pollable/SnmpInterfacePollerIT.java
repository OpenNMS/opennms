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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.common.LocationAwareSnmpClientRpcImpl;
import org.opennms.netmgt.snmpinterfacepoller.SnmpPollInterfaceMonitor;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface.SnmpMinimalPollInterface;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml" })
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(host = "192.168.2.2", resource = "classpath:/snmpInterfacePollerTestData.properties")
public class SnmpInterfacePollerIT {

    private LocationAwareSnmpClient locationAwareSnmpClient;

    List<SnmpMinimalPollInterface> interfacesResult = new ArrayList<>();

    private SnmpPollInterfaceMonitor snmpInterfaceMonitor;

    @Autowired
    private SnmpPeerFactory snmpPeerFactory;

    @Before
    public void setup() {
        locationAwareSnmpClient = new LocationAwareSnmpClientRpcImpl(new MockRpcClientFactory());
        snmpInterfaceMonitor = new SnmpPollInterfaceMonitor(locationAwareSnmpClient);
        SnmpPeerFactory.setInstance(snmpPeerFactory);
        // Set admin/operational status as unknown
        SnmpMinimalPollInterface iface = new SnmpMinimalPollInterface(6, SnmpMinimalPollInterface.IF_UNKNOWN,
                SnmpMinimalPollInterface.IF_UNKNOWN);
        interfacesResult.add(iface);

    }

    @Test
    public void testSnmpInterfacePoll() throws UnknownHostException {

        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
                .getAgentConfig(InetAddressUtils.getInetAddress("192.168.2.2"));
        // Invoke poll directly
        interfacesResult = snmpInterfaceMonitor.poll(agentConfig, interfacesResult);
        // Check if Poll Status is UP
        assertEquals(1, interfacesResult.get(0).getStatus().getStatusCode());
        // Check if Admin/Operational Status are retrieved properly
        assertEquals(1, interfacesResult.get(0).getAdminstatus());
        assertEquals(2, interfacesResult.get(0).getOperstatus());
    }

}
