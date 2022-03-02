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

package org.opennms.netmgt.graph.api.search;

import java.util.List;

import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.service.GraphService;

public interface SearchProvider {

    /**
     * Defines if the search can provide suggestions to the given namespace
     * @param namespace The namespace to make suggestions to.
     * @return True if suggestions can be made, false otherwise
     */
    boolean canSuggest(GraphService graphService, String namespace);

    /**
     * Provide suggestions for the given namespace, given the input.
     * Be aware that the input may only contain snippets of the actual input, e.g. `Rout` instead of `Routers` (type ahead).
     * @param searchContext The SearchContext to allow access to graphs and other configuration
     * @param namespace The namespace of the current selected provider to make mappings
     * @param input The current input, may be a snippet of the final input for type ahead support. It is never null or empty.
     * @return A list of suggestions the SearchProvider can use later to resolve to actual Vertices. Be aware, that this should only return actual results, e.g. "Routers" when input was "Rout".
     */
    List<SearchSuggestion> getSuggestions(SearchContext searchContext, String namespace, String input);

    /**
     * Returns the unique <code>providerId</code>.
     * This is required as there is a 1:n relationship between search provider and namespace, meaning multiple providers may provide
     * suggestions for the same namespace. In order to resolve from the correct suggestion, a 1:1 mapping backwards is required.
     * For this the providerId is used, which in return must be unique over all {@link SearchProvider}.
     *
     */
    default String getProviderId() {
        return this.getClass().getSimpleName();
    }

    /**
     * Resolves the given SearchCriteria to a list of vertices.
     *
     * @param graphService The GraphService to get access to a graph
     * @param searchCriteria The SearchCriteria to resolve
     * @return A list of vertices matching the SearchCriteria.
     */
    List<GenericVertex> resolve(GraphService graphService, SearchCriteria searchCriteria);
}
