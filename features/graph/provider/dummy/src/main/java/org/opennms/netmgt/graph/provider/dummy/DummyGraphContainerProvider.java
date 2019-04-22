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

package org.opennms.netmgt.graph.provider.dummy;

import org.opennms.netmgt.graph.api.GraphContainer;
import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.graph.simple.SimpleGraph;
import org.opennms.netmgt.graph.simple.SimpleGraphContainer;
import org.opennms.netmgt.graph.simple.SimpleVertex;

public class DummyGraphContainerProvider implements GraphContainerProvider {

    @Override
    public GraphContainer loadGraphContainer() {
        final GraphContainerInfo containerInfo = getContainerInfo();
        final SimpleGraphContainer container = new SimpleGraphContainer(containerInfo);
        final SimpleGraph graph = SimpleGraph.fromGraphInfo(containerInfo.getGraphInfo("graph1"));
        final SimpleVertex v1 = new SimpleVertex(graph.getNamespace(), "v1");
        final SimpleVertex v2 = new SimpleVertex(graph.getNamespace(), "v2");
        final SimpleEdge e1 = new SimpleEdge(graph.getNamespace(), v1, v2);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addEdge(e1);
        container.addGraph(graph);
        return container;
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        final DefaultGraphContainerInfo info = new DefaultGraphContainerInfo("dummy");
        info.setDescription("Dummy container for test purposes");
        info.setLabel("Dummy Graph Container");

        final DefaultGraphInfo graphInfo = new DefaultGraphInfo("graph1", SimpleVertex.class);
        graphInfo.setDescription("The only graph of the container");
        graphInfo.setLabel("Graph 1");

        info.getGraphInfos().add(graphInfo);

        return info;
    }
}
