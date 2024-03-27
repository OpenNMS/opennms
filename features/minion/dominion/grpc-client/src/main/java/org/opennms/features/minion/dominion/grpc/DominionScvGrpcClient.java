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
package org.opennms.features.minion.dominion.grpc;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.opennms.dominion.local.rpc.grpc.DominionGrpc;
import org.opennms.dominion.local.rpc.grpc.SecureCredentialsVaultGrpc;

import io.grpc.ManagedChannel;

public class DominionScvGrpcClient {

    private final String clientId;
    private final String clientSecret;
    private final SecureCredentialsVaultGrpc.SecureCredentialsVaultBlockingStub clientStub;

    public DominionScvGrpcClient(String clientId, String clientSecret, ManagedChannel channel) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        clientStub = SecureCredentialsVaultGrpc.newBlockingStub(channel);
    }

    public DominionGrpc.ScvSupportedAliasesResponse getAliases() {
        DominionGrpc.ScvSupportedAliasesRequest request = DominionGrpc.ScvSupportedAliasesRequest.newBuilder()
                .setClientCredentials(DominionGrpc.ClientCredentials
                        .newBuilder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .build()
                )
                .build();

        return clientStub.scvGetSupportedAliases(request);
    }

    public DominionGrpc.ScvGetCredentialsResponse getCredentials(String alias) {
        Objects.requireNonNull(alias);

        DominionGrpc.ScvGetCredentialsRequest request = DominionGrpc.ScvGetCredentialsRequest.newBuilder()
                .setClientCredentials(DominionGrpc.ClientCredentials
                        .newBuilder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .build()
                )
                .setAlias(alias)
                .build();

        return clientStub.scvGetCredentials(request);
    }

    public DominionGrpc.ScvSetCredentialsResponse setCredentials(String alias,
                                                                 String username,
                                                                 String password,
                                                                 Map<String, String> attributes) {
        Objects.requireNonNull(alias);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        DominionGrpc.ScvSetCredentialsRequest request = DominionGrpc.ScvSetCredentialsRequest.newBuilder()
                .setClientCredentials(DominionGrpc.ClientCredentials
                        .newBuilder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .build()
                )
                .setAlias(alias)
                .setUser(username)
                .setPassword(password)
                .putAllAttributes(attributes != null ? attributes : Collections.emptyMap())
                .build();
        return clientStub.scvSetCredentials(request);
    }

}
