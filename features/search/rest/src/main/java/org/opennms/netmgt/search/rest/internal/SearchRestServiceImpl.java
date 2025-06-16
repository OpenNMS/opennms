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
package org.opennms.netmgt.search.rest.internal;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.features.apilayer.uiextension.UIExtensionRegistry;
import org.opennms.integration.api.v1.ui.UIExtension;
import org.opennms.netmgt.search.api.Match;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchResultItem;
import org.opennms.netmgt.search.api.SearchService;
import org.opennms.netmgt.search.rest.SearchRestService;

public class SearchRestServiceImpl implements SearchRestService {

    private final SearchService searchService;
    private final UIExtensionRegistry extensionRegistry;

    public SearchRestServiceImpl(SearchService searchService, UIExtensionRegistry extensionRegistry) {
        this.searchService = Objects.requireNonNull(searchService);
        this.extensionRegistry = Objects.requireNonNull(extensionRegistry);
    }

    @Override
    public Response query(SecurityContext securityContext, String context, String query, int limit) {
        final SearchQuery searchQuery = new SearchQuery(query);
        searchQuery.setPrincipal(securityContext.getUserPrincipal());
        searchQuery.setUserInRoleFunction(securityContext::isUserInRole);
        searchQuery.setContext(context);
        searchQuery.setMaxResults(limit < 0 ? 0 : limit);

        final List<SearchResult> searchResult = searchService.query(searchQuery);

        final SearchResult pluginSearchResult = getPluginSearchResult(query);

        if (searchResult.isEmpty() && pluginSearchResult.isEmpty()) {
            return Response.noContent().build();
        }

        final JSONArray jsonResult = new JSONArray();
        for (SearchResult eachResult : searchResult) {
            jsonResult.put(new JSONObject(eachResult));
        }

        if (!pluginSearchResult.isEmpty()) {
            jsonResult.put(new JSONObject(pluginSearchResult));
        }

        return Response.ok().entity(jsonResult.toString()).build();
    }

    private SearchResult getPluginSearchResult(String query) {
        SearchResult searchResult = new SearchResult("Plugins");
        List<UIExtension> extensions = this.extensionRegistry.listExtensions();

        for (UIExtension ext : extensions) {
            if (ext.getMenuEntry().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
                SearchResultItem item = new SearchResultItem();
                item.addMatch(new Match("label", "Plugin", ext.getMenuEntry()));

                item.setIdentifier(ext.getMenuEntry());
                item.setLabel(ext.getMenuEntry());
                item.setWeight(100);

                String relativeUrl = String.format("ui/#/plugins/%s/%s/%s",
                    ext.getExtensionId(), ext.getResourceRootPath(), ext.getModuleFileName());
                item.setUrl(relativeUrl);

                searchResult.addItem(item);
            }
        }

        return searchResult;
    }
}
