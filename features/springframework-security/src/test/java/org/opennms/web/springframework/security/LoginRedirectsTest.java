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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opennms.core.resource.Vault;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class LoginRedirectsTest {
    private static MockedStatic<Vault> vault;

    @BeforeClass
    public static void beforeClass() {
        // Vault.getProperty("opennms.web.base-url") returns null, so Util uses the request
        vault = mockStatic(Vault.class);
    }

    @AfterClass
    public static void afterClass() {
        vault.close();
    }

    @Test
    public void sanitizeFragmentAcceptsColonAndPercent() {
        assertEquals("#/node/fs:fid?x=a%3Ab&y=c:d", LoginRedirects.sanitizeFragment("#/node/fs:fid?x=a%3Ab&y=c:d"));
        assertEquals("#/node/1", LoginRedirects.sanitizeFragment("#/node/1"));
    }

    @Test
    public void sanitizeFragmentRejectsBadInput() {
        final String[] bad = {
            null, "", "#", "/node/1", "#node", "#//evil.example.com", "#/a\r\nSet-Cookie: x=1",
            "#/<script>", "#/a#b", "#/a b"
        };
        for (final String fragment : bad) {
            assertNull("should reject: " + fragment, LoginRedirects.sanitizeFragment(fragment));
        }
    }

    @Test
    public void sanitizeFragmentLengthLimit() {
        final String longest = "#/" + "a".repeat(2047);
        assertEquals(longest, LoginRedirects.sanitizeFragment(longest));
        assertNull(LoginRedirects.sanitizeFragment("#/" + "a".repeat(2048)));
    }

    @Test
    public void isSpaPath() {
        assertTrue(LoginRedirects.isSpaPath("/ui"));
        assertTrue(LoginRedirects.isSpaPath("/ui/"));
        assertTrue(LoginRedirects.isSpaPath("/ui/index.html"));
        assertFalse(LoginRedirects.isSpaPath("/uifoo"));
        assertFalse(LoginRedirects.isSpaPath("/index.jsp"));
        assertFalse(LoginRedirects.isSpaPath(null));
    }

    @Test
    public void savedRequestPathKeepsColonQuery() {
        assertEquals("/element/node.jsp?node=fs:fid", LoginRedirects.savedRequestPath(saved("/element/node.jsp", null, "node=fs:fid")));
    }

    @Test
    public void savedRequestPathKeepsPathInfo() {
        assertEquals("/graph/results.htm?a=1", LoginRedirects.savedRequestPath(saved("/graph", "/results.htm", "a=1")));
    }

    @Test
    public void savedRequestPathRejectsInvalid() {
        assertNull(LoginRedirects.savedRequestPath(saved("/rest/menu", null, null)));
        assertNull(LoginRedirects.savedRequestPath(saved("/ui/assets/index.js", null, null)));
        assertNull(LoginRedirects.savedRequestPath(null));
    }

    @Test
    public void buildTargetUrlWithoutSavedRequestUsesDefault() {
        assertEquals("http://localhost:8980/opennms/index.jsp", LoginRedirects.buildTargetUrl(request(), null, "#/node/1"));
    }

    @Test
    public void buildTargetUrlAddsFragmentForSpa() {
        assertEquals("http://localhost:8980/opennms/ui/#/node/1", LoginRedirects.buildTargetUrl(request(), saved("/ui/", null, null), "#/node/1"));
    }

    @Test
    public void buildTargetUrlDropsInvalidFragment() {
        assertEquals("http://localhost:8980/opennms/ui/index.html", LoginRedirects.buildTargetUrl(request(), saved("/ui/index.html", null, null), "#//x"));
    }

    @Test
    public void buildTargetUrlIgnoresFragmentForNonSpaTarget() {
        assertEquals("http://localhost:8980/opennms/element/node.jsp?node=fs:fid",
            LoginRedirects.buildTargetUrl(request(), saved("/element/node.jsp", null, "node=fs:fid"), "#/node/1"));
    }

    static MockHttpServletRequest request() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8980);
        request.setContextPath("/opennms");
        return request;
    }

    /** Saves a GET for the given path into a fresh session, the way Spring's ExceptionTranslationFilter does. */
    static SavedRequest saved(final String servletPath, final String pathInfo, final String query) {
        final MockHttpServletRequest original = request();
        original.setMethod("GET");
        original.setSession(new MockHttpSession());
        original.setServletPath(servletPath);
        original.setPathInfo(pathInfo);
        original.setQueryString(query);
        original.setRequestURI("/opennms" + servletPath + (pathInfo == null ? "" : pathInfo));
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final HttpSessionRequestCache cache = new HttpSessionRequestCache();
        cache.saveRequest(original, response);
        return cache.getRequest(original, response);
    }
}
