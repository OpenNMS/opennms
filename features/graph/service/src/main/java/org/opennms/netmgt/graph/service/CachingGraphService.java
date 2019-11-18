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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerCache;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphService;

public class CachingGraphService implements GraphService, GraphContainerCache {

    private final GraphService delegate;
    private final DefaultGraphContainerCache cache;

    public CachingGraphService(GraphService delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.cache = new DefaultGraphContainerCache(delegate::getGraphContainer);
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
    public GraphInfo getGraphInfo(String graphNamespace) {
        return delegate.getGraphInfo(graphNamespace);
    }

    @Override
    public GenericGraphContainer getGraphContainer(String containerId) {
        if (cache.has(containerId)) {
            return cache.get(containerId).asGenericGraphContainer();
        }
        final GenericGraphContainer graphContainer = delegate.getGraphContainer(containerId);
        if (graphContainer != null) {
            cache.put(graphContainer);
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

    @Override
    public GenericGraph getGraph(String namespace) {
        // TODO MVR this is a duplicate or DefaultGraphService.getGraph(String)
        final Optional<GraphContainerInfo> anyContainer = getGraphContainerInfos().stream().filter(container -> container.getNamespaces().contains(namespace)).findAny();
        if (anyContainer.isPresent()) {
            final GenericGraph graph = getGraphContainer(anyContainer.get().getId()).getGraph(namespace);
            return graph;
        }
        throw new NoSuchElementException("Could not find a Graph with namespace '" + namespace + "'.");
    }

    // TODO MVR this may not be the best approach of doing this
    @Override
    public boolean has(String containerId) {
        return cache.has(containerId);
    }

    // TODO MVR this may not be the best approach of doing this
    @Override
    public void invalidate(String containerId) {
        cache.invalidate(containerId);
    }

    // TODO MVR this may not be the best approach of doing this
    @Override
    public ImmutableGraphContainer get(String containerId) {
        return cache.get(containerId);
    }

    public void onUnbind(GraphContainerProvider graphContainerProvider, Map<String, String> props) {
        if (graphContainerProvider != null) {
            final String containerId = graphContainerProvider.getContainerInfo().getId();
            cache.cancel(containerId);
            cache.invalidate(containerId);
        }
    }

    public void onBind(GraphContainerProvider graphContainerProvider, Map<String, String> props) {
        final String containerId = graphContainerProvider.getContainerInfo().getId();
        final int periodicallyReload = Integer.valueOf(props.getOrDefault("periodicallyReload", "0"));
        if (periodicallyReload != 0) {
            cache.periodicallyReload(containerId, periodicallyReload, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        cache.shutdown();
    }
}
