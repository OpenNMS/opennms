/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.importer.operations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.dao.api.NodeDao;
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
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
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
    @Override
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
    @Override
    public String toString() {
    	return "DELETE: Node "+m_nodeId;
    }

	/**
	 * <p>gatherAdditionalData</p>
	 */
    @Override
	public void gatherAdditionalData() {
		// no additional data to gather
	}
}
