package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.bsm.persistence.api.BusinessService;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class BusinessServiceSearchProvider extends AbstractSearchProvider implements SearchProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessServiceSearchProvider.class);

    private BusinessServiceDao m_businessServiceDao;

    @Override
    public String getSearchProviderNamespace() {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE.equalsIgnoreCase(namespace);
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return supportsPrefix("bsm=", searchPrefix);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer container) {
        LOG.info("BusinessServiceSearchProvider->query: called with search query: '{}'", searchQuery);
        List<SearchResult> results = Lists.newArrayList();

        String queryString = searchQuery.getQueryString();        
        CriteriaBuilder bldr = new CriteriaBuilder(BusinessService.class);
        if (queryString != null && queryString.length() > 0) {
            bldr.ilike("name", String.format("%%%s%%", queryString));
        }
        bldr.orderBy("name", true);
        bldr.limit(10);
        Criteria dbQueryCriteria = bldr.toCriteria();

        for (BusinessService bs: m_businessServiceDao.findMatching(dbQueryCriteria)) {
            SearchResult searchResult = new SearchResult(getSearchProviderNamespace(), String.valueOf(bs.getId()), bs.getName(), queryString);
            searchResult.setCollapsed(false);
            searchResult.setCollapsible(true);
            results.add(searchResult);
        }

        LOG.info("BusinessServiceSearchProvider->query: found {} results: {}", results.size(), results);
        return results;
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        // TODO: When is this called?
        return Collections.emptySet();
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("BusinessServiceSearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);

        BusinessServiceCriteria criterion = new BusinessServiceCriteria(searchResult.getId(), searchResult.getLabel());
        container.addCriteria(criterion);

        LOG.debug("BusinessServiceSearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        LOG.debug("BusinessServiceSearchProvider->addVertexHop: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("BusinessServiceSearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        BusinessServiceCriteria criterion = new BusinessServiceCriteria(searchResult.getId(), searchResult.getLabel());
        container.removeCriteria(criterion);

        LOG.debug("BusinessServiceSearchProvider->removeVertexHopCriteria: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    public void setBusinessServiceDao(BusinessServiceDao businessServiceDao) {
        m_businessServiceDao = businessServiceDao;
    }
}
