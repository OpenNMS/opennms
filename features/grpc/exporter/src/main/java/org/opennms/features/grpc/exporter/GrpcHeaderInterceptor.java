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

public class GrpcHeaderInterceptor implements ClientInterceptor {

    private final Metadata metadata;
    private ZenithConnectPersistenceService zenithConnectPersistenceService;

    public GrpcHeaderInterceptor(String tenantId) {
        metadata = new Metadata();
        metadata.put(Metadata.Key.of("tenant-id", Metadata.ASCII_STRING_MARSHALLER), tenantId);
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        addRefreshTokenInHeaders(metadata);
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

    private void addRefreshTokenInHeaders(Metadata headers){

        if(zenithConnectPersistenceService != null) {
            try {
                ZenithConnectRegistration registrations = zenithConnectPersistenceService.getRegistrations().first();
                if (registrations != null) {
                    headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + registrations.refreshToken);
                }
            } catch (ZenithConnectPersistenceException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
