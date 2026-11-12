/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.roles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AdminRoleServletTest {

    /** Records the call to doIt, so that the test does not need the role, user and group managers. */
    private static class RecordingServlet extends AdminRoleServlet {
        private static final long serialVersionUID = 1L;
        private boolean handled = false;

        @Override
        protected void doIt(final HttpServletRequest request, final HttpServletResponse response) {
            handled = true;
        }
    }

    @Test
    public void getRejectsOperationsThatChangeRoles() throws Exception {
        for (final String operation : new String[] {"delete", "saveDetails", "saveEntry"}) {
            final RecordingServlet servlet = new RecordingServlet();
            final MockHttpServletResponse response = new MockHttpServletResponse();
            servlet.doGet(request("GET", operation), response);

            assertEquals(operation, 405, response.getStatus());
            assertEquals(operation, "POST", response.getHeader("Allow"));
            assertFalse(operation, servlet.handled);
        }
    }

    @Test
    public void getAllowsReadOperations() throws Exception {
        for (final String operation : new String[] {null, "view", "new", "editDetails", "addEntry", "editEntry"}) {
            final RecordingServlet servlet = new RecordingServlet();
            servlet.doGet(request("GET", operation), new MockHttpServletResponse());
            assertTrue(String.valueOf(operation), servlet.handled);
        }
    }

    @Test
    public void postAllowsOperationsThatChangeRoles() throws Exception {
        for (final String operation : new String[] {"delete", "saveDetails", "saveEntry"}) {
            final RecordingServlet servlet = new RecordingServlet();
            servlet.doPost(request("POST", operation), new MockHttpServletResponse());
            assertTrue(operation, servlet.handled);
        }
    }

    private static MockHttpServletRequest request(final String method, final String operation) {
        final MockHttpServletRequest request = new MockHttpServletRequest(method, "/opennms/admin/userGroupView/roles");
        if (operation != null) {
            request.addParameter("operation", operation);
            request.addParameter("role", "test");
        }
        return request;
    }
}
