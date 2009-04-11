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

package org.opennms.web.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import org.opennms.core.resource.Vault;
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
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class EventFactory extends Object {

    /** Convenience class to determine sort style of a query. */
    public static class SortStyle extends Object {
        /* CORBA-style enumeration */
        public static final int _SEVERITY = 1;

        public static final int _TIME = 2;

        public static final int _NODE = 3;

        public static final int _INTERFACE = 4;

        public static final int _SERVICE = 5;

        public static final int _POLLER = 6;

        public static final int _ID = 7;

        public static final SortStyle SEVERITY = new SortStyle("SEVERITY", _SEVERITY);

        public static final SortStyle TIME = new SortStyle("TIME", _TIME);

        public static final SortStyle NODE = new SortStyle("NODE", _NODE);

        public static final SortStyle INTERFACE = new SortStyle("INTERFACE", _INTERFACE);

        public static final SortStyle SERVICE = new SortStyle("SERVICE", _SERVICE);

        public static final SortStyle POLLER = new SortStyle("POLLER", _POLLER);

        public static final SortStyle ID = new SortStyle("ID", _ID);

        public static final int _REVERSE_SEVERITY = 101;

        public static final int _REVERSE_TIME = 102;

        public static final int _REVERSE_NODE = 103;

        public static final int _REVERSE_INTERFACE = 104;

        public static final int _REVERSE_SERVICE = 105;

        public static final int _REVERSE_POLLER = 106;

        public static final int _REVERSE_ID = 107;

        public static final SortStyle REVERSE_SEVERITY = new SortStyle("REVERSE_SEVERITY", _REVERSE_SEVERITY);

        public static final SortStyle REVERSE_TIME = new SortStyle("REVERSE_TIME", _REVERSE_TIME);

        public static final SortStyle REVERSE_NODE = new SortStyle("REVERSE_NODE", _REVERSE_NODE);

        public static final SortStyle REVERSE_INTERFACE = new SortStyle("REVERSE_INTERFACE", _REVERSE_INTERFACE);

        public static final SortStyle REVERSE_SERVICE = new SortStyle("REVERSE_SERVICE", _REVERSE_SERVICE);

        public static final SortStyle REVERSE_POLLER = new SortStyle("REVERSE_POLLER", _REVERSE_POLLER);

        public static final SortStyle REVERSE_ID = new SortStyle("REVERSE_ID", _REVERSE_ID);

        protected String name;

        protected int id;

        private SortStyle(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String toString() {
            return ("Event.SortStyle." + this.name);
        }

        public String getName() {
            return (this.name);
        }

        public int getId() {
            return (this.id);
        }
    }

    /** Convenience class to determine what sort of events to include in a query. */
    public static class AcknowledgeType extends Object {
        /* CORBA-style enumeration */
        public static final int _ACKNOWLEDGED = 1;

        public static final int _UNACKNOWLEDGED = 2;

        public static final int _BOTH = 3;

        public static final AcknowledgeType ACKNOWLEDGED = new AcknowledgeType("ACKNOWLEDGED", _ACKNOWLEDGED);

        public static final AcknowledgeType UNACKNOWLEDGED = new AcknowledgeType("UNACKNOWLEDGED", _UNACKNOWLEDGED);

        public static final AcknowledgeType BOTH = new AcknowledgeType("BOTH", _BOTH);

        protected String name;

        protected int id;

        private AcknowledgeType(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String toString() {
            return ("Event.AcknowledgeType." + this.name);
        }

        public String getName() {
            return (this.name);
        }

        public int getId() {
            return (this.id);
        }

    }

    /** Private constructor so this class cannot be instantiated. */
    private EventFactory() {
    }

    /**
     * Count all outstanding (unacknowledged) events.
     */
    public static int getEventCount() throws SQLException {
        return getEventCount(AcknowledgeType.UNACKNOWLEDGED, new Filter[0]);
    }

    /**
     * Count the number of events for a given acknowledgement type.
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

            for (int i = 0; i < filters.length; i++) {
                select.append(" AND");
                select.append(filters[i].getParamSql());
            }

            select.append(" AND EVENTDISPLAY='Y' ");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
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

            for (int i = 0; i < filters.length; i++) {
                select.append(" AND");
                select.append(filters[i].getParamSql());
            }

            select.append(" AND EVENTDISPLAY='Y'");
            select.append(" GROUP BY EVENTSEVERITY");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
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

    /** Return a specific event. */
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

    /** Return all unacknowledged events sorted by time. */
    public static Event[] getEvents() throws SQLException {
        return (EventFactory.getEvents(SortStyle.TIME, AcknowledgeType.UNACKNOWLEDGED));
    }

    /** Return all unacknowledged or acknowledged events sorted by time. */
    public static Event[] getEvents(AcknowledgeType ackType) throws SQLException {
        return (EventFactory.getEvents(SortStyle.TIME, ackType));
    }

    /** Return all unacknowledged events sorted by the given sort style. */
    public static Event[] getEvents(SortStyle sortStyle) throws SQLException {
        return (EventFactory.getEvents(sortStyle, AcknowledgeType.UNACKNOWLEDGED));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
     * given sort style.
     * 
     * @deprecated Replaced by
     *             {@link " #getEvents(SortStyle,AcknowledgeType) getEvents(SortStyle, AcknowledgeType)"}
     */
    public static Event[] getEvents(SortStyle sortStyle, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (EventFactory.getEvents(sortStyle, ackType));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
     * given sort style.
     */
    public static Event[] getEvents(SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return (EventFactory.getEvents(sortStyle, ackType, new Filter[0]));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
     * given sort style.
     */
    public static Event[] getEvents(SortStyle sortStyle, AcknowledgeType ackType, Filter[] filters) throws SQLException {
        return (EventFactory.getEvents(sortStyle, ackType, filters, -1, -1));
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

            for (int i = 0; i < filters.length; i++) {
                select.append(" AND");
                select.append(filters[i].getParamSql());
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
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
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

    /** Return all unacknowledged events sorted by event ID for the given node. */
    public static Event[] getEventsForNode(int nodeId) throws SQLException {
        return (getEventsForNode(nodeId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by event
     * ID for the given node.
     * 
     * @deprecated Replaced by
     *             {@link " #getEventsForNode(int,SortStyle,AcknowledgeType) getEventsForNode( int, SortStyle, AcknowledgeType )"}
     */
    public static Event[] getEventsForNode(int nodeId, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (getEventsForNode(nodeId, SortStyle.ID, ackType, -1, -1));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by the
     * given sort style for the given node.
     */
    public static Event[] getEventsForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return (getEventsForNode(nodeId, sortStyle, ackType, -1, -1));
    }

    /**
     * Return some maximum number of events or less (optionally only
     * unacknowledged events) sorted by the given sort style for the given node.
     * 
     * @param throttle
     *            a value less than one means no throttling
     */
    public static Event[] getEventsForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId) };
        return (EventFactory.getEvents(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return the number of events for this node and the given acknowledgement
     * type.
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
     */
    public static Event[] getEventsForInterface(int nodeId, String ipAddress) throws SQLException {
        return (getEventsForInterface(nodeId, ipAddress, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * for the given interface.
     * 
     * @deprecated Replaced by
     *             {@link " #getEventsForInterface(int,String,SortStyle,AcknowledgeType) getEventsForInterface( int, String, SortStyle, AcknowledgeType )"}
     */
    public static Event[] getEventsForInterface(int nodeId, String ipAddress, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (getEventsForInterface(nodeId, ipAddress, SortStyle.ID, ackType, -1, -1));
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
     */
    public static Event[] getEventsForInterface(int nodeId, String ipAddress, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress) };
        return (EventFactory.getEvents(sortStyle, ackType, filters, throttle, offset));
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
     */
    public static Event[] getEventsForInterface(int nodeId, int ifIndex, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new  IfIndexFilter(ifIndex)};
        return (EventFactory.getEvents(sortStyle, ackType, filters, throttle, offset));
    }


    /**
     * Return all unacknowledged events sorted by time for that have the given
     * IP address, regardless of what node they belong to.
     */
    public static Event[] getEventsForInterface(String ipAddress) throws SQLException {
        return (getEventsForInterface(ipAddress, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * that have the given IP address, regardless of what node they belong to.
     * 
     * @deprecated Replaced by
     *             {@link " #getEventsForInterface(String,SortStyle,AcknowledgeType) getEventsForInterface( String, SortStyle, AcknowledgeType )"}
     */
    public static Event[] getEventsForInterface(String ipAddress, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (getEventsForInterface(ipAddress, SortStyle.ID, ackType, -1, -1));
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
     */
    public static Event[] getEventsForInterface(String ipAddress, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new InterfaceFilter(ipAddress) };
        return (EventFactory.getEvents(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return the number of events for this node ID, IP address, and the given
     * acknowledgement type.
     */
    public static int getEventCountForInterface(int nodeId, String ipAddress, AcknowledgeType ackType) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress) };
        return (getEventCount(ackType, filters));
    }

    /**
     * Return the number of events for this IP address and the given
     * acknowledgement type.
     */
    public static int getEventCountForInterface(String ipAddress, AcknowledgeType ackType) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new InterfaceFilter(ipAddress) };
        return (getEventCount(ackType, filters));
    }

    /*
     * ****************************************************************************
     * S E R V I C E M E T H O D S
     * ****************************************************************************
     */

    /** Return all unacknowledged events sorted by time for the given service. */
    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId) throws SQLException {
        return (getEventsForService(nodeId, ipAddress, serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * for the given service.
     * 
     * @deprecated Replaced by
     *             {@link " #getEventsForService(int,String,int,SortStyle,AcknowledgeType,int,int) getEventsForService( int, String, int, SortStyle, AcknowledgeType, int, int )"}
     */
    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (getEventsForService(nodeId, ipAddress, serviceId, SortStyle.ID, ackType, -1, -1));
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
     */
    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress), new ServiceFilter(serviceId) };
        return (EventFactory.getEvents(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return all unacknowledged events sorted by time for the given service
     * type, regardless of what node or interface they belong to.
     */
    public static Event[] getEventsForService(int serviceId) throws SQLException {
        return (getEventsForService(serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * for the given service type, regardless of what node or interface they
     * belong to.
     */
    public static Event[] getEventsForService(int serviceId, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (getEventsForService(serviceId, SortStyle.ID, ackType, -1, -1));
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
     */
    public static Event[] getEventsForService(int serviceId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new ServiceFilter(serviceId) };
        return (EventFactory.getEvents(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return the number of events for this node ID, IP address, service ID, and
     * the given acknowledgement type.
     */
    public static int getEventCountForService(int nodeId, String ipAddress, int serviceId, AcknowledgeType ackType) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress), new ServiceFilter(serviceId) };
        return (getEventCount(ackType, filters));
    }

    /**
     * Return the number of events for this node ID, IP address, service ID, and
     * the given acknowledgement type.
     */
    public static int getEventCountForService(int serviceId, AcknowledgeType ackType) throws SQLException {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new ServiceFilter(serviceId) };
        return (getEventCount(ackType, filters));
    }

    /**
     * Return all unacknowledged events sorted by time for the given severity.
     */
    public static Event[] getEventsForSeverity(int severity) throws SQLException {
        return (EventFactory.getEventsForSeverity(severity, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * for the given severity.
     * 
     * @deprecated Replaced by
     *             {@link " #getEventsForSeverity(int,SortStyle,AcknowledgeType) getEventsForSeverity( int, SortStyle, AcknowledgeType )"}
     */
    public static Event[] getEventsForSeverity(int severity, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (EventFactory.getEventsForSeverity(severity, SortStyle.ID, ackType));
    }

    public static Event[] getEventsForSeverity(int severity, SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return (EventFactory.getEvents(sortStyle, ackType, new Filter[] { new SeverityFilter(severity) }));
    }

    /**
     * Return all unacknowledged events sorted by time for that have the given
     * distributed poller.
     */
    public static Event[] getEventsForPoller(String poller) throws SQLException {
        return (getEventsForPoller(poller, false));
    }

    /**
     * Return all events (optionally only unacknowledged events) sorted by time
     * that have the given distributed poller.
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
     */
    public static void acknowledge(Event[] events, String user) throws SQLException {
        acknowledge(events, user, new Date());
    }

    /**
     * Acknowledge a list of events with the given username and the given time.
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
     */
    public static void acknowledge(int[] eventIds, String user) throws SQLException {
        acknowledge(eventIds, user, new Date());
    }

    /**
     * Acknowledge a list of events with the given username and the given time.
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
     */
    public static void acknowledge(Filter[] filters, String user) throws SQLException {
        acknowledge(filters, user, new Date());
    }

    /**
     * Acknowledge with the given username and the given time all events that
     * match the given filter criteria.
     */
    public static void acknowledge(Filter[] filters, String user, Date time) throws SQLException {
        if (filters == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer update = new StringBuffer("UPDATE EVENTS SET EVENTACKUSER=?, EVENTACKTIME=? WHERE");
        update.append(getAcknowledgeTypeClause(AcknowledgeType.UNACKNOWLEDGED));

        for (int i = 0; i < filters.length; i++) {
            update.append(" AND");
            update.append(filters[i].getParamSql());
        }

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement(update.toString());
            stmt.setString(1, user);
            stmt.setTimestamp(2, new Timestamp(time.getTime()));
            
            int parameterIndex = 3;
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
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
     */
    public static void acknowledgeAll(String user) throws SQLException {
        acknowledgeAll(user, new Date());
    }

    /**
     * Acknowledge all unacknowledged events with the given username and the
     * given time.
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
     */
    public static void unacknowledge(Filter[] filters) throws SQLException {
        if (filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer update = new StringBuffer("UPDATE EVENTS SET EVENTACKUSER=NULL, EVENTACKTIME=NULL WHERE");
        update.append(getAcknowledgeTypeClause(AcknowledgeType.ACKNOWLEDGED));

        for (int i = 0; i < filters.length; i++) {
            update.append(" AND");
            update.append(filters[i].getParamSql());
        }

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement(update.toString());
            
            int parameterIndex = 1;
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
            }

            stmt.executeUpdate();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
    }

    /**
     * Unacknowledge all acknowledged events.
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
     */
    // FIXME: Don't reuse the same "element" variable for multiple objects.
    protected static Event[] rs2Events(ResultSet rs) throws SQLException {
        Event[] events = null;
        Vector<Event> vector = new Vector<Event>();

        while (rs.next()) {
            Event event = new Event();

            Object element = new Integer(rs.getInt("eventID"));
            event.id = ((Integer) element).intValue();

            element = rs.getString("eventUei");
            event.uei = (String) element;

            element = rs.getString("eventSnmp");
            event.snmp = (String) element;

            element = rs.getTimestamp("eventTime");
            event.time = new Date(((Timestamp) element).getTime());

            element = rs.getString("eventHost");
            event.host = (String) element;

            element = rs.getString("eventSnmpHost");
            event.snmphost = (String) element;

            element = rs.getString("eventDpName");
            event.dpName = (String) element;

            element = rs.getString("eventParms");
            event.parms = (String) element;

            // node id can be null
            element = rs.getObject("nodeID");
            if (element == null) {
                event.nodeID = new Integer(0);
            } else {
                event.nodeID = (Integer) element;
            }

            element = rs.getString("ipAddr");
            event.ipAddr = (String) element;

            element = rs.getObject("serviceID");
            event.serviceID = (Integer) element;

            element = rs.getString("nodeLabel");
            event.nodeLabel = (String) element;

            element = rs.getString("serviceName");
            event.serviceName = (String) element;

            element = rs.getTimestamp("eventCreateTime");
            event.createTime = new Date(((Timestamp) element).getTime());

            element = rs.getString("eventDescr");
            event.description = (String) element;

            element = rs.getString("eventLoggroup");
            event.logGroup = (String) element;

            element = rs.getString("eventLogmsg");
            event.logMessage = (String) element;

            element = new Integer(rs.getInt("eventSeverity"));
            event.severity = ((Integer) element).intValue();

            element = rs.getString("eventOperInstruct");
            event.operatorInstruction = (String) element;

            element = rs.getString("eventAutoAction");
            event.autoAction = (String) element;

            element = rs.getString("eventOperAction");
            event.operatorAction = (String) element;

            element = rs.getString("eventOperActionMenuText");
            event.operatorActionMenuText = (String) element;

            element = rs.getString("eventNotification");
            event.notification = (String) element;

            element = rs.getString("eventTticket");
            event.troubleTicket = (String) element;

            element = rs.getObject("eventTticketState");
            event.troubleTicketState = (Integer) element;

            element = rs.getString("eventForward");
            event.forward = (String) element;

            element = rs.getString("eventMouseOverText");
            event.mouseOverText = (String) element;

            element = rs.getString("eventAckUser");
            event.acknowledgeUser = (String) element;

            element = rs.getTimestamp("eventAckTime");
            if (element != null) {
                event.acknowledgeTime = new Date(((Timestamp) element).getTime());
            }

            element = rs.getObject("alarmid");
            event.alarmId = (Integer) element;

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
     */
    protected static String getOrderByClause(SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String clause = null;

        switch (sortStyle.getId()) {
        case SortStyle._SEVERITY:
            clause = " ORDER BY EVENTSEVERITY DESC";
            break;

        case SortStyle._REVERSE_SEVERITY:
            clause = " ORDER BY EVENTSEVERITY ASC";
            break;

        case SortStyle._TIME:
            clause = " ORDER BY EVENTTIME DESC";
            break;

        case SortStyle._REVERSE_TIME:
            clause = " ORDER BY EVENTTIME ASC";
            break;

        case SortStyle._NODE:
            clause = " ORDER BY NODELABEL ASC";
            break;

        case SortStyle._REVERSE_NODE:
            clause = " ORDER BY NODELABEL DESC";
            break;

        case SortStyle._INTERFACE:
            clause = " ORDER BY IPADDR ASC";
            break;

        case SortStyle._REVERSE_INTERFACE:
            clause = " ORDER BY IPADDR DESC";
            break;

        case SortStyle._SERVICE:
            clause = " ORDER BY SERVICENAME ASC";
            break;

        case SortStyle._REVERSE_SERVICE:
            clause = " ORDER BY SERVICENAME DESC";
            break;

        case SortStyle._POLLER:
            clause = " ORDER BY EVENTDPNAME ASC";
            break;

        case SortStyle._REVERSE_POLLER:
            clause = " ORDER BY EVENTDPNAME DESC";
            break;

        case SortStyle._ID:
            clause = " ORDER BY EVENTID DESC";
            break;

        case SortStyle._REVERSE_ID:
            clause = " ORDER BY EVENTID ASC";
            break;

        default:
            throw new IllegalArgumentException("Unknown EventFactory.SortStyle: " + sortStyle.getName());
        }

        return clause;
    }

    /**
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * to a given sort style.
     * 
     * @param ackType
     *            the acknowledge type to map to a clause
     */
    protected static String getAcknowledgeTypeClause(AcknowledgeType ackType) {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String clause = null;

        switch (ackType.getId()) {
        case AcknowledgeType._ACKNOWLEDGED:
            clause = " EVENTACKUSER IS NOT NULL ";
            break;

        case AcknowledgeType._UNACKNOWLEDGED:
            clause = " EVENTACKUSER IS NULL ";
            break;

        case AcknowledgeType._BOTH:
            clause = " (EVENTACKUSER IS NULL OR EVENTACKUSER IS NOT NULL) ";
            break;

        default:
            throw new IllegalArgumentException("Unknown EventFactory.AcknowledgeType: " + ackType.getName());
        }

        return clause;
    }

}
