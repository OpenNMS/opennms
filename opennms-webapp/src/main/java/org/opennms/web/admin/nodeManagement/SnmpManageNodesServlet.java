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
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.events.api.EventConstants;
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
            Connection connection = DataSourceFactory.getInstance().getConnection();
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
