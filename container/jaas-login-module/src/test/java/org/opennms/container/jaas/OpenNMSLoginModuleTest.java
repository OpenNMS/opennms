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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.config.api.UserConfig;
import org.opennms.netmgt.config.users.User;

public class OpenNMSLoginModuleTest {
    @Rule
    public TemporaryFolder m_opennmsHome = new TemporaryFolder();

    @Before
    public void setUp() {
        // Keep Authentication from picking up a security-roles.properties outside of this test.
        System.setProperty("opennms.home", m_opennmsHome.getRoot().getAbsolutePath());
    }

    @After
    public void tearDown() {
        System.clearProperty("opennms.home");
    }

    @Test
    public void authenticatesAdminUsingOnlyUserConfig() throws Exception {
        final UserConfig userConfig = userConfig("admin", "secret", "ROLE_ADMIN", "ROLE_USER");
        final Subject subject = new Subject();
        final OpenNMSLoginModule module = loginModule(userConfig, subject, "admin", "secret");

        assertTrue(module.login());
        assertTrue(module.commit());
        assertEquals(Set.of("ADMIN", "admin", "ROLE_ADMIN", "USER", "user", "ROLE_USER"), roleNames(subject));
    }

    @Test
    public void grantsRoleUserToAdministrators() throws Exception {
        // ROLE_ADMIN implies ROLE_USER, as it does for the web user interface.
        final UserConfig userConfig = userConfig("admin", "secret", "ROLE_ADMIN");
        final Subject subject = new Subject();
        final OpenNMSLoginModule module = loginModule(userConfig, subject, "admin", "secret");

        assertTrue(module.login());
        assertTrue(module.commit());
        assertEquals(Set.of("ADMIN", "admin", "ROLE_ADMIN", "USER", "user", "ROLE_USER"), roleNames(subject));
    }

    @Test
    public void rejectsNonAdminUsers() throws Exception {
        final UserConfig userConfig = userConfig("operator", "secret", "ROLE_USER");
        final OpenNMSLoginModule module = loginModule(userConfig, new Subject(), "operator", "secret");

        final LoginException exception = assertThrows(LoginException.class, module::login);
        assertTrue(exception.getMessage().contains("is not an administrator"));
    }

    @Test
    public void rejectsRolesThatOnlyResembleTheAdminRole() throws Exception {
        // None of these are valid OpenNMS roles, so none of them may unlock the OSGi console.
        for (final String role : List.of("admin", "Admin", "role_admin", "ROLE_ADMINISTRATOR")) {
            final UserConfig userConfig = userConfig("someone", "secret", role);
            final OpenNMSLoginModule module = loginModule(userConfig, new Subject(), "someone", "secret");

            final LoginException exception = assertThrows("role " + role + " must not grant console access",
                    LoginException.class, module::login);
            assertTrue(exception.getMessage().contains("is not an administrator"));
        }
    }

    @Test
    public void ignoresRolesThatOpenNmsDoesNotKnow() throws Exception {
        final UserConfig userConfig = userConfig("admin", "secret", "ROLE_ADMIN", "ROLE_MADE_UP");
        final Subject subject = new Subject();
        final OpenNMSLoginModule module = loginModule(userConfig, subject, "admin", "secret");

        assertTrue(module.login());
        assertTrue(module.commit());
        assertEquals(Set.of("ADMIN", "admin", "ROLE_ADMIN", "USER", "user", "ROLE_USER"), roleNames(subject));
    }

    @Test
    public void rejectsInvalidPasswords() throws Exception {
        final UserConfig userConfig = userConfig("admin", "secret", "ROLE_ADMIN");
        final OpenNMSLoginModule module = loginModule(userConfig, new Subject(), "admin", "wrong");

        assertThrows(FailedLoginException.class, module::login);
    }

    @Test
    public void failsWhenUserConfigIsUnavailable() {
        final OpenNMSLoginModule module = loginModule(null, new Subject(), "admin", "secret");

        final LoginException exception = assertThrows(LoginException.class, module::login);
        assertTrue(exception.getMessage().contains("UserConfig service is unavailable"));
    }

    private static Set<String> roleNames(final Subject subject) {
        return subject.getPrincipals(RolePrincipal.class).stream()
                .map(RolePrincipal::getName)
                .collect(Collectors.toSet());
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
