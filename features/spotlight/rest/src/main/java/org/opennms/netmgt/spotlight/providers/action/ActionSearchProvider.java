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

package org.opennms.netmgt.spotlight.providers.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.spotlight.api.Contexts;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchQuery;
import org.opennms.netmgt.spotlight.api.SearchResult;
import org.opennms.netmgt.spotlight.providers.QueryUtils;

import com.google.common.collect.Lists;

public class ActionSearchProvider implements SearchProvider {

    private static class Action {
        private final String label;
        private final String url;
        private final List<String> privilegedRoles = Lists.newArrayList();

        private Action(String label, String url, String... roles) {
            this.label = Objects.requireNonNull(label);
            this.url = Objects.requireNonNull(url);
            if (roles != null) {
                privilegedRoles.addAll(Lists.newArrayList(roles));
            }
        }
    }

    private final List<Action> actions = new ArrayList<>();

    public ActionSearchProvider() {
        actions.add(new Action("Configure OpenNMS", "admin/index.jsp", "ROLE_ADMIN"));
        actions.add(new Action("Configure Geocoder Service", "admin/geoservice/index.jsp#!/geocoding/config", "ROLE_ADMIN"));
        actions.add(new Action("Show System Information", "admin/sysconfig.jsp", "ROLE_ADMIN"));
        actions.add(new Action("Manage Flow Classification", "admin/classification/index.jsp", "ROLE_ADMIN"));
        actions.add(new Action("Database Reports", "report/database/index.htm"));
    }

    @Override
    public List<SearchResult> query(SearchQuery query) {
        Objects.requireNonNull(query.getPrincipal());

        final String input = query.getInput();
        final List<SearchResult> searchResults = actions.stream()
                .filter(action -> action.privilegedRoles.isEmpty() || action.privilegedRoles.stream().anyMatch(query::isUserInRole))
                .filter(action -> QueryUtils.matches(action.label, input))
                .map(action -> {
                    final SearchResult searchResult = new SearchResult();
                    searchResult.setContext(Contexts.Action);
                    searchResult.setLabel(action.label);
                    searchResult.setUrl(action.url);
                    searchResult.setIdentifier(action.url);
                    return searchResult;
                })
                .limit(query.getMaxResults())
                .collect(Collectors.toList());
        return searchResults;
    }
}
