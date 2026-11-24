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
import static org.mockito.Mockito.mockStatic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opennms.core.resource.Vault;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

public class OpenNMSAuthFailureHandlerTest {
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
    public void failureRedirectKeepsValidFragment() throws Exception {
        assertEquals("http://localhost:8980/opennms/login.jsp?login_error=1#/node/1", failWithFragment("#/node/1"));
    }

    @Test
    public void failureRedirectDropsInvalidFragment() throws Exception {
        assertEquals("http://localhost:8980/opennms/login.jsp?login_error=1", failWithFragment("#/a\r\nSet-Cookie: x=1"));
    }

    private static String failWithFragment(final String fragment) throws Exception {
        final OpenNMSAuthFailureHandler handler = new OpenNMSAuthFailureHandler();
        handler.setDefaultFailureUrl("/login.jsp?login_error=1");

        final MockHttpServletRequest request = LoginRedirectsTest.request();
        request.setMethod("POST");
        request.addParameter(LoginRedirects.FRAGMENT_PARAMETER, fragment);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("bad"));
        return response.getRedirectedUrl();
    }
}
