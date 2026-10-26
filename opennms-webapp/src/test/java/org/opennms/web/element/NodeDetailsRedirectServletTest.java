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
package org.opennms.web.element;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class NodeDetailsRedirectServletTest {
    private static final String BASE = "http://localhost:8980/opennms/";

    private NodeDetailsRedirectServlet servlet;

    @Before
    public void setUp() throws Exception {
        servlet = new NodeDetailsRedirectServlet();
        servlet.init(new MockServletConfig(new MockServletContext()));
    }

    @Test
    public void numericIdRedirectsToNodeDetails() throws Exception {
        assertEquals(BASE + "ui/index.html#/node/42", redirectFor("GET", "42"));
    }

    @Test
    public void foreignIdKeepsTheColon() throws Exception {
        assertEquals(BASE + "ui/index.html#/node/SmokeTests:TestMachine1", redirectFor("GET", "SmokeTests:TestMachine1"));
    }

    @Test
    public void reservedCharactersAreEncodedAsOnePathSegment() throws Exception {
        assertEquals(BASE + "ui/index.html#/node/My%20Source:a%2Fb%23c%3Fd%25e", redirectFor("GET", "My Source:a/b#c?d%e"));
    }

    @Test
    public void invalidIdIsPassedThroughForTheVuePageToReport() throws Exception {
        assertEquals(BASE + "ui/index.html#/node/abc", redirectFor("GET", "abc"));
    }

    @Test
    public void missingNodeRedirectsToNodeList() throws Exception {
        assertEquals(BASE + "ui/index.html#/nodes", redirectFor("GET", null));
    }

    @Test
    public void blankNodeRedirectsToNodeList() throws Exception {
        assertEquals(BASE + "ui/index.html#/nodes", redirectFor("GET", " "));
    }

    @Test
    public void postRedirectsToo() throws Exception {
        assertEquals(BASE + "ui/index.html#/node/7", redirectFor("POST", "7"));
    }

    private String redirectFor(final String method, final String node) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, "/opennms/element/node.jsp");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8980);
        request.setContextPath("/opennms");
        if (node != null) {
            request.setParameter("node", node);
        }
        final MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        assertEquals(302, response.getStatus());
        return response.getRedirectedUrl();
    }
}
