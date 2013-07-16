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

package org.opennms.web.admin.nodeManagement;

import static org.opennms.core.utils.InetAddressUtils.addr;

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

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;
import org.opennms.web.element.NetworkElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class SnmpManageNodesServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(SnmpManageNodesServlet.class);

    private static final long serialVersionUID = 1604691299928314549L;
    private static final String UPDATE_INTERFACE = "UPDATE snmpInterface SET snmpCollect = ? WHERE id = ?";

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Throwable e) {
            throw new ServletException("Could not initialize database factory: " + e.getMessage(), e);
        }

        try {
            NotificationFactory.init();
        } catch (Throwable e) {
            throw new ServletException("Could not initialize notification factory: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);
        List<SnmpManagedInterface> allInterfaces = getManagedInterfacesFromSession(userSession);

        // the node being modified
        String nodeIdString = request.getParameter("node");
        int currNodeId = WebSecurityUtils.safeParseInt(nodeIdString);

        String primeInt = null;

        for (final SnmpManagedInterface testInterface : allInterfaces) {
            if (testInterface.getNodeid() == currNodeId && PrimaryType.PRIMARY.getCode().equals(testInterface.getStatus())) {
                // Get the IP address of the primary SNMP interface
                primeInt = NetworkElementFactory.getInstance(this.getServletContext()).getIpPrimaryAddress(currNodeId);
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
                    LOG.debug("option = {}", option);
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
        EventBuilder bldr = new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "web ui");
        bldr.setNodeid(nodeid);
        bldr.setInterface(addr(primeInt));

        sendEvent(bldr.getEvent());
    }

    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

}
