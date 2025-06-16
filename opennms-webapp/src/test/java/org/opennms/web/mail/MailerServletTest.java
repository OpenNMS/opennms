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
