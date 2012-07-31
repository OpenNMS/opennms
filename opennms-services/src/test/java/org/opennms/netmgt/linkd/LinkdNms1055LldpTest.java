/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.linkd.snmp.Dot1dBaseGroup;
import org.opennms.netmgt.linkd.snmp.LldpLocalGroup;
import org.opennms.netmgt.linkd.snmp.LldpRemTable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-linkd-test.xml"
})
@JUnitConfigurationEnvironment
public class LinkdNms1055LldpTest extends LinkdNms1055NetworkBuilder implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt")
    })
    public void testNetwork1055LLDPLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddress.getByName(PENROSE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(PENROSE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals("80711f8fafc0",m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals("penrose-mx480", m_lLldpLocalGroup.getLldpLocSysname());
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=PENROSE_IP, port=161, resource="classpath:linkd/nms1055/"+PENROSE_NAME+"_"+PENROSE_IP+".txt")
    })
    public void testNetwork1055Dot1dBaseCollection() throws Exception {

        String name = "dot1dbase";
        Dot1dBaseGroup m_lLldpLocalGroup = new Dot1dBaseGroup(InetAddress.getByName(PENROSE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(PENROSE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        assertEquals("80711f8fafd0",m_lLldpLocalGroup.getBridgeAddress());
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DELAWARE_IP, port=161, resource="classpath:linkd/nms1055/"+DELAWARE_NAME+"_"+DELAWARE_IP+".txt")
    })
    public void testNetwork1055LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddress.getByName(DELAWARE_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(DELAWARE_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }
        
        assertEquals(4, m_lldpRemTable.getEntries().size());
    }

}
