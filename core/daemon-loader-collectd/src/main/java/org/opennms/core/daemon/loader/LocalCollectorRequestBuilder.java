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
package org.opennms.core.daemon.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectorRequestBuilder;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local CollectorRequestBuilder for standalone daemon containers.
 * Executes collections directly in-process without RPC.
 */
public class LocalCollectorRequestBuilder implements CollectorRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(LocalCollectorRequestBuilder.class);

    private final ServiceCollectorRegistry registry;
    private final Executor executor;

    private CollectionAgent agent;
    private ServiceCollector collector;
    private String collectorClassName;
    private final Map<String, Object> attributes = new HashMap<>();

    public LocalCollectorRequestBuilder(ServiceCollectorRegistry registry, Executor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @Override
    public CollectorRequestBuilder withAgent(CollectionAgent agent) {
        this.agent = agent;
        return this;
    }

    @Override
    public CollectorRequestBuilder withSystemId(String systemId) {
        // ignored for local execution
        return this;
    }

    @Override
    public CollectorRequestBuilder withCollector(ServiceCollector collector) {
        this.collector = collector;
        return this;
    }

    @Override
    public CollectorRequestBuilder withCollectorClassName(String className) {
        this.collectorClassName = className;
        return this;
    }

    @Override
    public CollectorRequestBuilder withTimeToLive(Long ttlInMs) {
        // ignored for local execution
        return this;
    }

    @Override
    public CollectorRequestBuilder withAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    @Override
    public CollectorRequestBuilder withAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    public CompletableFuture<CollectionSet> execute() {
        final ServiceCollector effectiveCollector;
        if (collector != null) {
            effectiveCollector = collector;
        } else if (collectorClassName != null) {
            try {
                effectiveCollector = registry.getCollectorFutureByClassName(collectorClassName).join();
            } catch (Exception e) {
                return CompletableFuture.failedFuture(
                        new IllegalArgumentException("Collector not found: " + collectorClassName, e));
            }
        } else {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("No collector or collector class name specified"));
        }

        if (agent == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Collection agent is required"));
        }

        final Map<String, Object> params = new HashMap<>(attributes);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return effectiveCollector.collect(agent, params);
            } catch (Exception e) {
                LOG.error("Error collecting from {} with collector {}", agent, effectiveCollector.getClass().getName(), e);
                throw new RuntimeException("Collection failed: " + e.getMessage(), e);
            }
        }, executor);
    }
}
