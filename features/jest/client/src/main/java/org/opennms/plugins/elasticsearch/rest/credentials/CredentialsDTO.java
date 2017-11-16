/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest.credentials;

import java.util.Objects;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;

public class CredentialsDTO {
    private final Credentials credentials;
    private final AuthScope authScope;

    public CredentialsDTO(AuthScope authScope, Credentials credentials) {
        this.authScope = Objects.requireNonNull(authScope);
        this.credentials = Objects.requireNonNull(credentials);
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public AuthScope getAuthScope() {
        return authScope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CredentialsDTO that = (CredentialsDTO) o;
        final boolean equals = Objects.equals(credentials, that.credentials)
                && Objects.equals(authScope, that.authScope);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentials, authScope);
    }
}
