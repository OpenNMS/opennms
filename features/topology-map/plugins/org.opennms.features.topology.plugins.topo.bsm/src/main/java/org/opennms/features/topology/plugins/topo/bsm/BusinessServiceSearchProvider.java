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

import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SimpleSearchProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;

import com.google.common.collect.Lists;

public class BusinessServiceSearchProvider extends SimpleSearchProvider {

    private BusinessServiceManager businessServiceManager;

    @Override
    public String getSearchProviderNamespace() {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    @Override
    public List<? extends VertexRef> queryVertices(SearchQuery searchQuery, GraphContainer container) {
        List<BusinessServiceVertex> results = Lists.newArrayList();

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
            if (container.getTopologyServiceClient().getVertex(businessServiceVertex) != null) {
                results.add(businessServiceVertex);
            }
        }

        return results;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
    }
}
