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
package org.opennms.features.topology.plugins.topo.bsm;

import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.simple.SimpleSearchProvider;
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
