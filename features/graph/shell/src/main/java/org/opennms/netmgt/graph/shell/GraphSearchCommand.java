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

package org.opennms.netmgt.graph.shell;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.renderer.GraphRenderer;
import org.opennms.netmgt.graph.api.search.GraphSearchService;
import org.opennms.netmgt.graph.api.search.SearchCriteria;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.shell.completer.NamespaceCompleter;
import org.opennms.netmgt.graph.shell.completer.SuggestionCompleter;

@Service
@Command(scope = "opennms", name = "graph-search", description="Searches vertices in a given namespace (graph)")
public class GraphSearchCommand implements Action {

    @Reference
    private GraphService graphService;

    @Reference
    private GraphSearchService graphSearchService;

    @Reference
    private GraphRenderer graphRenderer;

    @Completion( NamespaceCompleter.class)
    @Option(name="--namespace", description="The namespace of the graph", required = true)
    private String namespace;

    @Completion(SuggestionCompleter.class)
    @Option(name = "--search", description = "The search input", required = true, multiValued = false)
    private String input;

    @Override
    public Object execute() {
        final GenericGraph genericGraph = graphService.getGraph(namespace);
        if (genericGraph == null) {
            System.out.println("No graph with namespace " + namespace + " found");
        } else {
            // hack to prevent StringIndexOutOfBoundsException in Karaf Code:
            final String searchString = input.replace("_", " ");
            final List<SearchSuggestion> suggestions = graphSearchService.getSuggestions(namespace, searchString);
            if (suggestions.size() > 0) {
                final SearchSuggestion suggestion = suggestions.get(0);
                final SearchCriteria searchCriteria = new SearchCriteria(
                        suggestion.getProvider(),
                        namespace,
                        suggestion.getId()
                );
                final List<GenericVertex> vertices = graphSearchService.search(searchCriteria);
                final String rendered = vertices.stream()
                        .map(vertex -> graphRenderer.render(2, vertex))
                        .collect(Collectors.joining(",\n"));
                System.out.println("Search results: " + vertices.size() + " vertices found.");
                System.out.println(rendered);
            } else {
                System.out.println(String.format("No vertices found for namespace='%s' and input='%s'", namespace, input));
            }
        }
        return null;
    }
}
