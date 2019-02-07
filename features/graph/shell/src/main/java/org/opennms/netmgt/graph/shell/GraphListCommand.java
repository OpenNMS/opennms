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
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphService;

@Command(scope = "graph", name = "list", description="List all available graph containers and its graphs")
@Service
public class GraphListCommand implements Action {

    private static final int MAX_DESCRIPTION_LENGTH = 40;
    private static final String CONTAINER_ROW_TEMPLATE = "%%-%ds   %%-%ds   %%-%ds   %%s";
    private static final String GRAPH_ROW_TEMPLATE = "%%-%ds   %%-%ds   %%-%ds   %%s";

    @Reference
    private GraphService graphService;

    @Override
    public Object execute() throws Exception {
        final List<GraphContainerInfo> graphContainerInfoList = graphService.getGraphContainerInfos();
        if (!graphContainerInfoList.isEmpty()) {
            // Print containers
            System.out.println("Registered Graph Containers:");
            final int maxContainerIdLength = graphContainerInfoList.stream().mapToInt(ci -> ci.getId().length()).max().getAsInt();
            final int maxContainerLabelLength = graphContainerInfoList.stream().mapToInt(ci -> ci.getLabel().length()).max().getAsInt();
            final String ContainerRowFormat = String.format(CONTAINER_ROW_TEMPLATE, maxContainerIdLength > "Container ID".length() ? maxContainerIdLength : "Container ID".length(), maxContainerLabelLength, MAX_DESCRIPTION_LENGTH);
            System.out.println(String.format(ContainerRowFormat, "Container ID", "Label", "Description", "Graph Namespaces"));
            for (GraphContainerInfo eachContainerInfo : graphContainerInfoList) {
                final String description = cutString(eachContainerInfo.getDescription());
                System.out.println(String.format(ContainerRowFormat, eachContainerInfo.getId(), eachContainerInfo.getLabel(), description, eachContainerInfo.getNamespaces()));
            }

            // Print graphs
            System.out.println();
            System.out.println("Registered Graphs:");
            final List<GraphInfo> graphInfos = graphContainerInfoList.stream().flatMap(ci -> ci.getGraphInfos().stream()).collect(Collectors.toList());
            final int maxNamespaceLength = graphInfos.stream().mapToInt(gi -> gi.getNamespace().length()).max().getAsInt();
            final int maxGraphLabelLength = graphInfos.stream().mapToInt(gi -> gi.getLabel() != null ? gi.getLabel().length() : 0).max().getAsInt();
            final String GraphRowFormat = String.format(GRAPH_ROW_TEMPLATE, maxNamespaceLength > "Namespace".length() ? maxNamespaceLength : "Namespace".length(), maxGraphLabelLength, MAX_DESCRIPTION_LENGTH);
            System.out.println(String.format(GraphRowFormat, "Namespace", "Label", "Description", "Container ID"));
            for (GraphContainerInfo eachContainerInfo : graphContainerInfoList) {
                for (GraphInfo eachGraphInfo : eachContainerInfo.getGraphInfos()) {
                    final String description = cutString(eachGraphInfo.getDescription());
                    System.out.println(String.format(GraphRowFormat, eachGraphInfo.getNamespace(), eachGraphInfo.getLabel(), description, eachContainerInfo.getId()));
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
        return input;
    }
}
