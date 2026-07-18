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

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.junit.Test;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.netmgt.config.users.User;

public class OpenNMSLoginModuleTest {
    @Test
    public void authenticatesAdminUsingOnlyUserConfig() throws Exception {
        final UserConfig userConfig = userConfig("admin", "secret", "ROLE_ADMIN", "ROLE_USER");
        final Subject subject = new Subject();
        final OpenNMSLoginModule module = loginModule(userConfig, subject, "admin", "secret");

        assertTrue(module.login());
        assertTrue(module.commit());
        assertTrue(subject.getPrincipals(RolePrincipal.class).stream()
                .anyMatch(principal -> "admin".equals(principal.getName())));
        assertTrue(subject.getPrincipals(RolePrincipal.class).stream()
                .anyMatch(principal -> "ROLE_ADMIN".equals(principal.getName())));
    }

    @Test
    public void rejectsNonAdminUsers() throws Exception {
        final UserConfig userConfig = userConfig("operator", "secret", "ROLE_USER");
        final OpenNMSLoginModule module = loginModule(userConfig, new Subject(), "operator", "secret");

        final LoginException exception = assertThrows(LoginException.class, module::login);
        assertTrue(exception.getMessage().contains("is not an administrator"));
    }

    @Test
    public void rejectsInvalidPasswords() throws Exception {
        final UserConfig userConfig = userConfig("admin", "secret", "ROLE_ADMIN");
        final OpenNMSLoginModule module = loginModule(userConfig, new Subject(), "admin", "wrong");

        assertThrows(FailedLoginException.class, module::login);
    }

    private static UserConfig userConfig(final String username, final String password, final String... roles)
            throws Exception {
        final User user = new User();
        user.setRoles(List.of(roles));

        final UserConfig userConfig = mock(UserConfig.class);
        when(userConfig.getUser(username)).thenReturn(user);
        when(userConfig.comparePasswords(username, password)).thenReturn(true);
        return userConfig;
    }

    private static OpenNMSLoginModule loginModule(final UserConfig userConfig, final Subject subject,
            final String username, final String password) {
        final OpenNMSLoginModule module = new OpenNMSLoginModule() {
            @Override
            protected UserConfig userConfig() {
                return userConfig;
            }
        };
        module.initialize(subject, callbackHandler(username, password), Map.of(), Map.of());
        return module;
    }

    private static CallbackHandler callbackHandler(final String username, final String password) {
        return callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        };
    }
}
