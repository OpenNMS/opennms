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

package org.opennms.features.grpc.exporter;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectPersistenceException;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectPersistenceService;
import org.opennms.features.zenithconnect.persistence.api.ZenithConnectRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcHeaderInterceptor implements ClientInterceptor {
    public static final Logger LOG = LoggerFactory.getLogger(GrpcHeaderInterceptor.class);
    private final Metadata metadata;
    private ZenithConnectPersistenceService zenithConnectPersistenceService;
    private final Metadata.Key authorizationKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    private final Metadata.Key tenantIdKey = Metadata.Key.of("tenant-id", Metadata.ASCII_STRING_MARSHALLER);
    private final Metadata.Key authorizationBypassKey = Metadata.Key.of("Bypass-Authorization", Metadata.ASCII_STRING_MARSHALLER);
    private final boolean zenithConnectEnabled;

    public GrpcHeaderInterceptor(String tenantId, boolean zenithEnabled) {
        metadata = new Metadata();
        metadata.put(tenantIdKey, tenantId);
        zenithConnectEnabled = zenithEnabled;
        if (!zenithConnectEnabled) {
            metadata.put(authorizationBypassKey, Boolean.TRUE.toString());
        }
    }

    public GrpcHeaderInterceptor(String tenantId) {
       this(tenantId, Boolean.FALSE);
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        if (zenithConnectEnabled) {
            addAccessTokenInHeaders(metadata);
        }
        return new HeaderAttachingClientCall<>(next.newCall(method, callOptions), metadata);
    }

    private final static class HeaderAttachingClientCall<ReqT, RespT>
            extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {

        private final Metadata metadata;
        HeaderAttachingClientCall(ClientCall<ReqT, RespT> call, Metadata metadataToAttach) {
            super(call);
            this.metadata = metadataToAttach;
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            headers.merge(metadata);
            super.start(responseListener, headers);
        }
    }

    public void setZenithConnectPersistenceService(ZenithConnectPersistenceService service) {
        this.zenithConnectPersistenceService = service;
    }

    private void addAccessTokenInHeaders(Metadata headers) {
        if (zenithConnectPersistenceService != null) {
            try {
                ZenithConnectRegistration registrations = zenithConnectPersistenceService.getRegistrations().first();
                if (registrations != null) {
                    if (headers.containsKey(authorizationKey)) {
                        headers.removeAll(authorizationKey);
                    }
                    headers.put(authorizationKey, "Bearer " + registrations.accessToken);
                }
            } catch (ZenithConnectPersistenceException e) {
                LOG.warn("Error while fetching data from zenithConnectPersistenceService ", e);
            }
        }
    }
}
