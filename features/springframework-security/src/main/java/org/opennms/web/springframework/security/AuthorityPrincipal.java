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
