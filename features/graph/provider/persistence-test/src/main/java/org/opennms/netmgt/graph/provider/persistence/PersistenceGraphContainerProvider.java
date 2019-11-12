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

package org.opennms.netmgt.graph.provider.persistence;

import java.util.Objects;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.persistence.GraphRepository;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;

/**
 * This {@link GraphContainerProvider} is an example on how to use the {@link GraphRepository} within a provider.
 *
 * @author mvrueden
 */
public class PersistenceGraphContainerProvider implements GraphContainerProvider {

    private static final String CONTAINER_ID = "persistence-example";

    private final GraphRepository graphRepository;

    public PersistenceGraphContainerProvider(GraphRepository graphRepository) {
        this.graphRepository = Objects.requireNonNull(graphRepository);
    }

    @Override
    public ImmutableGraphContainer loadGraphContainer() {
        return graphRepository.findContainerById(CONTAINER_ID);
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        return graphRepository.findContainerInfoById(CONTAINER_ID);
    }

    // This ensures that the container info is already present, even if nothing was persisted yet
    public void init() {
        this.graphRepository.deleteContainer(CONTAINER_ID);
        this.graphRepository.save(createContainerInfo());
    }

    protected static GraphContainerInfo createContainerInfo() {
        final DefaultGraphContainerInfo info = new DefaultGraphContainerInfo(CONTAINER_ID);
        info.setDescription("Example container which uses the GraphRepository for persistence");
        info.setLabel("Example Persistence Graph Container");

        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(CONTAINER_ID + ".graph", CustomVertex.class);
        graphInfo.setDescription("The only graph of the container");
        graphInfo.setLabel("Graph");
        info.getGraphInfos().add(graphInfo);

        return info;
    }

}