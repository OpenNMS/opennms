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

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.web.alarm.filter.Filter;
import org.opennms.web.alarm.filter.InterfaceFilter;
import org.opennms.web.alarm.filter.NodeFilter;
import org.opennms.web.alarm.filter.ServiceFilter;
import org.opennms.web.alarm.filter.SeverityFilter;

/**
 * Encapsulates all querying functionality for alarms.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.6.12
 */
public class AlarmFactory extends Object {

    /** Convenience class to determine sort style of a query. */
    public static class SortStyle extends Object {
        /* CORBA-style enumeration */
        public static final int _SEVERITY = 1;

        public static final int _LASTEVENTTIME = 2;

        public static final int _FIRSTEVENTTIME = 3;

        public static final int _NODE = 4;

        public static final int _INTERFACE = 5;

        public static final int _SERVICE = 6;

        public static final int _POLLER = 7;

        public static final int _ID = 8;

        public static final int _COUNT = 9;

        public static final SortStyle SEVERITY = new SortStyle("SEVERITY", _SEVERITY);

        public static final SortStyle LASTEVENTTIME = new SortStyle("LASTEVENTTIME", _LASTEVENTTIME);

        public static final SortStyle FIRSTEVENTTIME = new SortStyle("FIRSTEVENTTIME", _FIRSTEVENTTIME);

        public static final SortStyle NODE = new SortStyle("NODE", _NODE);

        public static final SortStyle INTERFACE = new SortStyle("INTERFACE", _INTERFACE);

        public static final SortStyle SERVICE = new SortStyle("SERVICE", _SERVICE);

        public static final SortStyle POLLER = new SortStyle("POLLER", _POLLER);

        public static final SortStyle ID = new SortStyle("ID", _ID);

        public static final SortStyle COUNT = new SortStyle("COUNT", _COUNT);

        public static final int _REVERSE_SEVERITY = 101;

        public static final int _REVERSE_LASTEVENTTIME = 102;

        public static final int _REVERSE_FIRSTEVENTTIME = 103;

        public static final int _REVERSE_NODE = 104;

        public static final int _REVERSE_INTERFACE = 105;

        public static final int _REVERSE_SERVICE = 106;

        public static final int _REVERSE_POLLER = 107;

        public static final int _REVERSE_ID = 108;

        public static final int _REVERSE_COUNT = 109;

        public static final SortStyle REVERSE_SEVERITY = new SortStyle("REVERSE_SEVERITY", _REVERSE_SEVERITY);

        public static final SortStyle REVERSE_LASTEVENTTIME = new SortStyle("REVERSE_LASTEVENTTIME", _REVERSE_LASTEVENTTIME);

        public static final SortStyle REVERSE_FIRSTEVENTTIME = new SortStyle("REVERSE_FIRSTEVENTTIME", _REVERSE_FIRSTEVENTTIME);

        public static final SortStyle REVERSE_NODE = new SortStyle("REVERSE_NODE", _REVERSE_NODE);

        public static final SortStyle REVERSE_INTERFACE = new SortStyle("REVERSE_INTERFACE", _REVERSE_INTERFACE);

        public static final SortStyle REVERSE_SERVICE = new SortStyle("REVERSE_SERVICE", _REVERSE_SERVICE);

        public static final SortStyle REVERSE_POLLER = new SortStyle("REVERSE_POLLER", _REVERSE_POLLER);

        public static final SortStyle REVERSE_ID = new SortStyle("REVERSE_ID", _REVERSE_ID);

        public static final SortStyle REVERSE_COUNT = new SortStyle("REVERSE_COUNT", _REVERSE_COUNT);

        protected String name;

        protected int id;

        private SortStyle(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String toString() {
            return ("Alarm.SortStyle." + this.name);
        }

        public String getName() {
            return (this.name);
        }

        public int getId() {
            return (this.id);
        }
    }

    /** Convenience class to determine what sort of alarms to include in a query. */
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
            return ("Alarm.AcknowledgeType." + this.name);
        }

        public String getName() {
            return (this.name);
        }

