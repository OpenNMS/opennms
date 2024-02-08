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
package org.opennms.netmgt.poller.client.rpc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcTarget;
import org.opennms.core.mate.api.MetadataConstants;
import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerRequestBuilder;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

public class PollerRequestBuilderImpl implements PollerRequestBuilder {

    private MonitoredService service;

    private String systemId;

    private String className;

    private LocationAwarePollerClientImpl client;

    private final Map<String, Object> attributes = new HashMap<>();

    private final List<ServiceMonitorAdaptor> adaptors = new LinkedList<>();

    private final Map<String, String> patternVariables = new HashMap<>();

    private Long ttlInMs;

    public PollerRequestBuilderImpl(LocationAwarePollerClientImpl client) {
        this.client = client;
    }

    @Override
    public PollerRequestBuilder withService(MonitoredService service) {
        this.service = service;
        return this;
    }

    @Override
    public PollerRequestBuilder withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    @Override
    public PollerRequestBuilder withMonitorLocator(ServiceMonitorLocator serviceMonitorLocator) {
        return this.withMonitorClassName(serviceMonitorLocator.getServiceLocatorKey());
    }

    @Override
    public PollerRequestBuilder withMonitorClassName(String className) {
        this.className = className;
        return this;
    }

    @Override
    public PollerRequestBuilder withTimeToLive(Long ttlInMs) {
        this.ttlInMs = ttlInMs;
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
        adaptors.add(adaptor);
        return this;
    }

    @Override
    public PollerRequestBuilder withPatternVariables(Map<String, String> patternVariables) {
        this.patternVariables.putAll(patternVariables);
        return this;
    }

    private Scope getScope() {
        return new FallbackScope(
                this.client.getEntityScopeProvider().getScopeForNode(this.service.getNodeId()),
                this.client.getEntityScopeProvider().getScopeForInterface(this.service.getNodeId(), this.service.getIpAddr()),
                this.client.getEntityScopeProvider().getScopeForService(this.service.getNodeId(), this.service.getAddress(), this.service.getSvcName()),
                MapScope.singleContext(Scope.ScopeName.SERVICE, "pattern", this.patternVariables)
        );
    }

    @Override
    public Map<String, Object> getInterpolatedAttributes() {
        return Interpolator.interpolateObjects(this.attributes, getScope());
    }

    @Override
    public CompletableFuture<PollerResponse> execute() {
        if (className == null) {
            throw new IllegalArgumentException("Monitor class name is required.");
        } else if (service == null) {
            throw new IllegalArgumentException("Monitored service is required.");
        }

        final var serviceMonitor = client.getRegistry().getMonitorByClassName(className);
        if (serviceMonitor == null) {
            throw new IllegalArgumentException("Monitor not found: " + className);
        }

        final Map<String, Object> interpolatedAttributes = this.getInterpolatedAttributes();

        final RpcTarget target = client.getRpcTargetHelper().target()
                .withNodeId(service.getNodeId())
                .withLocation(service.getNodeLocation())
                .withSystemId(systemId)
                .withServiceAttributes(interpolatedAttributes)
                .withLocationOverride((s) -> serviceMonitor.getEffectiveLocation(s))
                .build();

        final PollerRequestDTO request = new PollerRequestDTO();
        request.setLocation(target.getLocation());
        request.setSystemId(target.getSystemId());
        request.setClassName(className);
        request.setServiceName(service.getSvcName());
        request.setAddress(service.getAddress());
        request.setNodeId(service.getNodeId());
        request.setNodeLabel(service.getNodeLabel());
        request.setNodeLocation(service.getNodeLocation());
        //Overwrite if ttl exists in metadata
        ttlInMs = ParameterMap.getLongValue(MetadataConstants.TTL, interpolatedAttributes.get(MetadataConstants.TTL), ttlInMs);
        request.setTimeToLiveMs(ttlInMs);
        request.addAttributes(interpolatedAttributes);
        request.addTracingInfo(RpcRequest.TAG_NODE_ID, String.valueOf(service.getNodeId()));
        request.addTracingInfo(RpcRequest.TAG_NODE_LABEL, service.getNodeLabel());
        request.addTracingInfo(RpcRequest.TAG_CLASS_NAME, className);
        request.addTracingInfo(RpcRequest.TAG_IP_ADDRESS, InetAddressUtils.toIpAddrString(service.getAddress()));

        // Retrieve the runtime attributes, which may include attributes
        // such as the agent details and other state related attributes
        // which should be included in the request
        final Map<String, Object> parameters = request.getMonitorParameters();
        request.addAttributes(Interpolator.interpolateAttributes(serviceMonitor.getRuntimeAttributes(request, parameters), getScope()));

        // Execute the request
        return client.getDelegate().execute(request).thenApply(results -> {
            PollStatus pollStatus = results.getPollStatus();
            // Invoke the adapters in the same order as which they were added
            for (ServiceMonitorAdaptor adaptor : adaptors) {
                // The adapters may update the status
                pollStatus = adaptor.handlePollResult(service, new HashMap<>(interpolatedAttributes), pollStatus);
            }
            results.setPollStatus(pollStatus);
            return results;
        });
    }


}
