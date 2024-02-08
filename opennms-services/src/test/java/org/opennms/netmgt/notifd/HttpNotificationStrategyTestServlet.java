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