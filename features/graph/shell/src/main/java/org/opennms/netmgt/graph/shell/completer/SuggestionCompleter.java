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

package org.opennms.netmgt.graph.shell.completer;


import java.util.List;

import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.opennms.netmgt.graph.api.search.GraphSearchService;
import org.opennms.netmgt.graph.api.search.SearchSuggestion;

import com.google.common.collect.Lists;

@Service
public class SuggestionCompleter implements Completer {

    @Reference
    private GraphSearchService graphSearchService;

    @Override
    public int complete(Session session, CommandLine commandLine, List<String> candidates) {
        final String currentSearch = commandLine.getCursorArgument();
        final String namespace = extractNamespace(Lists.newArrayList(commandLine.getArguments()));
        final StringsCompleter delegate = new StringsCompleter();
        if (currentSearch != null && currentSearch.length() > 0 && namespace.length() > 0 ) {
            final List<SearchSuggestion> searchSuggestions = graphSearchService.getSuggestions(namespace, currentSearch);
            for (SearchSuggestion suggestion : searchSuggestions) {
                // hack to prevent StringIndexOutOfBoundsException in Karaf Code:
                String label = suggestion.getLabel().replace(" ", "_");
                delegate.getStrings().add(label);
            }
        }
        return delegate.complete(session, commandLine, candidates);
    }

    protected String extractNamespace(List<String> arguments) {
        final String namespace = CommandLineUtils.extractArgument(arguments, "--namespace");
        if (namespace == null) {
            return "";
        }
        return namespace;
    }

}
