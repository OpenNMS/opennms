/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.app.internal.support;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractCollapsibleVertex;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SearchCriteria;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.BackendGraph;
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

	public static class CategoryVertex extends AbstractCollapsibleVertex {
        public CategoryVertex(String categoryName) {
			super(NAMESPACE, NAMESPACE + ":" + categoryName, categoryName);
			setIconKey(Constants.GROUP_ICON_KEY);
		}
    }

    public CategoryHopCriteria(SearchResult searchResult, CategoryProvider categoryProvider, GraphContainer graphContainer) {
		super(searchResult.getLabel());
		m_collapsed = searchResult.isCollapsed();
		m_categoryName = searchResult.getLabel();
		m_collapsedVertex = new CategoryVertex(m_categoryName);
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

	@Override
	public Set<VertexRef> getVertices() {
		final OnmsCategory category = categoryProvider.findCategoryByName(m_categoryName);
		if (category == null) {
			return Collections.emptySet();
		} else {
			final List<OnmsNode> nodes = categoryProvider.findNodesForCategory(category);
			final List<Integer> nodeIds = nodes.stream().map(n -> n.getId()).collect(Collectors.toList());
			final GraphProvider graphProvider = graphContainer.getTopologyServiceClient().getGraphProviderBy(graphContainer.getTopologyServiceClient().getNamespace());
			final BackendGraph currentGraph = graphProvider.getCurrentGraph();
			return currentGraph.getVertices().stream()
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
