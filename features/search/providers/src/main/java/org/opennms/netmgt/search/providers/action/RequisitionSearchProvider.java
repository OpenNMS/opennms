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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.search.api.Contexts;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.netmgt.search.api.UrlUtils;
import org.opennms.web.svclayer.api.RequisitionAccessService;

import com.google.common.collect.Lists;

public class RequisitionSearchProvider implements SearchProvider {

    private final RequisitionAccessService requisitionAccessService;

    public RequisitionSearchProvider(final RequisitionAccessService requisitionAccessService) {
        this.requisitionAccessService = Objects.requireNonNull(requisitionAccessService);
    }

    @Override
    public SearchContext getContext() {
        return Contexts.Action;
    }

    @Override
    public SearchResult query(SearchQuery query) {
        final List<Requisition> requisitions = Lists.newArrayList(requisitionAccessService.getRequisitions())
                .stream()
                .filter(r -> QueryUtils.matches(r.getForeignSource(), query.getInput()))
                .sorted(Comparator.comparing(Requisition::getForeignSource))
                .limit(query.getMaxResults())
                .collect(Collectors.toList());
        final List<SearchResultItem> resultItems = requisitions.stream().map(r -> {
                final SearchResultItem searchResultItem = new SearchResultItem();
                searchResultItem.setIdentifier(r.getForeignSource());
                searchResultItem.setUrl(String.format("admin/ng-requisitions/index.jsp#/requisitions/%s", UrlUtils.encode(r.getForeignSource())));
                searchResultItem.setLabel(String.format("Edit Requisition '%s'", r.getForeignSource()));
                searchResultItem.setIcon("fa fa-pencil");
                return searchResultItem;
            })
            .collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(Contexts.Action)
                .withMore(requisitions, resultItems)
                .withResults(resultItems);
        return searchResult;
    }
}
