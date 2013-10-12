package org.opennms.features.topology.plugins.ncs;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.plugins.ncs.internal.NCSCriteriaServiceManager;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import java.util.List;
import java.util.Set;

public class NCSViewContribution implements SearchProvider {

    public class NCSHopCriteria extends VertexHopCriteria{

        private final Set<VertexRef> m_vertices;

        public NCSHopCriteria(Set<VertexRef> vertices, String label){
            m_vertices = vertices;
            setLabel(label);
        }

        @Override
        public Set<VertexRef> getVertices() {
            return m_vertices;
        }

        @Override
        public String getNamespace() {
            return "ncs service";
        }
    }

	private NCSComponentRepository m_ncsComponentRepository;
	private NCSEdgeProvider m_ncsEdgeProvider;
    private NCSCriteriaServiceManager m_serviceManager;
    private int m_serviceCount = 0;
    NCSServiceContainer m_container;

    public void setNcsComponentRepository(NCSComponentRepository ncsComponentRepository) {
		m_ncsComponentRepository = ncsComponentRepository;
        m_container = new NCSServiceContainer(m_ncsComponentRepository);
	}

    protected void selectVerticesForEdge(Criteria criteria, SelectionManager selectionManager) {
	    selectionManager.setSelectedVertexRefs(getVertexRefsForEdges(criteria));
	    
    }

    protected void deselectVerticesForEgde(Criteria criteria, SelectionManager selectionManager) {
        selectionManager.deselectVertexRefs(getVertexRefsForEdges(criteria));
    }

    private List<VertexRef> getVertexRefsForEdges(Criteria criteria) {
        List<VertexRef> vertexRefs = Lists.newArrayList();
        List<Edge> edges = m_ncsEdgeProvider.getEdges(criteria);
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
    public List<SearchResult> query(SearchQuery searchQuery) {
        List<SearchResult> searchResults = Lists.newArrayList();

        List<NCSComponent> components = m_ncsComponentRepository.findByType("Service");
        for (NCSComponent component : components) {
            if(searchQuery.matches(component.getName())) {
                searchResults.add(new SearchResult(String.valueOf(component.getId()), "ncs service", component.getName()));
            }

        }
        return searchResults;
    }

    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Lists.newArrayList(Long.parseLong(searchResult.getId())));

        m_serviceManager.registerCriteria(criteria, operationContext.getGraphContainer().getSessionId());
        if(m_serviceManager.isCriteriaRegistered("ncsPath", operationContext.getGraphContainer().getSessionId())) {
            m_serviceManager.unregisterCriteria("ncsPath", operationContext.getGraphContainer().getSessionId());
        }
        selectVerticesForEdge(criteria, operationContext.getGraphContainer().getSelectionManager());
    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Lists.newArrayList(Long.parseLong(searchResult.getId())));

        if(m_serviceManager.isCriteriaRegistered("ncsPath", operationContext.getGraphContainer().getSessionId())) {
            m_serviceManager.unregisterCriteria("ncsPath", operationContext.getGraphContainer().getSessionId());
        }
        deselectVerticesForEgde(criteria, operationContext.getGraphContainer().getSelectionManager());
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return searchPrefix.equals("ncs=");
    }

    @Override
    public List<VertexRef> getVertexRefsBy(SearchResult searchResult) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Lists.newArrayList(Long.parseLong(searchResult.getId())));
        return getVertexRefsForEdges(criteria);
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        Criteria criteria = NCSEdgeProvider.createCriteria(Lists.newArrayList(Long.parseLong(searchResult.getId())));
        container.setCriteria(new NCSHopCriteria(Sets.newHashSet(getVertexRefsForEdges(criteria)), searchResult.getLabel()));
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        Criteria[] criterias = container.getCriteria();
        for (Criteria criteria : criterias) {
            try {
                NCSHopCriteria ncsHopCriteria = (NCSHopCriteria) criteria;
                if(ncsHopCriteria.getLabel().toLowerCase().equals(searchResult.getLabel().toLowerCase())){
                    container.removeCriteria(ncsHopCriteria);
                }
            } catch (ClassCastException e) {}
        }
    }


}
