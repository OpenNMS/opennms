package org.opennms.web.springframework.security;

import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationServiceException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

public class HybridOpenNMSUserAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private UserManager m_userManager = null;

    @Override
    protected void doAfterPropertiesSet() throws Exception {
        Assert.notNull(m_userManager);
    }
    
    public UserManager getUserManager() {
        return m_userManager;
    }
    
    public void setUserManager(final UserManager userManager) {
        m_userManager = userManager;
    }

    @Override
    protected void additionalAuthenticationChecks(final UserDetails userDetails, final UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        final OnmsUser user;
        if (userDetails instanceof OnmsUser) {
            user = (OnmsUser)userDetails;
        } else {
            try {
                user = m_userManager.getOnmsUser(userDetails.getUsername());
            } catch (final Exception e) {
                throw new AuthenticationServiceException("Unable to retrieve " + userDetails.getUsername() + " from the UserManager", e);
            }
        }

        if (!m_userManager.comparePasswords(userDetails.getPassword(), authentication.getCredentials().toString())) {
            throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"), userDetails);
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // TODO Auto-generated method stub
        return null;
    }

}
