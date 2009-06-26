//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 24: Organize imports, use Java 5 generics and loops, and
//              comment-out unused fields. - dj@opennms.org
// 2004 Jan 06: Added support for STATUS_SUSPEND and STATUS_RESUME
// 2002 Nov 10: Removed "http://" from UEIs and removed references to bluebird.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.Util;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class ManageNodesServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -4938417809629844445L;

    // FIXME: Should this be removed?
    //private static final String UPDATE_INTERFACE = "UPDATE ipinterface SET isManaged = ? WHERE ipaddr IN (?)";

    private static final String UPDATE_SERVICE = "UPDATE ifservices SET status = ? WHERE ipaddr = ? AND nodeID = ? AND serviceid = ?";

    private static final String DELETE_SERVICE_OUTAGES = "DELETE FROM outages WHERE ipaddr = ? AND nodeID = ? AND serviceid = ? AND ifregainedservice IS NULL";

    private static final String INCLUDE_FILE_NAME = "include";

    public static final String GAINED_SERVICE_UEI = "uei.opennms.org/nodes/nodeGainedService";

    public static final String GAINED_INTERFACE_UEI = "uei.opennms.org/nodes/nodeGainedInterface";

    public static final String NOTICE_NAME = "Email-Reporting";

    // FIXME: Should this be removed?
    //private static final String NOTICE_COMMAND = "/opt/OpenNMS/bin/notify.sh ";

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

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);
        List<ManagedInterface> allNodes = getManagedInterfacesFromSession(userSession);

        // the list of all interfaces marked as managed
        List<String> interfaceList = getList(request.getParameterValues("interfaceCheck"));

        // the list of all services marked as managed
        List<String> serviceList = getList(request.getParameterValues("serviceCheck"));

        // the list of interfaces that need to be put into the URL file
        List<String> addToURL = new ArrayList<String>();

        // date to set on events sent out
        String curDate = EventConstants.formatToString(new Date());

        List<String> unmanageInterfacesList = new ArrayList<String>();
        List<String> manageInterfacesList = new ArrayList<String>();

        try {
            Connection connection = Vault.getDbConnection();
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmt = connection.prepareStatement(UPDATE_SERVICE);
                PreparedStatement outagesstmt = connection.prepareStatement(DELETE_SERVICE_OUTAGES);

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

                    List interfaceServices = curInterface.getServices();

                    for (int k = 0; k < interfaceServices.size(); k++) {
                        ManagedService curService = (ManagedService) interfaceServices.get(k);
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
                            stmt.setString(2, curInterface.getAddress());
                            stmt.setInt(3, curInterface.getNodeid());
                            stmt.setInt(4, curService.getId());
                            this.log("DEBUG: executing manage service update for " + curInterface.getAddress() + " " + curService.getName());
                            stmt.executeUpdate();
                        } else if (!serviceList.contains(serviceKey) && curService.getStatus().equals("managed")) {
                            Event newEvent = new Event();
                            newEvent.setUei("uei.opennms.org/nodes/serviceUnmanaged");
                            newEvent.setSource("web ui");
                            newEvent.setNodeid(curInterface.getNodeid());
                            newEvent.setInterface(curInterface.getAddress());
                            newEvent.setService(curService.getName());
                            newEvent.setTime(curDate);
                            sendEvent(newEvent);

                            stmt.setString(1, "S");
                            stmt.setString(2, curInterface.getAddress());
                            stmt.setInt(3, curInterface.getNodeid());
                            stmt.setInt(4, curService.getId());
                            outagesstmt.setString(1, curInterface.getAddress());
                            outagesstmt.setInt(2, curInterface.getNodeid());
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
                Vault.releaseDbConnection(connection);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
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
    private void manageInterfaces(List interfaces, Connection connection) throws SQLException {
        StringBuffer query = new StringBuffer("UPDATE ipinterface SET isManaged = ");
        query.append("'M'").append(" WHERE ipaddr IN (");

        for (int i = 0; i < interfaces.size(); i++) {
            query.append("'").append((String) interfaces.get(i)).append("'");

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
    private void unmanageInterfaces(List interfaces, Connection connection) throws SQLException {
        StringBuffer query = new StringBuffer("UPDATE ipinterface SET isManaged = ");
        query.append("'F'").append(" WHERE ipaddr IN (");

        for (int i = 0; i < interfaces.size(); i++) {
            query.append("'").append((String) interfaces.get(i)).append("'");

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
        Event scmRestart = new Event();
        scmRestart.setUei("uei.opennms.org/internal/restartSCM");
        scmRestart.setSource("web ui");
        scmRestart.setTime(EventConstants.formatToString(new Date()));

        sendEvent(scmRestart);
    }

    /**
     */
    // FIXME: This is totally the wrong place to be doing this.
    private void writeURLFile(List<String> interfaceList) throws ServletException {
        String path = System.getProperty("opennms.home") + File.separator + "etc" + File.separator;

        if (path != null) {
            String fileName = path + INCLUDE_FILE_NAME;

            try {
                FileWriter fileWriter = new FileWriter(fileName);

                for (int i = 0; i < interfaceList.size(); i++) {
                    fileWriter.write((String) interfaceList.get(i) + System.getProperty("line.separator"));
                }

                // write out the file and close
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                throw new ServletException("Error writing the include url file " + fileName + ": " + e.getMessage(), e);
            }
        } else {
            throw new ServletException("The path to the package URL include directory is null.");
        }
    }

    /**
     */
    private List<String> getList(String array[]) {
        List<String> newList = new ArrayList<String>();

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                newList.add(array[i]);
            }
        }

        return newList;
    }

    /**
     */
    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Exception e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }
}
