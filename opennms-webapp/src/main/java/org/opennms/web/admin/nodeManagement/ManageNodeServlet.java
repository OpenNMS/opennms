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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

import org.apache.commons.io.IOUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.model.capsd.DbIfServiceEntry;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @since 1.8.1
 */
public class ManageNodeServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(ManageNodeServlet.class);

    private static final long serialVersionUID = -544260517139205801L;

    // FIXME: Should this be deleted?
    //private static final String UPDATE_INTERFACE = "UPDATE ipinterface SET isManaged = ? WHERE ipaddr IN (?)";

    private static final String UPDATE_SERVICE = "UPDATE ifservices SET status = ? WHERE ipaddr = ? AND nodeID = ? AND serviceid = ?";

    private static final String DELETE_SERVICE_OUTAGES = "DELETE FROM outages WHERE ipaddr = ? AND nodeID = ? AND serviceid = ? AND ifregainedservice IS NULL";

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
        List<ManagedInterface> allNodes = getManagedInterfacesFromSession(userSession);

        // the list of all interfaces marked as managed
        List<String> interfaceList = getList(request.getParameterValues("interfaceCheck"));

        // the list of all services marked as managed
        List<String> serviceList = getList(request.getParameterValues("serviceCheck"));

        // the list of interfaces that need to be put into the URL file
        List<String> addToURL = new ArrayList<String>();

        List<String> unmanageInterfacesList = new ArrayList<String>();
        List<String> manageInterfacesList = new ArrayList<String>();
        
        Date curDate = new Date();

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection connection = Vault.getDbConnection();
            d.watch(connection);
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmt = connection.prepareStatement(UPDATE_SERVICE);
                d.watch(stmt);
                PreparedStatement outagesstmt = connection.prepareStatement(DELETE_SERVICE_OUTAGES);
                d.watch(outagesstmt);

                for (ManagedInterface curInterface : allNodes) {
                    String intKey = curInterface.getNodeid() + "-" + curInterface.getAddress();

                    // see if this interface needs added to the url list
                    if (interfaceList.contains(intKey)) {
                        addToURL.add(curInterface.getAddress());
                    }

                    // determine what is managed and unmanaged
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

                            stmt.setString(1, String.valueOf(DbIfServiceEntry.STATUS_RESUME));
                            stmt.setString(2, curInterface.getAddress());
                            stmt.setInt(3, curInterface.getNodeid());
                            stmt.setInt(4, curService.getId());
                            LOG.debug("doPost: executing manage service update for {} {}", curInterface.getAddress(), curService.getName());
                            stmt.executeUpdate();
                        } else if (!serviceList.contains(serviceKey) && curService.getStatus().equals("managed")) {
                            
                            EventBuilder bldr = new EventBuilder(EventConstants.SERVICE_UNMANAGED_EVENT_UEI, "web ui", curDate);
                            bldr.setNodeid(curInterface.getNodeid());
                            bldr.setInterface(addr(curInterface.getAddress()));
                            bldr.setService(curService.getName());
                            sendEvent(bldr.getEvent());

                            stmt.setString(1, String.valueOf(DbIfServiceEntry.STATUS_SUSPEND));
                            stmt.setString(2, curInterface.getAddress());
                            stmt.setInt(3, curInterface.getNodeid());
                            stmt.setInt(4, curService.getId());
                            outagesstmt.setString(1, curInterface.getAddress());
                            outagesstmt.setInt(2, curInterface.getNodeid());
                            outagesstmt.setInt(3, curService.getId());
                            LOG.debug("doPost: executing unmanage service update for {} {}", curInterface.getAddress(), curService.getName());
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
            return (List<ManagedInterface>) userSession.getAttribute("interfaces.nodemanagement");
        }
    }

    /**
     */
    private void manageInterfaces(List<String> interfaces, Connection connection) throws SQLException {
        StringBuffer query = new StringBuffer("UPDATE ipinterface SET isManaged = ");
        query.append("'M'").append(" WHERE ipaddr IN (");

        for (int i = 0; i < interfaces.size(); i++) {
            query.append("'").append(interfaces.get(i)).append("'");

            if (i < interfaces.size() - 1)
                query.append(",");
        }
        query.append(")");

        LOG.debug("manageInterfaces: query string: {}", query);
        Statement update = connection.createStatement();
        update.executeUpdate(query.toString());
        update.close();
    }

    /**
     */
    private void unmanageInterfaces(List<String> interfaces, Connection connection) throws SQLException {
        StringBuffer query = new StringBuffer("UPDATE ipinterface SET isManaged = ");
        query.append("'F'").append(" WHERE ipaddr IN (");

        for (int i = 0; i < interfaces.size(); i++) {
            query.append("'").append(interfaces.get(i)).append("'");

            if (i < interfaces.size() - 1)
                query.append(",");
        }
        query.append(")");

        LOG.debug("unmanageInterfaces: query: {}", query);
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
     * FIXME: This is totally the wrong place to be doing this.
     */
    private void writeURLFile(List<String> interfaceList) throws ServletException {
        String path = System.getProperty("opennms.home") + File.separator + "etc" + File.separator;

        String fileName = path + INCLUDE_FILE_NAME;

        FileOutputStream fos = null;
        Writer fileWriter = null;
        try {
        	fos = new FileOutputStream(fileName);
            fileWriter = new OutputStreamWriter(fos, "UTF-8");

            for (int i = 0; i < interfaceList.size(); i++) {
                fileWriter.write(interfaceList.get(i) + System.getProperty("line.separator"));
            }

            // write out the file and close
            fileWriter.flush();
        } catch (final IOException e) {
            throw new ServletException("Error writing the include url file " + fileName + ": " + e.getMessage(), e);
        } finally {
        	IOUtils.closeQuietly(fileWriter);
        	IOUtils.closeQuietly(fos);
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
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

}
