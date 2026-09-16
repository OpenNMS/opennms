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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Records what the webhook strategy sent and replies as the test asks. The
 * status and reply body are driven by the "status" and "respond" query
 * parameters so a single servlet can stand in for receivers that answer
 * differently (Slack's "ok", Discord's empty 204, an error response).
 */
public class WebhookNotificationStrategyTestServlet extends HttpServlet {

    private static final long serialVersionUID = 6413095428954620241L;

    private static String m_method;
    private static String m_contentType;
    private static String m_body;
    private static Map<String, String> m_headers = new HashMap<>();

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        m_method = req.getMethod();
        m_contentType = req.getContentType();
        m_body = req.getReader().lines().collect(Collectors.joining("\n"));

        m_headers = new HashMap<>();
        req.getHeaderNames().asIterator().forEachRemaining(name -> m_headers.put(name, req.getHeader(name)));

        final int status = req.getParameter("status") == null ? 200 : Integer.parseInt(req.getParameter("status"));
        final String responseText = req.getParameter("respond") == null ? "ok" : req.getParameter("respond");

        resp.setStatus(status);
        if (status == 204 || responseText.isEmpty()) {
            return;
        }

        resp.setContentType("text/plain");
        resp.setContentLength(responseText.length());
        final ServletOutputStream os = resp.getOutputStream();
        os.print(responseText);
        os.close();
    }

    public static void reset() {
        m_method = null;
        m_contentType = null;
        m_body = null;
        m_headers = new HashMap<>();
    }

    public static String getMethod() {
        return m_method;
    }

    public static String getContentType() {
        return m_contentType;
    }

    public static String getBody() {
        return m_body;
    }

    public static String getHeader(final String name) {
        return m_headers.get(name);
    }
}
