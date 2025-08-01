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
package org.opennms.netmgt.search.providers.action;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.QueryUtils;

public class ServiceSearchProvider implements SearchProvider {

    private final ServiceTypeDao serviceTypeDao;

    public ServiceSearchProvider(final ServiceTypeDao serviceTypeDao) {
        this.serviceTypeDao = Objects.requireNonNull(serviceTypeDao);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Action;
    }

    @Override
    public SearchResult query(final SearchQuery query) {
        final String input = query.getInput();
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsServiceType.class)
                .ilike("name", QueryUtils.ilike(input))
                .orderBy("name")
                .distinct();
        final int totalCount = serviceTypeDao.countMatching(builder.toCriteria());;
        final List<OnmsServiceType> matchingResult = serviceTypeDao.findMatching(builder.limit(query.getMaxResults()).toCriteria());
        final List<SearchResultItem> searchResultItems = matchingResult.stream().map(service -> {
            final SearchResultItem searchResultItem = new SearchResultItem();
            searchResultItem.setIdentifier(service.getId().toString());
            searchResultItem.setLabel("Show nodes with service '" + service.getName() + "'");
            searchResultItem.setUrl("element/nodeList.htm?service=" + service.getId());
            return searchResultItem;
        }).collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Action).withMore(totalCount > searchResultItems.size()).withResults(searchResultItems);
        return searchResult;
    }
}
