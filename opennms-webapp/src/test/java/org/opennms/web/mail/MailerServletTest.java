/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.mail;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.web.servlet.MissingParameterException;
import org.springframework.test.annotation.IfProfileValue;

public class MailerServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private MailerServlet servlet;

    @Before
    public void setUp() throws Exception {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        servlet = new MailerServlet();

        Mockito.when(request.getParameter("subject")).thenReturn("why don't crabs donate to charity?");
        Mockito.when(request.getParameter("msg")).thenReturn("because they're shellfish"); // sorry
        Mockito.when(request.getParameter("sendto")).thenReturn("amay@opennms.com");

        Mockito.doNothing().when(response).sendRedirect(Mockito.isA(String.class));
        Mockito.when(response.getWriter()).thenReturn(new PrintWriter(System.err));
    }

    /**
     *  This is the only test that will get to the initialization of the
     *  JavaMailer object, which does authentication. Only run if
     *  javamail-configuration.properties has valid info.
     */
    @IfProfileValue(name="runMailTests", value="true")
    public void testServlet() throws Exception {
        servlet.doPost(request, response);
    }

    @Test (expected = MissingParameterException.class)
    public void testNoSendTo() throws Exception {
        Mockito.when(request.getParameter("sendto")).thenReturn(null);
        servlet.doPost(request, response);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidSendTo() throws Exception {
        Mockito.when(request.getParameter("sendto")).thenReturn("not a valid email address");
        servlet.doPost(request, response);
    }

    @Test (expected = MissingParameterException.class)
    public void testMissingSubject() throws Exception {
        Mockito.when(request.getParameter("subject")).thenReturn(null);
        servlet.doPost(request, response);
    }

    @Test (expected = MissingParameterException.class)
    public void testMissingMsg() throws Exception {
        Mockito.when(request.getParameter("msg")).thenReturn(null);
        servlet.doPost(request, response);
    }
}
