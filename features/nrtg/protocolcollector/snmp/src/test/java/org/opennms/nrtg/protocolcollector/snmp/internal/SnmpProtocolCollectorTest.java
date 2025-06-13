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
package org.opennms.nrtg.protocolcollector.snmp.internal;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.DefaultCollectionJob;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * TODO Tak refactor this test to be snmp and not tca
 * @author Markus Neumann
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/SnmpProtocolCollectorTestContext.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(port = 9161, host = "127.0.0.1", resource = "classpath:/SnmpSample.properties")
public class SnmpProtocolCollectorTest implements InitializingBean {

    @Autowired
    private SnmpProtocolCollector protocolCollector;

    private CollectionJob collectionJob;
    private InetAddress localhost;
    private SnmpAgentConfig snmpAgentConfig;
    private Set<String> destinations;
    
    private final String testMetric = ".1.3.6.1.2.1.1.1.0";
    private final String testMetricValue = "Mock Juniper TCA Device";
    
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        protocolCollector.setNodeDao(new MockNodeDao());
    }

    @Before
    public void setup() throws Exception {
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        localhost = InetAddress.getByName("127.0.0.1");
        snmpAgentConfig = SnmpPeerFactory.getInstance().getAgentConfig(localhost);
        collectionJob = new DefaultCollectionJob();
        collectionJob.setProtocolConfiguration(snmpAgentConfig.toProtocolConfigString());
        destinations = new HashSet<>();
        destinations.add("test");
    }

    @Test
    public void testCollect() {
        collectionJob.setService("SNMP");
        collectionJob.setNodeId(1);
        collectionJob.setNetInterface(localhost.getHostAddress());
        collectionJob.addMetric(testMetric, destinations, "OnmsLocicMetricId");
        collectionJob.setId("testing");
        CollectionJob result = protocolCollector.collect(collectionJob);
        Assert.assertEquals(result.getService(), "SNMP");
        Assert.assertEquals(result.getMetricValue(testMetric), testMetricValue);
    }

    @Test
    public void testGetProtocol() {
        Assert.assertEquals("SNMP", protocolCollector.getProtcol());
    }

    @Test
    public void testAgent() throws Exception {
        SnmpValue snmpValue = SnmpUtils.get(snmpAgentConfig, SnmpObjId.get(".1.3.6.1.2.1.1.1.0"));
        Assert.assertEquals("Mock Juniper TCA Device", snmpValue.toDisplayString());
    }
}