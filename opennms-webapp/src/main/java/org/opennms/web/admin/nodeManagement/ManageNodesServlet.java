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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @since 1.8.1
 */
public class ManageNodesServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -4938417809629844445L;

    private static final String UPDATE_SERVICE = "UPDATE ifservices SET status = ? FROM ipInterface INNER JOIN node ON ipInterface.nodeId = node.nodeId WHERE ifServices.ipInterfaceId = ipInterface.id AND node.nodeId = ? AND ipInterface.ipAddr = ? AND ifServices.serviceId = ?";

    private static final String DELETE_SERVICE_OUTAGES = "DELETE FROM outages WHERE ifregainedservice IS NULL AND ifserviceid IN (SELECT ifServices.id FROM ifServices, ipInterface, node WHERE ifServices.ipInterfaceId = ipInterface.id AND ipInterface.nodeId = node.nodeId AND node.nodeId = ? AND ipInterface.ipAddr = ? AND ifServices.serviceId = ?)"; 

    private static final String INCLUDE_FILE_NAME = "include";

    /** Constant <code>NOTICE_NAME="Email-Reporting"</code> */
    public static final String NOTICE_NAME = "Email-Reporting";

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
        List<ManagedInterface> allNodes = getManagedInterfacesFromSession(userSession);
        List<String> interfaceList = new ArrayList<>();
        List<String> serviceList = new ArrayList<>();

        // the list of all interfaces marked as managed
        if(request.getParameterValues("interfaceCheck") != null) {
            interfaceList = Arrays.asList(request.getParameterValues("interfaceCheck"));
        }

        // the list of all services marked as managed
        if(request.getParameterValues("serviceCheck") != null) {
            serviceList = Arrays.asList(request.getParameterValues("serviceCheck"));
        }

        // the list of interfaces that need to be put into the URL file
        List<String> addToURL = new ArrayList<>();

        List<String> unmanageInterfacesList = new ArrayList<>();
        List<String> manageInterfacesList = new ArrayList<>();

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmt = connection.prepareStatement(UPDATE_SERVICE);
                d.watch(stmt);
                PreparedStatement outagesstmt = connection.prepareStatement(DELETE_SERVICE_OUTAGES);
                d.watch(outagesstmt);

                for (int j = 0; j < allNodes.size(); j++) {
                    ManagedInterface curInterface = allNodes.get(j);
                    String intKey = curInterface.getNodeid() + "-" + curInterface.getAddress();

                    // see if this interface needs added to the url list
                    if (interfaceList.contains(intKey)) {
                        addToURL.add(curInterface.getAddress());
                    }

                    // determine what is managed and unmanged
                    if (interfaceList.contains(intKey) && curInterface.getStatus().equals("unmanaged")) {
                        // Event newEvent = new Event();
                        // newEvent.setUei("uei.opennms.org/internal/interfaceManaged");
                        // newEvent.setSource("web ui");
                        // newEvent.setNodeid(curNode.getNodeID());
                        // newEvent.setInterface(curInterface.getAddress());
                        // newEvent.setTime(curDate);

                        // updateInterface(curInterface.getNodeid(),
                        // curInterface.getAddress(), new Event(), "M");
                        manageInterfacesList.add(curInterface.getAddress());
                    } else if (!interfaceList.contains(intKey) && curInterface.getStatus().equals("managed")) {
                        // Event newEvent = new Event();
                        // newEvent.setUei("uei.opennms.org/internal/interfaceUnmanaged");
                        // newEvent.setSource("web ui");
                        // newEvent.setNodeid(curNode.getNodeID());
                        // newEvent.setInterface(curInterface.getAddress());
                        // newEvent.setTime(curDate);

                        // updateInterface(curInterface.getNodeid(),
                        // curInterface.getAddress(), new Event(), "F");
                        unmanageInterfacesList.add(curInterface.getAddress());
                    }

                    List<ManagedService> interfaceServices = curInterface.getServices();

                    for (int k = 0; k < interfaceServices.size(); k++) {
                        ManagedService curService = interfaceServices.get(k);
                        String serviceKey = intKey + "-" + curService.getId();

                        if (serviceList.contains(serviceKey) && curService.getStatus().equals("unmanaged")) {
                            // Event newEvent = new Event();
                            // newEvent.setUei("uei.opennms.org/internal/serviceManaged");
                            // newEvent.setSource("web ui");
                            // newEvent.setNodeid(curNode.getNodeID());
                            // newEvent.setInterface(curInterface.getAddress());
                            // newEvent.setService(curService.getName());
                            // newEvent.setTime(curDate);

                            stmt.setString(1, "R");
                            stmt.setInt(2, curInterface.getNodeid());
                            stmt.setString(3, curInterface.getAddress());
                            stmt.setInt(4, curService.getId());
                            this.log("DEBUG: executing manage service update for " + curInterface.getAddress() + " " + curService.getName());
                            stmt.executeUpdate();
                        } else if (!serviceList.contains(serviceKey) && curService.getStatus().equals("managed")) {
                            EventBuilder bldr = new EventBuilder(EventConstants.SERVICE_UNMANAGED_EVENT_UEI, "web ui");
                            bldr.setNodeid(curInterface.getNodeid());
                            bldr.setInterface(addr(curInterface.getAddress()));
                            bldr.setService(curService.getName());

                            sendEvent(bldr.getEvent());

                            stmt.setString(1, "S");
                            stmt.setInt(2, curInterface.getNodeid());
                            stmt.setString(3, curInterface.getAddress());
                            stmt.setInt(4, curService.getId());

                            outagesstmt.setInt(1, curInterface.getNodeid());
                            outagesstmt.setString(2, curInterface.getAddress());
                            outagesstmt.setInt(3, curService.getId());

                            this.log("DEBUG: executing unmanage service update for " + curInterface.getAddress() + " " + curService.getName());
                            stmt.executeUpdate();
                            outagesstmt.executeUpdate();
                        }
                    } // end k loop
                } // end j loop

                if (manageInterfacesList.size() > 0)
                    manageInterfaces(manageInterfacesList, connection);
                if (unmanageInterfacesList.size() > 0)
                    unmanageInterfaces(unmanageInterfacesList, connection);

                // update the packages url file
                writeURLFile(addToURL);

                connection.commit();
            } finally { // close off the db connection
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            d.cleanUp();
        }

        // send the event to restart SCM
        sendSCMRestartEvent();

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/manageNodesFinish.jsp");
        dispatcher.forward(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<ManagedInterface> getManagedInterfacesFromSession(HttpSession userSession) {
        if (userSession == null) {
            return null;
        } else {
            return (List<ManagedInterface>) userSession.getAttribute("listAll.manage.jsp");
        }
    }

    /**
     */
    private void manageInterfaces(List<String> interfaces, Connection connection) throws SQLException {
        final StringBuilder query = new StringBuilder("UPDATE ipinterface SET isManaged = ");
        query.append("'M'").append(" WHERE ipaddr IN (");

        for (int i = 0; i < interfaces.size(); i++) {
            query.append("'").append(interfaces.get(i)).append("'");

            if (i < interfaces.size() - 1)
                query.append(",");
        }
        query.append(")");

        this.log("DEBUG: " + query.toString());
        Statement update = connection.createStatement();
        update.executeUpdate(query.toString());
        update.close();
    }

    /**
     */
    private void unmanageInterfaces(List<String> interfaces, Connection connection) throws SQLException {
        final StringBuilder query = new StringBuilder("UPDATE ipinterface SET isManaged = ");
        query.append("'F'").append(" WHERE ipaddr IN (");

        for (int i = 0; i < interfaces.size(); i++) {
            query.append("'").append(interfaces.get(i)).append("'");

            if (i < interfaces.size() - 1)
                query.append(",");
        }
        query.append(")");

        this.log("DEBUG: " + query.toString());
        Statement update = connection.createStatement();
        update.executeUpdate(query.toString());
        update.close();
    }

    /**
     */
    private void sendSCMRestartEvent() throws ServletException {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/internal/restartSCM", "web ui");

        sendEvent(bldr.getEvent());
    }

    /**
     */
    // FIXME: This is totally the wrong place to be doing this.
    // FIXME: This is totally the wrong place to be doing this.
    private void writeURLFile(List<String> interfaceList) throws ServletException {
        String path = System.getProperty("opennms.home") + File.separator + "etc" + File.separator;

        String fileName = path + INCLUDE_FILE_NAME;

        Writer fileWriter = null;
        FileOutputStream fos = null;
        try {
        	fos = new FileOutputStream(fileName);
        	fileWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            for (int i = 0; i < interfaceList.size(); i++) {
                fileWriter.write((String) interfaceList.get(i) + System.getProperty("line.separator"));
            }

            // write out the file and close
            fileWriter.flush();
        } catch (IOException e) {
            throw new ServletException("Error writing the include url file " + fileName + ": " + e.getMessage(), e);
        } finally {
        	IOUtils.closeQuietly(fileWriter);
        	IOUtils.closeQuietly(fos);
        }
    }

    /**
     */
    private static void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }
}
