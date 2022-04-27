/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.utils.mate;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import org.opennms.features.scv.api.SecureCredentialsVault;

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
