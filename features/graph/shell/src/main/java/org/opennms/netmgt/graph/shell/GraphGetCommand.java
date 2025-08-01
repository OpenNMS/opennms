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
package org.opennms.netmgt.graph.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.renderer.GraphRenderer;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.shell.completer.ContainerIdCompleter;
import org.opennms.netmgt.graph.shell.completer.ContainerNamespaceCompleter;

import com.google.common.base.Strings;

@Service
@Command(scope = "opennms", name = "graph-get", description="Gets a graph identified by its namespace")
public class GraphGetCommand implements Action {

    @Reference
    private GraphService graphService;

    @Reference
    private GraphRenderer graphRenderer;

    @Completion(ContainerIdCompleter.class)
    @Option(name="--container", description="The id of the container", required=false)
    private String containerId;

    @Completion(ContainerNamespaceCompleter.class)
    @Option(name="--namespace", description="The namespace of the graph", required = true)
    private String namespace;

    @Override
    public Object execute() throws Exception {
        final GenericGraph genericGraph = getGraph();
        if (genericGraph == null) {
            System.out.println("No graph in container with id '" + containerId + "' and namespace '" + namespace + "' found");
        } else {
            final String rendered = graphRenderer.render(2, genericGraph);
            System.out.println(rendered);
        }
        return null;
    }

    // If containerId is defined explicitly, use it otherwise only use namespace
    private GenericGraph getGraph() {
        if (!Strings.isNullOrEmpty(containerId)) {
            return graphService.getGraph(containerId, namespace);
        }
        return graphService.getGraph(namespace);
    }
}
