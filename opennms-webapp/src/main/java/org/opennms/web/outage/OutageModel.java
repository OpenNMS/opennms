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
// 2008 Mar 19: Changed outages select to be more efficient. Bug #2291
// 2003 Feb 01: Correct some SQL syntax. Bug #681.
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

package org.opennms.web.outage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.opennms.core.resource.Vault;
import org.opennms.web.element.Node;
import org.springframework.util.StringUtils;

/**
 * As the nonvisual logic for the Services Down (Outage) servlet and JSPs, this
 * class queries the database for current outages and provides utility methods
 * for manipulating that list of outages.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class OutageModel extends Object {
    /**
     * Create a new <code>OutageModel</code>.
     */
    public OutageModel() {
    }

    /**
     * Query the database to retrieve the current outages.
     *
     * @return An array of {@link Outage Outage}objects, or if there are none,
     *         an empty array.
     * @throws java.sql.SQLException
     *             If there is a problem getting a database connection or making
     *             a query.
     */
    public Outage[] getCurrentOutages() throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select o.outageid, o.nodeId, n.nodeLabel, o.ipaddr, ip.iphostname, s.servicename, o.serviceId, o.iflostservice, o.svclosteventid, no.notifyId, no.answeredBy from outages o left outer join notifications no on (o.svclosteventid = no.eventid) join ifservices if on (if.id = o.ifserviceid) join ipinterface ip on (ip.id = if.ipinterfaceid) join node n on (n.nodeid = ip.nodeid) join service s on (s.serviceid = if.serviceid) where ifregainedservice is null and n.nodeType != 'D' and ip.isManaged != 'D' and if.status != 'D' and o.serviceid=s.serviceid and (o.suppresstime is null or o.suppresstime < now()) order by n.nodelabel, ip.ipaddr, s.serviceName");

            outages = rs2Outages(rs, false, true);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * <p>getSuppressedOutages</p>
     *
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getSuppressedOutages() throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select o.outageid, o.nodeId, n.nodeLabel, o.ipaddr, ip.iphostname, s.servicename, o.serviceId, o.iflostservice, o.svclosteventid, no.notifyId, no.answeredBy from outages o left outer join notifications no on (o.svclosteventid = no.eventid) join ifservices if on (if.id = o.ifserviceid) join ipinterface ip on (ip.id = if.ipinterfaceid) join node n on (n.nodeid = ip.nodeid) join service s on (s.serviceid = if.serviceid) where ifregainedservice is null and n.nodeType != 'D' and ip.isManaged != 'D' and if.status != 'D' and o.serviceid=s.serviceid and o.suppresstime > now() order by n.nodelabel, ip.ipaddr, s.serviceName");

            outages = rs2Outages(rs, false, true);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * <p>getCurrentOutageCount</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public int getCurrentOutageCount() throws SQLException {
        int count = 0;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select count(o.outageid) from outages o left outer join notifications no on (o.svclosteventid = no.eventid) join ifservices if on (if.id = o.ifserviceid) join ipinterface ip on (ip.id = if.ipinterfaceid) join node n on (n.nodeid = ip.nodeid) join service s on (s.serviceid = if.serviceid) where ifregainedservice is null and n.nodeType != 'D' and ip.isManaged != 'D' and if.status != 'D' and o.serviceid=s.serviceid and (o.suppresstime is null or o.suppresstime < now()) ");

            if (rs.next()) {
                count = rs.getInt("count");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return count;
    }

    /**
     * <p>getSuppressedOutageCount</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public int getSuppressedOutageCount() throws SQLException {
        int count = 0;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select count(o.outageid) from outages o left outer join notifications no on (o.svclosteventid = no.eventid) join ifservices if on (if.id = o.ifserviceid) join ipinterface ip on (ip.id = if.ipinterfaceid) join node n on (n.nodeid = ip.nodeid) join service s on (s.serviceid = if.serviceid) where ifregainedservice is null and n.nodeType != 'D' and ip.isManaged != 'D' and if.status != 'D' and o.serviceid=s.serviceid and o.suppresstime > now() ");

            if (rs.next()) {
                count = rs.getInt("count");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return count;
    }
    
    /**
     * <p>getCurrentOutagesForNode</p>
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getCurrentOutagesForNode(int nodeId) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outages.outageid, outages.iflostservice, outages.ifregainedservice, outages.nodeID, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId " + "from outages, node, ipinterface, service " + "where outages.nodeid=? " + "and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr " + "and ifregainedservice is null " + " and suppresstime is null or suppresstime < now()" + "order by iflostservice desc");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs, false);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }
    
    /**
     * <p>getCurrentOutagesIdsForNode</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.Collection} object.
     * @throws java.sql.SQLException if any.
     */
    public Collection<Integer> getCurrentOutagesIdsForNode(int nodeId) throws SQLException {
        List<Integer> outageIds = new ArrayList<Integer>();
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outageid from outages where nodeid=?  and ifregainedservice is null and suppresstime is null or suppresstime < now();");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                outageIds.add(rs.getInt(1));
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outageIds;
    }

    
    /**
     * <p>filterNodesWithCurrentOutages</p>
     *
     * @param nodes an array of {@link org.opennms.web.element.Node} objects.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public Node[] filterNodesWithCurrentOutages(Node[] nodes) throws SQLException {
        HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>(nodes.length);
        for (Node n : nodes) {
            nodeMap.put(n.getNodeId(), n);
        }
        
        String nodeList = StringUtils.collectionToDelimitedString(nodeMap.keySet(), ", ");
        
        List<Node> newNodes = new ArrayList<Node>();
        
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT nodeid from outages where nodeid in ( " + nodeList + " ) and ifregainedservice is null and suppresstime is null or suppresstime < now();");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                newNodes.add(nodeMap.get(rs.getInt(1)));
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return newNodes.toArray(new Node[newNodes.size()]);
    }

    /**
     * <p>getNonCurrentOutagesForNode</p>
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getNonCurrentOutagesForNode(int nodeId) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outages.outageid, outages.iflostservice, outages.ifregainedservice, outages.nodeID, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId " + "from outages, node, ipinterface, service " + "where outages.nodeid=? " + "and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr " + "and ifregainedservice is not null " +  " and suppresstime is null or suppresstime < now() " + "order by iflostservice desc");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * Get all outages for a given node.
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getOutagesForNode(int nodeId) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outages.outageid, outages.iflostservice, outages.ifregainedservice, outages.nodeID, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId " + "from outages, node, ipinterface, service " + "where outages.nodeid=? and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr o" + "rder by iflostservice desc");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * Get all current outages and any resolved outages since the given time for
     * the given node.
     *
     * @param nodeId
     *            this is the node to query
     * @param time
     *            no resolved outages older than this time will be returned
     * @return All current outages and resolved outages no older than
     *         <code>time</code>.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getOutagesForNode(int nodeId, Date time) throws SQLException {
        if (time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();
        long timeLong = time.getTime();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outages.outageid, outages.iflostservice, outages.ifregainedservice, outages.nodeID, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId " + "from outages, node, ipinterface, service " + "where outages.nodeid=? and node.nodeid=outages.nodeid " + "and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr " + "and (ifregainedservice >= ? or ifregainedservice is null) order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setTimestamp(2, new Timestamp(timeLong));
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * <p>getOutagesForInterface</p>
     *
     * @param nodeId a int.
     * @param ipInterface a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getOutagesForInterface(int nodeId, String ipInterface) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outages.outageid, outages.iflostservice, outages.ifregainedservice, outages.nodeID, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId " + "from outages, node, ipinterface, service " + "where outages.nodeid=? and outages.ipaddr=? " + "and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr " + "order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipInterface);
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * Get all current outages and any resolved outages since the given time for
     * the given interface.
     *
     * @param nodeId
     *            this is the node to query
     * @param ipAddr
     *            this is the interface to query
     * @param time
     *            no resolved outages older than this time will be returned
     * @return All current outages and resolved outages no older than
     *         <code>time</code>.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getOutagesForInterface(int nodeId, String ipAddr, Date time) throws SQLException {
        if (ipAddr == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();
        long timeLong = time.getTime();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outages.outageid, outages.iflostservice, outages.ifregainedservice, outages.nodeID, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId " + "from outages, node, ipinterface, service " + "where outages.nodeid=? and outages.ipaddr=? " + "and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr " + "and (ifregainedservice >= ? or ifregainedservice is null) " + "order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setTimestamp(3, new Timestamp(timeLong));
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * <p>getOutagesForService</p>
     *
     * @param nodeId a int.
     * @param ipInterface a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getOutagesForService(int nodeId, String ipInterface, int serviceId) throws SQLException {
        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outages.outageid, outages.iflostservice, outages.ifregainedservice, outages.nodeID, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId " + "from outages, node, ipinterface, service " + "where outages.nodeid=? and outages.ipaddr=? and outages.serviceid=? " + "and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr " + "order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipInterface);
            stmt.setInt(3, serviceId);
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * Get all current outages and any resolved outages since the given time for
     * the given service.
     *
     * @param nodeId
     *            this is the node to query
     * @param ipAddr
     *            this is the interface to query
     * @param serviceId
     *            this is the service to query
     * @param time
     *            no resolved outages older than this time will be returned
     * @return All current outages and resolved outages no older than
     *         <code>time</code>.
     * @throws java.sql.SQLException if any.
     */
    public Outage[] getOutagesForService(int nodeId, String ipAddr, int serviceId, Date time) throws SQLException {
        if (ipAddr == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Outage[] outages = new Outage[0];
        Connection conn = Vault.getDbConnection();
        long timeLong = time.getTime();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT outages.outageid, outages.iflostservice, outages.ifregainedservice, outages.nodeID, node.nodeLabel, outages.ipaddr, ipinterface.iphostname, service.servicename, outages.serviceId " + "from outages, node, ipinterface, service " + "where outages.nodeid=? " + "and outages.ipaddr=? and outages.serviceid=? " + "and node.nodeid=outages.nodeid and outages.serviceid=service.serviceid and ipinterface.ipaddr=outages.ipaddr " + "and (ifregainedservice >= ? or ifregainedservice is null) " + "order by iflostservice desc");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setInt(3, serviceId);
            stmt.setTimestamp(4, new Timestamp(timeLong));
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /**
     * Return a list of IP addresses, the number of services down on each IP
     * address, and the longest time a service has been down for each IP
     * address. The list will be sorted in ascending order from the service down
     * longest to the service down shortest.
     *
     * @return an array of {@link org.opennms.web.outage.OutageSummary} objects.
     * @throws java.sql.SQLException if any.
     */
    public OutageSummary[] getCurrentOutageSummaries() throws SQLException {
        OutageSummary[] summaries = new OutageSummary[0];
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select distinct outages.nodeid, max(outages.iflostservice) as timeDown, node.nodelabel, now() as timeNow " + "from outages, node, ipinterface, ifservices " + "where ifregainedservice is null " + "and node.nodeid=outages.nodeid and ipinterface.nodeid = outages.nodeid and ifservices.nodeid=outages.nodeid " + "and ipinterface.ipaddr = outages.ipaddr and ifservices.ipaddr = outages.ipaddr " + "and ifservices.serviceid = outages.serviceid " + "and node.nodeType != 'D' and ipinterface.ismanaged != 'D' and ifservices.status != 'D' " + "group by outages.nodeid, node.nodelabel " + "order by timeDown desc;");

            List<OutageSummary> list = new ArrayList<OutageSummary>();

            while (rs.next()) {
                int nodeId = rs.getInt("nodeID");
                Timestamp timeDownTS = rs.getTimestamp("timeDown");
                long timeDown = timeDownTS.getTime();
                Date downDate = new Date(timeDown);
                String nodeLabel = rs.getString("nodelabel");
                Date now = new Date(rs.getTimestamp("timeNow").getTime());

                list.add(new OutageSummary(nodeId, nodeLabel, downDate, null, now));
            }

            rs.close();
            stmt.close();

            summaries = list.toArray(new OutageSummary[list.size()]);
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return summaries;
    }

    /**
     * Return a list of IP addresses, the number of services down on each IP
     * address, and the longest time a service has been down for each IP
     * address. The list will be sorted by the amount of time it has been down.
     *
     * @param date the starting date for the query
     * @return an array of {@link org.opennms.web.outage.OutageSummary} objects.
     * @throws java.sql.SQLException if any.
     */
    public OutageSummary[] getAllOutageSummaries(Date date) throws SQLException {
        OutageSummary[] summaries = new OutageSummary[0];
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT DISTINCT outages.nodeid, outages.iflostservice as timeDown, outages.ifregainedservice as timeUp, node.nodelabel "
                        + "FROM outages, node, ipinterface, ifservices "
                        + "WHERE node.nodeid=outages.nodeid AND ipinterface.nodeid=outages.nodeid AND ifservices.nodeid=outages.nodeid "
                        + "AND ipinterface.ipaddr=outages.ipaddr AND ifservices.ipaddr=outages.ipaddr "
                        + "AND ifservices.serviceid=outages.serviceid "
                        + "AND node.nodeType != 'D' "
                        + "AND ipinterface.ismanaged != 'D' "
                        + "AND ifservices.status != 'D' "
                        + "AND outages.iflostservice >= ? "
                        + "ORDER BY timeDown DESC;"
            );
            stmt.setTimestamp(1, new Timestamp(date.getTime()));
            ResultSet rs = stmt.executeQuery();

            List<OutageSummary> list = new ArrayList<OutageSummary>();

            while (rs.next()) {
                int nodeId = rs.getInt("nodeID");
                
                Timestamp timeDown = rs.getTimestamp("timeDown");
                Date downDate = new Date(timeDown.getTime());
                
                Timestamp timeUp = rs.getTimestamp("timeUp");
                Date upDate = null;
                if (timeUp != null) {
                    upDate = new Date(timeUp.getTime());
                }
                
                String nodeLabel = rs.getString("nodelabel");

                list.add(new OutageSummary(nodeId, nodeLabel, downDate, upDate));
            }

            rs.close();
            stmt.close();

            summaries = list.toArray(new OutageSummary[list.size()]);
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return summaries;
    }

    /**
     * Return a list of IP addresses, the number of services down on each IP
     * address, and the longest time a service has been down for each IP
     * address. The list will be sorted in ascending order from the service down
     * longest to the service down shortest. This is a clone of
     * getCurrentOutageSummaries for Harrah's (special consideration).
     *
     * @return an array of {@link org.opennms.web.outage.OutageSummary} objects.
     * @throws java.sql.SQLException if any.
     */
    public OutageSummary[] getCurrentSDSOutageSummaries() throws SQLException {
        OutageSummary[] summaries = new OutageSummary[0];
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select distinct outages.nodeid, max(outages.iflostservice) as timeDown, node.nodelabel from outages, node, ipinterface, ifservices, assets " + "where ifregainedservice is null " + "and node.nodeid=outages.nodeid and ipinterface.nodeid = outages.nodeid and ifservices.nodeid=outages.nodeid " + "and ipinterface.ipaddr = outages.ipaddr and ifservices.ipaddr = outages.ipaddr " + "and ifservices.serviceid = outages.serviceid " + "and node.nodeType != 'D' and ipinterface.ismanaged != 'D' and ifservices.status != 'D' " + "and assets.nodeid=node.nodeid and assets.displaycategory != 'SDS-A-Side' and assets.displaycategory != 'SDS-B-Side' " + "group by outages.nodeid, node.nodelabel " + "order by timeDown desc;");

            List<OutageSummary> list = new ArrayList<OutageSummary>();

            while (rs.next()) {
                int nodeId = rs.getInt("nodeID");
                Timestamp timeDownTS = rs.getTimestamp("timeDown");
                long timeDown = timeDownTS.getTime();
                Date downDate = new Date(timeDown);
                String nodeLabel = rs.getString("nodelabel");

                list.add(new OutageSummary(nodeId, nodeLabel, downDate));
            }

            rs.close();
            stmt.close();

            summaries = list.toArray(new OutageSummary[list.size()]);
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return summaries;
    }

    /**
     * <p>rs2Outages</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Outage[] rs2Outages(ResultSet rs) throws SQLException {
        return rs2Outages(rs, true);
    }

    /**
     * <p>rs2Outages</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @param includesRegainedTime a boolean.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Outage[] rs2Outages(ResultSet rs, boolean includesRegainedTime) throws SQLException {
        return rs2Outages(rs, includesRegainedTime, false);
    }

    /*
     * LJK Feb 21, 2002: all these special case result set methods need to be
     * cleaned up
     */
    /**
     * <p>rs2Outages</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @param includesRegainedTime a boolean.
     * @param includesNotifications a boolean.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Outage[] rs2Outages(ResultSet rs, boolean includesRegainedTime, boolean includesNotifications) throws SQLException {
        Outage[] outages = null;
        List<Outage> list = new ArrayList<Outage>();

        while (rs.next()) {
            Outage outage = new Outage();

            outage.nodeId = rs.getInt("nodeid");
            
            outage.ipAddress = rs.getString("ipaddr");

            outage.serviceId = rs.getInt("serviceid");

            outage.nodeLabel = rs.getString("nodeLabel");

            outage.hostname = rs.getString("iphostname");

            outage.serviceName = rs.getString("servicename");

            outage.outageId = rs.getInt("outageid");

            Timestamp lostService = rs.getTimestamp("iflostservice");
            if (!rs.wasNull()) {
                outage.lostServiceTime = new Date(lostService.getTime());
            }

            if (includesRegainedTime) {
                Timestamp regainedService = rs.getTimestamp("ifregainedservice");
                if (!rs.wasNull()) {
                    outage.regainedServiceTime = new Date(regainedService.getTime());
                }
            }

            if (includesNotifications) {
                int serviceLostEventId = rs.getInt("svclosteventid");
                if (!rs.wasNull()) {
                    outage.lostServiceEventId = new Integer(serviceLostEventId);
                }

                int notifyId = rs.getInt("notifyid");
                if (!rs.wasNull()) {
                    outage.lostServiceNotificationId = new Integer(notifyId);
                }

                outage.lostServiceNotificationAcknowledgedBy = rs.getString("answeredby");
            }

            list.add(outage);
        }

        outages = list.toArray(new Outage[list.size()]);

        return outages;
    }

}
