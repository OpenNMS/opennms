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
    public void sanitizeFragmentEncodesCharactersVueRouterLeavesUnencoded() {
        // vue-router leaves | [ ] { } ^ (and ` in query values) unencoded in the hash; a user-entered
        // name in the query, e.g. a scheduled outage, can contain them. Encoded, the route stays the same.
        assertEquals("#/scheduled-outages/edit?name=a%5B1%5D%7Cb%7Bc%7D%5Ed%60e",
            LoginRedirects.sanitizeFragment("#/scheduled-outages/edit?name=a[1]|b{c}^d`e"));
    }

    @Test
    public void sanitizeFragmentEncodesOtherPrintableCharacters() {
        assertEquals("#/a%20b", LoginRedirects.sanitizeFragment("#/a b"));
        assertEquals("#/a%23b", LoginRedirects.sanitizeFragment("#/a#b"));
        assertEquals("#/%3Cscript%3E%22%5C", LoginRedirects.sanitizeFragment("#/<script>\"\\"));
        // UTF-8 bytes
        assertEquals("#/node/%C3%A9", LoginRedirects.sanitizeFragment("#/node/\u00e9"));
    }

    @Test
    public void sanitizeFragmentRejectsBadInput() {
        final String[] bad = {
            null, "", "#", "/node/1", "#node", "#//evil.example.com", "#/a\r\nSet-Cookie: x=1",
            "#/a\tb", "#/a\u0000b", "#/a\u007fb"
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
    public void savedRequestPathKeepsEncodedPath() {
        // the servlet path and path info are decoded; the redirect must use the path as it was sent
        assertEquals("/graph/a%20b%3Fc%23d.htm?x=1",
            LoginRedirects.savedRequestPath(saved("/opennms/graph/a%20b%3Fc%23d.htm", "/graph", "/a b?c#d.htm", "x=1")));
    }

    @Test
    public void savedRequestPathRemovesPathParameters() {
        assertEquals("/element/node.jsp?node=1",
            LoginRedirects.savedRequestPath(saved("/opennms/element/node.jsp;jsessionid=abc123", "/element/node.jsp", null, "node=1")));
    }

    @Test
    public void savedRequestPathChecksTheDecodedPath() {
        // an encoded asset path must still be rejected
        assertNull(LoginRedirects.savedRequestPath(saved("/opennms/ui/assets/index%2Ejs", "/ui/assets/index.js", null, null)));
    }

    @Test
    public void decodedPathJoinsServletPathAndPathInfo() {
        assertEquals("/graph/results.htm", LoginRedirects.decodedPath("/graph", "/results.htm"));
        assertEquals("/index.jsp", LoginRedirects.decodedPath("/index.jsp", null));
        assertEquals("/x", LoginRedirects.decodedPath(null, "/x"));
        assertEquals("", LoginRedirects.decodedPath(null, null));
    }

    @Test
    public void isPasswordGatePending() {
        assertFalse(LoginRedirects.isPasswordGatePending(null));
        final MockHttpSession session = new MockHttpSession();
        assertFalse(LoginRedirects.isPasswordGatePending(session));
        session.setAttribute(LoginRedirects.PASSWORD_GATE_PENDING_ATTRIBUTE, Boolean.TRUE);
        assertTrue(LoginRedirects.isPasswordGatePending(session));
    }

    @Test
    public void buildTargetUrlWithoutSavedRequestUsesDefault() {
        assertEquals("http://localhost:8980/opennms/index.jsp", LoginRedirects.buildTargetUrl(request(), null, "#/node/1"));
    }

    @Test
    public void buildTargetUrlAddsFragmentForSpa() {
        assertEquals("http://localhost:8980/opennms/ui/#/node/1", LoginRedirects.buildTargetUrl(request(), "/ui/", "#/node/1"));
    }

    @Test
    public void buildTargetUrlDropsInvalidFragment() {
        assertEquals("http://localhost:8980/opennms/ui/index.html", LoginRedirects.buildTargetUrl(request(), "/ui/index.html", "#//x"));
    }

    @Test
    public void buildTargetUrlIgnoresFragmentForNonSpaTarget() {
        assertEquals("http://localhost:8980/opennms/element/node.jsp?node=fs:fid",
            LoginRedirects.buildTargetUrl(request(), "/element/node.jsp?node=fs:fid", "#/node/1"));
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
        return saved("/opennms" + servletPath + (pathInfo == null ? "" : pathInfo), servletPath, pathInfo, query);
    }

    /** As above, with the request URI as the client sent it (encoded, maybe with path parameters). */
    static SavedRequest saved(final String requestURI, final String servletPath, final String pathInfo, final String query) {
        final MockHttpServletRequest original = request();
        original.setMethod("GET");
        original.setSession(new MockHttpSession());
        original.setServletPath(servletPath);
        original.setPathInfo(pathInfo);
        original.setQueryString(query);
        original.setRequestURI(requestURI);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final HttpSessionRequestCache cache = new HttpSessionRequestCache();
        cache.saveRequest(original, response);
        return cache.getRequest(original, response);
    }
}
