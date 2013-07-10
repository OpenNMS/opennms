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

package org.opennms.web.outage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.core.resource.Vault;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.filter.InterfaceFilter;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.ServiceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates all querying functionality for outages.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 */
public class OutageFactory extends Object {
	
	private static final Logger LOG = LoggerFactory.getLogger(OutageFactory.class);


    /** Constant <code>log</code> */

    /** Private constructor so this class cannot be instantiated. */
    private OutageFactory() {
    }

    /**
     * Return the count of current outages.
     *
     * <p>
     * Note: This method has been optimized for the simplest query.
     * </p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
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
     *
     * @param outageType a {@link org.opennms.web.outage.OutageType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return a int.
     * @throws java.sql.SQLException if any.
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

    /**
     * <p>getOutage</p>
     *
     * @param outageId a int.
     * @return a {@link org.opennms.web.outage.Outage} object.
     * @throws java.sql.SQLException if any.
     */
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
     *
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutages() throws SQLException {
        return OutageFactory.getOutages(SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT, new Filter[0], -1, -1);
    }

    /**
     * Return all unresolved outages sorted by the given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutages(SortStyle sortStyle) throws SQLException {
        return OutageFactory.getOutages(sortStyle, OutageType.CURRENT, new Filter[0], -1, -1);
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outType a {@link org.opennms.web.outage.OutageType} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutages(SortStyle sortStyle, OutageType outType) throws SQLException {
        return OutageFactory.getOutages(sortStyle, outType, new Filter[0], -1, -1);
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outType a {@link org.opennms.web.outage.OutageType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
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
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outType a {@link org.opennms.web.outage.OutageType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
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

            LOG.debug(select.toString());

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

    /**
     * Return all current outages sorted by time for the given node.
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForNode(int nodeId, ServletContext servletContext) throws SQLException {
        return (getOutagesForNode(nodeId, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT, servletContext));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given node.
     *
     * @param nodeId a int.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outType a {@link org.opennms.web.outage.OutageType} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForNode(int nodeId, SortStyle sortStyle, OutageType outType, ServletContext servletContext) throws SQLException {
        if (sortStyle == null || outType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext) };
        return (OutageFactory.getOutages(sortStyle, outType, filters));
    }

    /**
     * Return all unresolved notices for the given interface.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForInterface(int nodeId, String ipAddress, ServletContext servletContext) throws SQLException {
        return (getOutagesForInterface(nodeId, ipAddress, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT, servletContext));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given interface.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outType a {@link org.opennms.web.outage.OutageType} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForInterface(int nodeId, String ipAddress, SortStyle sortStyle, OutageType outType, ServletContext servletContext) throws SQLException {
        if (ipAddress == null || sortStyle == null || outType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext), new InterfaceFilter(ipAddress) };
        return (OutageFactory.getOutages(sortStyle, outType, filters));
    }

    /**
     * Return all outages (optionally only unresolved notices) sorted by id for
     * the given interface.
     *
     * @deprecated Replaced by
     *             {@link " #getOutagesForInterface(int,String,SortStyle,OutageType) getOutagesForInterface(int,String,SortStyle,OutageType)"}
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param includeResolved a boolean.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForInterface(int nodeId, String ipAddress, boolean includeResolved, ServletContext servletContext) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        OutageType outageType = includeResolved ? OutageType.BOTH : OutageType.CURRENT;
        Outage[] outages = getOutagesForInterface(nodeId, ipAddress, SortStyle.DEFAULT_SORT_STYLE, outageType, servletContext);

        return outages;
    }

    /**
     * Return all unacknowledged notices sorted by time for that have the given
     * IP address, regardless of what node they belong to.
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForInterface(String ipAddress) throws SQLException {
        return (getOutagesForInterface(ipAddress, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given IP address.
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outType a {@link org.opennms.web.outage.OutageType} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
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
     * @param ipAddress a {@link java.lang.String} object.
     * @param includeResolved a boolean.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForInterface(String ipAddress, boolean includeResolved) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        OutageType outageType = includeResolved ? OutageType.BOTH : OutageType.CURRENT;
        Outage[] outages = getOutagesForInterface(ipAddress, SortStyle.DEFAULT_SORT_STYLE, outageType);

        return outages;
    }

    /**
     * Return all unresolved outages sorted by time for the given service.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForService(int nodeId, String ipAddress, int serviceId, ServletContext servletContext) throws SQLException {
        return (getOutagesForService(nodeId, ipAddress, serviceId, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT, servletContext));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given service.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outType a {@link org.opennms.web.outage.OutageType} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForService(int nodeId, String ipAddress, int serviceId, SortStyle sortStyle, OutageType outType, ServletContext servletContext) throws SQLException {
        if (ipAddress == null || sortStyle == null || outType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext), new InterfaceFilter(ipAddress), new ServiceFilter(serviceId) };
        return (OutageFactory.getOutages(sortStyle, outType, filters));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by time
     * for the given service.
     *
     * @deprecated Replaced by
     *             {@link " #getOutagesForService(int,String,int,SortStyle,OutageType) getOutagesForInterface(int,String,int,SortStyle,OutageType)"}
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param includeResolved a boolean.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForService(int nodeId, String ipAddress, int serviceId, boolean includeResolved, ServletContext servletContext) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        OutageType outageType = includeResolved ? OutageType.BOTH : OutageType.CURRENT;
        Outage[] outages = getOutagesForService(nodeId, ipAddress, serviceId, SortStyle.DEFAULT_SORT_STYLE, outageType, servletContext);

        return outages;
    }

    /**
     * Return all unresolved outages sorted by time for the given service type,
     * regardless of what node or interface they belong to.
     *
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Outage[] getOutagesForService(int serviceId) throws SQLException {
        return (getOutagesForService(serviceId, SortStyle.DEFAULT_SORT_STYLE, OutageType.CURRENT));
    }

    /**
     * Return all outages (optionally only unresolved outages) sorted by given
     * sort style for the given service identifier.
     *
     * @param serviceId a int.
     * @param sortStyle a {@link org.opennms.web.outage.SortStyle} object.
     * @param outType a {@link org.opennms.web.outage.OutageType} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
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
     * @param serviceId a int.
     * @param includeResolved a boolean.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
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
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.outage.Outage} objects.
     * @throws java.sql.SQLException if any.
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
                outage.lostServiceEventId = Integer.valueOf(intElement);
            }

            // can be null
            intElement = rs.getInt("svcRegainedEventID");
            if (!rs.wasNull()) {
                outage.regainedServiceEventId = Integer.valueOf(intElement);
            }

            // can be null
            intElement = rs.getInt("notifyid");
            if (!rs.wasNull()) {
                outage.lostServiceNotificationId = Integer.valueOf(intElement);
            }

            // can be null
            outage.lostServiceNotificationAcknowledgedBy = rs.getString("answeredby");

            list.add(outage);
        }

        outages = list.toArray(new Outage[list.size()]);

        return outages;
    }

}
