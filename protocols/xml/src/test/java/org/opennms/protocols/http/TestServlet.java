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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TestServlet.class);

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.info("JUnit Test Request: {}", req.getRequestURI());
        LOG.info("JUnit Test Content Type: {}", req.getContentType());
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
                    String[] pair = ((String)st.nextToken()).split("=");
                    data.addParameter(pair[0], pair[1]);
                }
                resp.getWriter().write(JaxbUtils.marshal(data));
            } else {
                resp.setContentType("text/plain");
                resp.getWriter().write("ERROR!");
            }
        }
        if (req.getRequestURI().equals("/junit/test/post-data")) {
            Person p = JaxbUtils.unmarshal(Person.class, requestContent);
            if ("Alejandro".equals(p.getFirstName()) && "Galue".equals(p.getLastName())) {
                SampleData data = new SampleData();
                data.addParameter("contributions", "500");
                data.addParameter("applications", "2");
                data.addParameter("frameworks", "25");
                resp.setContentType("application/xml");
                resp.getWriter().write(JaxbUtils.marshal(data));
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid request");
            }
        }
    }

}
