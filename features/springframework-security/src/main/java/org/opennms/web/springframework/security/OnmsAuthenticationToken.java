package org.opennms.web.springframework.security;

import org.opennms.netmgt.model.OnmsUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;

final class OnmsAuthenticationToken extends AbstractAuthenticationToken {
    private static final long serialVersionUID = -5896244818836123481L;
    private final OnmsUser m_user;

    OnmsAuthenticationToken(final OnmsUser user) {
        super(user.getAuthorities());
        m_user = user;
        setAuthenticated(true);
    }

    /**
     * This should always be a UserDetails. Java-Spec allows this,
     * spring can handle it and it's easier for us this way.
     */
    @Override
    public Object getPrincipal() {
        return m_user;
    }

    @Override
    public Object getCredentials() {
        return m_user.getPassword();
    }
}