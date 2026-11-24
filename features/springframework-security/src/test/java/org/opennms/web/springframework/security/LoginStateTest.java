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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class LoginStateTest {
    @Test
    public void remoteUserIsLoggedIn() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteUser("admin");
        assertTrue(LoginState.isLoggedIn(request));
    }

    /** An error page: the security filter has finished, so there is no remote user, but the session has the login. */
    @Test
    public void sessionLoginWithoutRemoteUserIsLoggedIn() {
        final MockHttpServletRequest request = requestWithSessionLogin(new UsernamePasswordAuthenticationToken("admin", null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        assertTrue(LoginState.isLoggedIn(request));
    }

    @Test
    public void anonymousIsNotLoggedIn() {
        assertFalse(LoginState.isLoggedIn(new MockHttpServletRequest()));

        final MockHttpServletRequest withEmptySession = new MockHttpServletRequest();
        withEmptySession.setSession(new MockHttpSession());
        assertFalse(LoginState.isLoggedIn(withEmptySession));

        assertFalse(LoginState.isLoggedIn(requestWithSessionLogin(new AnonymousAuthenticationToken("key", "anonymousUser",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))))));
        // a token that was not authenticated
        assertFalse(LoginState.isLoggedIn(requestWithSessionLogin(new UsernamePasswordAuthenticationToken("admin", "admin"))));
    }

    private static MockHttpServletRequest requestWithSessionLogin(final Authentication authentication) {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, new SecurityContextImpl(authentication));
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        return request;
    }
}
