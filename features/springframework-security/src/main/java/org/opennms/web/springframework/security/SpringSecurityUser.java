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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.model.OnmsUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Wrapper class for the OnmsUser object that implements
 * Spring's UserDetails interface.
 *
 * @author jwhite
 */
public class SpringSecurityUser implements UserDetails {

    private static final long serialVersionUID = 7736070473646649732L;

    private final OnmsUser m_user;

    private Collection<? extends GrantedAuthority> m_authorities;

    public SpringSecurityUser(OnmsUser user) {
        m_user = user;
    }

    @Override
    public String getUsername() {
        return m_user.getUsername();
    }

    @Override
    public String getPassword() {
        return m_user.getPassword();
    }

    public String getFullName() {
        return m_user.getFullName();
    }

    public String getComments() {
        return m_user.getComments();
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        m_authorities = authorities;
        
    }

    public void addAuthority(GrantedAuthority authority) {
        final Set<GrantedAuthority> authorities = new HashSet<>();
        if (m_authorities != null) authorities.addAll(m_authorities);
        authorities.add(authority);
        m_authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return m_authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_authorities == null) ? 0 : m_authorities.hashCode());
        result = prime * result + ((m_user == null) ? 0 : m_user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpringSecurityUser other = (SpringSecurityUser) obj;
        if (m_authorities == null) {
            if (other.m_authorities != null)
                return false;
        } else if (!m_authorities.equals(other.m_authorities))
            return false;
        if (m_user == null) {
            if (other.m_user != null)
                return false;
        } else if (!m_user.equals(other.m_user))
            return false;
        return true;
    }
}
