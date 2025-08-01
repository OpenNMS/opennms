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
