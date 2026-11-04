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
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

public class NoStoreUnauthorizedEntryPointTest {
    @Test
    public void sendsUncacheable401WithoutLoginRedirectOrSession() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/opennms/ui/assets/NodeDetails.js");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        new NoStoreUnauthorizedEntryPoint().commence(request, response, new InsufficientAuthenticationException("no"));

        assertEquals(401, response.getStatus());
        assertEquals("no-store", response.getHeader("Cache-Control"));
        // no browser login dialog, no redirect to login.jsp, no new session
        assertNull(response.getHeader("WWW-Authenticate"));
        assertNull(response.getRedirectedUrl());
        assertNull(request.getSession(false));
    }
}
