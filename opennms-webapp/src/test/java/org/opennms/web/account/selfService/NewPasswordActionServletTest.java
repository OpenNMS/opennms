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

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class NewPasswordActionServletTest {
    @Test
    public void missingSessionUserRedirectsToEntry() throws Exception {
        final NewPasswordActionServlet servlet = new NewPasswordActionServlet();
        servlet.init(new MockServletConfig(new MockServletContext()));

        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/opennms/account/selfService/newPasswordAction");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8980);
        request.setContextPath("/opennms");
        request.setSession(new MockHttpSession());
        final MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.doPost(request, response);

        assertEquals("http://localhost:8980/opennms/account/selfService/newPasswordEntry", response.getRedirectedUrl());
    }
}
