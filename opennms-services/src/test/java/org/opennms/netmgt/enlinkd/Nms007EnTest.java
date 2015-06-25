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

package org.opennms.netmgt.enlinkd;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.nb.Nms007NetworkBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.*;

public class Nms007EnTest extends EnLinkdTestBuilder {

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
    @JUnitSnmpAgents(value = {
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

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(true);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

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

        assertEquals(0, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly170.getId()));
        assertEquals(3, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly171.getId()));
        assertEquals(6, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly172.getId()));
        assertEquals(8, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly173.getId()));
        assertEquals(11, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly174.getId()));
        assertEquals(12, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly175.getId()));
        assertEquals(15, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly176.getId()));
        assertEquals(17, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly177.getId()));
        assertEquals(18, m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(fireFly189.getId()));
        assertEquals(34, m_ospfLinkDao.countAll());

        final List<OspfLink> links = m_ospfLinkDao.findAll();

        checkLinks(links,
                ospfLinkMatcher(fireFly171, fireFly170, 514, 507),
                ospfLinkMatcher(fireFly175, fireFly170, 517, 517),
                ospfLinkMatcher(fireFly189, fireFly170, 517, 517),
                ospfLinkMatcher(fireFly172, fireFly171, 517, 517),
                ospfLinkMatcher(fireFly173, fireFly171, 517, 517),
                ospfLinkMatcher(fireFly173, fireFly172, 517, 517),
                ospfLinkMatcher(fireFly174, fireFly173, 507, 507),
                ospfLinkMatcher(fireFly176, fireFly175, 507, 514),
                ospfLinkMatcher(fireFly189, fireFly175, 517, 517),
                ospfLinkMatcher(fireFly177, fireFly176, 517, 517)
        );
    }
}
