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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.OnmsNode;

public class Nms003Test extends Nms003NetworkBuilder {


    /*
     * switch1 0016c8bd4d80 --bft 0016C894AA81/2/3/4-->64
     * switch2 0016c894aa80 --bft 0016C8BD4D89/A/B/C-->64  F4EA67EBDC13/4-->72
     * switch3 f4ea67ebdc00 --bft 0016C894AA93/4-->56
     * 
     * 5001(Port-channel1):64:-------switch2-------:72:5002(Port-channel2)
     *       |                                                 |
     * 5001(Port-channel1):64:---switch1||switch3--:56:5001(Port-channel1)
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms003/switch1-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms003/switch2-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms003/switch3-walk.txt")
    })
    public void testNetwork003Links() throws Exception {
        
        m_nodeDao.save(getSwitch1());
        m_nodeDao.save(getSwitch2());
        m_nodeDao.save(getSwitch3());

        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseLldpDiscovery(false);
        example1.setUseBridgeDiscovery(true);
        example1.setUseOspfDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setSaveRouteTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setUseIsisDiscovery(false);

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(2,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> datalinkinterfaces = m_dataLinkInterfaceDao.findAll();

        int start=getStartPoint(datalinkinterfaces);

        for (final DataLinkInterface datalinkinterface: datalinkinterfaces) {
            printLink(datalinkinterface);
            /*
            Integer linkid = datalinkinterface.getId();
            if ( linkid == start) {
                // switch1 gi0/9 -> switch2 gi0/1 --lldp --cdp
                checkLink(switch2, switch1, 10101, 10109, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+11) {
                checkLink(switch2, switch1, 10101, 10109, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+1 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp --cdp
                checkLink(switch2, switch1, 10102, 10110, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+12 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp --cdp
                checkLink(switch2, switch1, 10102, 10110, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+2) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp --cdp
                checkLink(switch2, switch1, 10103, 10111, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+13) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp --cdp
                checkLink(switch2, switch1, 10103, 10111, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+3) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp --cdp
                checkLink(switch2, switch1, 10104, 10112, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+14) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp --cdp
                checkLink(switch2, switch1, 10104, 10112, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+4) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp --cdp
                checkLink(switch3, switch2, 10019, 10119, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+15) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp --cdp
                checkLink(switch3, switch2, 10019, 10119, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+5) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp --cdp
                checkLink(switch3, switch2, 10020, 10120, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+16) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp --cdp
                checkLink(switch3, switch2, 10020, 10120, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else {
                // error
                checkLink(switch1,switch1,-1,-1,datalinkinterface);
            } */     
        }
    }
}
