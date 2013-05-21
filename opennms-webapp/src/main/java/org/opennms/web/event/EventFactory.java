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

package org.opennms.web.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.event.filter.IfIndexFilter;
import org.opennms.web.event.filter.InterfaceFilter;
import org.opennms.web.event.filter.NodeFilter;
import org.opennms.web.event.filter.ServiceFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;

/**
 * Encapsulates all querying functionality for events.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 */
public class EventFactory {

    /** Private constructor so this class cannot be instantiated. */
    private EventFactory() {
    }

    /**
     * Count all outstanding (unacknowledged) events.
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getEventCount() throws SQLException {
        return getEventCount(AcknowledgeType.UNACKNOWLEDGED, new Filter[0]);
    }

    /**
     * Count the number of events for a given acknowledgement type.
     *
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getEventCount(AcknowledgeType ackType, Filter[] filters) throws SQLException {
        if (ackType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int eventCount = 0;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT COUNT(EVENTID) AS EVENTCOUNT FROM EVENTS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) WHERE ");
            select.append(getAcknowledgeTypeClause(ackType));

            for (Filter filter : filters) {
                select.append(" AND");
                select.append(filter.getParamSql());
            }

            select.append(" AND EVENTDISPLAY='Y' ");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                eventCount = rs.getInt("EVENTCOUNT");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return eventCount;
    }

    /**
     * Count the number of events for a given acknowledgement type.
     *
     * @return An array of event counts. Each index of the array corresponds to
     *         the event severity for the counts (indeterminate is 1, critical
     *         is 7, etc).
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @throws java.sql.SQLException if any.
     */
    public static int[] getEventCountBySeverity(AcknowledgeType ackType, Filter[] filters) throws SQLException {
        if (ackType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int[] eventCounts = new int[8];
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT EVENTSEVERITY, COUNT(*) AS EVENTCOUNT FROM EVENTS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) WHERE ");
            select.append(getAcknowledgeTypeClause(ackType));

            for (Filter filter : filters) {
                select.append(" AND");
                select.append(filter.getParamSql());
            }

            select.append(" AND EVENTDISPLAY='Y'");
            select.append(" GROUP BY EVENTSEVERITY");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int severity = rs.getInt("EVENTSEVERITY");
                int eventCount = rs.getInt("EVENTCOUNT");

                eventCounts[severity] = eventCount;
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return eventCounts;
    }

    /**
     * Return a specific event.
     *
     * @param eventId a int.
     * @return a {@link org.opennms.web.event.Event} object.
     * @throws java.sql.SQLException if any.
     */
    public static Event getEvent(int eventId) throws SQLException {
        Event event = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT EVENTS.*, NODE.NODELABEL, SERVICE.SERVICENAME FROM EVENTS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) WHERE EVENTID=? ");
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            Event[] events = rs2Events(rs);

            // what do I do if this actually returns more than one service?
            if (events.length > 0) {
                event = events[0];
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return event;
    }

