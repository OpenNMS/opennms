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
package org.opennms.netmgt.collection.client.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.MetadataConstants;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcTarget;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.CollectorAdaptor;
import org.opennms.netmgt.collection.api.CollectorRequestBuilder;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
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

    private final List<CollectorAdaptor> adaptors = new ArrayList<>();

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
        this.serviceCollector = client.getRegistry().getCollectorFutureByClassName(className).getNow(null);
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
    public CollectorRequestBuilder withAdaptor(CollectorAdaptor adaptor) {
        if (adaptor != null) {
            this.adaptors.add(adaptor);
        }
        return this;
    }

    @Override
    public CompletableFuture<CollectionSet> execute() {
        if (serviceCollector == null) {
            throw new IllegalArgumentException("Collector or collector class name is required.");
        } else if (agent == null) {
            throw new IllegalArgumentException("Agent is required.");
        }

        final Scope scope = new FallbackScope(
                this.client.getEntityScopeProvider().getScopeForNode(agent.getNodeId()),
                this.client.getEntityScopeProvider().getScopeForInterface(agent.getNodeId(), InetAddressUtils.toIpAddrString(agent.getAddress()))
        );

        final Map<String, Object> interpolatedAttributes = Interpolator.interpolateObjects(attributes, scope);

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
        // which should be included in the request.
        Map<String, Object> rawRuntimeAttributes = serviceCollector.getRuntimeAttributes(agent, interpolatedAttributes);
        // Same as above: adaptors substitute before Mate sees the tree.
        for (final CollectorAdaptor adaptor : adaptors) {
            final Map<String, Object> next = adaptor.beforeRuntimeInterpolation(agent, rawRuntimeAttributes);
            rawRuntimeAttributes = next != null ? next : rawRuntimeAttributes;
        }
        final Map<String, Object> runtimeAttributes = Interpolator.interpolateAttributes(rawRuntimeAttributes, scope);
        final Map<String, Object> dispatchedAttributes = new HashMap<>();
        dispatchedAttributes.putAll(interpolatedAttributes);
        dispatchedAttributes.putAll(runtimeAttributes);

        // The runtime attributes may include objects which need to be marshaled.
        // Only marshal these if the request is being executed at another location.
        if (MonitoringLocationUtils.isDefaultLocationName(request.getLocation())) {
            // As-is
            request.setAgent(agent);
            request.addAttributes(dispatchedAttributes);
        } else {
            // Marshal
            request.setAgent(new CollectionAgentDTO(agent));
            final Map<String, String> marshaledParms = serviceCollector.marshalParameters(dispatchedAttributes);
            marshaledParms.forEach(request::addAttribute);
            request.setAttributesNeedUnmarshaling(true);
        }

        return client.getDelegate().execute(request).handle((response, ex) ->
                applyAdaptorsAndPropagate(agent, dispatchedAttributes, adaptors,
                        ex == null ? response.getCollectionSet() : null, ex));
    }

    /**
     * Runs each adaptor's {@link CollectorAdaptor#handleCollectionResult} on
     * success and failure paths. On exceptional completion a synthetic
     * FAILED {@link CollectionSet} is supplied so adaptors that key on
     * {@code result.getStatus() == FAILED} (e.g. token-auth invalidation)
     * still get notified, and the original exception is re-thrown so
     * callers see it.
     */
    public static CollectionSet applyAdaptorsAndPropagate(
            CollectionAgent agent,
            Map<String, Object> dispatchedAttributes,
            List<CollectorAdaptor> adaptors,
            CollectionSet successResult,
            Throwable ex) {
        CollectionSet result = (ex != null)
                ? new CollectionSetBuilder(agent).withStatus(CollectionStatus.FAILED).build()
                : successResult;
        if (ex != null && hasAuthFailureMarker(ex)) {
            dispatchedAttributes.put(CollectorAdaptor.AUTH_FAILURE_PARAM, Boolean.TRUE);
        }
        for (final CollectorAdaptor adaptor : adaptors) {
            result = adaptor.handleCollectionResult(agent, dispatchedAttributes, result);
        }
        if (ex != null) {
            Throwable cause = (ex instanceof CompletionException && ex.getCause() != null) ? ex.getCause() : ex;
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new CompletionException(cause);
        }
        return result;
    }

    // Substring (not startsWith) so wrapped messages like
    // "Can't retrieve ... because auth failure: HTTP 401 ..." still match.
    private static boolean hasAuthFailureMarker(final Throwable ex) {
        Throwable t = ex;
        int depth = 0;
        while (t != null && depth++ < 16) {
            final String msg = t.getMessage();
            if (msg != null && msg.contains("auth failure:")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

}
