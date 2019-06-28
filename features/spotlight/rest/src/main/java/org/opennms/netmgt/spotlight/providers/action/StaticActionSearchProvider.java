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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.opennms.netmgt.spotlight.api.Contexts;
import org.opennms.netmgt.spotlight.api.SearchProvider;
import org.opennms.netmgt.spotlight.api.SearchQuery;
import org.opennms.netmgt.spotlight.api.SearchResult;
import org.opennms.netmgt.spotlight.providers.QueryUtils;

import com.google.common.base.Strings;

public class StaticActionSearchProvider implements SearchProvider {

    @Override
    public List<SearchResult> query(SearchQuery query) {
        Objects.requireNonNull(query.getPrincipal());

        // TODO MVR do not reload all the time
        final Path etc = Paths.get(System.getProperty("opennms.home"), "etc", "spotlight-actions.xml");
        final Actions actions = JAXB.unmarshal(etc.toFile(), Actions.class);
        final String input = query.getInput();
        final List<SearchResult> searchResults = actions.getActions().stream()
                .filter(action -> action.getPrivilegedRoles().isEmpty() || action.getPrivilegedRoles().stream().anyMatch(query::isUserInRole))
                .filter(action -> QueryUtils.matches(action.getLabel(), input))
                .map(action -> {
                    final SearchResult searchResult = new SearchResult();
                    searchResult.setContext(Contexts.Action);
                    searchResult.setLabel(action.getLabel());
                    searchResult.setUrl(action.getUrl());
                    searchResult.setIdentifier(action.getUrl());
                    if (!Strings.isNullOrEmpty(action.getIcon())) {
                        searchResult.setIcon(action.getIcon());
                    }
                    return searchResult;
                })
                .limit(query.getMaxResults())
                .collect(Collectors.toList());
        return searchResults;
    }
}
