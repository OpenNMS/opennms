/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.opennms.netmgt.config.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

public abstract class LoginModuleUtils {
    public static volatile Logger LOG = LoggerFactory.getLogger(LoginModuleUtils.class);

    protected LoginModuleUtils() {}

    public static boolean doLogin(final OpenNMSLoginHandler handler, final Subject subject, final Map<String, ?> sharedState, final Map<String, ?> options) throws LoginException {
        LOG.debug("OpenNMSLoginModule: login(): handler={}, subject={}, sharedState={}, options={}", handler.getClass(), subject.getClass(), sharedState, options);
        final Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            handler.callbackHandler().handle(callbacks);
        } catch (final IOException ioe) {
            LOG.debug("IO exception while attempting to prompt for username and password.", ioe);
            throw new LoginException(ioe.getMessage());
        } catch (final UnsupportedCallbackException uce) {
            LOG.debug("Username or password prompt not supported.", uce);
            throw new LoginException(uce.getMessage() + " not available to obtain information from user.");
        }

        final String user = ((NameCallback) callbacks[0]).getName();
        handler.setUser(user);
        if (user == null) {
            final String msg = "Username can not be null.";
            LOG.debug(msg);
            throw new LoginException(msg);
        }

        // password callback get value
        if (((PasswordCallback) callbacks[1]).getPassword() == null) {
            final String msg = "Password can not be null.";
            LOG.debug(msg);
            throw new LoginException(msg);
        }
        final String password = new String(((PasswordCallback) callbacks[1]).getPassword());

        final User configUser;
        final SpringSecurityUser onmsUser;
        try {
            configUser = handler.userConfig().getUser(user);
            onmsUser = handler.springSecurityUserDao().getByUsername(user);
        } catch (final Exception e) {
            final String message = "Failed to retrieve user " + user + " from OpenNMS UserConfig.";
            LOG.debug(message, e);
            throw new LoginException(message);
        }

        if (configUser == null) {
            final String msg = "User  " + user + " does not exist.";
            LOG.debug(msg);
            throw new FailedLoginException(msg);
        }

        if (!handler.userConfig().comparePasswords(user, password)) {
            final String msg = "Login failed: passwords did not match.";
            LOG.debug(msg);
            throw new FailedLoginException(msg);
        };

        boolean allowed = true;
        final Set<Principal> principals = LoginModuleUtils.createPrincipals(handler, onmsUser.getAuthorities());
        handler.setPrincipals(principals);

        if (handler.requiresAdminRole()) {
            allowed = false;
            for (final Principal principal : principals) {
                final String name = principal.getName().toLowerCase().replaceAll("^role_", "");
                if ("admin".equals(name)) {
                    allowed = true;
                }
            }
        }

        if (!allowed) {
            final String msg = "User " + user + " is not an administrator!  OSGi console access is forbidden.";
            LOG.debug(msg);
            throw new LoginException(msg);
        }
        LOG.debug("Successfully logged in {}.", user);
        return true;
    }
    
    public static Set<Principal> createPrincipals(final LoginHandler handler, final Collection<? extends GrantedAuthority> collection) {
        final Set<Principal> principals = new HashSet<>();
        for (final GrantedAuthority auth : collection) {
            final Set<Principal> ps = handler.createPrincipals(auth);
            LOG.debug("granted authority: {}, principals: {}", auth, ps.stream().map(Principal::getName).toArray());
            principals.addAll(ps);
        }
        return principals;
    }
}
