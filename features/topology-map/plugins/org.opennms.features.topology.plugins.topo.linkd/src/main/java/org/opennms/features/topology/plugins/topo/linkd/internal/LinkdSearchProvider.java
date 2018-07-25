package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class LinkdSearchProvider implements SearchProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LinkdSearchProvider.class);

    private final GraphProvider m_delegate;

    public LinkdSearchProvider(GraphProvider delegate) {
        m_delegate = delegate;
    }

    //Search Provider methods
    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->query: called with search query: '{}'", searchQuery);

        List<Vertex> vertices =  m_delegate.getVertices();
        List<SearchResult> searchResults = Lists.newArrayList();

        for(Vertex vertex : vertices){
            if(searchQuery.matches(vertex.getLabel())) {
                searchResults.add(new SearchResult(vertex, false, false));
            }
        }

        LOG.debug("SearchProvider->query: found {} search results.", searchResults.size());
        return searchResults;
    }
    

    @Override
    public String getSearchProviderNamespace() {
        return m_delegate.getNamespace();
    }
    
    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {

    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {

    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return AbstractSearchProvider.supportsPrefix("nodes=", searchPrefix);
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->getVertexRefsBy: called with search result: '{}'", searchResult);
        org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult.getId(), container);

        Set<VertexRef> vertices = ((VertexHopCriteria)criterion).getVertices();
        LOG.debug("SearchProvider->getVertexRefsBy: found '{}' vertices.", vertices.size());

        return vertices;
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);
        VertexHopCriteria criterion = LinkdHopCriteria.createCriteria(searchResult.getId(), searchResult.getLabel());
        container.addCriteria(criterion);
        LOG.debug("SearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        logCriteriaInContainer(container);
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        Criteria criterion = findCriterion(searchResult.getId(), container);

        if (criterion != null) {
            LOG.debug("SearchProvider->removeVertexHopCriteria: found criterion: {} for searchResult {}.", criterion, searchResult);
            container.removeCriteria(criterion);
        } else {
            LOG.debug("SearchProvider->removeVertexHopCriteria: did not find criterion for searchResult {}.", searchResult);
        }

        logCriteriaInContainer(container);
    }
    
    private org.opennms.features.topology.api.topo.Criteria findCriterion(String resultId, GraphContainer container) {

        org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
        for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
            if (criterion instanceof LinkdHopCriteria ) {
                String id = ((LinkdHopCriteria) criterion).getId();
                if (id.equals(resultId)) {
                    return criterion;
                }
            }
        }
        return null;
    }

    private void logCriteriaInContainer(GraphContainer container) {
        Criteria[] criteria = container.getCriteria();
        LOG.debug("SearchProvider->addVertexHopCriteria: there are now {} criteria in the GraphContainer.", criteria.length);
        for (Criteria crit : criteria) {
            LOG.debug("SearchProvider->addVertexHopCriteria: criterion: '{}' is in the GraphContainer.", crit);
        }
    }

    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->onCenterSearchResult: called with search result: '{}'", searchResult);
    }

    @Override
    public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->onToggleCollapse: called with search result: '{}'", searchResult);
    }

    @Override
    public boolean contributesTo(String namespace) {
        return m_delegate.contributesTo(namespace);
    }


}
