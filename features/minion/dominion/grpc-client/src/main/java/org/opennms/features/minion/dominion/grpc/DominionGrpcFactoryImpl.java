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

import java.util.Objects;

import org.opennms.distributed.core.api.MinionIdentity;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DominionGrpcFactoryImpl implements DominionGrpcFactory {

    private final ManagedChannel channel;
    private final String clientId;
    private final String clientSecret;

    public DominionGrpcFactoryImpl(String host, String port, String clientSecret, MinionIdentity minionIdentity) {
        this(host, port, Objects.requireNonNull(minionIdentity).getId(), clientSecret);
    }

    public DominionGrpcFactoryImpl(String host, String port, String clientId, String clientSecret) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(port);
        Objects.requireNonNull(clientId);
        Objects.requireNonNull(clientSecret);

        if (clientSecret.isEmpty()) {
            throw new IllegalArgumentException("The client secret cannot be empty");
        }
        
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        channel = ManagedChannelBuilder.forAddress(host, Integer.parseInt(port))
                .usePlaintext()
                .build();
    }

    public void destroy() {
        channel.shutdown();
    }

    @Override
    public DominionScvGrpcClient scvClient() {
        return new DominionScvGrpcClient(clientId, clientSecret, channel);
    }

}