    /**
     * Return all unacknowledged events sorted by time.
     *
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEvents() throws SQLException {
        return getEvents(SortStyle.TIME, AcknowledgeType.UNACKNOWLEDGED);
    }

    /**
     * Return all unacknowledged or acknowledged events sorted by time.
     *
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEvents(AcknowledgeType ackType) throws SQLException {
        return getEvents(SortStyle.TIME, ackType);
    }

    /**
     * Return all unacknowledged events sorted by the given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEvents(SortStyle sortStyle) throws SQLException {
        return getEvents(sortStyle, AcknowledgeType.UNACKNOWLEDGED);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
     * given sort style.
     *
     * @deprecated Replaced by
     *             {@link " #getEvents(SortStyle,AcknowledgeType) getEvents(SortStyle, AcknowledgeType)"}
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEvents(SortStyle sortStyle, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEvents(sortStyle, ackType);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEvents(SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return getEvents(sortStyle, ackType, new Filter[0]);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEvents(SortStyle sortStyle, AcknowledgeType ackType, Filter[] filters) throws SQLException {
        return getEvents(sortStyle, ackType, filters, -1, -1);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
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
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEvents(SortStyle sortStyle, AcknowledgeType ackType, Filter[] filters, int limit, int offset) throws SQLException {
        if (sortStyle == null || ackType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        boolean useLimits = false;
        if (limit > 0 && offset > -1) {
            useLimits = true;
        }

        Event[] events = null;
        Connection conn = Vault.getDbConnection();

        try {
        	/*
            StringBuffer select = new StringBuffer("" +
            		"  SELECT EVENTS.*, NODE.NODELABEL, SERVICE.SERVICENAME " +
            		"    FROM EVENTS " +
            		"LEFT OUTER JOIN NODE USING(NODEID) " +
            		"LEFT OUTER JOIN SERVICE USING(SERVICEID) WHERE");
            */
            StringBuffer select = new StringBuffer("" +
            		"          SELECT events.*, node.nodelabel, service.servicename " + 
            		"            FROM node " + 
            		"RIGHT OUTER JOIN events " +
            		"              ON (events.nodeid = node.nodeid) " + 
            		" LEFT OUTER JOIN service " +
            		"              ON (service.serviceid = events.serviceid) " + 
            		"           WHERE ");
            
            select.append(getAcknowledgeTypeClause(ackType));

            for (Filter filter : filters) {
                select.append(" AND");
                select.append(filter.getParamSql());
            }

            select.append(" AND EVENTDISPLAY='Y' ");
            select.append(getOrderByClause(sortStyle));

            if (useLimits) {
                select.append(" LIMIT ");
                select.append(limit);
                select.append(" OFFSET ");
                select.append(offset);
            }

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();

            events = rs2Events(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return events;
    }

    /*
     * ****************************************************************************
     * N O D E M E T H O D S
     * ****************************************************************************
     */

    /**
     * Return all unacknowledged events sorted by event ID for the given node.
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForNode(int nodeId, ServletContext servletContext) throws SQLException {
        return getEventsForNode(nodeId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1, servletContext);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by event
     * ID for the given node.
     *
     * @deprecated Replaced by
     *             {@link " #getEventsForNode(int,SortStyle,AcknowledgeType) getEventsForNode( int, SortStyle, AcknowledgeType )"}
     * @param nodeId a int.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForNode(int nodeId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForNode(nodeId, SortStyle.ID, ackType, -1, -1, servletContext);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
     * given sort style for the given node.
     *
     * @param nodeId a int.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        return getEventsForNode(nodeId, sortStyle, ackType, -1, -1, servletContext);
    }

    /**
     * Return some maximum number of events or less (optionally only
     * unacknowledged events) sorted by the given sort style for the given node.
     *
     * @param throttle
     *            a value less than one means no throttling
     * @param nodeId a int.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @param offset a int.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext) };
        return getEvents(sortStyle, ackType, filters, throttle, offset);
    }

    /**
     * Return the number of events for this node and the given acknowledgment
     * type.
     *
     * @param nodeId a int.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getEventCountForNode(int nodeId, AcknowledgeType ackType) throws SQLException {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        int eventCount = 0;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT COUNT(EVENTID) AS EVENTCOUNT FROM EVENTS WHERE ");
            select.append(getAcknowledgeTypeClause(ackType));

            select.append(" AND NODEID=?");
            select.append(" AND EVENTDISPLAY='Y' ");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                eventCount = rs.getInt("EVENTCOUNT");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return eventCount;
    }

    /*
     * ****************************************************************************
     * I N T E R F A C E M E T H O D S
     * ****************************************************************************
     */

