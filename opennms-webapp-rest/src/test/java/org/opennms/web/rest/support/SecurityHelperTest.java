/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import static org.mockito.Matchers.anyString;
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
            final String role = invocation.getArgumentAt(0, String.class);
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
