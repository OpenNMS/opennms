/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

/**
 * 
 * @author <a href=mailto:thedesloge@opennms.org>Donald Desloge</a>
 * @author <a href=mailto:seth@opennms.org>Seth Leger</a>
 *
 */
public class LinkdHopCriteria extends VertexHopCriteria {

	public static final String NAMESPACE = "nodes";
	private final String m_nodeId;
	
	private NodeDao m_nodeDao;
	

	public static class LinkdVertex extends AbstractVertex {

        public LinkdVertex(String namespace, String id, String label) {
			super(namespace, id, label);
		}

		@Override
		public boolean isGroup() {
			return false;
		}

    }

    public LinkdHopCriteria(String nodeId, NodeDao dao) {
    	super(nodeId);
        m_nodeId = nodeId;
        m_nodeDao = dao;
    }

    public LinkdHopCriteria(String nodeId, String nodeLabel, NodeDao dao) {
        super(nodeLabel);
        setId(nodeId);
        m_nodeId = nodeId;
        m_nodeDao = dao;
    }
    
	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao dao) {
		this.m_nodeDao = dao;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_nodeId == null) ? 0 : m_nodeId.hashCode());
        result = prime * result
                + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof LinkdHopCriteria) {
            LinkdHopCriteria ref = (LinkdHopCriteria)obj;
			return ref.m_nodeId.equals(m_nodeId) && ref.getNamespace().equals(getNamespace());
        }
        
        return false;
    }

	@Override
	public Set<VertexRef> getVertices() {
		
		Integer id = Integer.valueOf(m_nodeId);
        OnmsNode node = m_nodeDao.get(id);
		
		Set<VertexRef> vertices = new TreeSet<VertexRef>(new RefComparator());

        if(node != null) {
            String label = node.getLabel();
            vertices.add(new DefaultVertexRef("nodes", m_nodeId, label));
        }
		
		return vertices;
	}
	
}
