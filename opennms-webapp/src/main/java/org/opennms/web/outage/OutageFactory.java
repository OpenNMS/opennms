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
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
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

package org.opennms.web.outage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.filter.InterfaceFilter;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.ServiceFilter;

/**
 * Encapsulates all querying functionality for outages.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class OutageFactory extends Object {

    protected static final ThreadCategory log = ThreadCategory.getInstance("OutageFactory");

    /** Private constructor so this class cannot be instantiated. */
    private OutageFactory() {
    }

    /**
     * Return the count of current outages.
     * 
     * <p>
     * Note: This method has been optimized for the simplest query.
     * </p>
     */
    public static int getOutageCount() throws SQLException {
        int outageCount = 0;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(OUTAGEID) AS OUTAGECOUNT FROM OUTAGES " + "JOIN NODE USING(NODEID) " + "JOIN IPINTERFACE ON OUTAGES.NODEID=IPINTERFACE.NODEID AND OUTAGES.IPADDR=IPINTERFACE.IPADDR " + "JOIN IFSERVICES ON OUTAGES.NODEID=IFSERVICES.NODEID AND OUTAGES.IPADDR=IFSERVICES.IPADDR AND OUTAGES.SERVICEID=IFSERVICES.SERVICEID " + "WHERE IFREGAINEDSERVICE IS NULL " + "AND (NODE.NODETYPE != 'D' AND IPINTERFACE.ISMANAGED != 'D' AND IFSERVICES.STATUS != 'D') ");

            if (rs.next()) {
                outageCount = rs.getInt("OUTAGECOUNT");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outageCount;
    }

    /**
     * Count the number of outages for a given outage type.
     */
    public static int getOutageCount(OutageType outageType, Filter[] filters) throws SQLException {
        if (outageType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int outageCount = 0;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT COUNT(OUTAGEID) AS OUTAGECOUNT FROM OUTAGES " + "JOIN NODE USING(NODEID) " + "JOIN IPINTERFACE ON OUTAGES.NODEID=IPINTERFACE.NODEID AND OUTAGES.IPADDR=IPINTERFACE.IPADDR " + "JOIN IFSERVICES ON OUTAGES.NODEID=IFSERVICES.NODEID AND OUTAGES.IPADDR=IFSERVICES.IPADDR AND OUTAGES.SERVICEID=IFSERVICES.SERVICEID " + "LEFT OUTER JOIN SERVICE ON OUTAGES.SERVICEID=SERVICE.SERVICEID " + "LEFT OUTER JOIN NOTIFICATIONS ON SVCLOSTEVENTID=NOTIFICATIONS.NOTIFYID " + "WHERE (NODE.NODETYPE != 'D' AND IPINTERFACE.ISMANAGED != 'D' AND IFSERVICES.STATUS != 'D') " + "AND ");
            select.append(outageType.getClause());

            for (Filter filter : filters) {
                select.append(" AND ");
                select.append(filter.getParamSql());
            }

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                outageCount = rs.getInt("OUTAGECOUNT");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outageCount;
    }

    public static Outage getOutage(int outageId) throws SQLException {
        Outage outage = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT OUTAGES.*, NODE.NODELABEL, IPINTERFACE.IPHOSTNAME, SERVICE.SERVICENAME, NOTIFICATIONS.NOTIFYID, NOTIFICATIONS.ANSWEREDBY FROM OUTAGES " + "JOIN NODE USING(NODEID) " + "JOIN IPINTERFACE ON OUTAGES.NODEID=IPINTERFACE.NODEID AND OUTAGES.IPADDR=IPINTERFACE.IPADDR " + "LEFT OUTER JOIN SERVICE USING(SERVICEID) " + "LEFT OUTER JOIN NOTIFICATIONS ON SVCLOSTEVENTID=NOTIFICATIONS.EVENTID " + "WHERE OUTAGEID=?");
            stmt.setInt(1, outageId);
            ResultSet rs = stmt.executeQuery();

            Outage[] outages = rs2Outages(rs);

            if (outages != null && outages.length > 0) {
                outage = outages[0];
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outage;
    }

    /**
     * Return all unresolved outages sorted by the default sort style, outage
     * identifier.
     */
    public static Outage[] getOutages() throws SQLException {
        return OutageFactory.getOutages(SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT, new Filter[0], -1, -1);
    }

    /** Return all unresolved outages sorted by the given sort style. */
    public static Outage[] getOutages(SortStyle sortStyle) throws SQLException {
        return OutageFactory.getOutages(sortStyle, OutageType.CURRENT, new Filter[0], -1, -1);
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by the
     * given sort style.
     */
    public static Outage[] getOutages(SortStyle sortStyle, OutageType outType) throws SQLException {
        return OutageFactory.getOutages(sortStyle, outType, new Filter[0], -1, -1);
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by the
     * given sort style.
     */
    public static Outage[] getOutages(SortStyle sortStyle, OutageType outType, Filter[] filters) throws SQLException {
        return OutageFactory.getOutages(sortStyle, outType, filters, -1, -1);
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by the
     * given sort style.
     * 
     * <p>
     * <strong>Note: </strong> This limit/offset code is <em>Postgres 
     * specific!</em>
     * Per <a href="mailto:shaneo@opennms.org">Shane </a>, this is okay for now
     * until we can come up with an Oracle alternative too.
     * </p>
     * 
     * @param limit
     *            if -1 or zero, no limit or offset is used
     * @param offset
     *            if -1, no limit or offset if used
     */
    public static Outage[] getOutages(SortStyle sortStyle, OutageType outType, Filter[] filters, int limit, int offset) throws SQLException {
        if (sortStyle == null || outType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        boolean useLimits = false;
        if (limit > 0 && offset > -1) {
            useLimits = true;
        }

        Outage[] outages = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT OUTAGES.*, NODE.NODELABEL, IPINTERFACE.IPHOSTNAME, SERVICE.SERVICENAME, NOTIFICATIONS.NOTIFYID, NOTIFICATIONS.ANSWEREDBY FROM OUTAGES " + "JOIN NODE USING(NODEID) " + "JOIN IPINTERFACE ON OUTAGES.NODEID=IPINTERFACE.NODEID AND OUTAGES.IPADDR=IPINTERFACE.IPADDR " + "JOIN IFSERVICES ON OUTAGES.NODEID=IFSERVICES.NODEID AND OUTAGES.IPADDR=IFSERVICES.IPADDR AND OUTAGES.SERVICEID=IFSERVICES.SERVICEID " + "LEFT OUTER JOIN SERVICE ON OUTAGES.SERVICEID=SERVICE.SERVICEID " + "LEFT OUTER JOIN NOTIFICATIONS ON SVCLOSTEVENTID=NOTIFICATIONS.EVENTID " + "WHERE (NODE.NODETYPE != 'D' AND IPINTERFACE.ISMANAGED != 'D' AND IFSERVICES.STATUS != 'D') " + "AND ");
            select.append(outType.getClause());

            for (Filter filter : filters) {
                select.append(" AND ");
                select.append(filter.getParamSql());
            }

            select.append(sortStyle.getOrderByClause());

            if (useLimits) {
                select.append(" LIMIT ");
                select.append(limit);
                select.append(" OFFSET ");
                select.append(offset);
            }

            log.debug(select.toString());

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();

            outages = rs2Outages(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return outages;
    }

    /** Return all current outages sorted by time for the given node. */
    public static Outage[] getOutagesForNode(int nodeId) throws SQLException {
        return (getOutagesForNode(nodeId, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given node.
     */
    public static Outage[] getOutagesForNode(int nodeId, SortStyle sortStyle, OutageType outType) throws SQLException {
        if (sortStyle == null || outType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId) };
        return (OutageFactory.getOutages(sortStyle, outType, filters));
    }

    /** Return all unresolved notices for the given interface. */
    public static Outage[] getOutagesForInterface(int nodeId, String ipAddress) throws SQLException {
        return (getOutagesForInterface(nodeId, ipAddress, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given interface.
     */
    public static Outage[] getOutagesForInterface(int nodeId, String ipAddress, SortStyle sortStyle, OutageType outType) throws SQLException {
        if (ipAddress == null || sortStyle == null || outType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress) };
        return (OutageFactory.getOutages(sortStyle, outType, filters));
    }

    /**
     * Return all outages (optionally only unresolved notices) sorted by id for
     * the given interface.
     * 
     * @deprecated Replaced by
     *             {@link " #getOutagesForInterface(int,String,SortStyle,OutageType) getOutagesForInterface(int,String,SortStyle,OutageType)"}
     */
    public static Outage[] getOutagesForInterface(int nodeId, String ipAddress, boolean includeResolved) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        OutageType outageType = includeResolved ? OutageType.BOTH : OutageType.CURRENT;
        Outage[] outages = getOutagesForInterface(nodeId, ipAddress, SortStyle.DEFAULT_SORT_STYLE, outageType);

        return outages;
    }

    /**
     * Return all unacknowledged notices sorted by time for that have the given
     * IP address, regardless of what node they belong to.
     */
    public static Outage[] getOutagesForInterface(String ipAddress) throws SQLException {
        return (getOutagesForInterface(ipAddress, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given IP address.
     */
    public static Outage[] getOutagesForInterface(String ipAddress, SortStyle sortStyle, OutageType outType) throws SQLException {
        if (ipAddress == null || sortStyle == null || outType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new InterfaceFilter(ipAddress) };
        return (OutageFactory.getOutages(sortStyle, outType, filters));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by id that
     * have the given IP address, regardless of what node they belong to.
     * 
     * @deprecated Replaced by
     *             {@link " #getOutagesForInterface(String,SortStyle,OutageType) getOutagesForInterface(String,SortStyle,OutageType)"}
     */
    public static Outage[] getOutagesForInterface(String ipAddress, boolean includeResolved) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        OutageType outageType = includeResolved ? OutageType.BOTH : OutageType.CURRENT;
        Outage[] outages = getOutagesForInterface(ipAddress, SortStyle.DEFAULT_SORT_STYLE, outageType);

        return outages;
    }

    /** Return all unresolved outages sorted by time for the given service. */
    public static Outage[] getOutagesForService(int nodeId, String ipAddress, int serviceId) throws SQLException {
        return (getOutagesForService(nodeId, ipAddress, serviceId, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given service.
     */
    public static Outage[] getOutagesForService(int nodeId, String ipAddress, int serviceId, SortStyle sortStyle, OutageType outType) throws SQLException {
        if (ipAddress == null || sortStyle == null || outType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress), new ServiceFilter(serviceId) };
        return (OutageFactory.getOutages(sortStyle, outType, filters));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by time
     * for the given service.
     * 
     * @deprecated Replaced by
     *             {@link " #getOutagesForService(int,String,int,SortStyle,OutageType) getOutagesForInterface(int,String,int,SortStyle,OutageType)"}
     */
    public static Outage[] getOutagesForService(int nodeId, String ipAddress, int serviceId, boolean includeResolved) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        OutageType outageType = includeResolved ? OutageType.BOTH : OutageType.CURRENT;
        Outage[] outages = getOutagesForService(nodeId, ipAddress, serviceId, SortStyle.DEFAULT_SORT_STYLE, outageType);

        return outages;
    }

    /**
     * Return all unresolved outages sorted by time for the given service type,
     * regardless of what node or interface they belong to.
     */
    public static Outage[] getOutagesForService(int serviceId) throws SQLException {
        return (getOutagesForService(serviceId, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given service identifier.
     */
    public static Outage[] getOutagesForService(int serviceId, SortStyle sortStyle, OutageType outType) throws SQLException {
        if (sortStyle == null || outType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new ServiceFilter(serviceId) };
        return (OutageFactory.getOutages(sortStyle, outType, filters));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by id for
     * the given service type, regardless of what node or interface they belong
     * to.
     * 
     * @deprecated Replaced by
     *             {@link " #getOutagesForService(int,SortStyle,OutageType) getOutagesForInterface(int,SortStyle,OutageType)"}
     */
    public static Outage[] getOutagesForService(int serviceId, boolean includeResolved) throws SQLException {
        OutageType outageType = includeResolved ? OutageType.BOTH : OutageType.CURRENT;
        Outage[] outages = getOutagesForService(serviceId, SortStyle.DEFAULT_SORT_STYLE, outageType);

        return outages;
    }

    /**
     * Convenience method for translating a <code>java.sql.ResultSet</code>
     * containing outage information into an array of <code>Outage</code>
     * objects.
     */
    protected static Outage[] rs2Outages(ResultSet rs) throws SQLException {
        Outage[] outages = null;
        List<Outage> list = new ArrayList<Outage>();

        // FIXME: Don't reuse the "element" variable for multiple objects.
        while (rs.next()) {
            Outage outage = new Outage();

            Object element = null;
            int intElement = -1;

            // cannot be null
            outage.outageId = rs.getInt("outageid");
            outage.nodeId = rs.getInt("nodeid");
            outage.ipAddress = rs.getString("ipaddr");
            outage.serviceId = rs.getInt("serviceid");

            // cannot be null
            element = rs.getTimestamp("iflostservice");
            outage.lostServiceTime = new java.util.Date(((Timestamp) element).getTime());

            // can be null
            outage.hostname = rs.getString("iphostname"); // from ipinterface
                                                            // table

            // can be null
            outage.nodeLabel = rs.getString("nodelabel"); // from node table

            // can be null
            outage.serviceName = rs.getString("servicename"); // from service
                                                                // table

            // can be null
            element = rs.getTimestamp("ifregainedservice");
            if (element != null) {
                outage.regainedServiceTime = new java.util.Date(((Timestamp) element).getTime());
            }

            // can be null
            intElement = rs.getInt("svcLostEventID");
            if (!rs.wasNull()) {
                outage.lostServiceEventId = new Integer(intElement);
            }

            // can be null
            intElement = rs.getInt("svcRegainedEventID");
            if (!rs.wasNull()) {
                outage.regainedServiceEventId = new Integer(intElement);
            }

            // can be null
            intElement = rs.getInt("notifyid");
            if (!rs.wasNull()) {
                outage.lostServiceNotificationId = new Integer(intElement);
            }

            // can be null
            outage.lostServiceNotificationAcknowledgedBy = rs.getString("answeredby");

            list.add(outage);
        }

        outages = list.toArray(new Outage[list.size()]);

        return outages;
    }

}
