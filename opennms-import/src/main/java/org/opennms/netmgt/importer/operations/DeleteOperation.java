//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
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

package org.opennms.netmgt.importer.operations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>DeleteOperation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DeleteOperation extends AbstractImportOperation {
    
    Integer m_nodeId;
    NodeDao m_nodeDao;

    /**
     * <p>Constructor for DeleteOperation.</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public DeleteOperation(Integer nodeId, String foreignSource, String foreignId, NodeDao nodeDao) {
        m_nodeId = nodeId;
        m_nodeDao = nodeDao;
    }

    /**
     * <p>persist</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Event> persist() {

    	//TODO: whatif node comes back as null?  can this happend?
    	OnmsNode node = m_nodeDao.get(m_nodeId);
    	if (node == null) return new ArrayList<Event>(0);

    	m_nodeDao.delete(node);

    	final List<Event> events = new LinkedList<Event>();

    	EntityVisitor eventAccumlator = new DeleteEventVisitor(events);

    	node.visit(eventAccumlator);

    	return events;
    }


    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
    	return "DELETE: Node "+m_nodeId;
    }

	/**
	 * <p>gatherAdditionalData</p>
	 */
	public void gatherAdditionalData() {
		// no additional data to gather
	}
}
