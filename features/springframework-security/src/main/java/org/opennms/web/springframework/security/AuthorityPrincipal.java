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
package org.opennms.web.springframework.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;

public class AuthorityPrincipal implements Principal, Serializable {
    private static final long serialVersionUID = 1L;
    private final String m_name;

    public AuthorityPrincipal() {
        m_name = null;
    }
    public AuthorityPrincipal(final GrantedAuthority authority) {
        m_name = authority.getAuthority().toLowerCase().replaceFirst("^role_", "");
    }

    @Override
    public String getName() {
        return m_name;
    }


    @Override
    public int hashCode() {
        return Objects.hash(m_name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof AuthorityPrincipal) {
            final AuthorityPrincipal other = (AuthorityPrincipal) obj;
            return Objects.equals(m_name, other.m_name);
        }
        return false;
    }

}
