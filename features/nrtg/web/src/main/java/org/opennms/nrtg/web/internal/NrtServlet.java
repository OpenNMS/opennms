/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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
