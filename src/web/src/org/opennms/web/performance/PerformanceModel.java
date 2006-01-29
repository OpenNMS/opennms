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
// 02 Oct 2005: Use File.separator to join file path components instead of "/". -- DJ Gregor
//
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

package org.opennms.web.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.IntSet;
import org.opennms.netmgt.utils.IfLabel;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.Util;
import org.opennms.web.graph.PrefabGraph;
import org.opennms.web.graph.GraphModel;
import org.opennms.web.graph.GraphModelAbstract;

/**
 * Encapsulates all SNMP performance reporting for the web user interface.
 * 
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public class PerformanceModel extends GraphModelAbstract {
    public static final String RRDTOOL_GRAPH_PROPERTIES_FILENAME =
		File.separator + "etc" + File.separator + "snmp-graph.properties";

    public static final String INTERFACE_GRAPH_TYPE = "interface";

    public static final String NODE_GRAPH_TYPE = "node";

    /**
     * Create a new instance.
     * 
     * @param homeDir
     *            the OpenNMS home directory, see {@link Vault#getHomeDir
     *            Vault.getHomeDir}.
     */
    public PerformanceModel(String homeDir) throws IOException {
	loadProperties(homeDir, RRDTOOL_GRAPH_PROPERTIES_FILENAME);
    }

    public List getDataSourceList(String nodeId, String intf,
				  boolean includeNodeQueries) {
        if (nodeId == null || intf == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        File nodeDir = new File(getRrdDirectory(), nodeId);
        File intfDir = new File(nodeDir, intf);

	ArrayList dataSources = new ArrayList();

        if (includeNodeQueries) {
            dataSources.addAll(getDataSourceList(nodeId));
        }

	dataSources.addAll(getDataSourcesInDirectory(intfDir));

	return dataSources;
    }

    /**
     * Returns a list of data structures representing the nodes that have SNMP
     * performance data collected.
     * 
     * <p>
     * First the list of RRD files is collected. From those filenames, the IP
     * address is extracted from each. A list of unique IP addresses is created,
     * discarding the duplicates. At the same time, a mapping of unique IP
     * address to RRD files is created. Then a database call is made to
     * determine the node identifier and human-readable label for each node
     * containing the IP addresses. From that list, an array of data structures,
     * <code>QueryableNode</code>s, are created.
     * </p>
     */
    public QueryableNode[] getQueryableNodes() throws SQLException {
        // Get all of the numeric directory names in the RRD directory; these
        // are the nodeids of the nodes that have performance data
        File[] nodeDirs =
	    getRrdDirectory().listFiles(RrdFileConstants.NODE_DIRECTORY_FILTER);

        if (nodeDirs == null || nodeDirs.length == 0) {
	    return new QueryableNode[0];
	}

	List nodeList = new LinkedList();

	// Construct a set containing the nodeIds that are queryable
        IntSet queryableIds = new IntSet();
	for (int i = 0; i < nodeDirs.length; i++) {
	    String fileName = nodeDirs[i].getName();
	    int nodeId = Integer.parseInt(fileName);
	    queryableIds.add(nodeId);
	}

	// create the main stem of the select statement
	StringBuffer select = new StringBuffer("SELECT DISTINCT NODEID, NODELABEL FROM NODE WHERE NODETYPE != 'D' ORDER BY NODELABEL");
	
	Connection conn = Vault.getDbConnection();

	Statement stmt = null;
	ResultSet rs = null;
	try {
	    stmt = conn.createStatement();
	    rs = stmt.executeQuery(select.toString());

	    while (rs.next()) {
		int nodeId = rs.getInt("nodeid");

		if (queryableIds.contains(nodeId)) {
		    String nodeLabel = rs.getString("nodeLabel");
		    nodeList.add(new QueryableNode(nodeId, nodeLabel));
		}
	    }
	} finally {
	    if (rs != null)
		rs.close();
	    if (stmt != null)
		stmt.close();
	    Vault.releaseDbConnection(conn);
	}

	return (QueryableNode[])
	    nodeList.toArray(new QueryableNode[nodeList.size()]);
    }

    public ArrayList getQueryableInterfacesForNode(int nodeId) {
        return getQueryableInterfacesForNode(String.valueOf(nodeId));
    }

    public ArrayList getQueryableInterfacesForNode(String nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        ArrayList intfs = new ArrayList();
        File nodeDir = new File(getRrdDirectory(), nodeId);

        if (!nodeDir.isDirectory()) {
            throw new IllegalArgumentException("No such directory: " + nodeDir);
        }

	File[] intfDirs =
	    nodeDir.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        if (intfDirs != null && intfDirs.length > 0) {
            intfs.ensureCapacity(intfDirs.length);
            for (int i = 0; i < intfDirs.length; i++) {
		intfs.add(intfDirs[i].getName());
	    }
        }

        return intfs;
    }

    public boolean isQueryableNode(int nodeId) {
        return isQueryableNode(String.valueOf(nodeId));
    }

    public boolean isQueryableNode(String nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        File nodeDir = new File(getRrdDirectory(), nodeId);
	return RrdFileConstants.isValidRRDNodeDir(nodeDir);
    }

    public boolean isQueryableInterface(int nodeId, String ifLabel) {
        if (ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

	File intfDir = new File(getRrdDirectory(),
				String.valueOf(nodeId) + File.separator
				+ ifLabel);
	return RrdFileConstants.isValidRRDInterfaceDir(intfDir);
    }

    public boolean isQueryableInterface(String nodeId, String ifLabel) {
        if (nodeId == null || ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return isQueryableInterface(Integer.parseInt(nodeId), ifLabel);
    }

    public boolean encodeNodeIdInRRDParm() {
        return true;
    }

    public String getType() {
        return "performance";
    }

    /**
     * Return a human-readable description (usually an IP address or hostname)
     * for the interface given.
     */
    public String getHumanReadableNameForIfLabel(int nodeId, String ifLabel)
		throws SQLException {
	return getHumanReadableNameForIfLabel(nodeId, ifLabel, true);
    }
}
