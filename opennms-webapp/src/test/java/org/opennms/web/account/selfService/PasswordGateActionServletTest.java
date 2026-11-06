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
package org.opennms.web.account.selfService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;
import org.opennms.web.springframework.security.LoginRedirects;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

public class PasswordGateActionServletTest {
    private static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    private static final String NEW_PASSWORD = "Abcdefgh1234!";

    private UserManager userManager;
    private boolean userManagerUsed;
    private PasswordGateActionServlet servlet;
    private MockHttpSession session;

    @Before
    public void setUp() throws Exception {
        userManager = mock(UserManager.class);
        userManagerUsed = false;
        servlet = new PasswordGateActionServlet() {
            private static final long serialVersionUID = 1L;

            @Override
            protected UserManager getUserManager() {
                userManagerUsed = true;
                return userManager;
            }
        };
        servlet.init(new MockServletConfig(new MockServletContext()));
        session = new MockHttpSession();
    }

    @Test
    public void noPendingGateRedirectsHomeWithoutTouchingUsers() throws Exception {
        final MockHttpServletResponse response = post(true);

        assertEquals("http://localhost:8980/opennms/index.jsp", response.getRedirectedUrl());
        assertFalse(userManagerUsed);
    }

    @Test
    public void skipRedirectsToSavedRequestWithColonQuery() throws Exception {
        startGate("/element/node.jsp", "node=fs:fid", null);

        final MockHttpServletResponse response = post(true);

        assertEquals("http://localhost:8980/opennms/element/node.jsp?node=fs:fid", response.getRedirectedUrl());
        assertGateFinished();
    }

    @Test
    public void skipRedirectsToSpaWithStoredFragment() throws Exception {
        startGate("/ui/", null, "#/node/1");

        final MockHttpServletResponse response = post(true);

        assertEquals("http://localhost:8980/opennms/ui/#/node/1", response.getRedirectedUrl());
        assertGateFinished();
    }

    @Test
    public void changePasswordRedirectsToSavedRequest() throws Exception {
        startGate("/element/node.jsp", "node=fs:fid", null);
        when(userManager.getUser("admin")).thenReturn(new User("admin"));
        when(userManager.comparePasswords("admin", "admin")).thenReturn(true);
        when(userManager.encryptedPassword(anyString(), anyBoolean())).thenReturn("encrypted");

        final MockHttpServletResponse response = post(false);

        verify(userManager).saveUser(eq("admin"), any(User.class));
        assertEquals("http://localhost:8980/opennms/element/node.jsp?node=fs:fid", response.getRedirectedUrl());
        assertGateFinished();
    }

    @Test
    public void wrongCurrentPasswordKeepsGatePending() throws Exception {
        startGate("/element/node.jsp", "node=fs:fid", null);
        when(userManager.getUser("admin")).thenReturn(new User("admin"));
        when(userManager.comparePasswords(anyString(), anyString())).thenReturn(false);

        final MockHttpServletResponse response = post(false);

        assertEquals("/account/selfService/passwordGate.jsp?action=redo", response.getForwardedUrl());
        assertEquals(Boolean.TRUE, session.getAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE));
    }

    /** The state OpenNMSAuthSuccessHandler leaves after an admin/admin login. */
    private void startGate(final String savedPath, final String savedQuery, final String fragment) {
        final MockHttpServletRequest original = request("GET");
        original.setServletPath(savedPath);
        original.setQueryString(savedQuery);
        original.setRequestURI("/opennms" + savedPath);
        new HttpSessionRequestCache().saveRequest(original, new MockHttpServletResponse());

        session.setAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE, Boolean.TRUE);
        if (fragment != null) {
            session.setAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE, fragment);
        }
    }

    private void assertGateFinished() {
        assertNull(session.getAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE));
        assertNull(session.getAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE));
        assertNull(session.getAttribute(SAVED_REQUEST));
    }

    private MockHttpServletResponse post(final boolean skip) throws Exception {
        final MockHttpServletRequest request = request("POST");
        request.setServletPath("/account/selfService/passwordGateAction");
        request.setRequestURI("/opennms/account/selfService/passwordGateAction");
        request.setRemoteUser("admin");
        if (skip) {
            request.addParameter("skip", "1");
        } else {
            request.addParameter("currentPassword", "admin");
            request.addParameter("newPassword", NEW_PASSWORD);
        }
        final MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doPost(request, response);
        return response;
    }

    private MockHttpServletRequest request(final String method) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, "/opennms");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8980);
        request.setContextPath("/opennms");
        request.setSession(session);
        return request;
    }
}
