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
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.linkd.snmp.CdpCacheTable;
import org.opennms.netmgt.linkd.snmp.CdpCacheTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpLocTable;
import org.opennms.netmgt.linkd.snmp.LldpLocTableEntry;
import org.opennms.netmgt.linkd.snmp.LldpLocalGroup;
import org.opennms.netmgt.linkd.snmp.LldpRemTable;
import org.opennms.netmgt.linkd.snmp.LldpRemTableEntry;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/applicationContext-linkd-test.xml"
})
@JUnitConfigurationEnvironment
public class LinkdNms17216CdpSnmpWalkTest extends LinkdNms17216NetworkBuilder implements InitializingBean {
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/"+SWITCH1_NAME+"-walk.txt")
    })
    public void testNetwork17216Switch1CdpCacheTableCollection() throws Exception {

        String name = "cdpCacheTable";
        CdpCacheTable m_cdpCacheTable = new CdpCacheTable(InetAddress.getByName(SWITCH1_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[] {m_cdpCacheTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(SWITCH1_IP));
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
    

    private void checkSwitch1Row(CdpCacheTableEntry cdpCacheTableEntry) {
        /*
        final Integer lldpLocPortNum = lldpLocTableEntry.getLldpLocPortNum();
        String lldpLocPortid = lldpLocTableEntry.getLldpLocPortid();
        Integer lldpLocPortidSubtype = lldpLocTableEntry.getLldpLocPortIdSubtype();
        printLldpLocRow(lldpLocPortNum, lldpLocPortidSubtype, lldpLocPortid);
        assertEquals(5,lldpLocPortidSubtype.intValue());
        
        if (lldpLocPortNum.intValue() >= 1 && lldpLocPortNum.intValue() <= 24) {
            assertEquals("Fa0/"+lldpLocPortNum,lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 25) {
            assertEquals("Gi0/1",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 26) {
            assertEquals("Gi0/2",lldpLocPortid);
        } else if (lldpLocPortNum.intValue() == 104) {
            assertEquals("St1",lldpLocPortid);
        } else {
            assertEquals(true,false);
        }
        */
    }
    

}
