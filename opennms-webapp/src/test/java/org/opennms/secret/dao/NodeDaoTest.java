//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.secret.dao;

import java.util.Collection;

import junit.framework.TestCase;

import org.opennms.secret.dao.impl.NodeDaoSimple;
import org.opennms.secret.model.Node;
public class NodeDaoTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getDpName()'
     */
    public void testGetDpName() {
        
        NodeDao dao = new NodeDaoSimple();
        Node node = dao.getNode(new Long(1));
        
        Long id = node.getNodeId();
        assertNotNull(id);
        assertEquals(1L, id.longValue());
        String dpName = node.getDpName();
        assertNotNull(dpName);

    }
    
    public void testGetNodeInterfaces() {
        
        NodeDao dao = new NodeDaoSimple();
        Node node = dao.getNode(new Long(10));
        
        Collection interfaces = dao.getInterfaceCollection(node);
        
        assertNotNull(interfaces);
    }
    
    public void testFindAll() {
        NodeDao dao = new NodeDaoSimple();
        Collection nodes = dao.findAll();
        assertNotNull(nodes);
        assertFalse(nodes.isEmpty());
    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setDpName(String)'
     */
    public void testSetDpName() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getLastCapsdPoll()'
     */
    public void testGetLastCapsdPoll() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setLastCapsdPoll(Date)'
     */
    public void testSetLastCapsdPoll() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeCreateTime()'
     */
    public void testGetNodeCreateTime() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeCreateTime(Date)'
     */
    public void testSetNodeCreateTime() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeDomainName()'
     */
    public void testGetNodeDomainName() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeDomainName(String)'
     */
    public void testSetNodeDomainName() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeId()'
     */
    public void testGetNodeId() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeId(Long)'
     */
    public void testSetNodeId() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeLabel()'
     */
    public void testGetNodeLabel() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeLabel(String)'
     */
    public void testSetNodeLabel() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeLabelSource()'
     */
    public void testGetNodeLabelSource() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeLabelSource(String)'
     */
    public void testSetNodeLabelSource() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeNetBiosName()'
     */
    public void testGetNodeNetBiosName() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeNetBiosName(String)'
     */
    public void testSetNodeNetBiosName() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeParentId()'
     */
    public void testGetNodeParentId() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeParentId(Long)'
     */
    public void testSetNodeParentId() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeSysContact()'
     */
    public void testGetNodeSysContact() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeSysContact(String)'
     */
    public void testSetNodeSysContact() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeSysDescription()'
     */
    public void testGetNodeSysDescription() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeSysDescription(String)'
     */
    public void testSetNodeSysDescription() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeSysLocation()'
     */
    public void testGetNodeSysLocation() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeSysLocation(String)'
     */
    public void testSetNodeSysLocation() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeSysName()'
     */
    public void testGetNodeSysName() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeSysName(String)'
     */
    public void testSetNodeSysName() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeSysOid()'
     */
    public void testGetNodeSysOid() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeSysOid(String)'
     */
    public void testSetNodeSysOid() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getNodeType()'
     */
    public void testGetNodeType() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setNodeType(String)'
     */
    public void testSetNodeType() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.getOperatingSystem()'
     */
    public void testGetOperatingSystem() {

    }

    /*
     * Test method for 'org.opennms.secret.model.Node.setOperatingSystem(String)'
     */
    public void testSetOperatingSystem() {

    }

}
