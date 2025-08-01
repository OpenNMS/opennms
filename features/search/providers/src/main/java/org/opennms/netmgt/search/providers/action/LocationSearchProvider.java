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

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.netmgt.search.api.UrlUtils;

public class LocationSearchProvider implements SearchProvider {

    private final MonitoringLocationDao monitoringLocationDao;

    public LocationSearchProvider(final MonitoringLocationDao monitoringLocationDao) {
        this.monitoringLocationDao = Objects.requireNonNull(monitoringLocationDao);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Action;
    }

    @Override
    public SearchResult query(final SearchQuery query) {
        final String input = query.getInput();
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoringLocation.class)
                .ilike("locationName", QueryUtils.ilike(input))
                .orderBy("locationName")
                .distinct();
        final int totalCount = monitoringLocationDao.countMatching(builder.toCriteria());
        final Criteria criteria = builder.limit(query.getMaxResults()).toCriteria();
        final List<OnmsMonitoringLocation> matchingResult = monitoringLocationDao.findMatching(criteria);
        final List<SearchResultItem> searchResultItems = matchingResult.stream().map(monitoringLocation -> {
            final SearchResultItem searchResultItem = new SearchResultItem();
            searchResultItem.setIdentifier(monitoringLocation.getLocationName());
            searchResultItem.setLabel("Show nodes in location '" + monitoringLocation.getLocationName() + "'");
            searchResultItem.setUrl("element/nodeList.htm?monitoringLocation=" + UrlUtils.encode(monitoringLocation.getLocationName()));
            return searchResultItem;
        }).collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Action).withMore(totalCount > searchResultItems.size()).withResults(searchResultItems);
        return searchResult;
    }
}
