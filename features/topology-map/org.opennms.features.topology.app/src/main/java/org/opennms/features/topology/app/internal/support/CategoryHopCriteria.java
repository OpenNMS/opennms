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

package org.opennms.features.topology.app.internal.support;

import java.util.*;

import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

/**
 * 
 * @author <a href=mailto:thedesloge@opennms.org>Donald Desloge</a>
 * @author <a href=mailto:seth@opennms.org>Seth Leger</a>
 *
 */
public class CategoryHopCriteria extends VertexHopCriteria implements CollapsibleCriteria {

	private final String m_categoryName;
	private CategoryDao m_categoryDao;
	private NodeDao m_nodeDao;
	private boolean m_collapsed = false;
	private CategoryVertex m_collapsedVertex;

	public static class CategoryVertex extends AbstractVertex implements GroupRef {
		private Set<VertexRef> m_children = new HashSet<VertexRef>();

        public CategoryVertex(String namespace, String id, String label) {
			super(namespace, id, label);
			setIconKey("group");
		}

		@Override
		public boolean isGroup() {
			return true;
		}

        @Override
        public Set<VertexRef> getChildren() {
            return m_children;
        }

        public void setChildren(Set<VertexRef> children) {
            m_children = children;
        }
    }

	public CategoryHopCriteria(String categoryName) {
		super(categoryName);
		m_categoryName = categoryName;
		m_collapsedVertex = new CategoryVertex("category", "category:" + m_categoryName, m_categoryName);
	}

    public CategoryHopCriteria(String categoryName, NodeDao nodeDao, CategoryDao categoryDao){
        this(categoryName);
        setNodeDao(nodeDao);
        setCategoryDao(categoryDao);
        m_collapsedVertex.setChildren(getVertices());
    }

	public CategoryDao getCategoryDao() {
		return m_categoryDao;
	}

	public void setCategoryDao(CategoryDao categoryDao) {
		this.m_categoryDao = categoryDao;
        if(getId().endsWith("")){
            setId(m_categoryDao.findByName(m_categoryName).getId().toString());
        }
	}

	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.m_nodeDao = nodeDao;
	}

	/**
	 * TODO: This return value doesn't matter since we just delegate
	 * to the m_delegate provider.
	 */
	@Override
	public String getNamespace() {
		return "category";
	}

    @Override
    public int hashCode() {
        return m_categoryName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CategoryHopCriteria){
            CategoryHopCriteria c = (CategoryHopCriteria) obj;
            return c.m_categoryName.equals(m_categoryName);
        }
        return false;
    }

    public String getCategoryName() {
		return m_categoryName;
	}

	@Override
	public Set<VertexRef> getVertices() {
		OnmsCategory category = m_categoryDao.findByName(m_categoryName);
		if (category == null) {
			return Collections.emptySet();
		} else {
			List<OnmsNode> nodes = m_nodeDao.findByCategory(category);
			Set<VertexRef> retval = new TreeSet<VertexRef>(new RefComparator());
			for (OnmsNode node : nodes) {
				retval.add(new DefaultVertexRef("nodes", String.valueOf(node.getId())));
			}
			return retval;
		}
	}

	@Override
	public boolean isCollapsed() {
		return m_collapsed;
	}

	@Override
	public void setCollapsed(boolean collapsed) {
		if (collapsed != isCollapsed()) {
			this.m_collapsed = collapsed;
			setDirty(true);
		}
	}

	@Override
	public Vertex getCollapsedRepresentation() {
		return m_collapsedVertex;
	}
}
