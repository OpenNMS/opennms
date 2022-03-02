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
