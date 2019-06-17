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
import org.opennms.netmgt.spotlight.api.SearchResult;

public class ActionSearchProvider implements SearchProvider {

    private static class Action {
        private final String label;
        private final String url;

        private Action(String label, String url) {
            this.label = Objects.requireNonNull(label);
            this.url = Objects.requireNonNull(url);
        }
    }

    private final List<Action> actions = new ArrayList<>();

    public ActionSearchProvider() {
        actions.add(new Action("Configure OpenNMS", "admin/index.jsp")); // TODO MVR this is role dependant
        actions.add(new Action("Configure Geocoder Service", "admin/geoservice/index.jsp#!/geocoding/config")); // TODO MVR this is role dependant
        actions.add(new Action("Show System Information", "admin/sysconfig.jsp")); // TODO MVR this is role dependant
        actions.add(new Action("Manage Flow Classification", "admin/classification/index.jsp")); // TODO MVR this is role dependant
        actions.add(new Action("Database Reports", "report/database/index.htm"));
    }

    @Override
    public List<SearchResult> query(String input) {
        final List<SearchResult> searchResults = actions.stream()
                .filter(action -> action.label.toLowerCase().contains(input.toLowerCase()))
                .map(action -> {
                    final SearchResult searchResult = new SearchResult();
                    searchResult.setContext(Contexts.Action);
                    searchResult.setLabel(action.label);
                    searchResult.setUrl(action.url);
                    searchResult.setIdentifer(action.url);
                    return searchResult;
                })
                .collect(Collectors.toList());
        return searchResults;
    }
}
