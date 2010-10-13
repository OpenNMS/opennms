/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 14, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.element;

import java.sql.SQLException;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.dao.db.PopulatedTemporaryDatabaseTestCase;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class NetworkElementFactoryTest extends PopulatedTemporaryDatabaseTestCase {
    @Override
    protected void setUp() throws Exception {
        setSetupIpLike(true);
        
        super.setUp();
        
        Vault.setDataSource(getDataSource());
    }
    
    public void testGetNodeLabel() throws SQLException {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType, nodeLabel) VALUES (1, now(), 'A', 'nodeLabel')");
        
        //String nodeLabel = NetworkElementFactory.getNodeLabel(1);
        
        //assertEquals(nodeLabel, "nodeLabel");
        assertTrue(false);
    }
    
    public void testGetIpPrimaryAddress() throws SQLException {
        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType, nodeLabel) VALUES (1, now(), 'A', 'nodeLabel')");
        jdbcTemplate.update("INSERT INTO ipinterface (nodeid, ipaddr, iplastcapsdpoll, issnmpprimary) VALUES (1, '172.168.1.1', now(), 'P')");
        
        String ipAddr = "172.168.1.0";//NetworkElementFactory.getIpPrimaryAddress(1);
        
        assertEquals(ipAddr, "172.168.1.1");
    }
    
//    public void testGetNodesWithIpLikeOneInterface() throws Exception {
//        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType) VALUES (1, now(), 'A')");
//        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.1', 'M')");
//        
//        assertEquals("node count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM node"));
//        assertEquals("ipInterface count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM ipInterface"));
//        
//        Node[] nodes = NetworkElementFactory.getInstance(null).getNodesWithIpLike("*.*.*.*");
//        assertEquals("node count", 1, nodes.length);
//    }
    
    // bug introduced in revision 2932
    public void testGetNodesWithIpLikeTwoInterfaces() throws Exception {
//        jdbcTemplate.update("INSERT INTO node (nodeId, nodeCreateTime, nodeType) VALUES (1, now(), 'A')");
//        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.1', 'M')");
//        jdbcTemplate.update("INSERT INTO ipInterface (nodeId, ipAddr, isManaged) VALUES (1, '1.1.1.2', 'M')");
//        
//        assertEquals("node count in DB", 1, jdbcTemplate.queryForInt("SELECT count(*) FROM node"));
//        assertEquals("ipInterface count in DB", 2, jdbcTemplate.queryForInt("SELECT count(*) FROM ipInterface"));
//
//        Node[] nodes = NetworkElementFactory.getNodesWithIpLike("*.*.*.*");
//        assertEquals("node count", 1, nodes.length);
    }
}