        public int getId() {
            return (this.id);
        }
    }

    /** Private constructor so this class cannot be instantiated. */
    private AlarmFactory() {
    }
    
    private static Category log() {
    	return ThreadCategory.getInstance();
    }

    /**
     * Count all outstanding (unacknowledged) alarms.
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getAlarmCount() throws SQLException {
        return getAlarmCount(AcknowledgeType.UNACKNOWLEDGED, new Filter[0]);
    }

    /**
     * Count the number of alarms for a given acknowledgement type.
     *
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @param filters an array of {@link org.opennms.web.alarm.filter.Filter} objects.
     * @return a int.
     * @throws java.sql.SQLException if any.
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
            select.append(getAcknowledgeTypeClause(ackType));

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
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @param filters an array of {@link org.opennms.web.alarm.filter.Filter} objects.
     * @throws java.sql.SQLException if any.
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
            select.append(getAcknowledgeTypeClause(ackType));

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

    /**
     * Return a specific alarm.
     *
     * @param alarmId a int.
     * @return a {@link org.opennms.web.alarm.Alarm} object.
     * @throws java.sql.SQLException if any.
     */
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

    /**
     * Return all unacknowledged alarms sorted by time.
     *
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarms() throws SQLException {
        return (AlarmFactory.getAlarms(SortStyle.LASTEVENTTIME, AcknowledgeType.UNACKNOWLEDGED));
    }

    /**
     * Return all unacknowledged or acknowledged alarms sorted by time.
     *
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarms(AcknowledgeType ackType) throws SQLException {
        return (AlarmFactory.getAlarms(SortStyle.LASTEVENTTIME, ackType));
    }

    /**
     * Return all unacknowledged alarms sorted by the given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarms(SortStyle sortStyle) throws SQLException {
        return (AlarmFactory.getAlarms(sortStyle, AcknowledgeType.UNACKNOWLEDGED));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarms(SortStyle sortStyle, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (AlarmFactory.getAlarms(sortStyle, ackType));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarms(SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return (AlarmFactory.getAlarms(sortStyle, ackType, new Filter[0]));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @param filters an array of {@link org.opennms.web.alarm.filter.Filter} objects.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @param filters an array of {@link org.opennms.web.alarm.filter.Filter} objects.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
            select.append(getAcknowledgeTypeClause(ackType));

            for (int i = 0; i < filters.length; i++) {
                select.append(" AND ");
                select.append(filters[i].getParamSql());
            }

//            select.append(" AND ALARMDISPLAY='Y' ");
            select.append(getOrderByClause(sortStyle));

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

    /**
     * Return all unacknowledged alarms sorted by alarm ID for the given node.
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarmsForNode(int nodeId) throws SQLException {
        return (getAlarmsForNode(nodeId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by the
     * given sort style for the given node.
     *
     * @param nodeId a int.
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     * @param nodeId a int.
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @param offset a int.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     *
     * @param nodeId a int.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
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
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     * @param ipAddress a {@link java.lang.String} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     * @param ipAddress a {@link java.lang.String} object.
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
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
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
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

    /**
     * Return all unacknowledged alarms sorted by time for the given service.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
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
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     *
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarmsForService(int serviceId) throws SQLException {
        return (getAlarmsForService(serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by time
     * for the given service type, regardless of what node or interface they
     * belong to.
     *
     * @param serviceId a int.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     * @param serviceId a int.
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
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
     *
     * @param serviceId a int.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
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
     *
     * @param severity a int.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarmsForSeverity(int severity) throws SQLException {
        return (AlarmFactory.getAlarmsForSeverity(severity, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED));
    }

    /**
     * <p>getAlarmsForSeverity</p>
     *
     * @param severity a int.
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @param ackType a {@link org.opennms.web.alarm.AlarmFactory.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarmsForSeverity(int severity, SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return (AlarmFactory.getAlarms(sortStyle, ackType, new Filter[] { new SeverityFilter(severity) }));
    }

    /**
     * Return all unacknowledged alarms sorted by time for that have the given
     * distributed poller.
     *
     * @param poller a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Alarm[] getAlarmsForPoller(String poller) throws SQLException {
        return (getAlarmsForPoller(poller, false));
    }

    /**
     * Return all alarms (optionally only unacknowledged alarms) sorted by time
     * that have the given distributed poller.
     *
     * @param poller a {@link java.lang.String} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     *
     * @param alarms an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Alarm[] alarms, String user) throws SQLException {
        acknowledge(alarms, user, new Date());
    }

    /**
     * Acknowledge a list of alarms with the given username and the given time.
     *
     * @param alarms an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @param user a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @throws java.sql.SQLException if any.
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
     *
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(int[] alarmIds, String user) throws SQLException {
        acknowledge(alarmIds, user, new Date());
    }

    /**
     * Acknowledge a list of alarms with the given username and the given time.
     *
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @throws java.sql.SQLException if any.
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
     *
     * @param filters an array of {@link org.opennms.web.alarm.filter.Filter} objects.
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Filter[] filters, String user) throws SQLException {
        acknowledge(filters, user, new Date());
    }

    /**
     * Acknowledge with the given username and the given time all alarms that
     * match the given filter criteria.
     *
     * @param filters an array of {@link org.opennms.web.alarm.filter.Filter} objects.
     * @param user a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Filter[] filters, String user, Date time) throws SQLException {
        if (filters == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer update = new StringBuffer("UPDATE ALARMS SET ALARMACKUSER=?, ALARMACKTIME=? WHERE");
        update.append(getAcknowledgeTypeClause(AcknowledgeType.UNACKNOWLEDGED));

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
     *
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledgeAll(String user) throws SQLException {
        acknowledgeAll(user, new Date());
    }

    /**
     * Acknowledge all unacknowledged alarms with the given username and the
     * given time.
     *
     * @param user a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @throws java.sql.SQLException if any.
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
     *
     * @param alarms an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
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
     *
     * @param alarmIds an array of int.
     * @throws java.sql.SQLException if any.
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
     *
     * @param filters an array of {@link org.opennms.web.alarm.filter.Filter} objects.
     * @throws java.sql.SQLException if any.
     */
    public static void unacknowledge(Filter[] filters) throws SQLException {
        if (filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        StringBuffer update = new StringBuffer("UPDATE ALARMS SET ALARMACKUSER=NULL, ALARMACKTIME=NULL WHERE");
        update.append(getAcknowledgeTypeClause(AcknowledgeType.ACKNOWLEDGED));

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
     *
     * @throws java.sql.SQLException if any.
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
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.alarm.Alarm} objects.
     * @throws java.sql.SQLException if any.
     */
    // FIXME: Don't reuse the same "element" variable for different objects
    protected static Alarm[] rs2Alarms(ResultSet rs) throws SQLException {
        Alarm[] alarms = null;
        Vector<Alarm> vector = new Vector<Alarm>();

        while (rs.next()) {
            Alarm alarm = new Alarm();

            Object element = new Integer(rs.getInt("alarmID"));
            alarm.id = ((Integer) element).intValue();

            element = rs.getString("eventUei");
            alarm.uei = (String) element;

            element = rs.getString("dpName");
            alarm.dpName = (String) element;

            // node id can be null
            element = rs.getObject("nodeID");
            if (element == null) {
                alarm.nodeID = new Integer(0);
            } else {
                alarm.nodeID = (Integer) element;
            }

            element = rs.getString("ipAddr");
            alarm.ipAddr = (String) element;

            element = rs.getObject("serviceID");
            alarm.serviceID = (Integer) element;

            element = rs.getString("reductionKey");
            alarm.reductionKey = (String) element;

            element = rs.getObject("counter");
            alarm.count = ((Integer) element).intValue();

            element = new Integer(rs.getInt("severity"));
            alarm.severity = ((Integer) element).intValue();

            element = new Integer(rs.getInt("lastEventID"));
            alarm.lastEventID = ((Integer) element).intValue();

            element = rs.getTimestamp("firsteventtime");
            alarm.firsteventtime = new Date(((Timestamp) element).getTime());

            element = rs.getTimestamp("lasteventtime");
            alarm.lasteventtime = new Date(((Timestamp) element).getTime());

            element = rs.getString("description");
            alarm.description = (String) element;

            element = rs.getString("logmsg");
            alarm.logMessage = (String) element;

            element = rs.getString("OperInstruct");
            alarm.operatorInstruction = (String) element;

            element = rs.getString("TTicketID");
            alarm.troubleTicket = (String) element;

            Integer stateCode = (Integer)rs.getObject("TTicketState");
            for (TroubleTicketState state : TroubleTicketState.values()) {
                if (stateCode != null && state.ordinal() == stateCode.intValue()) {
                    alarm.troubleTicketState = state;
                }
            }

            element = rs.getString("MouseOverText");
            alarm.mouseOverText = (String) element;

            element = rs.getTimestamp("suppressedUntil");
            alarm.suppressedUntil = new Date(((Timestamp) element).getTime());

            element = rs.getString("suppressedUser");
            alarm.suppressedUser = (String) element;

            element = rs.getTimestamp("suppressedTime");
            alarm.suppressedTime = new Date(((Timestamp) element).getTime());

            element = rs.getString("alarmAckUser");
            alarm.acknowledgeUser = (String) element;

            element = rs.getTimestamp("alarmAckTime");
            if (element != null) {
                alarm.acknowledgeTime = new Date(((Timestamp) element).getTime());
            }

            element = rs.getString("nodeLabel");
            alarm.nodeLabel = (String) element;

            element = rs.getString("serviceName");
            alarm.serviceName = (String) element;

            vector.addElement(alarm);
        }

        alarms = new Alarm[vector.size()];

        for (int i = 0; i < alarms.length; i++) {
            alarms[i] = vector.elementAt(i);
        }

        return alarms;
    }

    /**
     * Convenience method for getting the SQL <em>ORDER BY</em> clause related
     * to a given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.alarm.AlarmFactory.SortStyle} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String getOrderByClause(SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String clause = null;

        switch (sortStyle.getId()) {
        case SortStyle._SEVERITY:
            clause = " ORDER BY SEVERITY DESC";
            break;

        case SortStyle._REVERSE_SEVERITY:
            clause = " ORDER BY SEVERITY ASC";
            break;

        case SortStyle._LASTEVENTTIME:
            clause = " ORDER BY LASTEVENTTIME DESC";
            break;

        case SortStyle._REVERSE_LASTEVENTTIME:
            clause = " ORDER BY LASTEVENTTIME ASC";
            break;

        case SortStyle._FIRSTEVENTTIME:
            clause = " ORDER BY FIRSTEVENTTIME DESC";
            break;

        case SortStyle._REVERSE_FIRSTEVENTTIME:
            clause = " ORDER BY FIRSTEVENTTIME ASC";
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
            clause = " ORDER BY ALARMID DESC";
            break;

        case SortStyle._REVERSE_ID:
            clause = " ORDER BY ALARMID ASC";
            break;

        case SortStyle._COUNT:
            clause = " ORDER BY COUNTER DESC";
            break;

        case SortStyle._REVERSE_COUNT:
            clause = " ORDER BY COUNTER ASC";
            break;

        default:
            throw new IllegalArgumentException("Unknown AlarmFactory.SortStyle: " + sortStyle.getName());
        }

        return clause;
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

        String clause = null;

        switch (ackType.getId()) {
        case AcknowledgeType._ACKNOWLEDGED:
            clause = " ALARMACKUSER IS NOT NULL";
            break;

        case AcknowledgeType._UNACKNOWLEDGED:
            clause = " ALARMACKUSER IS NULL";
            break;

        case AcknowledgeType._BOTH:
            clause = " (ALARMACKUSER IS NULL OR ALARMACKUSER IS NOT NULL)";
            break;

        default:
            throw new IllegalArgumentException("Unknown AlarmFactory.AcknowledgeType: " + ackType.getName());
        }

        return clause;
    }
    
    /**
     * Escalate a list of alarms using the given username and the current time
     *
     * @throws java.sql.SQLException if any.
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     */
    public static void escalateAlarms(int[] alarmIds, String user) throws SQLException {
    	escalateAlarms(alarmIds, user, new Date());
    }
    
    /**
     * Escalate a list of alarms.  The username and time are currently discarded, but
     * are required for future use.
     *
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @throws java.sql.SQLException if any.
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
                stmt.setInt(1, OnmsAlarm.CLEARED_SEVERITY);
                stmt.setInt(2, OnmsAlarm.WARNING_SEVERITY);
                stmt.setInt(3, OnmsAlarm.CRITICAL_SEVERITY);
                stmt.setInt(4, OnmsAlarm.CRITICAL_SEVERITY);
                stmt.setInt(5, OnmsAlarm.PROBLEM_TYPE);
                stmt.setInt(6, OnmsAlarm.RESOLUTION_TYPE);
                stmt.setInt(7, OnmsAlarm.CLEARED_SEVERITY);
                stmt.setInt(8, OnmsAlarm.PROBLEM_TYPE);
                stmt.setInt(9, OnmsAlarm.NORMAL_SEVERITY);
                stmt.setInt(10, OnmsAlarm.CRITICAL_SEVERITY);

                stmt.executeUpdate();
            } finally {
                d.cleanUp();
            }
        }
    }
    
    /**
     * Clear a list of alarms, using the given username and the current time
     *
     * @throws java.sql.SQLException if any.
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     */
    public static void clearAlarms(int[] alarmIds, String user) throws SQLException {
    	clearAlarms(alarmIds, user, new Date());
    }

    /**
     * Clear a list of alarms.  The username and time are currently discarded, but
     * are required for future use.
     *
     * @param alarmIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @throws java.sql.SQLException if any.
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
                stmt.setInt(1, OnmsAlarm.CLEARED_SEVERITY);
                stmt.setInt(2, OnmsAlarm.RESOLUTION_TYPE);
                stmt.setInt(3, OnmsAlarm.NORMAL_SEVERITY);
                stmt.setInt(4, OnmsAlarm.CRITICAL_SEVERITY);

                stmt.executeUpdate();
            } finally {
                d.cleanUp();
            }
        }
    }

}
