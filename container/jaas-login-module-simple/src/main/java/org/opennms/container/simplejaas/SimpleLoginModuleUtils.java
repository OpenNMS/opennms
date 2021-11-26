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

package org.opennms.container.simplejaas;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.netmgt.config.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class SimpleLoginModuleUtils {
    public static volatile Logger LOG = LoggerFactory.getLogger(SimpleLoginModuleUtils.class);

    protected SimpleLoginModuleUtils() {}

    public static boolean doLogin(final SimpleOpenNMSLoginHandler handler, final Subject subject, final Map<String, ?> sharedState, final Map<String, ?> options) throws LoginException {
        LOG.debug("OpenNMSLoginModule: login(): handler={}, subject={}, sharedState={}, options={}", handler.getClass(), subject.getClass(), sharedState, options);
        final Callback[] callbacks = new Callback[2];

        // A U T H E N T I C A T I O N

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
        final String password = new String(((PasswordCallback) callbacks[1]).getPassword());
        if (password == null) {
            final String msg = "Password can not be null.";
            LOG.debug(msg);
            throw new LoginException(msg);
        }

        final UserConfig userConfig = handler.userConfig();
        if (userConfig==null) {
            final String message = "Could not retrieve UserConfig.";
            LOG.error(message);
            throw new LoginException(message);
        }
        // file for userConfig = /Users/markcbordelon/Documents/clodovicus/opennms/opennms/opennms-base-assembly/target/classes/etc/users.xml

        final User configUser;
        try {
            configUser = userConfig.getUser(user);
        } catch (IOException e) {
            final String msg = "Could not retrieve " + user + " from OpenNMS UserConfig.";
            LOG.error(msg);
            throw new FailedLoginException(msg);
        }

        if (configUser == null) {
            final String msg = "User " + user + " does not exist in OpenNMS UserConfig.";
            LOG.debug(msg);
            throw new FailedLoginException(msg);
        }

        if (!userConfig.comparePasswords(user, password)) {
            final String msg = "Login failed: passwords did not match for User "+ user + " in OpenNMS UserConfig.";
            LOG.debug(msg);
            throw new FailedLoginException(msg);
        };


        //  logon  "A U T H O R I Z A T I O N"

        boolean allowed = true; // except if handler requires admin and user is not admin
        /*
        final SpringSecurityUser onmsUser;
        try {
            //onmsUser = handler.springSecurityUserDao().getByUsername(user);
        } catch (final Exception e) {
            final String message = "Could not retrieve OnmsCser " + onmsUser + " via userDao.";
            LOG.debug(message, e);
            throw new LoginException(message);
        }
        */

        // must set principals to prevent commit phrase from failing
        final Set<Principal> principals = SimpleLoginModuleUtils.createPrincipals(handler, configUser);
        handler.setPrincipals(principals);

        if (handler.requiresAdminRole()) {
            allowed = false;
            for (final Principal principal : principals) {
                final String name = principal.getName().toLowerCase().replaceAll("^role_", "");
                if ("admin".equals(name)) {
                    allowed = true;
                    break;
                }
            }
        }

        if (!allowed) {
            final String msg = "User " + user + " is not an administrator!  OSGi console access is forbidden.";
            LOG.debug(msg);
            throw new LoginException(msg);
        }

        LOG.debug("Successfully logged in {}.", user);
        return true; // in the LoginContext, after the LOGIN method, the COMMIT throws an exc which invokes Abort.
    }

    /*
    public static Set<Principal> createPrincipals(final SimpleLoginHandler handler, final Collection<? extends GrantedAuthority> collection) {
        final Set<Principal> principals = new HashSet<>();
        for (final GrantedAuthority auth : collection) {
            final Set<Principal> ps = handler.createPrincipals(auth);
            LOG.debug("granted authority: {}, principals: {}", auth, ps.stream().map(Principal::getName).toArray());
            principals.addAll(ps);
        }
        return principals;
    }
    */
    private static Set<Principal> createPrincipals(SimpleOpenNMSLoginHandler handler, User configUser) {
        final Set<Principal> principals = new LinkedHashSet<>();
        for (final String role : configUser.getRoles()) {
            principals.add(new RolePrincipal(role));
        }
        return principals;
    }

}
