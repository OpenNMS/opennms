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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.model.OnmsUser;
import org.opennms.web.api.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

/**
 * Implements the interface to allow the servlet container to check our users.xml file to authenticate users.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:eric@tuxbot.com">Eric Molitor</A>
 * @author <A HREF="mailto:agalue@opennms.org">Alejandro Galue</A>
 */
public class SpringSecurityUserDaoImpl implements SpringSecurityUserDao, InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SpringSecurityUserDaoImpl.class);

    /** The user manager. */
    private UserManager m_userManager;

    /** The users configuration file. */
    private String m_usersConfigurationFile;

    /** The set of valid users from users.xml, keyed by userId. */
    private Map<String, OnmsUser> m_users = null;

    /** The m users last modified. */
    private long m_usersLastModified;

    /** The security roles map. */
    private Map<String,List<GrantedAuthority>> m_roles = null;

    /** The authorities map. */
    private Map<String,GrantedAuthority> m_authorities = new HashMap<>();

    /**
     * Instantiates a new spring security user DAO implementation.
     */
    public SpringSecurityUserDaoImpl() {
    }

    /**
     * Parses the users.
     * 
     * <p>Convenience method for parsing the users.xml file.</p>
     * <p>This method is synchronized so only one thread at a time
     * can parse the users.xml file and create the <code>principal</code>
     * instance variable.</p>
     *
     * @throws DataRetrievalFailureException the data retrieval failure exception
     */
    private void parseUsers() throws DataRetrievalFailureException {
        final HashMap<String, OnmsUser> users = new HashMap<String, OnmsUser>();
        final Map<String, List<GrantedAuthority>> roles = new HashMap<String, List<GrantedAuthority>>();

        try {
            for (final OnmsUser user : m_userManager.getOnmsUserList()) {
                final String username = user.getUsername();
                users.put(username, user);
                if (!roles.containsKey(username)) {
                    roles.put(username, new LinkedList<GrantedAuthority>());
                }
                for (final String role : user.getRoles()) {
                    if (Authentication.isValidRole(role)) {
                        roles.get(username).add(getAuthority(role));
                        if (Authentication.ROLE_ADMIN.equals(role)) {
                            roles.get(username).add(getAuthority(Authentication.ROLE_USER));
                        }
                    }
                }
            }
        } catch (final Throwable t) {
            throw new DataRetrievalFailureException("Unable to get user list.", t);
        }

        LOG.debug("Loaded the users.xml file with {} users", users.size());

        m_usersLastModified = m_userManager.getLastModified();
        m_users = users;
        m_roles = roles;
    }

    /**
     * Gets the authority.
     *
     * @param role the role
     * @return the authority
     */
    protected GrantedAuthority getAuthority(final String role) {
        if (!m_authorities.containsKey(role)) {
            m_authorities.put(role, new SimpleGrantedAuthority(role));
        }
        return m_authorities.get(role);
    }

    /**
     * Gets the authorities by username.
     *
     * @param username the username
     * @return the authorities by username
     */
    protected Collection<? extends GrantedAuthority> getAuthoritiesByUsername(final String username) {
        if (m_roles.containsKey(username)) {
            final Collection<? extends GrantedAuthority> roles = m_roles.get(username);
            if (!roles.isEmpty()) {
                LOG.debug("User {} has roles: {}", username, roles);
                return roles;
            }
        } 
        final List<GrantedAuthority> roles = Arrays.asList(new GrantedAuthority[] { ROLE_USER });
        LOG.debug("User {} has roles: {}", username, roles);
        return roles;

    }

    /**
     * Checks the last modified time of the user file against
     * the last known last modified time. If the times are different, then the
     * file must be reparsed.
     * 
     * <p>
     * Note that the <code>lastModified</code> variables are not set here.
     * This is in case there is a problem parsing either file. If we set the
     * value here, and then try to parse and fail, then we will not try to parse
     * again until the file changes again. Instead, when we see the file
     * changes, we continue parsing attempts until the parsing succeeds.
     * </p>
     *
     * @return true, if is users parse necessary
     */
    private boolean isUsersParseNecessary() {
        if (m_users == null) {
            return true;
        } else {
            try {
                m_userManager.update();
            } catch (final Throwable t) {
                LOG.warn("Attempting to update users failed because: {}. Continuing with previous cache of users.", t.getLocalizedMessage());
            }
            return m_usersLastModified != m_userManager.getLastModified();
        }
    }

    /**
     * Sets the users configuration file.
     *
     * @param usersConfigurationFile the new users configuration file
     */
    public void setUsersConfigurationFile(String usersConfigurationFile) {
        m_usersConfigurationFile = usersConfigurationFile;
        UserFactory.setInstance(null);
    }

    /**
     * Gets the users configuration file.
     *
     * @return the users configuration file
     */
    public String getUsersConfigurationFile() {
        return m_usersConfigurationFile;
    }

    /* (non-Javadoc)
     * @see org.opennms.web.springframework.security.SpringSecurityUserDao#getByUsername(java.lang.String)
     */
    @Override
    public SpringSecurityUser getByUsername(String username) {
        reloadIfNecessary();

        final OnmsUser user = m_users.get(username);
        if (user == null) {
            return null;
        }

        final SpringSecurityUser springUser = new SpringSecurityUser(user);
        springUser.setAuthorities(getAuthoritiesByUsername(username));
        return springUser;
    }

    /**
     * Reload if necessary.
     */
    private void reloadIfNecessary() {
        if (isUsersParseNecessary()) {
            parseUsers();
        }
    }

    /**
     * Gets the users last modified.
     *
     * @return the users last modified
     */
    public long getUsersLastModified() {
        return m_usersLastModified;
    }

    /**
     * Gets the user manager.
     *
     * @return the user manager
     */
    public UserManager getUserManager() {
        return m_userManager;
    }

    /**
     * Sets the user manager.
     *
     * @param mgr the new user manager
     */
    public void setUserManager(final UserManager mgr) {
        m_userManager = mgr;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_usersConfigurationFile != null, "usersConfigurationFile parameter must be set to the location of the users.xml configuration file");
        Assert.notNull(m_userManager);
    }
}
