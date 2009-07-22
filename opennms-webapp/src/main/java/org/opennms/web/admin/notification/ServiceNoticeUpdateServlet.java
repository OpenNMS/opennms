//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr: refactoring to support ACL DAO work
// 2008 Mar 20: Remove System.out.println. - dj@opennms.org
// 2007 Jul 23: Add serialVersionUID and use Java 5 generics. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

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
import org.opennms.web.WebSecurityUtils;

/**
 * A servlet that handles updating the ifservices table with the notice status
 * 
 * @author <a href="mailto:jason@opennms.org">Jason Johns</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public class ServiceNoticeUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String UPDATE_SERVICE = "UPDATE ifservices SET notify = ? WHERE nodeID = ? AND ipaddr = ? AND serviceid = ?";

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
