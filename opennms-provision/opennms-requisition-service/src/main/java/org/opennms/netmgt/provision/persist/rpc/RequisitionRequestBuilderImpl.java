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
package org.opennms.netmgt.provision.persist.rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.opennms.netmgt.provision.persist.RequisitionProvider;
import org.opennms.netmgt.provision.persist.RequisitionRequest;
import org.opennms.netmgt.provision.persist.RequisitionRequestBuilder;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequisitionRequestBuilderImpl implements RequisitionRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(RequisitionRequestBuilderImpl.class);

    public static final String LOCATION_PARAMETER_NAME = "location";

    public static final String TTL_PARAMETER_NAME = "ttl";

    private final LocationAwareRequisitionClientImpl client;

    protected final Map<String, String> parameters = new HashMap<>();

    private RequisitionProvider provider;

    private String location;

    private String systemId;

    private Long ttlInMs;
 
    public RequisitionRequestBuilderImpl(LocationAwareRequisitionClientImpl client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public RequisitionRequestBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public RequisitionRequestBuilder withSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    @Override
    public RequisitionRequestBuilder withRequisitionProviderType(String type) {
        final RequisitionProvider provider = client.getRegistry().getProviderByType(type);
        if (provider == null) {
            throw new IllegalArgumentException(String.format("No provider found for type '%s'. Avaiable types include: %s",
                    type, client.getRegistry().getTypes()));
        }
        this.provider = provider;
        return this;
    }

    @Override
    public RequisitionRequestBuilder withRequisitionProvider(RequisitionProvider provider) {
        this.provider = provider;
        return this;
    }

    @Override
    public RequisitionRequestBuilder withParameters(Map<String, String> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    @Override
    public RequisitionRequestBuilder withTimeToLive(Long ttlInMs) {
        this.ttlInMs = ttlInMs;
        return this;
    }

    @Override
    public CompletableFuture<Requisition> execute() {
        if (provider == null) {
            throw new IllegalArgumentException("Provider or provider type is required.");
        }

        final RequisitionRequestDTO request = new RequisitionRequestDTO();
        request.setType(provider.getType());

        // Use the location from the parameter map if set
        if (parameters.containsKey(LOCATION_PARAMETER_NAME)) {
            request.setLocation(parameters.get(LOCATION_PARAMETER_NAME));
        }
        // But override it with the given location, if set
        if (location != null) {
            request.setLocation(location);
        }

        // Direct the request to the given system
        request.setSystemId(systemId);

        // Attempt to use the TTL from the parameter map if set
        if (parameters.containsKey(TTL_PARAMETER_NAME)) {
            try {
                final Long ttlParam = Long.parseLong(parameters.get(TTL_PARAMETER_NAME));
                request.setTimeToLiveMs(ttlParam);
            } catch (Throwable t) {
                LOG.warn("Failed to parse TTL from parameter map: {}", parameters, t);
            }
        }
        // But override it with the given TTL, if set
        if (ttlInMs != null) {
            request.setTimeToLiveMs(ttlInMs);
        }

        // Build the provider specific request
        final RequisitionRequest providerRequest = provider.getRequest(parameters);

        // Optionally marshal the request if were targeting a remote location
        if (MonitoringLocationUtils.isDefaultLocationName(request.getLocation())) {
            request.setProviderRequest(providerRequest);
        } else {
            request.setProviderRequest(provider.marshalRequest(providerRequest));
        }

        // Execute!
        return client.getDelegate().execute(request).thenApply(results -> {
            return results.getRequisition();
        });
    }

}
