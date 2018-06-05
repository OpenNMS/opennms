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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.IgnoreHopCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.GroupRef;
import org.opennms.features.topology.api.topo.SearchCriteria;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.CategoryProvider;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

/**
 * 
 * @author <a href=mailto:thedesloge@opennms.org>Donald Desloge</a>
 * @author <a href=mailto:seth@opennms.org>Seth Leger</a>
 *
 */
public class CategoryHopCriteria extends VertexHopCriteria implements SearchCriteria {

	public final static String NAMESPACE = "category";

	private final String m_categoryName;
	private CategoryProvider categoryProvider;
	private boolean m_collapsed = false;
	private CategoryVertex m_collapsedVertex;

	private GraphContainer graphContainer;

	public static class CategoryVertex extends AbstractVertex implements GroupRef {
		private Set<VertexRef> m_children = new HashSet<>();

        public CategoryVertex(String namespace, String id, String label) {
			super(namespace, id, label);
			setIconKey(Constants.GROUP_ICON_KEY);
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

    public CategoryHopCriteria(SearchResult searchResult, CategoryProvider categoryProvider, GraphContainer graphContainer) {
		super(searchResult.getLabel());
		m_collapsed = searchResult.isCollapsed();
		m_categoryName = searchResult.getLabel();
		m_collapsedVertex = new CategoryVertex(NAMESPACE, NAMESPACE + ":" + m_categoryName, m_categoryName);
		this.categoryProvider = Objects.requireNonNull(categoryProvider);
		this.graphContainer = graphContainer;
        m_collapsedVertex.setChildren(getVertices());
		setId(this.categoryProvider.findCategoryByName(m_categoryName).getId().toString());
    }

	@Override
	public String getSearchString() {
		return this.m_categoryName;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
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
		OnmsCategory category = categoryProvider.findCategoryByName(m_categoryName);
		if (category == null) {
			return Collections.emptySet();
		} else {
			List<OnmsNode> nodes = categoryProvider.findNodesForCategory(category);
			List<Integer> nodeIds = nodes.stream().map(n -> n.getId()).collect(Collectors.toList());

			GraphProvider graphProvider = graphContainer.getTopologyServiceClient().getGraphProviderBy(graphContainer.getTopologyServiceClient().getNamespace());
			return graphProvider.getVertices(new IgnoreHopCriteria()).stream()
					.filter(v -> v.getNodeID() != null)
					.filter(v -> nodeIds.contains(v.getNodeID()))
					.collect(Collectors.toSet());
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
