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
