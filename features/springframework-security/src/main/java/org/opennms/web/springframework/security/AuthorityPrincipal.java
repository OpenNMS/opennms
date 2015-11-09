package org.opennms.web.springframework.security;

import java.io.Serializable;
import java.security.Principal;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AuthorityPrincipal other = (AuthorityPrincipal) obj;
        if (m_name == null) {
            if (other.m_name != null) {
                return false;
            }
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        return true;
    }

}
