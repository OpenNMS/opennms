/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class BusinessServiceSearchProvider extends AbstractSearchProvider implements SearchProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessServiceSearchProvider.class);

    private BusinessServiceManager businessServiceManager;

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

        for (BusinessService bs : businessServiceManager.findMatching(dbQueryCriteria)) {
            final BusinessServiceVertex businessServiceVertex = new BusinessServiceVertex(bs, 0);
            // Only consider results which are available in the Topology Provider, see BSM-191
            if (container.getBaseTopology().getVertex(businessServiceVertex) != null) {
                SearchResult searchResult = new SearchResult(businessServiceVertex);
                searchResult.setCollapsed(false);
                searchResult.setCollapsible(true);
                results.add(searchResult);
            }
        }

        LOG.info("BusinessServiceSearchProvider->query: found {} results: {}", results.size(), results);
        return results;
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        VertexRef vertexToFocus = new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel());
        return Sets.newHashSet(vertexToFocus);
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("BusinessServiceSearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);

        DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel()));
        container.addCriteria(criterion);

        LOG.debug("BusinessServiceSearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        LOG.debug("BusinessServiceSearchProvider->addVertexHop: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("BusinessServiceSearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        DefaultVertexHopCriteria criterion = new DefaultVertexHopCriteria(new DefaultVertexRef(searchResult.getNamespace(), searchResult.getId(), searchResult.getLabel()));
        container.removeCriteria(criterion);

        LOG.debug("BusinessServiceSearchProvider->removeVertexHopCriteria: current criteria {}.", Arrays.toString(container.getCriteria()));
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
    }
}
