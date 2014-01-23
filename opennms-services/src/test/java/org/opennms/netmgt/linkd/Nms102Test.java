/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsNode;

public class Nms102Test extends Nms102NetworkBuilder {
	
    @Before
    public void setUpForceDisvoeryOnEthernet() {
    for (Package pkg : Collections.list(m_linkdConfig.enumeratePackage())) {
            pkg.setForceIpRouteDiscoveryOnEthernet(true);
        }
    }
    
    /*
     *  Discover the following topology
     * 
     *                     mikrotik
     *                         |
     *  ----------------------wifi-----------------
     *    |      |        |            |
     *  mac1    mac2    samsung     mobile
     *  
     */
    @Test
    @JUnitSnmpAgents(value={
        @JUnitSnmpAgent(host=MIKROTIK_IP, port=161, resource="classpath:linkd/nms102/"+MIKROTIK_NAME+"-"+MIKROTIK_IP+"-walk.txt"),
        @JUnitSnmpAgent(host=SAMSUNG_IP, port=161, resource="classpath:linkd/nms102/"+SAMSUNG_NAME+"-"+SAMSUNG_IP+"-walk.txt"),
        @JUnitSnmpAgent(host=MAC1_IP, port=161, resource="classpath:linkd/nms102/"+"mac-"+MAC1_IP+"-walk.txt"),
        @JUnitSnmpAgent(host=MAC2_IP, port=161, resource="classpath:linkd/nms102/"+"mac-"+MAC2_IP+"-walk.txt")
    })
    public void testLinks() throws Exception {

    	m_nodeDao.save(getMac1());
        m_nodeDao.save(getMac2());
        m_nodeDao.save(getMikrotik());
   	m_nodeDao.save(getSamsung());
   	m_nodeDao.save(getNodeWithoutSnmp("mobile", "192.168.0.13"));
    	m_nodeDao.flush();
    	
        final OnmsNode mac1 = m_nodeDao.findByForeignId("linkd", MAC1_NAME);
        final OnmsNode mac2 = m_nodeDao.findByForeignId("linkd", MAC2_NAME);
        final OnmsNode samsung = m_nodeDao.findByForeignId("linkd", SAMSUNG_NAME);
        final OnmsNode mikrotik = m_nodeDao.findByForeignId("linkd", MIKROTIK_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mac1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mac2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(samsung.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mikrotik.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(mac1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mac2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(samsung.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(mikrotik.getId()));
 
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));
        
        final List<DataLinkInterface> ifaces = m_dataLinkInterfaceDao.findAll();
        for (final DataLinkInterface link: ifaces) {
            printLink(link);
        }

        assertEquals("we should have found 3 data links", 3, ifaces.size());
    }

}
