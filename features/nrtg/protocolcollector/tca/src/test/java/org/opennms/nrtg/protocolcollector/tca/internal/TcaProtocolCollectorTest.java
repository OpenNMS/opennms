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
package org.opennms.nrtg.protocolcollector.tca.internal;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.DefaultCollectionJob;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.nrtg.api.ProtocolCollector;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Markus Neumann
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/TcaProtocolCollectorTestContext.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(port = 9161, host = "127.0.0.1", resource = "classpath:/juniperTcaSample.properties")
public class TcaProtocolCollectorTest implements InitializingBean {

    @Autowired
    private ProtocolCollector protocolCollector;
    
    private CollectionJob collectionJob;
    private InetAddress localhost;
    private SnmpAgentConfig snmpAgentConfig;
    private Set<String> destinations;
    
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
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
    public void testGetCompositeValue() {
        // snmpResult without "|amount-of-elements|"
        // timestamp, inboundDelay, inboundJitter, outboundDelay, outboundJitter, timesyncStatus
        String snmpResult = "1327451762,42,23,11,0,1|1327451763,11,0,11,0,1|1327451764,11,0,11,0,1|1327451765,11,0,11,0,1|1327451766,11,0,11,0,1|1327451767,11,0,11,0,1|1327451768,11,0,11,0,1|1327451769,11,0,11,0,1|1327451770,11,0,11,0,1|1327451771,11,0,11,0,1|1327451772,11,0,11,0,1|1327451773,11,0,11,0,1|1327451774,11,0,11,0,1|1327451775,11,0,11,0,1|1327451776,11,0,11,0,1|1327451777,11,0,11,0,1|1327451778,11,0,11,0,1|1327451779,11,0,11,0,1|1327451780,11,0,11,0,1|1327451781,11,0,11,0,1|1327451782,11,0,11,0,1|1327451783,11,0,11,0,1|1327451784,11,0,11,0,1|1327451785,11,0,11,0,1|1327451786,12,0,11,0,423|";
        
        TcaProtocolCollector tcaProtocolCollector = (TcaProtocolCollector)protocolCollector;
        String result = tcaProtocolCollector.getCompositeValue("inboundDelay", "|1|" + snmpResult);
        Assert.assertEquals("42", result);
        
        result = tcaProtocolCollector.getCompositeValue("inboundJitter", "|1|" + snmpResult);
        Assert.assertEquals("23", result);

        result = tcaProtocolCollector.getCompositeValue("timesyncStatus", "|25|" + snmpResult );
        Assert.assertEquals("423", result);
        
        Assert.assertNull(tcaProtocolCollector.getCompositeValue("foo", "|1|" + snmpResult));
        
        Assert.assertNull(tcaProtocolCollector.getCompositeValue(null, "|1|" + snmpResult));
        
        Assert.assertNull(tcaProtocolCollector.getCompositeValue("inboundDelay", null));
    }
    
    @Test
    public void testCollectWithCompountMertic() {

        final String testMetric = ".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60_inboundDelay";
        final String testMetricValue = "12";
        
        collectionJob.setService("TCA");
        collectionJob.setNodeId(1);
        collectionJob.setNetInterface(localhost.getHostAddress());
        collectionJob.addMetric(testMetric, destinations, "OnmsLocicMetricId");
        collectionJob.setId("testing");
        CollectionJob result = protocolCollector.collect(collectionJob);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getMetricValue(testMetric), testMetricValue);
    }

    @Test
    public void testGetProtocol() {
        Assert.assertEquals("TCA", protocolCollector.getProtcol());
    }

    @Test
    public void testAgent() throws Exception {
        SnmpValue snmpValue = SnmpUtils.get(snmpAgentConfig, SnmpObjId.get(".1.3.6.1.2.1.1.1.0"));
        Assert.assertEquals("Mock Juniper TCA Device", snmpValue.toDisplayString());
    }
}