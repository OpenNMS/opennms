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

package org.opennms.netmgt.graph.service;

import java.util.Objects;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer.GenericGraphContainerBuilder;
import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphProvider;

public class SingleGraphContainerProvider implements GraphContainerProvider {

    private final GraphContainerInfo containerInfo;
    private final GraphProvider graphProvider;

    public SingleGraphContainerProvider(GraphProvider graphProvider, GraphContainerInfo containerInfo) {
        this.graphProvider = Objects.requireNonNull(graphProvider);
        this.containerInfo = Objects.requireNonNull(containerInfo);
    }

    @Override
    public ImmutableGraphContainer loadGraphContainer() {
        final GenericGraphContainerBuilder containerBuilder = GenericGraphContainer.builder().applyContainerInfo(getContainerInfo());
        final GenericGraph graph = graphProvider.loadGraph().asGenericGraph();
        if (graph != null) {
            containerBuilder.addGraph(graph);
        }
        return containerBuilder.build();
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        final DefaultGraphContainerInfo containerInfo = new DefaultGraphContainerInfo(this.containerInfo.getId());
        containerInfo.setDescription(this.containerInfo.getDescription());
        containerInfo.setLabel(this.containerInfo.getLabel());
        containerInfo.getGraphInfos().add(graphProvider.getGraphInfo());
        return containerInfo;
    }
}
