/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.search.rest.internal;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.search.api.SearchQuery;
import org.opennms.netmgt.search.api.SearchResult;
import org.opennms.netmgt.search.api.SearchService;
import org.opennms.netmgt.search.rest.SearchRestService;

public class SearchRestServiceImpl implements SearchRestService {

    private final SearchService searchService;

    public SearchRestServiceImpl(SearchService searchService) {
        this.searchService = Objects.requireNonNull(searchService);
    }

    @Override
    public Response query(SecurityContext securityContext, String context, String query, int limit) {
        final SearchQuery searchQuery = new SearchQuery(query);
        searchQuery.setPrincipal(securityContext.getUserPrincipal());
        searchQuery.setUserInRoleFunction(securityContext::isUserInRole);
        searchQuery.setContext(context);
        searchQuery.setMaxResults(limit < 0 ? 0 : limit);

        final List<SearchResult> searchResult = searchService.query(searchQuery);
        if (searchResult.isEmpty()) {
            return Response.noContent().build();
        }
        final JSONArray jsonResult = new JSONArray();
        for (SearchResult eachResult : searchResult) {
            jsonResult.put(new JSONObject(eachResult));
        }
        return Response.ok().entity(jsonResult.toString()).build();
    }
}
