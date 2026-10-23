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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opennms.core.resource.Vault;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

public class OpenNMSAuthSuccessHandlerTest {
    private static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

    private static MockedStatic<Vault> vault;

    @BeforeClass
    public static void beforeClass() {
        vault = mockStatic(Vault.class);
    }

    @AfterClass
    public static void afterClass() {
        vault.close();
    }

    @Test
    public void redirectsToSavedRequestWithColonQuery() throws Exception {
        final MockHttpServletRequest request = login("bob", "x", "/element/node.jsp", "node=fs:fid", null);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertEquals("http://localhost:8980/opennms/element/node.jsp?node=fs:fid", response.getRedirectedUrl());
        assertNull(request.getSession().getAttribute(SAVED_REQUEST));
    }

    @Test
    public void redirectsToSpaWithFragment() throws Exception {
        final MockHttpServletRequest request = login("bob", "x", "/ui/", null, "#/node/1");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertEquals("http://localhost:8980/opennms/ui/#/node/1", response.getRedirectedUrl());
    }

    @Test
    public void redirectsToSpaWithEncodedFragment() throws Exception {
        // a user-entered name in the query of a Vue route, with characters vue-router does not encode
        final MockHttpServletRequest request = login("bob", "x", "/ui/", null, "#/scheduled-outages/edit?name=a[1]|b");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertEquals("http://localhost:8980/opennms/ui/#/scheduled-outages/edit?name=a%5B1%5D%7Cb", response.getRedirectedUrl());
    }

    @Test
    public void redirectsToSpaWithoutRejectedFragment() throws Exception {
        final MockHttpServletRequest request = login("bob", "x", "/ui/", null, "#/a\r\nSet-Cookie: x=1");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertEquals("http://localhost:8980/opennms/ui/", response.getRedirectedUrl());
    }

    @Test
    public void ignoresFragmentForNonSpaTarget() throws Exception {
        final MockHttpServletRequest request = login("bob", "x", "/outage/index.jsp", null, "#/node/1");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertEquals("http://localhost:8980/opennms/outage/index.jsp", response.getRedirectedUrl());
    }

    @Test
    public void assetSavedRequestFallsBackToDefault() throws Exception {
        final MockHttpServletRequest request = login("bob", "x", "/ui/assets/index.js", null, null);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertEquals("http://localhost:8980/opennms/index.jsp", response.getRedirectedUrl());
    }

    @Test
    public void defaultAdminGoesToGateAndKeepsState() throws Exception {
        final MockHttpServletRequest request = login("admin", "admin", "/ui/", null, "#/node/1");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertEquals("http://localhost:8980/opennms/account/selfService/passwordGate.jsp", response.getRedirectedUrl());
        assertEquals(Boolean.TRUE, request.getSession().getAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE));
        assertEquals("#/node/1", request.getSession().getAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE));
        assertNotNull(request.getSession().getAttribute(SAVED_REQUEST));
    }

    @Test
    public void defaultAdminInvalidFragmentNotStored() throws Exception {
        final MockHttpServletRequest request = login("admin", "admin", "/ui/", null, "#//x");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler().onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertEquals(Boolean.TRUE, request.getSession().getAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE));
        assertNull(request.getSession().getAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE));
    }

    @Test
    public void otherLoginClearsStaleGateState() throws Exception {
        // a second login in the same session, after an admin/admin login, without logout
        final MockHttpServletRequest request = login("bob", "x", "/outage/index.jsp", null, null);
        request.getSession().setAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE, Boolean.TRUE);
        request.getSession().setAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE, "#/node/1");

        handler().onAuthenticationSuccess(request, new MockHttpServletResponse(), mock(Authentication.class));

        assertNull(request.getSession().getAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE));
        assertNull(request.getSession().getAttribute(LoginRedirects.FRAGMENT_SESSION_ATTRIBUTE));
    }

    private static OpenNMSAuthSuccessHandler handler() {
        final OpenNMSAuthSuccessHandler handler = new OpenNMSAuthSuccessHandler();
        handler.setDefaultTargetUrl("/index.jsp");
        return handler;
    }

    /**
     * A login POST whose session holds a saved request for the given page, as if the
     * user had asked for that page before login.
     */
    private static MockHttpServletRequest login(final String username, final String password, final String savedPath,
                                                final String savedQuery, final String fragment) {
        final MockHttpSession session = new MockHttpSession();

        final MockHttpServletRequest original = LoginRedirectsTest.request();
        original.setMethod("GET");
        original.setSession(session);
        original.setServletPath(savedPath);
        original.setQueryString(savedQuery);
        original.setRequestURI("/opennms" + savedPath);
        new HttpSessionRequestCache().saveRequest(original, new MockHttpServletResponse());

        final MockHttpServletRequest request = LoginRedirectsTest.request();
        request.setMethod("POST");
        request.setSession(session);
        request.setServletPath("/j_spring_security_check");
        request.setRequestURI("/opennms/j_spring_security_check");
        request.addParameter("j_username", username);
        request.addParameter("j_password", password);
        if (fragment != null) {
            request.addParameter(LoginRedirects.FRAGMENT_PARAMETER, fragment);
        }
        return request;
    }
}
