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

/**
 * Service to search all graphs
 */
public interface GraphSearchService {

    /**
     * Returns a list of suggestions for the given namespace and input, where input may only be a
     * snippet of the whole data, e.g. for type ahead support.
     *
     * @param namespace The namespace to search in
     * @param input The "thing" to search
     * @return A list of results, the user may select from
     */
    List<SearchSuggestion> getSuggestions(String namespace, String input);

    List<GenericVertex> search(SearchCriteria searchCriteria);
}
