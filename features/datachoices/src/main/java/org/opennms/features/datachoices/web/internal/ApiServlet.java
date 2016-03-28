/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.web.internal;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.opennms.features.datachoices.internal.StateManager;
import org.opennms.features.datachoices.internal.UsageStatisticsReporter;

import com.google.common.base.Throwables;

/** 
 * Servlet mounted at /datachoices. Supported paths are:
 *
 * POST /opennms/datachoices?action=enable
 * POST /opennms/datachoices?action=disable
 * GET /opennms/datachoices
 *
 * Ideally this would be implemented as a JAX-RS endpoint registered
 * with CXF via Blueprint, but this doesn't currently play well with our
 * HTTP bridge.
 *
 * @author jwhite
 */
public class ApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private StateManager m_stateManager;
    private UsageStatisticsReporter m_usageStatisticsReporter;

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        final String action = request.getParameter("action");
        if (action == null) {
            return;
        }

        try {
            switch (action) {
            case "enable":
                m_stateManager.setEnabled(true, request.getRemoteUser());
                break;
            case "disable":
                m_stateManager.setEnabled(false, request.getRemoteUser());
                break;
            default:
                // pass
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(MediaType.APPLICATION_JSON);
        response.getWriter().println(m_usageStatisticsReporter.generateReport().toJson(true));
    }

    public void setStateManager(StateManager stateManager) {
        m_stateManager = stateManager;
    }

    public void setUsageStatisticsReporter(UsageStatisticsReporter usageStatisticsReporter) {
        m_usageStatisticsReporter = Objects.requireNonNull(usageStatisticsReporter);
    }
}
