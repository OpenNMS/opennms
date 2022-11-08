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

package org.opennms.dominion.features.scv.grpc;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.dominion.local.rpc.grpc.DominionGrpc;
import org.opennms.dominion.local.rpc.grpc.SecureCredentialsVaultGrpc;
import org.opennms.features.minion.dominion.grpc.DominionGrpcFactory;
import org.opennms.features.minion.dominion.grpc.DominionGrpcFactoryImpl;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.dominion.grpc.DominionSecureCredentialsVault;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class DominionSecureCredentialsVaultIT {

    private static final String HOST = "localhost";
    private static final String CLIENT_ID = "client.id";
    private static final String CLIENT_SECRET = "client.secret";
    private static final String SUPPORTED_ALIAS = "supported.alias";
    private static final List<String> aliases = Collections.singletonList(SUPPORTED_ALIAS);

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    private ManagedChannel channel;
    private SecureCredentialsVault scv;

    @Before
    public void setup() throws IOException {
        int freePort;

        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            freePort = socket.getLocalPort();
        }

        channel = ManagedChannelBuilder.forAddress(HOST, freePort)
                .usePlaintext()
                .build();
        Server server = ServerBuilder.forPort(freePort).addService(new MockDominionServer()).build();
        server.start();
        CompletableFuture.runAsync(() -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        });


        DominionGrpcFactory factory = new DominionGrpcFactoryImpl(HOST, Integer.toString(freePort), CLIENT_ID,
                CLIENT_SECRET);
        scv = new DominionSecureCredentialsVault(factory.scvClient());
    }

    @After
    public void stop() {
        channel.shutdownNow();
    }

    @Test
    public void canGetAliases() {
        assertThat(scv.getAliases(), contains(aliases.toArray()));
    }

    @Test
    public void canGetCredentials() {
        Credentials retrieved = scv.getCredentials(SUPPORTED_ALIAS);
        Credentials expected = new Credentials(USERNAME, PASSWORD);

        assertThat(retrieved, equalTo(expected));
    }

    public static class MockDominionServer extends SecureCredentialsVaultGrpc.SecureCredentialsVaultImplBase {
        @Override
        public void scvGetSupportedAliases(DominionGrpc.ScvSupportedAliasesRequest request,
                                           StreamObserver<DominionGrpc.ScvSupportedAliasesResponse> responseObserver) {
            if (!Objects.equals(request.getClientCredentials().getClientId(), CLIENT_ID) ||
                    !Objects.equals(request.getClientCredentials().getClientSecret(), CLIENT_SECRET)) {
                throw new StatusRuntimeException(Status.UNAUTHENTICATED);
            }

            if (Objects.equals(request.getClientCredentials().getClientId(), CLIENT_ID)) {
                responseObserver.onNext(DominionGrpc.ScvSupportedAliasesResponse
                        .newBuilder()
                        .addAllAliases(aliases)
                        .build());
            }

            responseObserver.onCompleted();
        }

        @Override
        public void scvGetCredentials(DominionGrpc.ScvGetCredentialsRequest request,
                                      StreamObserver<DominionGrpc.ScvGetCredentialsResponse> responseObserver) {
            if (!Objects.equals(request.getClientCredentials().getClientId(), CLIENT_ID) ||
                    !Objects.equals(request.getClientCredentials().getClientSecret(), CLIENT_SECRET)) {
                throw new StatusRuntimeException(Status.UNAUTHENTICATED);
            }

            if (!request.getAlias().equals(SUPPORTED_ALIAS)) {
                throw new StatusRuntimeException(Status.NOT_FOUND);
            }

            responseObserver.onNext(DominionGrpc.ScvGetCredentialsResponse
                    .newBuilder()
                    .setUser(USERNAME)
                    .setPassword(PASSWORD)
                    .build());

            responseObserver.onCompleted();
        }

        @Override
        public void scvSetCredentials(DominionGrpc.ScvSetCredentialsRequest request,
                                      StreamObserver<DominionGrpc.ScvSetCredentialsResponse> responseObserver) {
            if (!Objects.equals(request.getClientCredentials().getClientId(), CLIENT_ID) ||
                    !Objects.equals(request.getClientCredentials().getClientSecret(), CLIENT_SECRET)) {
                throw new StatusRuntimeException(Status.UNAUTHENTICATED);
            }

            // The Dominion impl doesn't currently support this call so we won't support it here either
            throw new StatusRuntimeException(Status.UNIMPLEMENTED);
        }
    }

}
