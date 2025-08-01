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
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphService;

@Service
public class ContainerNamespaceCompleter implements Completer {

    @Reference
    private GraphService graphService;

    @Override
    public int complete(Session session, CommandLine commandLine, List<String> candidates) {
        // If container is already specified, try limiting the completion to the container instead of showing all
        final String containerId = CommandLineUtils.extractArgument(commandLine, "--container");
        if (containerId != null) {
            final GraphContainerInfo graphContainerInfo = graphService.getGraphContainerInfo(containerId);
            if (graphContainerInfo != null) {
                final List<String> namespaces = graphContainerInfo.getNamespaces();
                final StringsCompleter delegate = new StringsCompleter();
                delegate.getStrings().addAll(namespaces);
                return delegate.complete(session, commandLine, candidates);
            }
            return -1; // no result
        }
        return new NamespaceCompleter(graphService).complete(session, commandLine, candidates);
    }
}
