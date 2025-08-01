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
