/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.http;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.protocols.xml.config.Person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TestServlet.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TestServlet.class);

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.info("JUnit Test Request: %s", req.getRequestURI());
        LOG.info("JUnit Test Content Type: %s", req.getContentType());
        String requestContent = IOUtils.toString(req.getReader());
        if (req.getRequestURI().equals("/junit/test/sample")) {
            resp.getWriter().write("OK!");
        }
        if (req.getRequestURI().equals("/junit/test/post")) {
            if (req.getContentType().startsWith("application/xml")) {
                resp.setContentType("application/xml");
                Person p = JaxbUtils.unmarshal(Person.class, requestContent);
                SampleData data = new SampleData();
                data.addParameter("firstName", p.getFirstName());
                data.addParameter("lastName", p.getLastName());
                resp.getWriter().write(JaxbUtils.marshal(data));
            } else if (req.getContentType().startsWith("application/json")) {
                resp.setContentType("application/json");
                JSONObject object = JSONObject.fromObject(requestContent);
                SampleData data = new SampleData();
                data.addParameter("firstName", object.getJSONObject("person").getString("firstName"));
                data.addParameter("lastName", object.getJSONObject("person").getString("lastName"));
                resp.getWriter().write(JaxbUtils.marshal(data));
            } else if (req.getContentType().startsWith("application/x-www-form-urlencoded")) {
                resp.setContentType("application/xml");
                StringTokenizer st = new StringTokenizer(requestContent, "&");
                SampleData data = new SampleData();
                while (st.hasMoreTokens()) {
                    String pair[] = ((String)st.nextToken()).split("=");
                    data.addParameter(pair[0], pair[1]);
                }
                resp.getWriter().write(JaxbUtils.marshal(data));
            } else {
                resp.setContentType("text/plain");
                resp.getWriter().write("ERROR!");
            }
        }
    }

}
