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
