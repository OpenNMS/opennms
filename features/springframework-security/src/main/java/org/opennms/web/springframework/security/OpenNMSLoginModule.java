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

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.opennms.netmgt.config.api.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

public class OpenNMSLoginModule implements LoginModule, LoginHandler, OpenNMSLoginHandler {
    private static final transient Logger LOG = LoggerFactory.getLogger(OpenNMSLoginModule.class);

    private static transient volatile UserConfig m_userConfig;
    private static transient volatile SpringSecurityUserDao m_springSecurityUserDao;

    protected Subject m_subject;
    protected CallbackHandler m_callbackHandler;
    protected Map<String, ?> m_sharedState;
    protected Map<String, ?> m_options;

    protected String m_user;
    protected Set<Principal> m_principals = new HashSet<>();

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        LOG.info("OpenNMS Login Module initializing.");
        m_subject = subject;
        m_callbackHandler = callbackHandler;
        m_sharedState = sharedState;
        m_options = options;
    }

    @Override
    public boolean login() throws LoginException {
        return LoginModuleUtils.doLogin(this, m_subject, m_sharedState, m_options);
    }

    @Override
    public boolean abort() throws LoginException {
        LOG.debug("Aborting {} login.", m_user);
        m_user = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        LOG.debug("Logging out user {}.", m_user);
        m_subject.getPrincipals().removeAll(m_principals);
        m_principals.clear();
        return true;
    }

    public static synchronized UserConfig getUserConfig() {
        return m_userConfig;
    }

    public static synchronized void setUserConfig(final UserConfig userConfig) {
        m_userConfig = userConfig;
    }

    public static synchronized SpringSecurityUserDao getSpringSecurityUserDao() {
        return m_springSecurityUserDao;
    }

    public static synchronized void setSpringSecurityUserDao(final SpringSecurityUserDao userDao) {
        m_springSecurityUserDao = userDao;
    }

    @Override
    public boolean commit() throws LoginException {
        final Set<Principal> principals = principals();
        if (principals.isEmpty()) {
            return false;
        }
        m_subject.getPrincipals().addAll(principals);
        return true;
    }

    public CallbackHandler callbackHandler() {
        return m_callbackHandler;
    }

    @Override
    public UserConfig userConfig() {
        return m_userConfig;
    }

    @Override
    public SpringSecurityUserDao springSecurityUserDao() {
        return m_springSecurityUserDao;
    }

    @Override
    public String user() {
        return m_user;
    }

    @Override
    public void setUser(final String user) {
        m_user = user;
    }

    public Set<Principal> createPrincipals(final GrantedAuthority authority) {
        return Collections.singleton(new AuthorityPrincipal(authority));
    }

    @Override
    public Set<Principal> principals() {
        return m_principals;
    }

    @Override
    public void setPrincipals(final Set<Principal> principals) {
        m_principals = principals;
    }

    @Override
    public boolean requiresAdminRole() {
        // this LoginHandler is used for JMX access, allow JMX to handle checking roles for authority
        return false;
    }
}
