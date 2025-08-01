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
package org.opennms.features.apilayer.common.scv;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.scv.Credentials;
import org.opennms.integration.api.v1.scv.SecureCredentialsVault;
import org.opennms.integration.api.v1.scv.immutables.ImmutableCredentials;

/** Exposes SecureCredentialsVault via Integration API */
public class SecureCredentialsVaultImpl implements SecureCredentialsVault {

    static final String OIA_PREFIX = "_oia_"; //prefix to prevent plugin access system keys, must be lower key.

    private final org.opennms.features.scv.api.SecureCredentialsVault delegate;

    public SecureCredentialsVaultImpl(org.opennms.features.scv.api.SecureCredentialsVault delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public Set<String> getAliases() {
        return delegate
                .getAliases()
                .stream()
                .filter(s -> s.startsWith(OIA_PREFIX))
                .map(s -> s.substring(OIA_PREFIX.length()))
                .collect(Collectors.toSet());
    }

    @Override
    public Credentials getCredentials(String alias) {
        Objects.requireNonNull(alias);
        return Optional.ofNullable(delegate.getCredentials(OIA_PREFIX + alias.toLowerCase()))
                .map(c -> new ImmutableCredentials(c.getUsername(), c.getPassword(), c.getAttributes()))
                .orElse(null);
    }

    @Override
    public void setCredentials(String alias, Credentials credentials) {
        Objects.requireNonNull(alias);
        Objects.requireNonNull(credentials);
        this.delegate.setCredentials(OIA_PREFIX + alias.toLowerCase(), new org.opennms.features.scv.api.Credentials(
                credentials.getUsername(),
                credentials.getPassword(),
                credentials.getAttributes())
        );
    }

    @Override
    public void deleteCredentials(String alias) {
        Objects.requireNonNull(alias);
        this.delegate.deleteCredentials(OIA_PREFIX + alias.toLowerCase());
    }
}
