/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly170_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly170_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly170_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly171_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly171_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly171_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly172_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly172_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly172_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly173_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly173_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly173_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly174_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly174_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly174_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly175_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly175_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly175_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly176_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly176_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly176_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly177_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly177_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly177_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly189_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly189_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FireFly189_SNMP_RESOURCE;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms007NetworkBuilder;

public class Nms007Test extends LinkdTestBuilder {

	Nms007NetworkBuilder builder = new Nms007NetworkBuilder();
    /*
     * FireFly170 -- ospfid 192.168.168.170 -----> 10.0.0.171/24:192.168.168.171:FireFly171
     *                                      -----> 20.0.0.175/24:192.168.168.175:FireFly175
     *                                      -----> 20.0.0.189/24:192.168.168.189:FireFly189
     *                                      
     * FireFly171 -- ospfid 192.168.168.171 -----> 10.0.0.170/24:192.168.168.170:FireFly170
     *                                      -----> 10.0.1.172/24:192.168.168.172:FireFly172
     *                                      -----> 10.0.1.173/24:192.168.168.173:FireFly173
     *                                      
     * FireFly172 -- ospfid 192.168.168.172 -----> 10.0.1.171/24:192.168.168.171:FireFly171
     *                                      -----> 10.0.1.173/24:192.168.168.173:FireFly173
     *                                      
     * FireFly173 -- ospfid 192.168.168.173 -----> 10.0.1.171/24:192.168.168.171:FireFly171
     *                                      -----> 10.0.1.172/24:192.168.168.172:FireFly172
     *                                      -----> 10.0.2.174/24:192.168.168.174:FireFly174
     *                                      
     * FireFly174 -- ospfid 192.168.168.174 -----> 10.0.2.173/24:192.168.168.173:FireFly173
     *                                      
     * FireFly175 -- ospfid 192.168.168.175 -----> 10.0.3.176/24:192.168.168.176:FireFly176
     *                                      -----> 20.0.0.170/24:192.168.168.175:FireFly170
     *                                      -----> 20.0.0.189/24:192.168.168.189:FireFly189
     *                                      
     * FireFly176 -- ospfid 192.168.168.176 -----> 10.0.3.175/24:192.168.168.175:FireFly175
     *                                      -----> 10.0.4.177/24:192.168.168.177:FireFly177
     *                                      
     * FireFly177 -- ospfid 192.168.168.177 -----> 10.0.4.176/24:192.168.168.176:FireFly176
     *                                      
     * FireFly189 -- ospfid 192.168.168.189 -----> 20.0.0.170/24:192.168.168.170:FireFly176
     *                                      -----> 20.0.0.175/24:192.168.168.175:FireFly175
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = FireFly170_IP, port = 161, resource = FireFly170_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = FireFly171_IP, port = 161, resource = FireFly171_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = FireFly172_IP, port = 161, resource = FireFly172_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = FireFly173_IP, port = 161, resource = FireFly173_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = FireFly174_IP, port = 161, resource = FireFly174_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = FireFly175_IP, port = 161, resource = FireFly175_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = FireFly176_IP, port = 161, resource = FireFly176_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = FireFly177_IP, port = 161, resource = FireFly177_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = FireFly189_IP, port = 161, resource = FireFly189_SNMP_RESOURCE)
    })
    public void testOspfLinks() throws Exception {
        m_nodeDao.save(builder.getFireFly170());
        m_nodeDao.save(builder.getFireFly171());
        m_nodeDao.save(builder.getFireFly172());
        m_nodeDao.save(builder.getFireFly173());
        m_nodeDao.save(builder.getFireFly174());
        m_nodeDao.save(builder.getFireFly175());
        m_nodeDao.save(builder.getFireFly176());
        m_nodeDao.save(builder.getFireFly177());
        m_nodeDao.save(builder.getFireFly189());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(false);
        example1.setUseLldpDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseOspfDiscovery(true);
        example1.setUseIsisDiscovery(false);

        example1.setSaveRouteTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        m_linkdConfig.update();

        
        final OnmsNode fireFly170 = m_nodeDao.findByForeignId("linkd", FireFly170_NAME);
        final OnmsNode fireFly171 = m_nodeDao.findByForeignId("linkd", FireFly171_NAME);
        final OnmsNode fireFly172 = m_nodeDao.findByForeignId("linkd", FireFly172_NAME);
        final OnmsNode fireFly173 = m_nodeDao.findByForeignId("linkd", FireFly173_NAME);
        final OnmsNode fireFly174 = m_nodeDao.findByForeignId("linkd", FireFly174_NAME);
        final OnmsNode fireFly175 = m_nodeDao.findByForeignId("linkd", FireFly175_NAME);
        final OnmsNode fireFly176 = m_nodeDao.findByForeignId("linkd", FireFly176_NAME);
        final OnmsNode fireFly177 = m_nodeDao.findByForeignId("linkd", FireFly177_NAME);
        final OnmsNode fireFly189 = m_nodeDao.findByForeignId("linkd", FireFly189_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(fireFly170.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(fireFly171.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(fireFly172.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(fireFly173.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(fireFly174.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(fireFly175.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(fireFly176.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(fireFly177.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(fireFly189.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly170.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(fireFly171.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(fireFly172.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(fireFly173.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(fireFly174.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(fireFly175.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(fireFly176.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(fireFly177.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(fireFly189.getId()));
               
        assertEquals(0,m_dataLinkInterfaceDao.countAll());

        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

                
        assertEquals(10,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();
        final int start = getStartPoint(links);
        for (final DataLinkInterface datalinkinterface: links) {
            int linkid = datalinkinterface.getId().intValue();
            if (linkid == start) 
                checkLink(fireFly171, fireFly170, 514, 507, datalinkinterface);
            else if (linkid == start +1 )
                checkLink(fireFly175, fireFly170, 517, 517, datalinkinterface);
            else if (linkid == start +2 )
                checkLink(fireFly189, fireFly170, 517, 517, datalinkinterface);
            else if (linkid == start +3 )
                checkLink(fireFly172, fireFly171, 517, 517, datalinkinterface);
            else if (linkid == start +4 )
                checkLink(fireFly173, fireFly171, 517, 517, datalinkinterface);
            else if (linkid == start +5 )
                checkLink(fireFly173, fireFly172, 517, 517, datalinkinterface);
            else if (linkid == start +6 )
                checkLink(fireFly174, fireFly173, 507, 507, datalinkinterface);
            else if (linkid == start +7 )
                checkLink(fireFly176, fireFly175, 507, 514, datalinkinterface);
            else if (linkid == start +8 )
                checkLink(fireFly189, fireFly175, 517, 517, datalinkinterface);
            else if (linkid == start +9 )
                checkLink(fireFly177, fireFly176, 517, 517, datalinkinterface);
            else 
                checkLink(fireFly177, fireFly177, -1, -1, datalinkinterface);

        }
    }

}
