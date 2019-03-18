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

package org.opennms.netmgt.graph.provider.application;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphProvider;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.graph.simple.SimpleGraph;
import org.opennms.netmgt.graph.simple.SimpleVertex;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ApplicationGraphProvider implements GraphProvider {

    static final String TOPOLOGY_NAMESPACE = "application";
    private final static String CACHE_KEY = "CACHE_KEY";

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationGraphProvider.class);

    private final LoadingCache<String, Graph> graph = createCache(this::createGraph);

    private ApplicationDao applicationDao;

    public ApplicationGraphProvider(ApplicationDao applicationDao) {
        Objects.requireNonNull(applicationDao);
        this.applicationDao = applicationDao;
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), TOPOLOGY_NAMESPACE);
    }

    @Override
    public Graph<?, ?> loadGraph() {
        return graph.getUnchecked(CACHE_KEY);
    }

    @Override
    public GraphInfo<?> getGraphInfo() {
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(TOPOLOGY_NAMESPACE, SimpleVertex.class);
        graphInfo.setLabel("Application Graph");
        graphInfo.setDescription("This Topology Provider displays all defined Applications and their calculated states.");
        return graphInfo;
    }

    private Graph<?, ?> createGraph() {
        final SimpleGraph graph = new SimpleGraph(getGraphInfo());


        for (OnmsApplication application : applicationDao.findAll()) {
            ApplicationVertex applicationVertex = new ApplicationVertex(application);
            graph.addVertex(applicationVertex);

            for (OnmsMonitoredService eachMonitoredService : application.getMonitoredServices()) {
                final ApplicationVertex serviceVertex = new ApplicationVertex(eachMonitoredService);
                applicationVertex.addChildren(serviceVertex);
                graph.addVertex(applicationVertex);

                // connect with application
                String id = String.format("connection:%s:%s", applicationVertex.getId(), serviceVertex.getId());
                SimpleEdge edge = new SimpleEdge(applicationVertex, serviceVertex);
                graph.addEdge(edge);
            }
        }
        return graph;
    }

    // TODO Patrick remove once we have a general caching strategy
    private <KEY, VALUE> LoadingCache<KEY, VALUE> createCache(Supplier<VALUE> entitySupplier) {
        CacheLoader<KEY, VALUE> loader = new CacheLoader<KEY, VALUE>() {
            @Override
            public VALUE load(KEY key) {
                return entitySupplier.get();
            }
        };
        return CacheBuilder
                .newBuilder()
                .expireAfterWrite(20, TimeUnit.SECONDS)
                .build(loader);
    }
}
