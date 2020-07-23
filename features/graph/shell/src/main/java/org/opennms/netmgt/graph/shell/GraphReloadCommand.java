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
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerCache;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.shell.completer.ContainerIdCompleter;

@Service
@Command(scope = "opennms", name = "graph-force-reload", description="Invalidates the cache for the given container, forcing it to reload when next request occurs")
public class GraphReloadCommand implements Action {

    @Reference
    private GraphContainerCache graphContainerCache;

    @Reference
    private GraphService graphService;

    @Completion(ContainerIdCompleter.class)
    @Option(name="--container", description="The id of the container", required=true)
    private String containerId;

    @Override
    public Object execute() throws Exception {
        final GraphContainerInfo graphContainerInfo = graphService.getGraphContainerInfo(containerId);
        if (graphContainerInfo == null) {
            System.out.println("No graph in container with id '" + containerId + "' found");
        } else {
            graphContainerCache.invalidate(containerId);
            System.out.println("Container invalidated. On next request, the container is reloaded");
        }
        return null;
    }
}
