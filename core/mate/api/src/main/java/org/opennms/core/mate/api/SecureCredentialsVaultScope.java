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
package org.opennms.core.mate.api;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.features.scv.api.SecureCredentialsVault;

import com.google.common.base.Strings;

public class SecureCredentialsVaultScope implements Scope {
    public static final String CONTEXT = "scv";
    
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    private final SecureCredentialsVault scv;

    public SecureCredentialsVaultScope(final SecureCredentialsVault scv) {
        this.scv = Objects.requireNonNull(scv);
    }

    @Override
    public Optional<ScopeValue> get(final ContextKey contextKey) {
        if (!CONTEXT.equals(contextKey.context)) {
            return Optional.empty();
        }

        final var split = contextKey.key.indexOf(':');
        if (split == -1) {
            return Optional.empty();
        }

        final var alias = contextKey.key.substring(0, split);
        final var attr = contextKey.key.substring(split + 1);
        
        if (Strings.isNullOrEmpty(alias) || Strings.isNullOrEmpty(attr)) {
            return Optional.empty();
        }

        final var cred = this.scv.getCredentials(alias);
        if (cred == null) {
            return Optional.empty();
        }

        final String value;
        if (USERNAME.equals(attr)) {
             value = cred.getUsername();
        } else if (PASSWORD.equals(attr)) {
            value = cred.getPassword();
        } else {
            value = cred.getAttribute(attr);
        }

        if (value == null) {
            return  Optional.empty();
        }

        return Optional.of(new ScopeValue(ScopeName.GLOBAL, value));
    }

    @Override
    public Set<ContextKey> keys() {
        return this.scv.getAliases().stream()
                .flatMap(alias -> Stream.concat(
                        Stream.of(USERNAME, PASSWORD),
                        this.scv.getCredentials(alias).getAttributeKeys().stream())
                                        .map(attr -> new ContextKey(CONTEXT, String.format("%s:%s", alias, attr))))
                .collect(Collectors.toSet());
    }
}
