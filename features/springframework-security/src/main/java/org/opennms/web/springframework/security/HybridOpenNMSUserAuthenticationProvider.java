/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.springframework.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.UserManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public class HybridOpenNMSUserAuthenticationProvider implements AuthenticationProvider, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(HybridOpenNMSUserAuthenticationProvider.class);
    private UserManager m_userManager = null;
    private SpringSecurityUserDao m_userDao = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_userManager);
        Assert.notNull(m_userDao);
    }
    
    public UserManager getUserManager() {
        return m_userManager;
    }
    
    public void setUserManager(final UserManager userManager) {
        m_userManager = userManager;
    }

    public SpringSecurityUserDao getUserDao() {
        return m_userDao;
    }
    
    public void setUserDao(final SpringSecurityUserDao userDao) {
        m_userDao = userDao;
    }
    
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String authUsername = authentication.getPrincipal().toString();
        final String authPassword = authentication.getCredentials().toString();
        final SpringSecurityUser user = m_userDao.getByUsername(authUsername);

        if (user == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        try {
            checkUserPassword(authUsername, authPassword, user);
        } catch (final AuthenticationException e) {
            // if we fail, try refreshing the user manager and re-authenticate
            try {
                m_userManager.reload();
            } catch (final Exception reloadException) {
                LOG.debug("Failed to reload UserManager.", reloadException);
            }
            checkUserPassword(authUsername, authPassword, user);
        }

        if (user.getAuthorities().size() == 0) {
            user.addAuthority(SpringSecurityUserDao.ROLE_USER);
        }

        return new OnmsAuthenticationToken(user);
    }

    protected void checkUserPassword(final String authUsername, final String authPassword, final SpringSecurityUser user) throws AuthenticationException {
        final String existingPassword = user.getPassword();
        boolean hasUser = false;
        try {
            hasUser = m_userManager.hasUser(user.getUsername());
        } catch (final Exception e) {
            throw new AuthenticationServiceException("An error occurred while checking for " + authUsername + " in the UserManager", e);
        }

        if (hasUser) {
            if (!m_userManager.comparePasswords(authUsername, authPassword)) {
                throw new BadCredentialsException("Bad credentials");
            }
        } else {
            if (!m_userManager.checkSaltedPassword(authPassword, existingPassword)) {
                throw new BadCredentialsException("Bad credentials");
            }
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
