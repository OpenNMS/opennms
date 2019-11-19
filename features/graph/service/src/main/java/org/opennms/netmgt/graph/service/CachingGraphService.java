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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphService;

public class CachingGraphService implements GraphService {

    private final GraphService delegate;
    private final DefaultGraphContainerCache graphContainerCache;

    public CachingGraphService(GraphService delegate, DefaultGraphContainerCache graphContainerCache) {
        this.delegate = Objects.requireNonNull(delegate);
        this.graphContainerCache = Objects.requireNonNull(graphContainerCache);
    }

    @Override
    public List<GraphContainerInfo> getGraphContainerInfos() {
        return delegate.getGraphContainerInfos();
    }

    @Override
    public GraphContainerInfo getGraphContainerInfo(String containerId) {
        return delegate.getGraphContainerInfo(containerId);
    }

    @Override
    public GraphContainerInfo getGraphContainerInfoByNamespace(String namespace) {
        return delegate.getGraphContainerInfoByNamespace(namespace);
    }

    @Override
    public GraphInfo getGraphInfo(String graphNamespace) {
        return delegate.getGraphInfo(graphNamespace);
    }

    @Override
    public synchronized GenericGraphContainer getGraphContainer(String containerId) {
        if (graphContainerCache.has(containerId)) {
            return graphContainerCache.get(containerId).asGenericGraphContainer();
        }
        final GenericGraphContainer graphContainer = delegate.getGraphContainer(containerId);
        if (graphContainer != null) {
            graphContainerCache.put(graphContainer);
        }
        return graphContainer;
    }

    @Override
    public GenericGraph getGraph(String containerId, String graphNamespace) {
        final GenericGraphContainer graphContainer = getGraphContainer(containerId);
        if (graphContainer != null) {
            return graphContainer.getGraph(graphNamespace);
        }
        return null;
    }

    public void onUnbind(GraphContainerProvider graphContainerProvider, Map<String, String> props) {
        if (graphContainerProvider != null) {
            final String containerId = graphContainerProvider.getContainerInfo().getId();
            graphContainerCache.cancel(containerId);
            graphContainerCache.invalidate(containerId);
        }
    }

    public void onBind(GraphContainerProvider graphContainerProvider, Map<String, String> props) {
        final String containerId = graphContainerProvider.getContainerInfo().getId();
        final int cacheInvalidateInterval = Integer.valueOf(props.getOrDefault("cacheInvalidateInterval", "0"));
        if (cacheInvalidateInterval > 0) {
            graphContainerCache.periodicallyInvalidate(containerId, cacheInvalidateInterval, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        graphContainerCache.shutdown();
    }
}
