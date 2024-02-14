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
