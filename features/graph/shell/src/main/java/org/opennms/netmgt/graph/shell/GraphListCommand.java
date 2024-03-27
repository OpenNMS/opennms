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

import java.util.List;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphService;

@Service
@Command(scope = "opennms", name = "graph-list", description="List all available graph containers and its graphs")
public class GraphListCommand implements Action {

    private static final int MAX_DESCRIPTION_LENGTH = 100;
    private static final String CONTAINER_ROW_TEMPLATE = "%%-%ds   %%-%ds   %%-%ds   %%s";
    private static final String GRAPH_ROW_TEMPLATE = "%%-%ds   %%-%ds   %%-%ds   %%s";

    @Option(name="-a", aliases = {"--all"}, description = "Lists all graphs as well")
    public boolean showAll = false;

    @Reference
    private GraphService graphService;

    @Override
    public Object execute() throws Exception {
        final List<GraphContainerInfo> graphContainerInfoList = graphService.getGraphContainerInfos();
        if (!graphContainerInfoList.isEmpty()) {
            final int containerCount = graphContainerInfoList.size();
            final int graphCount = graphContainerInfoList.stream().mapToInt(gi -> gi.getNamespaces().size()).sum();

            // Print containers
            System.out.println(containerCount + " registered Graph Container(s):");
            final int maxContainerIdLength = graphContainerInfoList.stream().mapToInt(ci -> ci.getId().length()).max().getAsInt();
            final int maxContainerLabelLength = graphContainerInfoList.stream().mapToInt(ci -> ci.getLabel().length()).max().getAsInt();
            final String ContainerRowFormat = String.format(CONTAINER_ROW_TEMPLATE, maxContainerIdLength > "Container ID".length() ? maxContainerIdLength : "Container ID".length(), maxContainerLabelLength, MAX_DESCRIPTION_LENGTH);
            System.out.println(String.format(ContainerRowFormat, "Container ID", "Label", "Description", "Graph Namespaces"));
            for (GraphContainerInfo eachContainerInfo : graphContainerInfoList) {
                final String description = cutString(eachContainerInfo.getDescription());
                System.out.println(String.format(ContainerRowFormat, eachContainerInfo.getId(), eachContainerInfo.getLabel() == null ? "" : eachContainerInfo.getLabel(), description, eachContainerInfo.getNamespaces()));
            }

            // Print graphs
            if (showAll) {
                System.out.println();
                System.out.println(graphCount + " registered Graph(s):");
                final List<GraphInfo> graphInfos = graphContainerInfoList.stream().flatMap(ci -> ci.getGraphInfos().stream()).collect(Collectors.toList());
                final int maxNamespaceLength = graphInfos.stream().mapToInt(gi -> gi.getNamespace().length()).max().getAsInt();
                final int maxGraphLabelLength = graphInfos.stream().mapToInt(gi -> gi.getLabel() != null ? gi.getLabel().length() : 0).max().getAsInt();
                final String GraphRowFormat = String.format(GRAPH_ROW_TEMPLATE, maxNamespaceLength > "Namespace".length() ? maxNamespaceLength : "Namespace".length(), maxGraphLabelLength, MAX_DESCRIPTION_LENGTH);
                System.out.println(String.format(GraphRowFormat, "Namespace", "Label", "Description", "Container ID"));
                for (GraphContainerInfo eachContainerInfo : graphContainerInfoList) {
                    for (GraphInfo eachGraphInfo : eachContainerInfo.getGraphInfos()) {
                        final String description = cutString(eachGraphInfo.getDescription());
                        System.out.println(String.format(GraphRowFormat, eachGraphInfo.getNamespace(), eachGraphInfo.getLabel() == null ? "" : eachGraphInfo.getLabel(), description, eachContainerInfo.getId()));
                    }
                }
            }
        } else {
            System.out.println("No " + GraphContainerProvider.class.getSimpleName() + " registered");
        }
        return null;
    }

    private static String cutString(String input) {
        if (input != null && input.length() > MAX_DESCRIPTION_LENGTH) {
            return input.substring(0, MAX_DESCRIPTION_LENGTH - 3) + "...";
        }
        if (input == null) {
            return "";
        }
        return input;
    }
}
