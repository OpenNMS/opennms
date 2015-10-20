/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.nodeManagement;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;
import org.opennms.web.svclayer.api.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet that handles deleting nodes from the database
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class DeleteNodesServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(DeleteNodesServlet.class);

    private static final long serialVersionUID = 573510937493956121L;

    private File m_snmpRrdDirectory;

    private File m_rtRrdDirectory;

    /** {@inheritDoc} */
    @Override
    public void init() throws ServletException {
        WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        ResourceService resourceService = (ResourceService) webAppContext.getBean("resourceService", ResourceService.class);

        m_snmpRrdDirectory = new File(resourceService.getRrdDirectory(), ResourceTypeUtils.SNMP_DIRECTORY);
        LOG.debug("SNMP RRD directory: {}", m_snmpRrdDirectory);

        m_rtRrdDirectory = new File(resourceService.getRrdDirectory(), ResourceTypeUtils.RESPONSE_DIRECTORY);
        LOG.debug("Response time RRD directory: {}", m_rtRrdDirectory);
    }

    /** {@inheritDoc} */
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
                LOG.debug("Attempting to delete node data directory: {}", nodeDir.getAbsolutePath());
                if (deleteDir(nodeDir)) {
                    LOG.info("Node SNMP data directory deleted successfully: {}", nodeDir.getAbsolutePath());
                } else {
                    LOG.warn("Node SNMP data directory *not* deleted successfully: {}", nodeDir.getAbsolutePath());
                }
            }
            
            // Response time RRD directories
            for (String ipAddr : ipAddrs) {
                File intfDir = new File(m_rtRrdDirectory, ipAddr);

                if (intfDir.exists() && intfDir.isDirectory()) {
                    LOG.debug("Attempting to delete node response time data directory: {}", intfDir.getAbsolutePath());
                    if (deleteDir(intfDir)) {
                        LOG.info("Node response time data directory deleted successfully: {}", intfDir.getAbsolutePath());
                    } else {
                        LOG.warn("Node response time data directory *not* deleted successfully: {}", intfDir.getAbsolutePath());
                    }
                }
            }
        }

        // Now, tell capsd to delete the node from the database
        for (Integer nodeId : nodeList) {
            sendDeleteNodeEvent(nodeId);
            LOG.debug("End of delete of node {}", nodeId);
        }

        // forward the request for proper display
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/admin/deleteNodesFinish.jsp");
        dispatcher.forward(request, response);
    }

    private List<String> getIpAddrsForNode(Integer nodeId) throws ServletException {
        List<String> ipAddrs = new ArrayList<String>();
        final DBUtils d = new DBUtils(getClass());

        try {
            Connection conn = DataSourceFactory.getInstance().getConnection();
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
        EventBuilder bldr = new EventBuilder(EventConstants.DELETE_NODE_EVENT_UEI, "web ui");
        bldr.setNodeid(node);
        
        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, "webUI");

        sendEvent(bldr.getEvent());
    }

    private void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Throwable e) {
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
            LOG.warn("Failed to delete file: {}", file.getAbsolutePath());
        }
        return successful;
    }

}
