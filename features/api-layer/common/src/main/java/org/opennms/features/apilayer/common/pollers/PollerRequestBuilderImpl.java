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
package org.opennms.features.apilayer.common.pollers;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.integration.api.v1.pollers.PollerRequestBuilder;
import org.opennms.integration.api.v1.pollers.PollerResult;
import org.opennms.integration.api.v1.pollers.Status;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;

import com.google.common.base.Enums;

/**
 * Builder implementation for {@link org.opennms.integration.api.v1.pollers.ServicePollerClient}.
 */
public class PollerRequestBuilderImpl implements PollerRequestBuilder {

    public PollerRequestBuilderImpl(LocationAwarePollerClient pollerClient) {
        this.pollerClient = pollerClient;
    }

    private String className;

    private InetAddress address;

    private String location;

    private String serviceName;

    private Long ttlInMs;

    private Map<String, String> attributes = new HashMap<>();

    private final LocationAwarePollerClient pollerClient;


    @Override
    public PollerRequestBuilder withPollerClassName(String className) {
        this.className = className;
        return this;
    }

    @Override
    public PollerRequestBuilder withAddress(InetAddress address) {
        this.address = address;
        return this;
    }


    @Override
    public PollerRequestBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public PollerRequestBuilder withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    @Override
    public PollerRequestBuilder withAttribute(String key, String value) {
        this.attributes.put(key, value);
        return this;
    }

    @Override
    public PollerRequestBuilder withAttributes(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    public PollerRequestBuilder withTimeToLive(Long ttlInMs) {
        this.ttlInMs = ttlInMs;
        return this;
    }

    @Override
    public CompletableFuture<PollerResult> execute() {
        Map<String, Object> props = Collections.unmodifiableMap(attributes);
        MonitoredService service = new SimpleMonitoredService(address, serviceName, location);
        CompletableFuture<PollerResponse> future = pollerClient.poll()
                .withService(service)
                .withMonitorClassName(className)
                .withTimeToLive(ttlInMs)
                .withAttributes(props)
                .execute();
        // convert the response to PollResult.
        CompletableFuture<PollerResult> result = new CompletableFuture<>();

        try {
            PollerResponse pollerResponse = future.get();
            result.complete(new PollerResultImpl(pollerResponse.getPollStatus()));
        } catch (InterruptedException | ExecutionException e) {
           result.completeExceptionally(e);
        }
        return result;
    }

    private class PollerResultImpl implements PollerResult {

        private final Status status;
        private String reason;
        private Map<String, Number> properties;

        private PollerResultImpl(PollStatus pollStatus) {
            this.status = Enums.getIfPresent(Status.class, pollStatus.getStatusName()).or(Status.Unknown);
            if(status.equals(Status.Up)) {
                properties = pollStatus.getProperties();
            } else {
                this.reason = pollStatus.getReason();
            }
        }

        @Override
        public Status getStatus() {
            return this.status;
        }

        @Override
        public String getReason() {
            return this.reason;
        }

        @Override
        public Map<String, Number> getProperties() {
            return this.properties;
        }
    }
}
