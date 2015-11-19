package org.opennms.web.springframework.security;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.callback.CallbackHandler;

import org.opennms.netmgt.config.api.UserConfig;
import org.springframework.security.core.GrantedAuthority;

public interface LoginHandler {

    public CallbackHandler callbackHandler();
    public UserConfig userConfig();
    public SpringSecurityUserDao springSecurityUserDao();

    public Set<Principal> createPrincipals(final GrantedAuthority authority);
    public String user();
    public void setUser(final String user);

    public Set<Principal> principals();
    public void setPrincipals(final Set<Principal> principals);

}
