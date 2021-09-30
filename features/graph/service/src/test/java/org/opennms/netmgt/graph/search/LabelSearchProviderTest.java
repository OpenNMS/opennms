/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
