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
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
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
        SnmpMinimalPollInterface iface = new SnmpMinimalPollInterface(6, SnmpInterfaceStatus.INVALID,
                SnmpInterfaceStatus.INVALID);
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
        assertEquals(SnmpInterfaceStatus.UP, interfacesResult.get(0).getAdminstatus());
        assertEquals(SnmpInterfaceStatus.DOWN, interfacesResult.get(0).getOperstatus());
    }

}