    /**
     * Return all unacknowledged events sorted by event ID for the given
     * interface.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForInterface(int nodeId, String ipAddress, ServletContext servletContext) throws SQLException {
        return getEventsForInterface(nodeId, ipAddress, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1, servletContext);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * for the given interface.
     *
     * @deprecated Replaced by
     *             {@link " #getEventsForInterface(int,String,SortStyle,AcknowledgeType) getEventsForInterface( int, String, SortStyle, AcknowledgeType )"}
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForInterface(int nodeId, String ipAddress, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForInterface(nodeId, ipAddress, SortStyle.ID, ackType, -1, -1, servletContext);
    }

    /**
     * Return some maximum number of events or less (optionally only
     * unacknowledged events) sorted by the given sort style for the given node
     * and IP address.
     *
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForInterface(int nodeId, String ipAddress, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext), new InterfaceFilter(ipAddress) };
        return getEvents(sortStyle, ackType, filters, throttle, offset);
    }

    /**
     * Return some maximum number of events or less (optionally only
     * unacknowledged events) sorted by the given sort style for the given node
     * and IP address.
     *
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     * @param nodeId a int.
     * @param ifIndex a int.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForInterface(int nodeId, int ifIndex, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext), new  IfIndexFilter(ifIndex)};
        return getEvents(sortStyle, ackType, filters, throttle, offset);
    }


    /**
     * Return all unacknowledged events sorted by time for that have the given
     * IP address, regardless of what node they belong to.
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForInterface(String ipAddress) throws SQLException {
        return getEventsForInterface(ipAddress, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * that have the given IP address, regardless of what node they belong to.
     *
     * @deprecated Replaced by
     *             {@link " #getEventsForInterface(String,SortStyle,AcknowledgeType) getEventsForInterface( String, SortStyle, AcknowledgeType )"}
     * @param ipAddress a {@link java.lang.String} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForInterface(String ipAddress, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForInterface(ipAddress, SortStyle.ID, ackType, -1, -1);
    }

    /**
     * Return some maximum number of events or less (optionally only
     * unacknowledged events) sorted by the given sort style for the given IP
     * address.
     *
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     * @param ipAddress a {@link java.lang.String} object.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForInterface(String ipAddress, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new InterfaceFilter(ipAddress) };
        return getEvents(sortStyle, ackType, filters, throttle, offset);
    }

    /**
     * Return the number of events for this node ID, IP address, and the given
     * acknowledgment type.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getEventCountForInterface(int nodeId, String ipAddress, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext), new InterfaceFilter(ipAddress) };
        return getEventCount(ackType, filters);
    }

    /**
     * Return the number of events for this IP address and the given
     * acknowledgment type.
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getEventCountForInterface(String ipAddress, AcknowledgeType ackType) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new InterfaceFilter(ipAddress) };
        return getEventCount(ackType, filters);
    }

    /*
     * ****************************************************************************
     * S E R V I C E M E T H O D S
     * ****************************************************************************
     */

