/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd.snmp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.linkd.nb.Nms17216NetworkBuilder;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
public class Nms17216CdpTest extends Nms17216NetworkBuilder implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testNetwork17216Switch1CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(5, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt")
    })
    public void testNetwork17216Switch2CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH2_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(6, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt")
    })
    public void testNetwork17216Switch3CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH3_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt")
    })
    public void testNetwork17216Switch4CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH4_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH4_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(1, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/switch5-walk.txt")
    })
    public void testNetwork17216Switch5CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(SWITCH5_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SWITCH5_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(2, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER1_IP, port=161, resource="classpath:linkd/nms17216/router1-walk.txt")
    })
    public void testNetwork17216Router1CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(ROUTER1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ROUTER1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(2, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER2_IP, port=161, resource="classpath:linkd/nms17216/router2-walk.txt")
    })
    public void testNetwork17216Router2CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(ROUTER2_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ROUTER2_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(2, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource="classpath:linkd/nms17216/router3-walk.txt")
    })
    public void testNetwork17216Router3CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(ROUTER3_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ROUTER3_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(3, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER4_IP, port=161, resource="classpath:linkd/nms17216/router4-walk.txt")
    })
    public void testNetwork17216Router4CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddressUtils.addr(ROUTER4_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(ROUTER4_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(1, m_cdpCacheTable.size());
        for (CdpCacheTableEntry cdpCacheTableEntry: m_cdpCacheTable) {
            printCdpRow(cdpCacheTableEntry);
        }
    }        

}
