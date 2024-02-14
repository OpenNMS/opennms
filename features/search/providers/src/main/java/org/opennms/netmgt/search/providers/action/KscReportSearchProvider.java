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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.search.api.SearchContext;
import org.opennms.netmgt.search.api.SearchProvider;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.QueryUtils;
import org.opennms.web.svclayer.api.KscReportService;

public class KscReportSearchProvider implements SearchProvider {

    private final SearchContext CONTEXT = new SearchContext("KSC Report");

    private final KscReportService kscReportService;

    @Override
    public SearchContext getContext() {
        return CONTEXT;
    }

    public KscReportSearchProvider(KscReportService kscReportService) {
        this.kscReportService = Objects.requireNonNull(kscReportService);
    }

    @Override
    public SearchResult query(SearchQuery query) {
        final Collection<Report> reportList = kscReportService.getReportMap().values();
        final List<SearchResultItem> results = reportList.stream()
                .filter(report -> QueryUtils.matches(report.getTitle(), query.getInput()))
                .sorted(Comparator.comparing(Report::getTitle))
                .map(report -> {
                    final SearchResultItem result = new SearchResultItem();
                    result.setLabel(report.getTitle());
                    result.setIdentifier(report.getTitle());
                    result.setUrl("KSC/customView.htm?type=custom&report=" + report.getId());
                    return result;
                })
                .limit(query.getMaxResults())
                .collect(Collectors.toList());
        final SearchResult searchResult = new SearchResult(CONTEXT).withMore(reportList, results).withResults(results);
        return searchResult;
    }
}
