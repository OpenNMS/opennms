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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerRequestBuilder;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local PollerRequestBuilder for standalone daemon containers.
 * Executes polls directly in-process without RPC.
 */
public class LocalPollerRequestBuilder implements PollerRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(LocalPollerRequestBuilder.class);

    private final ServiceMonitorRegistry registry;
    private final Executor executor;

    private MonitoredService service;
    private String className;
    private final Map<String, Object> attributes = new HashMap<>();
    private final List<ServiceMonitorAdaptor> adaptors = new LinkedList<>();
    private final Map<String, String> patternVariables = new HashMap<>();

    public LocalPollerRequestBuilder(ServiceMonitorRegistry registry, Executor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @Override
    public PollerRequestBuilder withService(MonitoredService service) {
        this.service = service;
        return this;
    }

    @Override
    public PollerRequestBuilder withSystemId(String systemId) {
        // ignored for local execution
        return this;
    }

    @Override
    public PollerRequestBuilder withMonitorLocator(ServiceMonitorLocator locator) {
        this.className = locator.getServiceLocatorKey();
        return this;
    }

    @Override
    public PollerRequestBuilder withMonitorClassName(String className) {
        this.className = className;
        return this;
    }

    @Override
    public PollerRequestBuilder withTimeToLive(Long ttlInMs) {
        // ignored for local execution
        return this;
    }

    @Override
    public PollerRequestBuilder withAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    @Override
    public PollerRequestBuilder withAttributes(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    public PollerRequestBuilder withAdaptor(ServiceMonitorAdaptor adaptor) {
        this.adaptors.add(adaptor);
        return this;
    }

    @Override
    public PollerRequestBuilder withPatternVariables(Map<String, String> patterns) {
        this.patternVariables.putAll(patterns);
        return this;
    }

    @Override
    public Map<String, Object> getInterpolatedAttributes() {
        // No MATE interpolation in standalone mode — return attributes as-is
        return new HashMap<>(attributes);
    }

    @Override
    public CompletableFuture<PollerResponse> execute() {
        if (className == null) {
            throw new IllegalArgumentException("Monitor class name is required.");
        }
        if (service == null) {
            throw new IllegalArgumentException("Monitored service is required.");
        }

        final ServiceMonitor monitor = registry.getMonitorByClassName(className);
        if (monitor == null) {
            throw new IllegalArgumentException("Monitor not found: " + className);
        }

        final Map<String, Object> params = getInterpolatedAttributes();

        return CompletableFuture.supplyAsync(() -> {
            PollStatus pollStatus;
            try {
                pollStatus = monitor.poll(service, params);
            } catch (RuntimeException e) {
                LOG.error("Error polling {} with monitor {}", service, className, e);
                pollStatus = PollStatus.down("Exception: " + e.getMessage());
            }

            // Apply adaptors in order
            for (ServiceMonitorAdaptor adaptor : adaptors) {
                pollStatus = adaptor.handlePollResult(service, new HashMap<>(params), pollStatus);
            }

            final PollStatus finalStatus = pollStatus;
            return (PollerResponse) () -> finalStatus;
        }, executor);
    }
}
