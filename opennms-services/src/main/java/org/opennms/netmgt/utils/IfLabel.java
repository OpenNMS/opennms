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
// 2005 Jan 03: minor mod to support lame SNMP hosts
// 25 Sep 2003: Fixed a bug with SNMP Performance link on webUI.
// 31 Jan 2003: Cleaned up some unused imports.
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

package org.opennms.netmgt.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ThreadCategory;

/**
 * A convenience class for methods to encode/decode ifLabel descriptions for
 * storing SNMP data in an RRD file.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski </a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 */
public class IfLabel extends Object {

    protected static Category log = ThreadCategory.getInstance(IfLabel.class);

    /**
     * Return a map of useful SNMP information for the interface specified by
     * the nodeId and ifLabel. Essentially a "decoding" algorithm for the
     * ifLabel.
     * 
     * @param conn
     *            Database connection
     * @param nodeId
     *            Node id
     * @param ifLabel
     *            Interface label of format: <description>- <macAddr>
     * 
     * @return Map of SNMP info keyed by 'snmpInterface' table column names for
     *         the interface specified by nodeId and ifLabel args.
     * 
     * @throws SQLException
     *             if error occurs accessing the database.
     */
    public static Map<String, String> getInterfaceInfoFromIfLabel(Connection conn, int nodeId, String ifLabel) throws SQLException {
        if (ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Map<String, String> info = new HashMap<String, String>();
        String desc = ifLabel;
        String mac = null;

        // first I have to strip off the MAC address from the end, if there is
        // one
        int dashIndex = ifLabel.lastIndexOf("-");

        if (dashIndex >= 0) {
            desc = ifLabel.substring(0, dashIndex);
            mac = ifLabel.substring(dashIndex + 1, ifLabel.length());
        }

        log.debug("getInterfaceInfoFromIfLabel: desc=" + desc + " mac=" + mac);

        String query = "SELECT * FROM snmpinterface WHERE nodeid = '" + String.valueOf(nodeId) + "'";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            // If the description portion of ifLabel matches an entry
            // in the snmpinterface table...

            /*
             * When Cisco Express Forwarding (CEF) or some ATM encapsulations
             * (AAL5) are used on Cisco routers, an additional entry might be 
             * in the ifTable for these sub-interfaces, but there is no
             * performance data available for collection.  This check excludes
             * ifTable entries where ifDescr contains "-cef".  See bug #803.
             */
            if (rs.getString("snmpifdescr") != null) {
                if (Pattern.matches(".*-cef.*", rs.getString("snmpifdescr")))
                    continue;
            }

            if ((AlphaNumeric.parseAndReplace(rs.getString("snmpifname"), '_').equals(desc)) || (AlphaNumeric.parseAndReplace(rs.getString("snmpifdescr"), '_').equals(desc))) {

                // If the mac address portion of the ifLabel matches
                // an entry in the snmpinterface table...
                if (mac == null || mac.equals(rs.getString("snmpphysaddr"))) {
                    ThreadCategory.getInstance(IfLabel.class).debug("getInterfaceInfoFromIfLabel: found match...");
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        // Get extra information about the interface
                        info.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                    }

                    break;
                }
            }
        }

        rs.close();
        stmt.close();

