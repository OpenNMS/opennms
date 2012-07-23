/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.springframework.security;

import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

public class HybridOpenNMSUserAuthenticationProvider implements AuthenticationProvider, InitializingBean {
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
        final String username = authentication.getPrincipal().toString();
        final String password = authentication.getCredentials().toString();
        final OnmsUser user = m_userDao.getByUsername(username);

        boolean hasUser = false;

        if (user == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        try {
            hasUser = m_userManager.hasUser(user.getUsername());
        } catch (final Exception e) {
            throw new AuthenticationServiceException("An error occurred while checking for " + username + " in the UserManager", e);
        }
        if (hasUser) {
            if (!m_userManager.comparePasswords(username, password)) {
                throw new BadCredentialsException("Bad credentials");
            }
        } else {
            if (!m_userManager.checkSaltedPassword(password, user.getPassword())) {
                throw new BadCredentialsException("Bad credentials");
            }
        }

        if (user.getAuthorities().size() == 0) {
            user.addAuthority(SpringSecurityUserDao.ROLE_USER);
        }

        final AbstractAuthenticationToken token = new AbstractAuthenticationToken(user.getAuthorities()) {
            private static final long serialVersionUID = 3659409846867741010L;

            /**
             * This should always be a UserDetails. Java-Spec allows this,
             * spring can handle it nad it's easier for us this way.
             */
            @Override
            public Object getPrincipal() {
                return user;
            }

            @Override
            public Object getCredentials() {
                return user.getPassword();
            }
        };
        token.setAuthenticated(true);

        return token;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean supports(final Class authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}