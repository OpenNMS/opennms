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
// 2009 Feb 27: Updated to be aware of the snmpInterface snmpCollect column
// 2007 Jun 25: Use Java 5 generics and for loops and remove unused variable.
//              - dj@opennms.org
// 2002 Sep 24: Added the ability to select SNMP interfaces for collection.
//              Code based on original manage/unmanage code.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.admin.nodeManagement;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.Util;
import org.opennms.web.WebSecurityUtils;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class SnmpManageNodesServlet extends HttpServlet {
    private static final long serialVersionUID = 1604691299928314549L;
    private static final String UPDATE_INTERFACE = "UPDATE snmpInterface SET snmpCollect = ? WHERE id = ?";

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
            throw new ServletException("Could not initialize database factory: " + e.getMessage(), e);
        }

        try {
            NotificationFactory.init();
        } catch (Exception e) {
            throw new ServletException("Could not initialize notification factory: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);
        List<SnmpManagedInterface> allInterfaces = getManagedInterfacesFromSession(userSession);

        // the node being modified
        String nodeIdString = request.getParameter("node");
        int currNodeId = WebSecurityUtils.safeParseInt(nodeIdString);

        String primeInt = null;

        for (SnmpManagedInterface testInterface : allInterfaces) {
            if (testInterface.getNodeid() == currNodeId && "P".equals(testInterface.getStatus())) {
                primeInt = testInterface.getAddress();
            }
        }

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection connection = Vault.getDbConnection();
            d.watch(connection);
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmt = connection.prepareStatement(UPDATE_INTERFACE);
                d.watch(stmt);

                for (SnmpManagedInterface curInterface : allInterfaces) {
                    String option = request.getParameter("collect-" + curInterface.getIfIndex());
                    System.err.println(String.format("option = %s", option));
                    stmt.setString(1, option);
                    stmt.setInt(2, curInterface.getSnmpInterfaceId());
                    stmt.execute();
                }

                connection.commit();
            } finally { // close off the db connection
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            d.cleanUp();
        }

        // send the event to restart SNMP Collection
        if (primeInt != null) {
            sendSNMPRestartEvent(currNodeId, primeInt);
        }

        // forward the request for proper display
        // TODO This will redirect to the node page, but the URL will be admin/changeCollectStatus. Needs fixed.
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/element/node.jsp?node=" + currNodeId);
        dispatcher.forward(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<SnmpManagedInterface> getManagedInterfacesFromSession(HttpSession userSession) {
        if (userSession == null) {
            return null;
        } else {
            return (List<SnmpManagedInterface>) userSession.getAttribute("listInterfacesForNode.snmpselect.jsp");
        }
    }

    private void sendSNMPRestartEvent(int nodeid, String primeInt) throws ServletException {
        Event snmpRestart = new Event();
        snmpRestart.setUei("uei.opennms.org/nodes/reinitializePrimarySnmpInterface");
        snmpRestart.setNodeid(nodeid);
        snmpRestart.setInterface(primeInt);
        snmpRestart.setSource("web ui");
        snmpRestart.setTime(EventConstants.formatToString(new java.util.Date()));

        sendEvent(snmpRestart);
    }

    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Exception e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

}