        // The map will remain empty if the information was not located in the
        // DB.
        return info;
    }

    /**
     * Return a map of useful SNMP information for the interface specified by
     * the nodeId and ifLabel. Essentially a "decoding" algorithm for the
     * ifLabel.
     * 
     * Overloaded method which first obtains a database connection from the
     * vault.
     * 
     * @param nodeId
     *            Node id
     * @param ifLabel
     *            Interface label of format: <description>-<macAddr>
     * 
     * @return Map of SNMP info keyed by 'snmpInterface' table column names for
     *         the interface specified by nodeId and ifLabel args.
     * 
     * @throws SQLException
     *             if error occurs accessing the database.
     */
    public static Map<String, String> getInterfaceInfoFromIfLabel(int nodeId, String ifLabel) throws SQLException {
        Connection conn = Vault.getDbConnection();

        Map<String, String> info = null;
        try {
            info = getInterfaceInfoFromIfLabel(conn, nodeId, ifLabel);
        } finally {
            Vault.releaseDbConnection(conn);
        }

        // The map will remain null if the information was not located in the
        // DB.
        return info;
    }

    /** Get the interface labels for each interface on a given node. */
    public static String[] getIfLabels(int nodeId) throws SQLException {
        ArrayList<String> list = new ArrayList<String>();
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT snmpifname, snmpifdescr,snmpphysaddr from snmpinterface, ipinterface where (ipinterface.ismanaged!='D') AND ipinterface.nodeid=snmpinterface.nodeid AND ifindex = snmpifindex AND ipinterface.nodeid=?");
            stmt.setInt(1, nodeId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("snmpifname");
                String descr = rs.getString("snmpifdescr");
                String physAddr = rs.getString("snmpphysaddr");

                list.add(getIfLabel(name, descr, physAddr));
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        String[] labels = list.toArray(new String[list.size()]);

        return labels;
    }

    public static String getIfLabel(Connection conn, int nodeId, String ipAddr) throws SQLException {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String label = null;

        PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT snmpifname, snmpifdescr,snmpphysaddr from snmpinterface, ipinterface where (ipinterface.ismanaged!='D') AND ipinterface.nodeid=snmpinterface.nodeid AND ifindex=snmpifindex AND ipinterface.nodeid=? AND ipinterface.ipaddr=?");
        stmt.setInt(1, nodeId);
        stmt.setString(2, ipAddr);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String name = rs.getString("snmpifname");
            String descr = rs.getString("snmpifdescr");
            String physAddr = rs.getString("snmpphysaddr");

            if (name != null || descr != null) {
                label = getIfLabel(name, descr, physAddr);
            } else {
                log.warn("Interface (nodeId/ipAddr=" + nodeId + "/" + ipAddr + ") has no ifName and no ifDescr...setting to label to 'no_ifLabel'.");
                label = "no_ifLabel";
            }
        }

        if (rs.next()) {
            log.warn("Found more than one interface for node=" + nodeId + " ip=" + ipAddr);
        }

        rs.close();
        stmt.close();

        return label;

    }

    public static String getIfLabel(int nodeId, String ipAddr) throws SQLException {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String label = null;
        Connection conn = Vault.getDbConnection();

        try {
            label = getIfLabel(conn, nodeId, ipAddr);
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return label;
    }

    public static String getIfLabelfromIfIndex(int nodeId, String ipAddr, int ifIndex) throws SQLException, NumberFormatException {
        if (ipAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (ifIndex == -1) {
        	return getIfLabel(nodeId, ipAddr);
        }
        
        String label = null;
        Connection conn = Vault.getDbConnection();

        try {
        	Integer intIfIndex = Integer.valueOf(ifIndex);
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT snmpifname, snmpifdescr,snmpphysaddr from snmpinterface, ipinterface where (ipinterface.ismanaged!='D') AND ipinterface.nodeid=snmpinterface.nodeid AND ifindex=snmpifindex AND ipinterface.nodeid=? AND ipinterface.ipaddr=? AND ipinterface.ifindex=?");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setInt(3, intIfIndex);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("snmpifname");
                String descr = rs.getString("snmpifdescr");
                String physAddr = rs.getString("snmpphysaddr");

                if (name != null || descr != null) {
                    label = getIfLabel(name, descr, physAddr);
                } else {
                    log.warn("Interface (nodeId/ipAddr=" + nodeId + "/" + ipAddr + ") has no ifName and no ifDescr...setting to label to 'no_ifLabel'.");
                    label = "no_ifLabel";
                }
            }

            if (rs.next()) {
                log.warn("Found more than one interface for node=" + nodeId + " ip=" + ipAddr);
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return label;
    }

    public static String getIfLabel(String name, String descr, String physAddr) {
        // If available ifName is used to generate the label
        // since it is guaranteed to be unique. Otherwise
        // ifDescr is used. In either case, all non
        // alpha numeric characters are converted to
        // underscores to ensure that the resuling string
        // will make a decent file name and that RRD
        // won't have any problems using it
        //
        String label = null;

        if (name != null) {
            label = AlphaNumeric.parseAndReplace(name, '_');
        } else if (descr != null) {
            label = AlphaNumeric.parseAndReplace(descr, '_');
        } else {
            throw new IllegalArgumentException("Both name and descr are null, but at least one cannot be.");
        }

        // In order to assure the uniqueness of the
        // RRD file names we now append the MAC/physical
        // address to the end of label if it is available.
        // 
        if (physAddr != null) {
            physAddr = AlphaNumeric.parseAndTrim(physAddr);
            if (physAddr.length() == 12) {
                label = label + "-" + physAddr;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("initialize: physical address len is NOT 12, physAddr=" + physAddr);
                }
            }
        }

        return label;
    }
}
