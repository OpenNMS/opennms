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
            LOG.warn("User not found: " + authUsername);
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
        } catch (final Throwable e) {
            throw new AuthenticationServiceException("An error occurred while checking for " + authUsername + " in the UserManager", e);
        }

        if (hasUser) {
            if (!m_userManager.comparePasswords(authUsername, authPassword)) {
                LOG.warn("Password auth failed for user: " + authUsername);
                throw new BadCredentialsException("Bad credentials");
            }
        } else {
            if (!m_userManager.checkSaltedPassword(authPassword, existingPassword)) {
                LOG.warn("Salted password auth failed for user: " + authUsername);
                throw new BadCredentialsException("Bad credentials");
            }
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
