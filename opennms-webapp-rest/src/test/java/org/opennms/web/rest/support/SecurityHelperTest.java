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
package org.opennms.web.rest.support;

import org.junit.Test;
import org.mockito.stubbing.Answer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.SecurityContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.web.api.Authentication.ROLE_ADMIN;
import static org.opennms.web.api.Authentication.ROLE_DELEGATE;
import static org.opennms.web.api.Authentication.ROLE_MOBILE;
import static org.opennms.web.api.Authentication.ROLE_READONLY;
import static org.opennms.web.api.Authentication.ROLE_REST;
import static org.opennms.web.api.Authentication.ROLE_USER;

public class SecurityHelperTest {
    private static final String USER = "joe";
    private static final String OTHER_USER = "bob";

    @Test
    public void assertUserEditPrivilegesWithSameAckUser() {
        // Admin always allowed
        assertUserEditPrivileges(true, USER, ROLE_ADMIN);

        // REST, USER and MOBILE roles allowed
        assertUserEditPrivileges(true, USER, ROLE_USER);
        assertUserEditPrivileges(true, USER, ROLE_REST);
        assertUserEditPrivileges(true, USER, ROLE_MOBILE);

        // No role rejected
        assertUserEditPrivileges(false, USER);

        // Read-only users rejected
        assertUserEditPrivileges(false, USER, ROLE_USER, ROLE_READONLY);
    }

    @Test
    public void assertUserEditPrivilegesWithDifferentAckUser() {
        // Admin always allowed
        assertUserEditPrivileges(true, OTHER_USER, ROLE_ADMIN);

        // REST, USER and MOBILE roles not allowed
        assertUserEditPrivileges(false, OTHER_USER, ROLE_USER);
        assertUserEditPrivileges(false, OTHER_USER, ROLE_REST);
        assertUserEditPrivileges(false, OTHER_USER, ROLE_MOBILE);

        // REST, USER and MOBILE roles allowed when they have the delegate role too
        assertUserEditPrivileges(true, OTHER_USER, ROLE_USER, ROLE_DELEGATE);
        assertUserEditPrivileges(true, OTHER_USER, ROLE_REST, ROLE_DELEGATE);
        assertUserEditPrivileges(true, OTHER_USER, ROLE_MOBILE, ROLE_DELEGATE);
    }

    private void assertUserEditPrivileges(boolean isAllowed, String ackUser, String... roles) {
        final Set<String> userRoles = new HashSet<>(Arrays.asList(roles));
        SecurityContext securityContext = mock(SecurityContext.class, RETURNS_DEEP_STUBS);
        when(securityContext.getUserPrincipal().getName()).thenReturn(USER);
        when(securityContext.isUserInRole(anyString())).thenAnswer((Answer) invocation -> {
            final String role = invocation.getArgument(0);
            return userRoles.contains(role);
        });

        WebApplicationException ex = null;
        try {
            SecurityHelper.assertUserEditCredentials(securityContext, ackUser);
        } catch (WebApplicationException e) {
            ex = e;
        }

        if (isAllowed) {
            assertNull("Should be allowed, but got: " + ex, ex);
        } else {
            assertNotNull("Should not be allowed, but passed.", ex);
        }
    }
}
