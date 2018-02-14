/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.support.HistoryAwareSearchProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.ncs.internal.NCSCriteriaServiceManager;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;

public class NCSSearchProvider extends AbstractSearchProvider implements HistoryAwareSearchProvider {

    public static class NCSHopCriteria extends VertexHopCriteria {

        private final Set<VertexRef> m_vertices;

        public NCSHopCriteria(String id, Set<VertexRef> vertexRefs, String label) {
        	super(label);
            setId(id);
            m_vertices = vertexRefs;
        }

        @Override
        public Set<VertexRef> getVertices() {
            return m_vertices;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public int hashCode() {
            return m_vertices.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof NCSHopCriteria){
                NCSHopCriteria c = (NCSHopCriteria) obj;
                return c.m_vertices.equals(m_vertices);
            }
            return false;
        }
    }

    private static final String NAMESPACE = "ncs";
    private NCSComponentRepository m_ncsComponentRepository;
	private NCSEdgeProvider m_ncsEdgeProvider;
    private NCSCriteriaServiceManager m_serviceManager;
    NCSServiceContainer m_container;

    @Override
    public Criteria buildCriteriaFromQuery(SearchResult input, GraphContainer container) {
        Criteria c = NCSEdgeProvider.createCriteria(Collections.singletonList(Long.parseLong(input.getId())));
        return new NCSHopCriteria(input.getId(), new HashSet<VertexRef>(getVertexRefsForEdges(m_ncsEdgeProvider, c)), input.getLabel());
    }

    public void setNcsComponentRepository(NCSComponentRepository ncsComponentRepository) {
		m_ncsComponentRepository = ncsComponentRepository;
        m_container = new NCSServiceContainer(m_ncsComponentRepository);
	}

    protected static void selectVerticesForEdge(NCSEdgeProvider provider, Criteria criteria, SelectionManager selectionManager) {
	    selectionManager.setSelectedVertexRefs(getVertexRefsForEdges(provider, criteria));
	    
    }

    protected static void deselectVerticesForEdge(NCSEdgeProvider provider, Criteria criteria, SelectionManager selectionManager) {
        selectionManager.deselectVertexRefs(getVertexRefsForEdges(provider, criteria));
    }

    public static Set<VertexRef> getVertexRefsForEdges(NCSEdgeProvider provider, Criteria criteria) {
        Set<VertexRef> vertexRefs = new TreeSet<VertexRef>(new RefComparator());
        List<Edge> edges = provider.getEdges(criteria);
        for(Edge ncsEdge : edges) {
            vertexRefs.add(ncsEdge.getSource().getVertex());
            vertexRefs.add(ncsEdge.getTarget().getVertex());
        }
        return vertexRefs;
    }

	public void setNcsEdgeProvider(NCSEdgeProvider ncsEdgeProvider) {
        m_ncsEdgeProvider = ncsEdgeProvider;
    }
	
	public void setNcsCriteriaServiceManager(NCSCriteriaServiceManager manager) {
	    m_serviceManager = manager;
	}

    @Override
    public String getSearchProviderNamespace() {
        return NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return "nodes".equals(namespace);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
        List<SearchResult> searchResults = new ArrayList<>();

        List<NCSComponent> components = m_ncsComponentRepository.findByType("Service");
        for (NCSComponent component : components) {
            if(searchQuery.matches(component.getName())) {
                searchResults.add(new SearchResult(NAMESPACE, String.valueOf(component.getId()), component.getName(),
                        searchQuery.getQueryString(), !SearchResult.COLLAPSIBLE, !SearchResult.COLLAPSED));
            }

        }
        return searchResults;
    }

    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Collections.singletonList(Long.parseLong(searchResult.getId())));

        if(m_serviceManager.isCriteriaRegistered(NAMESPACE, operationContext.getGraphContainer().getSessionId())) {
            m_serviceManager.unregisterCriteria(NAMESPACE, operationContext.getGraphContainer().getSessionId());
        }
        m_serviceManager.registerCriteria(criteria, operationContext.getGraphContainer().getSessionId());

        //Juniper specifically asked for the selection of NCS services
        selectVerticesForEdge(m_ncsEdgeProvider, criteria, operationContext.getGraphContainer().getSelectionManager());

    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        if(m_serviceManager.isCriteriaRegistered(NAMESPACE, operationContext.getGraphContainer().getSessionId())) {
            m_serviceManager.unregisterCriteria(NAMESPACE, operationContext.getGraphContainer().getSessionId());
        }
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return supportsPrefix("ncs=", searchPrefix);
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Collections.singletonList(Long.parseLong(searchResult.getId())));
        return getVertexRefsForEdges(m_ncsEdgeProvider, criteria);
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Collections.singletonList(Long.parseLong(searchResult.getId())));
        container.addCriteria(new NCSHopCriteria(searchResult.getId(), new HashSet<VertexRef>(getVertexRefsForEdges(m_ncsEdgeProvider, criteria)), searchResult.getLabel()));
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Collections.singletonList(Long.parseLong(searchResult.getId())));
        container.removeCriteria(new NCSHopCriteria(searchResult.getId(), new HashSet<VertexRef>(getVertexRefsForEdges(m_ncsEdgeProvider, criteria)), searchResult.getLabel()));

        if(m_serviceManager.isCriteriaRegistered(NAMESPACE, container.getSessionId())) {
            m_serviceManager.unregisterCriteria(NAMESPACE, container.getSessionId());
        }
    }

    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Collections.singletonList(Long.parseLong(searchResult.getId())));

        if(m_serviceManager.isCriteriaRegistered(NAMESPACE, graphContainer.getSessionId())) {
            m_serviceManager.unregisterCriteria(NAMESPACE, graphContainer.getSessionId());
        }
        m_serviceManager.registerCriteria(criteria, graphContainer.getSessionId());
    }
}
