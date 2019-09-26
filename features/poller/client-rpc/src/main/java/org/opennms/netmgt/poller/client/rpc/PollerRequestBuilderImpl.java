/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.client.rpc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcTarget;
import org.opennms.core.rpc.utils.MetadataConstants;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Interpolator;
import org.opennms.core.rpc.utils.mate.MapScope;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerRequestBuilder;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;

public class PollerRequestBuilderImpl implements PollerRequestBuilder {

    private MonitoredService service;

    private String systemId;

    private String className;

    private ServiceMonitor serviceMonitor;

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
    public PollerRequestBuilder withMonitor(ServiceMonitor serviceMonitor) {
        this.serviceMonitor = serviceMonitor;
        return this;
    }

    @Override
    public PollerRequestBuilder withMonitorClassName(String className) {
        this.className = className;
        this.serviceMonitor = client.getRegistry().getMonitorByClassName(className);
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

    @Override
    public CompletableFuture<PollerResponse> execute() {
        if (serviceMonitor == null) {
            throw new IllegalArgumentException("Monitor or monitor class name is required.");
        } else if (service == null) {
            throw new IllegalArgumentException("Monitored service is required.");
        }

        final Map<String, Object> interpolatedAttributes = Interpolator.interpolateObjects(attributes, new FallbackScope(
            this.client.getEntityScopeProvider().getScopeForNode(service.getNodeId()),
            this.client.getEntityScopeProvider().getScopeForInterface(service.getNodeId(), service.getIpAddr()),
            this.client.getEntityScopeProvider().getScopeForService(service.getNodeId(), service.getAddress(), service.getSvcName()),
            MapScope.singleContext("pattern", this.patternVariables)
        ));

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
        final String pollerClassName = className != null ? className : serviceMonitor.getClass().getCanonicalName();
        request.setClassName(pollerClassName);
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
        request.addTracingInfo(RpcRequest.TAG_CLASS_NAME, pollerClassName);
        request.addTracingInfo(RpcRequest.TAG_IP_ADDRESS, InetAddressUtils.toIpAddrString(service.getAddress()));

        // Retrieve the runtime attributes, which may include attributes
        // such as the agent details and other state related attributes
        // which should be included in the request
        final Map<String, Object> parameters = request.getMonitorParameters();
        request.addAttributes(serviceMonitor.getRuntimeAttributes(request, parameters));

        // Execute the request
        return client.getDelegate().execute(request).thenApply(results -> {
            PollStatus pollStatus = results.getPollStatus();
            // Invoke the adapters in the same order as which they were added
            for (ServiceMonitorAdaptor adaptor : adaptors) {
                // The adapters may update the status
                pollStatus = adaptor.handlePollResult(service, interpolatedAttributes, pollStatus);
            }
            results.setPollStatus(pollStatus);
            return results;
        });
    }


}
