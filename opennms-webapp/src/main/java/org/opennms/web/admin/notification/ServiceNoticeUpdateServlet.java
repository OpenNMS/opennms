/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.notification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.WebSecurityUtils;

/**
 * A servlet that handles updating the ifservices table with the notice status
 *
 * @author <a href="mailto:jason@opennms.org">Jason Johns</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @author <a href="mailto:jason@opennms.org">Jason Johns</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ServiceNoticeUpdateServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -5205846787997118203L;
    private static final String UPDATE_SERVICE = "UPDATE ifservices SET notify = ? WHERE nodeID = ? AND ipaddr = ? AND serviceid = ?";

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);
        Map<String, String> servicesCheckedMap = getServicesChecked(userSession);

        String checkedServices[] = request.getParameterValues("serviceCheck");

        if (checkedServices != null) {
            for (String checkedService : checkedServices) {
                servicesCheckedMap.put(checkedService, "Y");
            }
        }

        Iterator<String> iterator = servicesCheckedMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();

            // decompose the key into node ID, IP address and service ID
            StringTokenizer tokenizer = new StringTokenizer(key, ",");
            int nodeID = WebSecurityUtils.safeParseInt(tokenizer.nextToken());
            String ipAddress = tokenizer.nextToken();
            int serviceID = WebSecurityUtils.safeParseInt(tokenizer.nextToken());

            updateService(nodeID, ipAddress, serviceID, servicesCheckedMap.get(key));
        }

        response.sendRedirect("index.jsp");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getServicesChecked(HttpSession userSession) {
        return (Map<String, String>) userSession.getAttribute("service.notify.map");
    }

    /**
     */
    private void updateService(int nodeID, String interfaceIP, int serviceID, String notifyFlag) throws ServletException {
        Connection connection = null;

        final DBUtils d = new DBUtils(getClass());
        try {
            connection = Vault.getDbConnection();
            d.watch(connection);

            PreparedStatement stmt = connection.prepareStatement(UPDATE_SERVICE);
            d.watch(stmt);
            stmt.setString(1, notifyFlag);
            stmt.setInt(2, nodeID);
            stmt.setString(3, interfaceIP);
            stmt.setInt(4, serviceID);

            stmt.executeUpdate();

            // close off the db connection
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException sqlEx) {
                throw new ServletException("Couldn't roll back update to service " + serviceID + " on interface " + interfaceIP + " notify as " + notifyFlag + " in the database.", sqlEx);
            }

            throw new ServletException("Error when updating to service " + serviceID + " on interface " + interfaceIP + " notify as " + notifyFlag + " in the database.", e);
        } finally {
            d.cleanUp();
        }
    }
}
