/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.rest.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.GraphSearchService;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.rest.api.GraphSearchRestService;

public class GraphSearchRestServiceImpl implements GraphSearchRestService {

    private GraphSearchService graphSearchService;

    public GraphSearchRestServiceImpl(GraphSearchService graphSearchService) {
        this.graphSearchService = Objects.requireNonNull(graphSearchService);
    }

    @Override
    public Response getSuggestions(String namespace, String input) {
        List<SearchSuggestion> result = graphSearchService.getSuggestions(namespace, input);
        if(result.size() < 1) {
            return Response.noContent().build();
        }
        return Response.ok(result).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response search(String namespace, String providerId, String criteria) {
        final SearchCriteria searchCriteria = new SearchCriteria(providerId, namespace, criteria);
        final List<GenericVertex> result = graphSearchService.search(searchCriteria);
        if(result.size() < 1) {
            return Response.noContent().build();
        }
        final List<VertexRef> vertexRefs = result.stream().map(v -> v.getVertexRef()).collect(Collectors.toList());
        return Response.ok(vertexRefs).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
