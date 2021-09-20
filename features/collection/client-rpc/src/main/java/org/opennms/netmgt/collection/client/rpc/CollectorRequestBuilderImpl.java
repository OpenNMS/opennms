/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.client.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcTarget;
import org.opennms.core.rpc.utils.MetadataConstants;
import org.opennms.core.rpc.utils.mate.FallbackScope;
import org.opennms.core.rpc.utils.mate.Interpolator;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectorRequestBuilder;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectorRequestBuilderImpl implements CollectorRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CollectorRequestBuilderImpl.class);

    private final LocationAwareCollectorClientImpl client;

    private final Map<String, Object> attributes = new HashMap<>();

    private CollectionAgent agent;

    private String systemId;

    private ServiceCollector serviceCollector;

    private Long ttlInMs;

    private String className;

    public CollectorRequestBuilderImpl(LocationAwareCollectorClientImpl client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public CollectorRequestBuilder withAgent(CollectionAgent agent) {
        this.agent = agent;
        return this;
    }

    @Override
    public CollectorRequestBuilder withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    @Override
    public CollectorRequestBuilder withCollector(ServiceCollector collector) {
        this.serviceCollector = collector;
        return this;
    }

    @Override
    public CollectorRequestBuilder withCollectorClassName(String className) {
        this.className = className;
        this.serviceCollector = client.getRegistry().getCollectorByClassName(className);
        return this;
    }

    @Override
    public CollectorRequestBuilder withTimeToLive(Long ttlInMs) {
        this.ttlInMs = ttlInMs;
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
        if (serviceCollector == null) {
            throw new IllegalArgumentException("Collector or collector class name is required.");
        } else if (agent == null) {
            throw new IllegalArgumentException("Agent is required.");
        }

        final Map<String, Object> interpolatedAttributes = Interpolator.interpolateObjects(attributes, new FallbackScope(
                this.client.getEntityScopeProvider().getScopeForNode(agent.getNodeId()),
                this.client.getEntityScopeProvider().getScopeForInterface(agent.getNodeId(), InetAddressUtils.toIpAddrString(agent.getAddress()))
        ));

        final RpcTarget target = client.getRpcTargetHelper().target()
                .withNodeId(agent.getNodeId())
                .withLocation(agent.getLocationName())
                .withSystemId(systemId)
                .withServiceAttributes(interpolatedAttributes)
                .withLocationOverride((s) -> serviceCollector.getEffectiveLocation(s))
                .build();

        CollectorRequestDTO request = new CollectorRequestDTO();
        request.setLocation(target.getLocation());
        request.setSystemId(target.getSystemId());
        // For Service collectors that implement integration api will have proxy collectors.
        // fetching class name from proxy won't match with class name in collector registry so prefer clasName if it present.
        final String collectorClassName = className != null ? className : serviceCollector.getClass().getCanonicalName();
        request.setClassName(collectorClassName);
        // Overwrite if ttl exists in metadata.
        ttlInMs = ParameterMap.getLongValue(MetadataConstants.TTL, interpolatedAttributes.get(MetadataConstants.TTL), ttlInMs);
        request.setTimeToLiveMs(ttlInMs);
        request.addTracingInfo(RpcRequest.TAG_NODE_ID, String.valueOf(agent.getNodeId()));
        request.addTracingInfo(RpcRequest.TAG_NODE_LABEL, agent.getNodeLabel());
        request.addTracingInfo(RpcRequest.TAG_CLASS_NAME, collectorClassName);
        request.addTracingInfo(RpcRequest.TAG_IP_ADDRESS, InetAddressUtils.toIpAddrString(agent.getAddress()));

        // Retrieve the runtime attributes, which may include attributes
        // such as the agent details and other state related attributes
        // which should be included in the request
        final Map<String, Object> runtimeAttributes = serviceCollector.getRuntimeAttributes(agent, interpolatedAttributes);
        final Map<String, Object> allAttributes = new HashMap<>();
        allAttributes.putAll(interpolatedAttributes);
        allAttributes.putAll(runtimeAttributes);

        // The runtime attributes may include objects which need to be marshaled.
        // Only marshal these if the request is being executed at another location.
        if (MonitoringLocationUtils.isDefaultLocationName(request.getLocation())) {
            // As-is
            request.setAgent(agent);
            request.addAttributes(allAttributes);
        } else {
            // Marshal
            request.setAgent(new CollectionAgentDTO(agent));
            final Map<String, String> marshaledParms = serviceCollector.marshalParameters(allAttributes);
            marshaledParms.forEach(request::addAttribute);
            request.setAttributesNeedUnmarshaling(true);
        }

        // Execute the request
        return client.getDelegate().execute(request).thenApply(CollectorResponseDTO::getCollectionSet);
    }

}
