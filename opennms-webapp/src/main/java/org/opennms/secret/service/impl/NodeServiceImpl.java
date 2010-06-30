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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.secret.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opennms.secret.dao.NodeDao;
import org.opennms.secret.model.Node;
import org.opennms.secret.service.NodeService;

/**
 * <p>NodeServiceImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class NodeServiceImpl implements NodeService {

	private NodeDao m_nodeDao;

	/** {@inheritDoc} */
	public Node getNodeById(Long id) {
		Node node = m_nodeDao.getNode(id);
        m_nodeDao.initialize(node);
		return node;
	}

	/**
	 * <p>setNodeDao</p>
	 *
	 * @param nodeDao a {@link org.opennms.secret.dao.NodeDao} object.
	 */
	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}

    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Node> findAll() {
        return new HashSet<Node>(m_nodeDao.findAll());
    }

    /** {@inheritDoc} */
    public Set<Node> findWithMatchingLabel(String searchKey) {
        Collection nodes = m_nodeDao.findAll();
        Set<Node> matching = new HashSet<Node>();
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            if (node.getNodeLabel().startsWith(searchKey)) {
                matching.add(node);
            }
        }
        return matching;
    }
}