    /**
     * Return all unacknowledged events sorted by time for the given service.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId, ServletContext servletContext) throws SQLException {
        return getEventsForService(nodeId, ipAddress, serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1, servletContext);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * for the given service.
     *
     * @deprecated Replaced by
     *             {@link " #getEventsForService(int,String,int,SortStyle,AcknowledgeType,int,int) getEventsForService( int, String, int, SortStyle, AcknowledgeType, int, int )"}
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForService(nodeId, ipAddress, serviceId, SortStyle.ID, ackType, -1, -1, servletContext);
    }

    /**
     * Return some maximum number of events or less (optionally only
     * unacknowledged events) sorted by the given sort style for the given node,
     * IP address, and service ID.
     *
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext), new InterfaceFilter(ipAddress), new ServiceFilter(serviceId, servletContext) };
        return getEvents(sortStyle, ackType, filters, throttle, offset);
    }

    /**
     * Return all unacknowledged events sorted by time for the given service
     * type, regardless of what node or interface they belong to.
     *
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForService(int serviceId, ServletContext servletContext) throws SQLException {
        return getEventsForService(serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1, servletContext);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * for the given service type, regardless of what node or interface they
     * belong to.
     *
     * @param serviceId a int.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForService(int serviceId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForService(serviceId, SortStyle.ID, ackType, -1, -1, servletContext);
    }

    /**
     * Return some maximum number of events or less (optionally only
     * unacknowledged events) sorted by the given sort style for the given
     * service ID.
     *
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     * @param serviceId a int.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForService(int serviceId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new ServiceFilter(serviceId, servletContext) };
        return getEvents(sortStyle, ackType, filters, throttle, offset);
    }

    /**
     * Return the number of events for this node ID, IP address, service ID, and
     * the given acknowledgement type.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getEventCountForService(int nodeId, String ipAddress, int serviceId, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId, servletContext), new InterfaceFilter(ipAddress), new ServiceFilter(serviceId, servletContext) };
        return getEventCount(ackType, filters);
    }

    /**
     * Return the number of events for this node ID, IP address, service ID, and
     * the given acknowledgement type.
     *
     * @param serviceId a int.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getEventCountForService(int serviceId, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new ServiceFilter(serviceId, servletContext) };
        return (getEventCount(ackType, filters));
    }

    /**
     * Return all unacknowledged events sorted by time for the given severity.
     *
     * @param severity a int.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForSeverity(int severity) throws SQLException {
        return getEventsForSeverity(severity, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * for the given severity.
     *
     * @deprecated Replaced by
     *             {@link " #getEventsForSeverity(int,SortStyle,AcknowledgeType) getEventsForSeverity( int, SortStyle, AcknowledgeType )"}
     * @param severity a int.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForSeverity(int severity, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForSeverity(severity, SortStyle.ID, ackType);
    }

    /**
     * <p>getEventsForSeverity</p>
     *
     * @param severity a int.
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @param ackType a {@link org.opennms.web.event.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForSeverity(int severity, SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return getEvents(sortStyle, ackType, new Filter[] { new SeverityFilter(severity) });
    }

    /**
     * Return all unacknowledged events sorted by time for that have the given
     * distributed poller.
     *
     * @param poller a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForPoller(String poller) throws SQLException {
        return getEventsForPoller(poller, false);
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * that have the given distributed poller.
     *
     * @param poller a {@link java.lang.String} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Event[] getEventsForPoller(String poller, boolean includeAcknowledged) throws SQLException {
        if (poller == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Event[] events = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT * FROM EVENTS WHERE EVENTDPNAME=?");

            if (!includeAcknowledged) {
                select.append(" AND EVENTACKUSER IS NULL");
            }

            select.append(" AND EVENTDISPLAY='Y' ");
            select.append(" ORDER BY EVENTID DESC");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            stmt.setString(1, poller);
            ResultSet rs = stmt.executeQuery();

            events = rs2Events(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return events;
    }

    /**
     * Acknowledge a list of events with the given username and the current
     * time.
     *
     * @param events an array of {@link org.opennms.web.event.Event} objects.
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Event[] events, String user) throws SQLException {
        acknowledge(events, user, new Date());
    }

    /**
     * Acknowledge a list of events with the given username and the given time.
     *
     * @param events an array of {@link org.opennms.web.event.Event} objects.
     * @param user a {@link java.lang.String} object.
     * @param time a java$util$Date object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Event[] events, String user, Date time) throws SQLException {
        if (events == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int[] ids = new int[events.length];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = events[i].getId();
        }

        acknowledge(ids, user, time);
    }

    /**
     * Acknowledge a list of events with the given username and the current
     * time.
     *
     * @param eventIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(int[] eventIds, String user) throws SQLException {
        acknowledge(eventIds, user, new Date());
    }

    /**
     * Acknowledge a list of events with the given username and the given time.
     *
     * @param eventIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @param time a java$util$Date object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(int[] eventIds, String user, Date time) throws SQLException {
        if (eventIds == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (eventIds.length > 0) {
            StringBuffer update = new StringBuffer("UPDATE EVENTS SET EVENTACKUSER=?, EVENTACKTIME=?");
            update.append(" WHERE EVENTID IN (");
            update.append(eventIds[0]);

            for (int i = 1; i < eventIds.length; i++) {
                update.append(",");
                update.append(eventIds[i]);
            }

            update.append(")");
            update.append(" AND EVENTACKUSER IS NULL");

            Connection conn = Vault.getDbConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement(update.toString());
                stmt.setString(1, user);
                stmt.setTimestamp(2, new Timestamp(time.getTime()));

                stmt.executeUpdate();
                stmt.close();
            } finally {
                Vault.releaseDbConnection(conn);
            }
        }
    }

    /**
     * Acknowledge with the given username and the current time all events that
     * match the given filter criteria.
     *
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Filter[] filters, String user) throws SQLException {
        acknowledge(filters, user, new Date());
    }

    /**
     * Acknowledge with the given username and the given time all events that
     * match the given filter criteria.
     *
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @param user a {@link java.lang.String} object.
     * @param time a java$util$Date object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Filter[] filters, String user, Date time) throws SQLException {
        if (filters == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer update = new StringBuffer("UPDATE EVENTS SET EVENTACKUSER=?, EVENTACKTIME=? WHERE");
        update.append(getAcknowledgeTypeClause(AcknowledgeType.UNACKNOWLEDGED));

        for (Filter filter : filters) {
            update.append(" AND");
            update.append(filter.getParamSql());
        }

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement(update.toString());
            stmt.setString(1, user);
            stmt.setTimestamp(2, new Timestamp(time.getTime()));
            
            int parameterIndex = 3;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }

            stmt.executeUpdate();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
    }

    /**
     * Acknowledge all unacknowledged events with the given username and the
     * given time.
     *
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledgeAll(String user) throws SQLException {
        acknowledgeAll(user, new Date());
    }

    /**
     * Acknowledge all unacknowledged events with the given username and the
     * given time.
     *
     * @param user a {@link java.lang.String} object.
     * @param time a java$util$Date object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledgeAll(String user, Date time) throws SQLException {
        if (user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE EVENTS SET EVENTACKUSER=?, EVENTACKTIME=? WHERE EVENTACKUSER IS NULL");
            stmt.setString(1, user);
            stmt.setTimestamp(2, new Timestamp(time.getTime()));

            stmt.executeUpdate();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

    }

    /**
     * Unacknowledge a list of events.
     *
     * @param events an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    public static void unacknowledge(Event[] events) throws SQLException {
        if (events == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int[] ids = new int[events.length];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = events[i].getId();
        }

        unacknowledge(ids);
    }

    /**
     * Unacknowledge a list of events.
     *
     * @param eventIds an array of int.
     * @throws java.sql.SQLException if any.
     */
    public static void unacknowledge(int[] eventIds) throws SQLException {
        if (eventIds == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (eventIds.length > 0) {
            StringBuffer update = new StringBuffer("UPDATE EVENTS SET EVENTACKUSER=NULL, EVENTACKTIME=NULL");
            update.append(" WHERE EVENTID IN (");
            update.append(eventIds[0]);

            for (int i = 1; i < eventIds.length; i++) {
                update.append(",");
                update.append(eventIds[i]);
            }

            update.append(")");

            Connection conn = Vault.getDbConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement(update.toString());
                stmt.executeUpdate();
                stmt.close();
            } finally {
                Vault.releaseDbConnection(conn);
            }
        }
    }

