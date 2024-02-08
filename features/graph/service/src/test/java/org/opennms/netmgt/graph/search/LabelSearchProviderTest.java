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
package org.opennms.netmgt.graph.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.OnmsAssert;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.search.SearchContext;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;

public class LabelSearchProviderTest {

    private final String TOPOLOGY_NAMESPACE = LabelSearchProviderTest.class.getSimpleName();
    private int id;

    private String providerId = new LabelSearchProvider().getProviderId();

    @Test
    public void shouldReturnEmptyListForEmptySearchResult() {
        assertSuggestions(new ArrayList<>(), "blah", new ArrayList<>());
        assertSuggestions(new ArrayList<>(), "", new ArrayList<>());
    }

    @Test
    public void shouldThrowExceptionForNullSearchParameter() {
        OnmsAssert.assertThrowsException(NullPointerException.class,
                () -> assertSuggestions(new ArrayList<>(), null, new ArrayList<>()));
    }

    @Test
    public void shouldReturnSuggestionsForValidSearch() {
        String searchTerm = "blah";
        List<GenericVertex> matchingVertices = new ArrayList<>();
        // vertices that should match:
        matchingVertices.add(createVertex(searchTerm));
        matchingVertices.add(createVertex("aa" + searchTerm + "bb"));
        matchingVertices.add(createVertex(searchTerm + "bb"));
        matchingVertices.add(createVertex("aa" + searchTerm + "bb"));
        matchingVertices.add(createVertex("aa" + searchTerm.toUpperCase() + "bb"));

        List<GenericVertex> allVertices = new ArrayList<>(matchingVertices);
        allVertices.add(createVertex("not matching"));

        List<SearchSuggestion> expectations = new ArrayList<>();
        for (GenericVertex vertex : matchingVertices) {
            SearchSuggestion suggestion = new SearchSuggestion(
                    providerId,
                    GenericVertex.class.getSimpleName(),
                    vertex.getLabel(),
                    vertex.getLabel());
            expectations.add(suggestion);
        }
        assertSuggestions(allVertices, searchTerm, expectations);
    }

    private GenericVertex createVertex(String label) {
        return GenericVertex.builder()
            .namespace(LabelSearchProviderTest.class.getSimpleName())
            .id("v" + id++)
            .label(label)
            .build();
    }

    private void assertSuggestions(List<GenericVertex> vertices, String input, List<SearchSuggestion> expectations) {
        GraphService graphService = Mockito.mock(GraphService.class);
        GenericGraph graph = GenericGraph.builder()
            .namespace(TOPOLOGY_NAMESPACE)
            .addVertices(vertices)
            .build();
        when(graphService.getGraph(any())).thenReturn(graph);
        LabelSearchProvider provider = new LabelSearchProvider();
        List<SearchSuggestion> results = provider.getSuggestions(
                SearchContext.builder().graphService(graphService).build(),
                TOPOLOGY_NAMESPACE, input);
        assertEquals(expectations, results);
    }
}
