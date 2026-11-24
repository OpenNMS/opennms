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

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class PageNavigationRequestMatcherTest {
    private static final String BROWSER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

    private final PageNavigationRequestMatcher matcher = new PageNavigationRequestMatcher();

    // Modern browsers send Fetch Metadata (Sec-Fetch-*) headers: only a top-level page navigation counts.

    @Test
    public void matchesBrowserNavigation() {
        assertTrue(matcher.matches(fetch("/element/node.jsp", BROWSER_ACCEPT, "navigate", "document")));
    }

    @Test
    public void rejectsBrowserSubresources() {
        // script, image, stylesheet, font: all send */* somewhere in Accept
        assertFalse(matcher.matches(fetch("/element/node.jsp", "*/*", "no-cors", "script")));
        assertFalse(matcher.matches(fetch("/element/node.jsp", "image/avif,image/webp,*/*;q=0.8", "no-cors", "image")));
        // fetch() of a page
        assertFalse(matcher.matches(fetch("/element/node.jsp", "*/*", "cors", "empty")));
    }

    @Test
    public void rejectsIframeNavigation() {
        // e.g. an embedded Vaadin or Grafana page: after login, the user must not land on the frame alone
        assertFalse(matcher.matches(fetch("/element/node.jsp", BROWSER_ACCEPT, "navigate", "iframe")));
    }

    @Test
    public void matchesNavigationWithoutDestHeader() {
        assertTrue(matcher.matches(fetch("/element/node.jsp", BROWSER_ACCEPT, "navigate", null)));
    }

    // Older clients (no Sec-Fetch-* headers): Accept decides.

    @Test
    public void matchesLegacyClientAcceptingHtml() {
        assertTrue(matcher.matches(request("GET", "/element/node.jsp", BROWSER_ACCEPT)));
    }

    @Test
    public void matchesLegacyClientWithAnyOrNoAccept() {
        // e.g. kiosk or wallboard tools and embedded browsers that send "*/*" for a page
        assertTrue(matcher.matches(request("GET", "/element/node.jsp", "*/*")));
        assertTrue(matcher.matches(request("GET", "/element/node.jsp", null)));
    }

    @Test
    public void rejectsLegacyClientNonHtmlAccept() {
        assertFalse(matcher.matches(request("GET", "/element/node.jsp", "image/avif,image/webp,*/*")));
        assertFalse(matcher.matches(request("GET", "/element/node.jsp", "application/json")));
    }

    @Test
    public void rejectsPost() {
        assertFalse(matcher.matches(request("POST", "/element/node.jsp", BROWSER_ACCEPT)));
    }

    @Test
    public void rejectsXhr() {
        final MockHttpServletRequest request = request("GET", "/element/node.jsp", "text/html");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        assertFalse(matcher.matches(request));
    }

    @Test
    public void rejectsAssetPath() {
        assertFalse(matcher.matches(request("GET", "/ui/assets/index.js", "text/html")));
        assertFalse(matcher.matches(request("GET", "/graph/graph.png", "*/*")));
        assertFalse(matcher.matches(fetch("/ui/assets/index.js", BROWSER_ACCEPT, "navigate", "document")));
    }

    @Test
    public void checksServletPathAndPathInfo() {
        final MockHttpServletRequest request = request("GET", "/ui", "*/*");
        request.setPathInfo("/assets/index.js");
        assertFalse(matcher.matches(request));
    }

    private static MockHttpServletRequest fetch(final String servletPath, final String accept, final String mode, final String dest) {
        final MockHttpServletRequest request = request("GET", servletPath, accept);
        request.addHeader("Sec-Fetch-Mode", mode);
        if (dest != null) {
            request.addHeader("Sec-Fetch-Dest", dest);
        }
        return request;
    }

    private static MockHttpServletRequest request(final String method, final String servletPath, final String accept) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, "/opennms" + servletPath);
        request.setContextPath("/opennms");
        request.setServletPath(servletPath);
        if (accept != null) {
            request.addHeader("Accept", accept);
        }
        return request;
    }
}
