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
package org.opennms.nrtg.web.internal;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.net.MediaType;
import org.opennms.netmgt.model.ResourceId;

public class NrtServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private NrtController m_controller;

    public void setController(NrtController controller) {
        m_controller = controller;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession httpSession = req.getSession(true);
        resp.setContentType(MediaType.JSON_UTF_8.toString());

        if (req.getParameter("nrtCollectionTaskId") != null) {
            m_controller.nrtCollectionJobTrigger(req.getParameter("nrtCollectionTaskId"), httpSession);

            if ("true".equals(req.getParameter("poll"))) {
                resp.getOutputStream().println(m_controller.getMeasurementSetsForDestination(req.getParameter("nrtCollectionTaskId")));
            }
        } else if (req.getParameter("resourceId") != null && req.getParameter("report") != null) {
            ModelAndView modelAndView = m_controller.nrtStart(ResourceId.fromString(req.getParameter("resourceId")), req.getParameter("report"), httpSession);

            String template = getTemplateAsString(modelAndView.getViewName() + ".template");

            for (Entry<String, Object> entry : modelAndView.getModel().entrySet()) {
                template = template.replaceAll("\\$\\{" + entry.getKey() + "\\}", (entry.getValue() != null ? entry.getValue().toString() : "null"));
            }
            resp.getOutputStream().write(template.getBytes());
        } else {
            throw new ServletException("unrecognized servlet parameters");
        }
    }

    public String getTemplateAsString(String templateName) throws IOException {

        BufferedReader r = null;
        try {
            final StringBuilder results = new StringBuilder();
            r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + templateName)));

            String line;
            while ((line = r.readLine()) != null) {
                results.append(line).append('\n');
            }

            return results.toString();
        } finally {
            if (r != null) { 
                r.close(); 
            }
        }
    }
}
