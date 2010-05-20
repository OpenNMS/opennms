//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Oct 04: Severity -> OnmsSeverity name change and some method name changes. - dj@opennms.org
// 2008 Sep 27: Use Java 5 enums for sort style and acknowledge type and
//              use new Severity enum from Alarm. - dj@opennms.org
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
// 2005 Apr 18: This file created from EventFactory.java
//
// Original Code Base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.alarm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.web.alarm.filter.InterfaceFilter;
import org.opennms.web.alarm.filter.NodeFilter;
import org.opennms.web.alarm.filter.ServiceFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.filter.Filter;

/**
 * Encapsulates all querying functionality for alarms.
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AlarmFactory extends Object {
    
    /** Private constructor so this class cannot be instantiated. */
    private AlarmFactory() {
    }
    
    private static ThreadCategory log() {
    	return ThreadCategory.getInstance();
    }

    /**
     * Count all outstanding (unacknowledged) alarms.
     */
    public static int getAlarmCount() throws SQLException {
        return getAlarmCount(AcknowledgeType.UNACKNOWLEDGED, new Filter[0]);
    }
    
    /**
     * Count the number of alarms for a given acknowledgement type.
     */
    public static int getAlarmCount(AcknowledgeType ackType, Filter[] filters) throws SQLException {
        if (ackType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int alarmCount = 0;

        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            StringBuffer select = new StringBuffer("SELECT COUNT(ALARMID) AS ALARMCOUNT FROM ALARMS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) WHERE ");
            select.append(ackType.getAcknowledgeTypeClause());

            for (int i = 0; i < filters.length; i++) {
                select.append(" AND ");
                select.append(filters[i].getParamSql());
            }

//            select.append(" AND ALARMDISPLAY='Y' ");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            d.watch(stmt);
            
            int parameterIndex = 1;
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            if (rs.next()) {
                alarmCount = rs.getInt("ALARMCOUNT");
            }
        } finally {
            d.cleanUp();
        }

        return alarmCount;
    }

    /**
     * Count the number of alarms for a given acknowledgement type.
     * 
     * @return An array of event counts. Each index of the array corresponds to
     *         the event severity for the counts (indeterminate is 1, critical
     *         is 7, etc).
     */
    public static int[] getAlarmCountBySeverity(AcknowledgeType ackType, Filter[] filters) throws SQLException {
        if (ackType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int[] alarmCounts = new int[8];

        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            StringBuffer select = new StringBuffer("SELECT SEVERITY, COUNT(ALARMID) AS ALARMCOUNT FROM ALARMS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) WHERE ");
            select.append(ackType.getAcknowledgeTypeClause());

            for (int i = 0; i < filters.length; i++) {
                select.append(" AND ");
                select.append(filters[i].getParamSql());
            }

//            select.append(" AND EVENTDISPLAY='Y'");
            select.append(" GROUP BY SEVERITY");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            d.watch(stmt);
            
            int parameterIndex = 1;
            for (int i = 1; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                int severity = rs.getInt("SEVERITY");
                int alarmCount = rs.getInt("ALARMCOUNT");

                alarmCounts[severity] = alarmCount;
            }
        } finally {
            d.cleanUp();
        }

        return alarmCounts;
    }

    /** Return a specific alarm. */
    public static Alarm getAlarms(int alarmId) throws SQLException {
        Alarm alarm = null;

        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT ALARMS.*, NODE.NODELABEL, SERVICE.SERVICENAME FROM ALARMS LEFT OUTER JOIN NODE USING (NODEID) LEFT OUTER JOIN SERVICE USING (SERVICEID) WHERE ALARMID=? ");
            d.watch(stmt);
            stmt.setInt(1, alarmId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            Alarm[] alarms = rs2Alarms(rs);

            // what do I do if this actually returns more than one service?
            if (alarms.length > 0) {
                alarm = alarms[0];
            }
        } finally {
            d.cleanUp();
        }

        return alarm;
    }

    /** Return all unacknowledged alarms sorted by time. */
    public static Alarm[] getAlarms() throws SQLException {
        return (AlarmFactory.getAlarms(SortStyle.LASTEVENTTIME, AcknowledgeType.UNACKNOWLEDGED));
    }

    /** Return all unacknowledged or acknowledged alarms sorted by time. */
    public static Alarm[] getAlarms(AcknowledgeType ackType) throws SQLException {
        return (AlarmFactory.getAlarms(SortStyle.LASTEVENTTIME, ackType));
    }

    /** Return all unacknowledged alarms sorted by the given sort style. */
    public static Alarm[] getAlarms(SortStyle sortStyle) throws SQLException {
        return (AlarmFactory.getAlarms(sortStyle, AcknowledgeType.UNACKNOWLEDGED));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
     * given sort style.
     * 
     */
    public static Alarm[] getAlarms(SortStyle sortStyle, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (AlarmFactory.getAlarms(sortStyle, ackType));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
     * given sort style.
     */
    public static Alarm[] getAlarms(SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return (AlarmFactory.getAlarms(sortStyle, ackType, new Filter[0]));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
     * given sort style.
     */
    public static Alarm[] getAlarms(SortStyle sortStyle, AcknowledgeType ackType, Filter[] filters) throws SQLException {
        return (AlarmFactory.getAlarms(sortStyle, ackType, filters, -1, -1));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
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
    public static Alarm[] getAlarms(SortStyle sortStyle, AcknowledgeType ackType, Filter[] filters, int limit, int offset) throws SQLException {
        if (sortStyle == null || ackType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        boolean useLimits = false;
        if (limit > 0 && offset > -1) {
            useLimits = true;
        }

        Alarm[] alarms = null;

        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            StringBuffer select = new StringBuffer("SELECT ALARMS.*, NODE.NODELABEL, SERVICE.SERVICENAME FROM ALARMS LEFT OUTER JOIN NODE USING(NODEID) LEFT OUTER JOIN SERVICE USING(SERVICEID) WHERE");
            select.append(ackType.getAcknowledgeTypeClause());

            for (int i = 0; i < filters.length; i++) {
                select.append(" AND ");
                select.append(filters[i].getParamSql());
            }

//            select.append(" AND ALARMDISPLAY='Y' ");
            select.append(sortStyle.getOrderByClause());

            if (useLimits) {
                select.append(" LIMIT ");
                select.append(limit);
                select.append(" OFFSET ");
                select.append(offset);
            }

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            d.watch(stmt);
            
            int parameterIndex = 1;
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            alarms = rs2Alarms(rs);
        } finally {
            d.cleanUp();
        }

        return alarms;
    }

    /*
     * ****************************************************************************
     * N O D E M E T H O D S
     * ****************************************************************************
     */

    /** Return all unacknowledged alarms sorted by alarm ID for the given node. */
    public static Alarm[] getAlarmsForNode(int nodeId) throws SQLException {
        return (getAlarmsForNode(nodeId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
     * given sort style for the given node.
     */
    public static Alarm[] getAlarmsForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return (getAlarmsForNode(nodeId, sortStyle, ackType, -1, -1));
    }

    /**
     * Return some maximum number of alarms or less (optionally only
     * unacknowledged alarms) sorted by the given sort style for the given node.
     * 
     * @param throttle
     *            a value less than one means no throttling
     */
    public static Alarm[] getAlarmsForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId) };
        return (AlarmFactory.getAlarms(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return the number of alarms for this node and the given acknowledgement
     * type.
     */
    public static int getAlarmCountForNode(int nodeId, AcknowledgeType ackType) throws SQLException {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId) };
        return (getAlarmCount(ackType, filters));
    }

    /*
     * ****************************************************************************
     * I N T E R F A C E M E T H O D S
     * ****************************************************************************
     */

    /**
     * Return all unacknowledged alarms sorted by event ID for the given
     * interface.
     */
    public static Alarm[] getAlarmsForInterface(int nodeId, String ipAddress) throws SQLException {
        return (getAlarmsForInterface(nodeId, ipAddress, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return some maximum number of alarms or less (optionally only
     * unacknowledged alarms) sorted by the given sort style for the given node
     * and IP address.
     * 
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     */
    public static Alarm[] getAlarmsForInterface(int nodeId, String ipAddress, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress) };
        return (AlarmFactory.getAlarms(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return all unacknowledged alarms sorted by time for that have the given
     * IP address, regardless of what node they belong to.
     */
    public static Alarm[] getAlarmsForInterface(String ipAddress) throws SQLException {
        return (getAlarmsForInterface(ipAddress, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by time
     * that have the given IP address, regardless of what node they belong to.
     * 
     * @deprecated Replaced by
     *             {@link " #getEventsForInterface(String,SortStyle,AcknowledgeType) getEventsForInterface( String, SortStyle, AcknowledgeType )"}
     */
    public static Alarm[] getAlarmsForInterface(String ipAddress, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (getAlarmsForInterface(ipAddress, SortStyle.ID, ackType, -1, -1));
    }

    /**
     * Return some maximum number of alarms or less (optionally only
     * unacknowledged alarms) sorted by the given sort style for the given IP
     * address.
     * 
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     */
    public static Alarm[] getAlarmsForInterface(String ipAddress, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new InterfaceFilter(ipAddress) };
        return (AlarmFactory.getAlarms(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return the number of alarms for this node ID, IP address, and the given
     * acknowledgement type.
     */
    public static int getAlarmCountForInterface(int nodeId, String ipAddress, AcknowledgeType ackType) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress) };
        return (getAlarmCount(ackType, filters));
    }

    /**
     * Return the number of alarms for this IP address and the given
     * acknowledgement type.
     */
    public static int getAlarmCountForInterface(String ipAddress, AcknowledgeType ackType) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new InterfaceFilter(ipAddress) };
        return (getAlarmCount(ackType, filters));
    }

    /*
     * ****************************************************************************
     * S E R V I C E M E T H O D S
     * ****************************************************************************
     */

    /** Return all unacknowledged alarms sorted by time for the given service. */
    public static Alarm[] getAlarmsForService(int nodeId, String ipAddress, int serviceId) throws SQLException {
        return (getAlarmsForService(nodeId, ipAddress, serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return some maximum number of alarms or less (optionally only
     * unacknowledged alarms) sorted by the given sort style for the given node,
     * IP address, and service ID.
     * 
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     */
    public static Alarm[] getAlarmsForService(int nodeId, String ipAddress, int serviceId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (ipAddress == null || sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress), new ServiceFilter(serviceId) };
        return (AlarmFactory.getAlarms(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return all unacknowledged alarms sorted by time for the given service
     * type, regardless of what node or interface they belong to.
     */
    public static Alarm[] getAlarmsForService(int serviceId) throws SQLException {
        return (getAlarmsForService(serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by time
     * for the given service type, regardless of what node or interface they
     * belong to.
     */
    public static Alarm[] getAlarmsForService(int serviceId, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (getAlarmsForService(serviceId, SortStyle.ID, ackType, -1, -1));
    }

    /**
     * Return some maximum number of alarms or less (optionally only
     * unacknowledged alarms) sorted by the given sort style for the given
     * service ID.
     * 
     * @param throttle
     *            a value less than one means no throttling
     * @param offset
     *            which row to start on in the result list
     */
    public static Alarm[] getAlarmsForService(int serviceId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new ServiceFilter(serviceId) };
        return (AlarmFactory.getAlarms(sortStyle, ackType, filters, throttle, offset));
    }

    /**
     * Return the number of alarms for this node ID, IP address, service ID, and
     * the given acknowledgement type.
     */
    public static int getAlarmCountForService(int nodeId, String ipAddress, int serviceId, AcknowledgeType ackType) throws SQLException {
        if (ipAddress == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new NodeFilter(nodeId), new InterfaceFilter(ipAddress), new ServiceFilter(serviceId) };
        return (getAlarmCount(ackType, filters));
    }

    /**
     * Return the number of alarms for this node ID, IP address, service ID, and
     * the given acknowledgement type.
     */
    public static int getAlarmCountForService(int serviceId, AcknowledgeType ackType) throws SQLException {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Filter[] filters = new Filter[] { new ServiceFilter(serviceId) };
        return (getAlarmCount(ackType, filters));
    }

    /**
     * Return all unacknowledged alarms sorted by time for the given severity.
     */
    public static Alarm[] getAlarmsForSeverity(int severity) throws SQLException {
        return (AlarmFactory.getAlarmsForSeverity(severity, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED));
    }

    public static Alarm[] getAlarmsForSeverity(int severity, SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return (AlarmFactory.getAlarms(sortStyle, ackType, new Filter[] { new SeverityFilter(OnmsSeverity.get(severity)) }));
    }

    /**
     * Return all unacknowledged alarms sorted by time for that have the given
     * distributed poller.
     */
    public static Alarm[] getAlarmsForPoller(String poller) throws SQLException {
        return (getAlarmsForPoller(poller, false));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by time
     * that have the given distributed poller.
     */
    public static Alarm[] getAlarmsForPoller(String poller, boolean includeAcknowledged) throws SQLException {
        if (poller == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Alarm[] alarms = null;

        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            StringBuffer select = new StringBuffer("SELECT * FROM ALARMS WHERE DPNAME=?");

            if (!includeAcknowledged) {
                select.append(" AND ALARMACKUSER IS NULL");
            }

            select.append(" AND ALARMDISPLAY='Y' ");
            select.append(" ORDER BY ALARMID DESC");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            d.watch(stmt);
            stmt.setString(1, poller);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            alarms = rs2Alarms(rs);
        } finally {
            d.cleanUp();
        }

        return alarms;
    }

    /**
     * Acknowledge a list of alarms with the given username and the current
     * time.
     */
    public static void acknowledge(Alarm[] alarms, String user) throws SQLException {
        acknowledge(alarms, user, new Date());
    }

    /**
     * Acknowledge a list of alarms with the given username and the given time.
     */
    public static void acknowledge(Alarm[] alarms, String user, Date time) throws SQLException {
        if (alarms == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int[] ids = new int[alarms.length];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = alarms[i].getId();
        }

        acknowledge(ids, user, time);
    }

    /**
     * Acknowledge a list of alarms with the given username and the current
     * time.
     */
    public static void acknowledge(int[] alarmIds, String user) throws SQLException {
        acknowledge(alarmIds, user, new Date());
    }

    /**
     * Acknowledge a list of alarms with the given username and the given time.
     */
    public static void acknowledge(int[] alarmIds, String user, Date time) throws SQLException {
        if (alarmIds == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (alarmIds.length > 0) {
            StringBuffer update = new StringBuffer("UPDATE ALARMS SET ALARMACKUSER=?, ALARMACKTIME=?");
            update.append(" WHERE ALARMID IN (");
            update.append(alarmIds[0]);

            for (int i = 1; i < alarmIds.length; i++) {
                update.append(",");
                update.append(alarmIds[i]);
            }

            update.append(")");
            update.append(" AND ALARMACKUSER IS NULL");

            final DBUtils d = new DBUtils(AlarmFactory.class);
            try {
                Connection conn = Vault.getDbConnection();
                d.watch(conn);

                PreparedStatement stmt = conn.prepareStatement(update.toString());
                d.watch(stmt);
                stmt.setString(1, user);
                stmt.setTimestamp(2, new Timestamp(time.getTime()));

                stmt.executeUpdate();
            } finally {
                d.cleanUp();
            }
        }
    }

    /**
     * Acknowledge with the given username and the current time all alarms that
     * match the given filter criteria.
     */
    public static void acknowledge(Filter[] filters, String user) throws SQLException {
        acknowledge(filters, user, new Date());
    }

    /**
     * Acknowledge with the given username and the given time all alarms that
     * match the given filter criteria.
     */
    public static void acknowledge(Filter[] filters, String user, Date time) throws SQLException {
        if (filters == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer update = new StringBuffer("UPDATE ALARMS SET ALARMACKUSER=?, ALARMACKTIME=? WHERE");
        update.append(AcknowledgeType.UNACKNOWLEDGED.getAcknowledgeTypeClause());

        for (int i = 0; i < filters.length; i++) {
            update.append(" AND ");
            update.append(filters[i].getParamSql());
        }

        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement(update.toString());
            d.watch(stmt);
            stmt.setString(1, user);
            stmt.setTimestamp(2, new Timestamp(time.getTime()));
            
            int parameterIndex = 3;
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
            }

            stmt.executeUpdate();
        } finally {
            d.cleanUp();
        }
    }

    /**
     * Acknowledge all unacknowledged alarms with the given username and the
     * given time.
     */
    public static void acknowledgeAll(String user) throws SQLException {
        acknowledgeAll(user, new Date());
    }

    /**
     * Acknowledge all unacknowledged alarms with the given username and the
     * given time.
     */
    public static void acknowledgeAll(String user, Date time) throws SQLException {
        if (user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement("UPDATE ALARMS SET ALARMACKUSER=?, ALARMACKTIME=? WHERE ALARMACKUSER IS NULL");
            d.watch(stmt);
            stmt.setString(1, user);
            stmt.setTimestamp(2, new Timestamp(time.getTime()));

            stmt.executeUpdate();
        } finally {
            d.cleanUp();
        }

    }

    /**
     * Unacknowledge a list of alarms.
     */
    public static void unacknowledge(Alarm[] alarms) throws SQLException {
        if (alarms == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int[] ids = new int[alarms.length];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = alarms[i].getId();
        }

        unacknowledge(ids);
    }

    /**
     * Unacknowledge a list of alarms.
     */
    public static void unacknowledge(int[] alarmIds) throws SQLException {
        if (alarmIds == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (alarmIds.length > 0) {
            StringBuffer update = new StringBuffer("UPDATE ALARMS SET ALARMACKUSER=NULL, ALARMACKTIME=NULL");
            update.append(" WHERE ALARMID IN (");
            update.append(alarmIds[0]);

            for (int i = 1; i < alarmIds.length; i++) {
                update.append(",");
                update.append(alarmIds[i]);
            }

            update.append(")");

            final DBUtils d = new DBUtils(AlarmFactory.class);
            try {
                Connection conn = Vault.getDbConnection();
                d.watch(conn);

                PreparedStatement stmt = conn.prepareStatement(update.toString());
                d.watch(stmt);
                stmt.executeUpdate();
            } finally {
                d.cleanUp();
            }
        }
    }

    /**
     * Unacknowledge alarms that match the given filter criteria.
     */
    public static void unacknowledge(Filter[] filters) throws SQLException {
        if (filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer update = new StringBuffer("UPDATE ALARMS SET ALARMACKUSER=NULL, ALARMACKTIME=NULL WHERE");
        update.append(AcknowledgeType.ACKNOWLEDGED.getAcknowledgeTypeClause());

        for (int i = 0; i < filters.length; i++) {
            update.append(" AND ");
            update.append(filters[i].getParamSql());
        }

        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement(update.toString());
            d.watch(stmt);
            
            int parameterIndex = 1;
            for (int i = 0; i < filters.length; i++) {
            	parameterIndex += filters[i].bindParam(stmt, parameterIndex);
            }

            stmt.executeUpdate();
        } finally {
            d.cleanUp();
        }
    }

    /**
     * Unacknowledge all acknowledged alarms.
     */
    public static void unacknowledgeAll() throws SQLException {
        final DBUtils d = new DBUtils(AlarmFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement("UPDATE ALARMS SET ALARMACKUSER=NULL, ALARMACKTIME=NULL WHERE ALARMACKUSER IS NOT NULL");
            d.watch(stmt);
            
            stmt.executeUpdate();
        } finally {
            d.cleanUp();
        }

    }

    /**
     * Convenience method for translating a <code>java.sql.ResultSet</code>
     * containing event information into an array of <code>Alarm</code>
     * objects.
     */
    protected static Alarm[] rs2Alarms(ResultSet rs) throws SQLException {
        Vector<Alarm> vector = new Vector<Alarm>();

        while (rs.next()) {
            Alarm alarm = new Alarm();

            alarm.id = rs.getInt("alarmID");

            alarm.uei = rs.getString("eventUei");

            alarm.dpName = rs.getString("dpName");

            // node id can be null, in which case nodeID will be 0
            alarm.nodeID = new Integer(rs.getInt("nodeID"));

            alarm.ipAddr = rs.getString("ipAddr");

            // This causes serviceID to be null if the column in the database is null
            alarm.serviceID = ((Integer) rs.getObject("serviceID"));

            alarm.reductionKey = rs.getString("reductionKey");

            alarm.count = rs.getInt("counter");

            alarm.severity = OnmsSeverity.get(rs.getInt("severity"));

            alarm.lastEventID = rs.getInt("lastEventID");

            alarm.firsteventtime = new Date(rs.getTimestamp("firsteventtime").getTime());

            alarm.lasteventtime = new Date(rs.getTimestamp("lasteventtime").getTime());

            alarm.description = rs.getString("description");

            alarm.logMessage = rs.getString("logmsg");

            alarm.operatorInstruction = rs.getString("OperInstruct");

            alarm.troubleTicket = rs.getString("TTicketID");
            
            Integer stateCode = (Integer) rs.getObject("TTicketState");
            for (TroubleTicketState state : TroubleTicketState.values()) {
                if (stateCode != null && state.ordinal() == stateCode.intValue()) {
                    alarm.troubleTicketState = state;
                }
            }

            alarm.mouseOverText = rs.getString("MouseOverText");

            alarm.suppressedUntil = new Date(rs.getTimestamp("suppressedUntil").getTime());

            alarm.suppressedUser = rs.getString("suppressedUser");

            alarm.suppressedTime = new Date(rs.getTimestamp("suppressedTime").getTime());

            alarm.acknowledgeUser = rs.getString("alarmAckUser");

            Timestamp alarmAckTime = rs.getTimestamp("alarmAckTime");
            if (alarmAckTime != null) {
                alarm.acknowledgeTime = new Date(alarmAckTime.getTime());
            }

            alarm.nodeLabel = rs.getString("nodeLabel");

            alarm.serviceName = rs.getString("serviceName");

            vector.addElement(alarm);
        }

        return vector.toArray(new Alarm[vector.size()]);
    }

    /**
     * Escalate a list of alarms using the given username and the current time
     * @throws SQLException 
     */
    public static void escalateAlarms(int[] alarmIds, String user) throws SQLException {
    	escalateAlarms(alarmIds, user, new Date());
    }
    
    /**
     * Escalate a list of alarms.  The username and time are currently discarded, but
     * are required for future use.
     */
    public static void escalateAlarms(int[] alarmIds, String user, Date time) throws SQLException {
        if (alarmIds == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (alarmIds.length > 0) {
            StringBuffer update = new StringBuffer("UPDATE ALARMS SET SEVERITY = (");
            update.append("CASE WHEN SEVERITY =? THEN ?");
            update.append(" ELSE (");
            update.append("  CASE WHEN SEVERITY <? THEN SEVERITY + 1");
            update.append("  ELSE ? END)");
            update.append(" END),");
            update.append(" ALARMTYPE =?");
            update.append(" WHERE ALARMID IN (");
            update.append(alarmIds[0]);

            for (int i = 1; i < alarmIds.length; i++) {
                update.append(",");
                update.append(alarmIds[i]);
            }

            update.append(")");
            update.append(" AND ( (");
            update.append("  ALARMTYPE =? AND");
            update.append("  SEVERITY =?");
            update.append(" ) OR (");
            update.append("  ALARMTYPE =? AND");
            update.append("  SEVERITY >? AND");
            update.append("  SEVERITY <=?");
            update.append(" ) )");

            if (log().isDebugEnabled()) {
            	log().debug("escalateAlarms: built query |" + update.toString() + "|");
            }

            final DBUtils d = new DBUtils(AlarmFactory.class);
            try {
                Connection conn = Vault.getDbConnection();
                d.watch(conn);
                
                PreparedStatement stmt = conn.prepareStatement(update.toString());
                d.watch(stmt);
                stmt.setInt(1, OnmsSeverity.CLEARED.getId());
                stmt.setInt(2, OnmsSeverity.WARNING.getId());
                stmt.setInt(3, OnmsSeverity.CRITICAL.getId());
                stmt.setInt(4, OnmsSeverity.CRITICAL.getId());
                stmt.setInt(5, Alarm.PROBLEM_TYPE);
                stmt.setInt(6, Alarm.RESOLUTION_TYPE);
                stmt.setInt(7, OnmsSeverity.CLEARED.getId());
                stmt.setInt(8, Alarm.PROBLEM_TYPE);
                stmt.setInt(9, OnmsSeverity.NORMAL.getId());
                stmt.setInt(10, OnmsSeverity.CRITICAL.getId());
                stmt.executeUpdate();
            } finally {
                d.cleanUp();
            }
        }
    }
    
    /**
     * Clear a list of alarms, using the given username and the current time
     * @throws SQLException 
     */
    public static void clearAlarms(int[] alarmIds, String user) throws SQLException {
    	clearAlarms(alarmIds, user, new Date());
    }

    /**
     * Clear a list of alarms.  The username and time are currently discarded, but
     * are required for future use.
     */
    public static void clearAlarms(int[] alarmIds, String user, Date time) throws SQLException {
        if (alarmIds == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (alarmIds.length > 0) {
            StringBuffer update = new StringBuffer("UPDATE ALARMS SET SEVERITY =?");
            update.append(", ALARMTYPE =?");
            update.append(" WHERE ALARMID IN (");
            update.append(alarmIds[0]);

            for (int i = 1; i < alarmIds.length; i++) {
                update.append(",");
                update.append(alarmIds[i]);
            }

            update.append(")");
            update.append(" AND SEVERITY >=?");
            update.append(" AND SEVERITY <=?");

            final DBUtils d = new DBUtils(AlarmFactory.class);
            try {
                Connection conn = Vault.getDbConnection();
                d.watch(conn);

                PreparedStatement stmt = conn.prepareStatement(update.toString());
                d.watch(stmt);
                stmt.setInt(1, OnmsSeverity.CLEARED.getId());
                stmt.setInt(2, Alarm.RESOLUTION_TYPE);
                stmt.setInt(3, OnmsSeverity.NORMAL.getId());
                stmt.setInt(4, OnmsSeverity.CRITICAL.getId());
                stmt.executeUpdate();
            } finally {
                d.cleanUp();
            }
        }
    }

}
