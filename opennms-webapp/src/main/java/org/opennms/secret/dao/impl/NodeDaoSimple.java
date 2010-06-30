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

package org.opennms.secret.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.opennms.secret.dao.NodeDao;
import org.opennms.secret.model.Node;
/**
 * <p>NodeDaoSimple class.</p>
 *
 * @author david
 * @version $Id: $
 * @since 1.6.12
 */
public class NodeDaoSimple implements NodeDao {
    
    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeDao#initialize(java.lang.Object)
     */
    /** {@inheritDoc} */
    public void initialize(Object obj) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeDao#getNode(java.lang.Long)
     */
    /** {@inheritDoc} */
    public Node getNode(Long id) {
        return createDummyNode(id);
    }

    private Node createDummyNode(Long id) {
        Node node = new Node();
        node.setNodeId(id);
        node.setNodeLabel("node"+"-"+id.toString());
        node.setDpName("localhost");
        node.setLastCapsdPoll(new Date());
        node.setNodeCreateTime(new Date());
        node.setNodeLabelSource("U");
        node.setNodeNetBiosName(node.getNodeLabel());
        node.setNodeParentId(id);
        node.setNodeSysContact("Ted");
        node.setNodeSysDescription("DevJam"+"-"+node.getNodeLabel());
        node.setNodeSysLocation("NC");
        node.setNodeSysOid(".1.3.6.1.4.1.9");
        node.setNodeType("A");
        node.setOperatingSystem("BEOS");

        return node;
    }

    /* (non-Javadoc)
     * @see org.opennms.secret.dao.NodeDao#createNode(org.opennms.secret.model.Node)
     */
    /** {@inheritDoc} */
    public void createNode(Node node) {
        
    }

    /** {@inheritDoc} */
    public Collection getInterfaceCollection(Node node) {
        return Collections.EMPTY_SET;
    }

    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Node> findAll() {
        List<Node> list = new ArrayList<Node>(10);
        for(int i = 0; i < 10; i++) {
            list.add(createDummyNode(new Long(i)));
        }
        return list;
    }

}
