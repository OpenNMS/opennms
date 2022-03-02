/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
