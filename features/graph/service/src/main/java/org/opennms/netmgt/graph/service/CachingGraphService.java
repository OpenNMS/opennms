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
package org.opennms.netmgt.graph.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerCache;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphService;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;

public class CachingGraphService implements GraphService, GraphContainerCache {

    private final GraphService delegate;
    private final LoadingCache<String, ImmutableGraphContainer> cache;
    private final Map<String, Long> expireMap = new ConcurrentHashMap<>();

    public CachingGraphService(final GraphService delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.cache = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, ImmutableGraphContainer>() {
                @Override
                public long expireAfterCreate(String key, ImmutableGraphContainer value, long currentTime) {
                    final Long expireInMs = expireMap.getOrDefault(key, Long.MAX_VALUE / 1000000);
                    return expireInMs * 1000000;
                }

                @Override
                public long expireAfterUpdate(String key, ImmutableGraphContainer value, long currentTime, long currentDuration) {
                   return currentDuration;
                }

                @Override
                public long expireAfterRead(String key, ImmutableGraphContainer value, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .recordStats()
            .build(delegate::getGraphContainer);
    }

    @Override
    public List<GraphContainerInfo> getGraphContainerInfos() {
        // no need to cache info calls
        return delegate.getGraphContainerInfos();
    }

    @Override
    public GraphContainerInfo getGraphContainerInfo(String containerId) {
        // no need to cache info calls
        return delegate.getGraphContainerInfo(containerId);
    }

    @Override
    public GraphContainerInfo getGraphContainerInfoByNamespace(String namespace) {
        // no need to cache info calls
        return delegate.getGraphContainerInfoByNamespace(namespace);
    }

    @Override
    public GraphInfo getGraphInfo(String graphNamespace) {
        // no need to cache info calls
        return delegate.getGraphInfo(graphNamespace);
    }

    @Override
    public GenericGraphContainer getGraphContainer(String containerId) {
        final ImmutableGraphContainer immutableGraphContainer = get(containerId);
        if (immutableGraphContainer != null) {
            return immutableGraphContainer.asGenericGraphContainer();
        }
        return null;
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
    public void invalidate(String containerId) {
        cache.invalidate(containerId);
    }

    @Override
    public ImmutableGraphContainer get(String containerId) {
        return cache.get(containerId);
    }

    public synchronized void onUnbind(GraphContainerProvider graphContainerProvider, Map<String, String> props) {
        if (graphContainerProvider != null) {
            final String containerId = graphContainerProvider.getContainerInfo().getId();
            cache.invalidate(containerId);
            expireMap.remove(containerId);
        }
    }

    public void onBind(GraphContainerProvider graphContainerProvider, Map<String, String> props) {
        final String containerId = graphContainerProvider.getContainerInfo().getId();
        final long cacheInvalidateIntervalInSeconds = Long.valueOf(props.getOrDefault("cacheInvalidateInterval", "0"));
        if (cacheInvalidateIntervalInSeconds > 0) {
            final long cacheInvalidateIntervalInMilliseconds = cacheInvalidateIntervalInSeconds * 1000;
            expireMap.put(containerId, cacheInvalidateIntervalInMilliseconds);
        }
    }
}