    /**
     * Unacknowledge events that match the given filter criteria.
     *
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @throws java.sql.SQLException if any.
     */
    public static void unacknowledge(Filter[] filters) throws SQLException {
        if (filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer update = new StringBuffer("UPDATE EVENTS SET EVENTACKUSER=NULL, EVENTACKTIME=NULL WHERE");
        update.append(getAcknowledgeTypeClause(AcknowledgeType.ACKNOWLEDGED));

        for (Filter filter : filters) {
            update.append(" AND");
            update.append(filter.getParamSql());
        }

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement(update.toString());
            
            int parameterIndex = 1;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }

            stmt.executeUpdate();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
    }

    /**
     * Unacknowledge all acknowledged events.
     *
     * @throws java.sql.SQLException if any.
     */
    public static void unacknowledgeAll() throws SQLException {
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE EVENTS SET EVENTACKUSER=NULL, EVENTACKTIME=NULL WHERE EVENTACKUSER IS NOT NULL");
            stmt.executeUpdate();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

    }

    /**
     * Convenience method for translating a <code>java.sql.ResultSet</code>
     * containing event information into an array of <code>Event</code>
     * objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Event[] rs2Events(ResultSet rs) throws SQLException {
        Event[] events = null;
        Vector<Event> vector = new Vector<Event>();

        while (rs.next()) {
            Event event = new Event();

            event.id = rs.getInt("eventID");
            event.uei = rs.getString("eventUei");
            event.snmp = rs.getString("eventSnmp");

            // event received time cannot be null
            Timestamp timestamp = rs.getTimestamp("eventTime");
            event.time = new Date(timestamp.getTime());

            event.host = rs.getString("eventHost");
            event.snmphost = rs.getString("eventSnmpHost");
            event.dpName = rs.getString("eventDpName");
            event.parms = rs.getString("eventParms");

            // node id can be null
            Object element = rs.getObject("nodeID");
            event.nodeID = (element == null) ? Integer.valueOf(0) : (Integer) element;

            event.ipAddr = rs.getString("ipAddr");

            // service id can be null 
            element = rs.getObject("serviceID");
            event.serviceID = (element == null ) ? Integer.valueOf(0) : (Integer) element;

            event.nodeLabel = rs.getString("nodeLabel");
            event.serviceName = rs.getString("serviceName");

            // event create time cannot be null 
            timestamp = rs.getTimestamp("eventCreateTime");
            event.createTime = new Date(timestamp.getTime());

            event.description = rs.getString("eventDescr");
            event.logGroup = rs.getString("eventLoggroup");
            event.logMessage = rs.getString("eventLogmsg");

            event.severity = OnmsSeverity.get(rs.getInt("eventSeverity"));

            event.operatorInstruction = rs.getString("eventOperInstruct");
            event.autoAction = rs.getString("eventAutoAction");
            event.operatorAction = rs.getString("eventOperAction");
            event.operatorActionMenuText = rs.getString("eventOperActionMenuText");
            event.notification = rs.getString("eventNotification");
            event.troubleTicket = rs.getString("eventTticket");

            // trouble ticket state can be null
            element = rs.getObject("eventTticketState");
            event.troubleTicketState = (element == null ) ? Integer.valueOf(0) : (Integer) element;

            event.forward = rs.getString("eventForward");
            event.mouseOverText = rs.getString("eventMouseOverText");

            event.acknowledgeUser = rs.getString("eventAckUser");

            // acknowledge time can be null
            timestamp = rs.getTimestamp("eventAckTime");
            event.acknowledgeTime = (timestamp != null) ? new Date(timestamp.getTime()) : null; 

            // alarm id can be null
            element = rs.getObject("alarmid");
            event.alarmId = (element == null ) ? Integer.valueOf(0) : (Integer) element;

            vector.addElement(event);
        }

        events = new Event[vector.size()];

        for (int i = 0; i < events.length; i++) {
            events[i] = vector.elementAt(i);
        }

        return events;
    }

    /**
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * to a given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.event.SortStyle} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String getOrderByClause(SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        return sortStyle.getOrderByClause();
    }

    /**
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * to a given sort style.
     *
     * @param ackType
     *            the acknowledge type to map to a clause
     * @return a {@link java.lang.String} object.
     */
    protected static String getAcknowledgeTypeClause(AcknowledgeType ackType) {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        return ackType.getAcknowledgeTypeClause();
    }

}
