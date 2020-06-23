/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpNotificationStrategyTestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static Map<String,String> m_parameters = new HashMap<String,String>();

    @Override protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    public static Map<String,String> getRequestParameters() {
        return m_parameters;
    }

    public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        m_parameters.clear();

        final Enumeration<String> e = req.getParameterNames();
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            m_parameters.put(key, req.getParameter(key));
        }

        final String responseText = "It worked!\n";
        final ServletOutputStream os = resp.getOutputStream();
        os.print(responseText);
        os.close();
        resp.setContentType("text/plain");
        resp.setContentLength(responseText.length());
    }
}