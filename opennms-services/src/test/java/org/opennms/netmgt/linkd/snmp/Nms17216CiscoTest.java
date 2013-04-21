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

import java.net.InetAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.linkd.snmp.SnmpStore;
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
@JUnitConfigurationEnvironment
public class Nms17216CiscoTest extends Nms17216NetworkBuilder implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt")
    })
    public void testNetwork17216CiscoVlanTable() throws Exception {
    	
        String name = "vlanTable";
        CiscoVlanTable m_vlan = new CiscoVlanTable(InetAddress.getByName(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_vlan};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
                walker.waitFor();
        } catch (final InterruptedException e) {

        }

        for (SnmpStore store: m_vlan) {
        	CiscoVlanTableEntry ent = (CiscoVlanTableEntry) store;
            System.out.println("VLAN-----Start");
        	System.out.println("vlan index: " + ent.getVlanIndex());
            System.out.println("vlan name: " + ent.getVlanName());
            System.out.println("vlan type: " + ent.getVlanType());
            System.out.println("vlan status: " + ent.getVlanStatus()); 
            System.out.println("VLAN-----End");
            
        }

        
        assertEquals(10, m_vlan.size());
        assertEquals(6, m_vlan.getVlansForSnmpCollection().size());
    }        

}
