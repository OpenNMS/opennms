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
package org.opennms.features.scv.dominion.grpc;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.opennms.dominion.local.rpc.grpc.DominionGrpc;
import org.opennms.features.minion.dominion.grpc.DominionScvGrpcClient;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

public class DominionSecureCredentialsVault implements SecureCredentialsVault {

    private final DominionScvGrpcClient client;

    public DominionSecureCredentialsVault(DominionScvGrpcClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public Set<String> getAliases() {
        return new HashSet<>(client.getAliases().getAliasesList());
    }

    @Override
    public Credentials getCredentials(String alias) {
        Objects.requireNonNull(alias);
        DominionGrpc.ScvGetCredentialsResponse response = client.getCredentials(alias);

        return new Credentials(response.getUser(), response.getPassword(), response.getAttributesMap());
    }

    @Override
    public void setCredentials(String alias, Credentials credentials) {
        Objects.requireNonNull(alias);
        Objects.requireNonNull(credentials);
        
        client.setCredentials(alias, credentials.getUsername(), credentials.getPassword(),
                credentials.getAttributes());
    }

    @Override
    public void deleteCredentials(final String alias) {
        Objects.requireNonNull(alias);
        throw new IllegalStateException("Not implemented yet");
    }
}
