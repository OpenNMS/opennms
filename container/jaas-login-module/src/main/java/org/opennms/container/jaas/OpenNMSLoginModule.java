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
package org.opennms.container.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.netmgt.config.users.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenNMSLoginModule extends AbstractKarafLoginModule {
    private static final transient Logger LOG = LoggerFactory.getLogger(OpenNMSLoginModule.class);

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
        LOG.info("OpenNMS Login Module initializing: subject={}, callbackHandler={}, sharedState={}, options={}", subject, callbackHandler, sharedState, options);
        super.initialize(subject, callbackHandler, options);
    }

    @Override
    public boolean login() throws LoginException {
        final NameCallback nameCallback = new NameCallback("Username: ");
        final PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        try {
            callbackHandler.handle(new Callback[] { nameCallback, passwordCallback });
        } catch (final IOException e) {
            LOG.debug("I/O exception while prompting for a username and password.", e);
            throw new LoginException(e.getMessage());
        } catch (final UnsupportedCallbackException e) {
            LOG.debug("Username or password prompt is not supported.", e);
            throw new LoginException(e.getMessage() + " not available to obtain login information.");
        }

        user = nameCallback.getName();
        if (user == null) {
            throw new LoginException("Username cannot be null.");
        }

        final char[] passwordChars = passwordCallback.getPassword();
        if (passwordChars == null) {
            throw new LoginException("Password cannot be null.");
        }

        final UserConfig userConfig = userConfig();
        if (userConfig == null) {
            throw new LoginException("The OpenNMS UserConfig service is unavailable.");
        }

        final User configUser;
        final String password = new String(passwordChars);
        passwordCallback.clearPassword();
        try {
            configUser = userConfig.getUser(user);
            if (configUser == null) {
                throw new FailedLoginException("User " + user + " does not exist.");
            }
            if (!userConfig.comparePasswords(user, password)) {
                throw new FailedLoginException("Login failed: passwords did not match.");
            }
        } catch (final LoginException e) {
            throw e;
        } catch (final Exception e) {
            final LoginException loginException = new LoginException(
                    "Failed to retrieve user " + user + " from OpenNMS UserConfig.");
            loginException.initCause(e);
            throw loginException;
        }

        principals = createPrincipals(configUser);
        if (!hasAdminRole(configUser)) {
            throw new LoginException("User " + user + " is not an administrator! OSGi console access is forbidden.");
        }

        succeeded = true;
        LOG.debug("Successfully logged in {}.", user);
        return true;
    }

    protected UserConfig userConfig() {
        return JaasSupport.getUserConfig();
    }

    private Set<Principal> createPrincipals(final User configUser) {
        final Set<Principal> principals = new HashSet<>();
        for (String configuredRole : configUser.getRoles()) {
            final String role = normalizeRole(configuredRole);
            principals.add(new RolePrincipal(role));
            principals.add(new RolePrincipal(role.toLowerCase(Locale.ROOT)));
            principals.add(new RolePrincipal(configuredRole));
        }
        LOG.debug("Created principals from user roles {}: {}", configUser.getRoles(), principals);
        return principals;
    }

    private boolean hasAdminRole(final User configUser) {
        return configUser.getRoles().stream()
                .map(OpenNMSLoginModule::normalizeRole)
                .anyMatch("admin"::equalsIgnoreCase);
    }

    private static String normalizeRole(final String role) {
        return role.replaceFirst("^[Rr][Oo][Ll][Ee]_", "");
    }
}
