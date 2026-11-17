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
package org.opennms.web.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.ZoneId;

import org.junit.After;
import org.junit.Test;
import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

public class UserSettingsFilterTest {

    private final UserManager m_previous = UserFactory.getInstance();

    @After
    public void restore() {
        UserFactory.setInstance(m_previous);
    }

    /** Without a user manager (a context-ordering problem), the request still goes through, with the system time zone. */
    @Test
    public void testNoUserManagerFallsBackToTheSystemTimeZone() throws Exception {
        UserFactory.setInstance(null);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        request.setRemoteUser("jdoe");
        final MockFilterChain chain = new MockFilterChain();

        new UserSettingsFilter().doFilter(request, new MockHttpServletResponse(), chain);

        assertNotNull("the request must reach the chain", chain.getRequest());
        assertEquals(ZoneId.systemDefault(), request.getSession().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID));
    }
}
