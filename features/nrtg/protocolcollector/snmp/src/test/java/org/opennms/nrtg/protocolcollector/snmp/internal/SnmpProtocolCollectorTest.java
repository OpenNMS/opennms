/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.nrtg.protocolcollector.snmp.internal;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.nrtg.api.ProtocolCollector;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.DefaultCollectionJob;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * TODO Tak refactor this test to be snmp and not tca
 * @author Markus Neumann
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml", "classpath:SnmpProtocolCollectorTestContext.xml"})
@JUnitSnmpAgent(port = 9161, host = "127.0.0.1", resource = "classpath:SnmpSample.properties")
public class SnmpProtocolCollectorTest implements InitializingBean {

    @Autowired
    private ProtocolCollector protocolCollector;
    
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
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setup() throws Exception {
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        localhost = InetAddress.getByName("127.0.0.1");
        snmpAgentConfig = SnmpPeerFactory.getInstance().getAgentConfig(localhost);
        collectionJob = new DefaultCollectionJob();
        collectionJob.setProtocolConfiguration(snmpAgentConfig.toProtocolConfigString());
        destinations = new HashSet<String>();
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