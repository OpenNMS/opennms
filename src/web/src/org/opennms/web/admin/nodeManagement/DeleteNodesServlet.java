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
// 2002 Nov 12: Added the ability to delete data dirs when deleting nodes.
// 2002 Nov 10: Removed "http://" from UEIs and references to bluebird.
// 2002 Oct 22: Removed the need for a restart.
// 2002 Sep 19: Added delete node page based on manage/unmanage node page.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.TcpEventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * A servlet that handles deleting nodes from the database
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class DeleteNodesServlet extends HttpServlet {

    public void init() throws ServletException {
        try {
            DatabaseConnectionFactory.init();
        } catch (Exception e) {
            throw new ServletException("Could not initialize database factory: " + e.getMessage(), e);
        }

    }

    public static final String RRDTOOL_SNMP_GRAPH_PROPERTIES_FILENAME = "/etc/snmp-graph.properties";

    public static final String RRDTOOL_RT_GRAPH_PROPERTIES_FILENAME = "/etc/response-graph.properties";

    protected Properties snmpProps;

    protected File snmpRrdDirectory;

    protected Properties rtProps;

    protected File rtRrdDirectory;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);

        // the list of all nodes marked for deletion
        java.util.List nodeList = getList(request.getParameterValues("nodeCheck"));
        java.util.List nodeDataList = getList(request.getParameterValues("nodeData"));

        // get the directories storing the response time and SNMP data
        this.snmpProps = new java.util.Properties();
        this.snmpProps.load(new FileInputStream(Vault.getHomeDir() + RRDTOOL_SNMP_GRAPH_PROPERTIES_FILENAME));

        this.snmpRrdDirectory = new File(this.snmpProps.getProperty("command.input.dir"));

        this.rtProps = new java.util.Properties();
        this.rtProps.load(new FileInputStream(Vault.getHomeDir() + RRDTOOL_RT_GRAPH_PROPERTIES_FILENAME));

        this.rtRrdDirectory = new File(this.rtProps.getProperty("command.input.dir"));

        // delete data directories if desired
        for (int j = 0; j < nodeDataList.size(); j++) {
            // SNMP RRD directory
            File nodeDir = new File(this.snmpRrdDirectory, (String) nodeDataList.get(j));

            if (nodeDir.exists() && nodeDir.isDirectory()) {
                this.log("DEBUG: Attempting to Delete Node Data Directory: " + nodeDir.getAbsolutePath());
                if (deleteDir(nodeDir))
                    this.log("DEBUG: Node Data Directory Deleted Successfully");
            }
            StringBuffer select = new StringBuffer("SELECT DISTINCT ipaddr FROM ipinterface WHERE nodeid=");

            select.append((String) nodeDataList.get(j));

            try {
                Connection conn = Vault.getDbConnection();
                ArrayList intfs = new ArrayList();

                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(select.toString());

                    while (rs.next()) {
                        String ipAddr = rs.getString("ipaddr");
                        // Response Time RRD directory
                        File intfDir = new File(this.rtRrdDirectory, ipAddr);

                        if (intfDir.exists() && intfDir.isDirectory()) {
                            this.log("DEBUG: Attempting to Delete Node Response Time Data Directory: " + intfDir.getAbsolutePath());
                            if (deleteDir(intfDir))
                                this.log("DEBUG: Node Response Time Data Directory Deleted Successfully");
                        }
                    }
                    rs.close();
                    stmt.close();
                } finally {
                    Vault.releaseDbConnection(conn);
                }
            } catch (SQLException e) {
                throw new ServletException("There was a problem with the database connection: " + e.getMessage(), e);
            }
        }

        // Now, Delete the node from the database

        for (int s = 0; s < nodeList.size(); s++) {
            int nodeid = Integer.parseInt((String) nodeList.get(s));
            sendDeleteNodeEvent(nodeid);

            this.log("DEBUG: End of Delete of Node Number: " + nodeList.get(s));

        } // end s loop

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/deleteNodesFinish.jsp");
        dispatcher.forward(request, response);

    }

    private void sendDeleteNodeEvent(int node) throws ServletException {
        Event nodeDeleted = new Event();
        nodeDeleted.setUei("uei.opennms.org/internal/capsd/deleteNode");
        nodeDeleted.setSource("web ui");
        nodeDeleted.setNodeid(node);
        nodeDeleted.setTime(EventConstants.formatToString(new java.util.Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;

        // Add Transaction ID
        eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        parmValue = new Value();
        parmValue.setContent("webUI");
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        nodeDeleted.setParms(eventParms);

        sendEvent(nodeDeleted);
    }

    private void sendEvent(Event event) throws ServletException {
        try {
            EventProxy eventProxy = Vault.createEventProxy();
            eventProxy.send(event);
        } catch (Exception e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

    private java.util.List getList(String array[]) {
        java.util.List newList = new ArrayList();

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                newList.add(array[i]);
            }
        }

        return newList;
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns
    // false.
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

}
