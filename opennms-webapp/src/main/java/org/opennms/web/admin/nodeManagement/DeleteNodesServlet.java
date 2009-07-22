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
// 2007 May 12: Reorganize, use Java 5 generics/loops, get RRD directories
//              from the ResourceService. - dj@opennms.org
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
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.web.Util;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.ResourceService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet that handles deleting nodes from the database
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class DeleteNodesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private File m_snmpRrdDirectory;

    private File m_rtRrdDirectory;

    private ResourceService m_resourceService;

    @Override
    public void init() throws ServletException {
        try {
            DataSourceFactory.init();
        } catch (Exception e) {
            throw new ServletException("Could not initialize database factory: " + e, e);
        }

        WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);

        m_snmpRrdDirectory = new File(m_resourceService.getRrdDirectory(), DefaultResourceDao.SNMP_DIRECTORY);
        log().debug("SNMP RRD directory: " + m_snmpRrdDirectory);

        m_rtRrdDirectory = new File(m_resourceService.getRrdDirectory(), DefaultResourceDao.RESPONSE_DIRECTORY);
        log().debug("Response time RRD directory: " + m_rtRrdDirectory);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Integer> nodeList = getList(request.getParameterValues("nodeCheck"));
        List<Integer> nodeDataList = getList(request.getParameterValues("nodeData"));

        for (Integer nodeId : nodeDataList) {
            // Get a list of response time IP address lists
            List<String> ipAddrs = getIpAddrsForNode(nodeId);

            // SNMP RRD directory
            File nodeDir = new File(m_snmpRrdDirectory, nodeId.toString());

            if (nodeDir.exists() && nodeDir.isDirectory()) {
                log().debug("Attempting to delete node data directory: " + nodeDir.getAbsolutePath());
                if (deleteDir(nodeDir)) {
                    log().info("Node SNMP data directory deleted successfully: " + nodeDir.getAbsolutePath());
                } else {
                    log().warn("Node SNMP data directory *not* deleted successfully: " + nodeDir.getAbsolutePath());
                }
            }
            
            // Response time RRD directories
            for (String ipAddr : ipAddrs) {
                File intfDir = new File(m_rtRrdDirectory, ipAddr);

                if (intfDir.exists() && intfDir.isDirectory()) {
                    log().debug("Attempting to delete node response time data directory: " + intfDir.getAbsolutePath());
                    if (deleteDir(intfDir)) {
                        log().info("Node response time data directory deleted successfully: " + intfDir.getAbsolutePath());
                    } else {
                        log().warn("Node response time data directory *not* deleted successfully: " + intfDir.getAbsolutePath());
                    }
                }
            }
        }

        // Now, tell capsd to delete the node from the database
        for (Integer nodeId : nodeList) {
            sendDeleteNodeEvent(nodeId);
            log().debug("End of delete of node " + nodeId);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/admin/deleteNodesFinish.jsp");
        dispatcher.forward(request, response);
    }

    private List<String> getIpAddrsForNode(Integer nodeId) throws ServletException {
        List<String> ipAddrs = new ArrayList<String>();
        final DBUtils d = new DBUtils(getClass());

        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT ipaddr FROM ipinterface WHERE nodeid=?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                ipAddrs.add(rs.getString("ipaddr"));
            }
        } catch (SQLException e) {
            throw new ServletException("There was a problem with the database connection: " + e, e);
        } finally {
            d.cleanUp();
        }
        return ipAddrs;
    }

    private void sendDeleteNodeEvent(int node) throws ServletException {
        Event nodeDeleted = new Event();
        nodeDeleted.setUei("uei.opennms.org/internal/capsd/deleteNode");
        nodeDeleted.setSource("web ui");
        nodeDeleted.setNodeid(node);
        nodeDeleted.setTime(EventConstants.formatToString(new Date()));

        // Add appropriate parms
        Parms eventParms = new Parms();

        // Add Transaction ID
        Parm eventParm = new Parm();
        eventParm.setParmName(EventConstants.PARM_TRANSACTION_NO);
        Value parmValue = new Value();
        parmValue.setContent("webUI");
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        // Add Parms to the event
        nodeDeleted.setParms(eventParms);

        sendEvent(nodeDeleted);
    }

    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Exception e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }

    private List<Integer> getList(String[] array) {
        if (array == null) {
            return new ArrayList<Integer>(0);
        }
        
        List<Integer> list = new ArrayList<Integer>(array.length);
        for (String a : array) {
            list.add(WebSecurityUtils.safeParseInt(a));
        }
        return list;
    }

    /**
     * Deletes all files and sub-directories under the specified directory
     * If a deletion fails, the method stops attempting to delete and returns
     * false.
     * 
     * @return true if all deletions were successful, false otherwise.
     */
    private boolean deleteDir(File file) {
        // If this file is a directory, delete all of its children
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (!deleteDir(child)) {
                    return false;
                }
            }
        }

        // Delete the file/directory itself
        boolean successful = file.delete();
        if (!successful) {
            log().warn("Failed to delete file: " + file.getAbsolutePath());
        }
        return successful;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
