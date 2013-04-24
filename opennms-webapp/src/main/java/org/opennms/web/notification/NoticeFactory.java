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

package org.opennms.web.notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.filter.NodeFilter;

/**
 * Encapsulates all querying functionality for notices
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 */
public class NoticeFactory {

    /** Private constructor so this class cannot be instantiated. */
    private NoticeFactory() {
    }

    /**
     * Count the number of notices for a given acknowledgement type.
     *
     * @param ackType a {@link org.opennms.web.notification.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getNoticeCount(AcknowledgeType ackType, Filter[] filters) throws SQLException {
        if (ackType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int noticeCount = 0;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT COUNT(NOTIFYID) AS NOTICECOUNT FROM NOTIFICATIONS WHERE");
            select.append(ackType.getAcknowledgeTypeClause());

            for (Filter filter : filters) {
                select.append(" AND");
                select.append(filter.getParamSql());
            }

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }
            
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                noticeCount = rs.getInt("NOTICECOUNT");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return noticeCount;
    }

    /**
     * Return a specific notice.
     *
     * @param noticeId a int.
     * @return a {@link org.opennms.web.notification.Notification} object.
     * @throws java.sql.SQLException if any.
     */
    public static Notification getNotice(int noticeId, ServletContext servletContext) throws SQLException {
        Notification notice = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NOTIFICATION WHERE NOTIFYID=?");
            stmt.setInt(1, noticeId);
            ResultSet rs = stmt.executeQuery();

            Notification[] notices = rs2Notices(rs, servletContext);

            // what do I do if this actually returns more than one service?
            if (notices.length > 0) {
                notice = notices[0];
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return notice;
    }

    /**
     * This method determines the log status of an event associated with a
     * notification
     *
     * @param eventId
     *            the unique id of the event from the notice
     * @return true if the event is display, false if log only
     */
    public static boolean canDisplayEvent(int eventId) {
        boolean display = false;

        Connection connection = null;
        try {
            connection = Vault.getDbConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT eventDisplay FROM events WHERE eventid=?");

            statement.setInt(1, eventId);

            ResultSet results = statement.executeQuery();

            results.next();
            String status = results.getString(1);

            if (status.equals("Y")) {
                display = true;
            }

            statement.close();
            results.close();
        } catch (SQLException e) {
            ThreadCategory.getInstance(NoticeFactory.class.getName()).error("Error getting event display status: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    Vault.releaseDbConnection(connection);
                } catch (SQLException e) {
                }
            }
        }

        return display;
    }

    /**
     * Return all unacknowledged notices sorted by id.
     *
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNotices(ServletContext servletContext) throws SQLException {
        return (NoticeFactory.getNotices(SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, servletContext));
    }

    /**
     * Return all unacknowledged or acknowledged notices sorted by id.
     *
     * @param ackType a {@link org.opennms.web.notification.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNotices(AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        return (NoticeFactory.getNotices(SortStyle.ID, ackType, servletContext));
    }

    /**
     * Return all unacknowledged notices sorted by the given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.notification.SortStyle} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNotices(SortStyle sortStyle, ServletContext servletContext) throws SQLException {
        return (NoticeFactory.getNotices(sortStyle, AcknowledgeType.UNACKNOWLEDGED, servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by the
     * given sort style.
     *
     * @deprecated Replaced by
     *             {@link " #getNotices(SortStyle,AcknowledgeType) getNotices( SortStyle, AcknowledgeType )"}
     * @param sortStyle a {@link org.opennms.web.notification.SortStyle} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNotices(SortStyle sortStyle, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (NoticeFactory.getNotices(sortStyle, ackType, servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.notification.SortStyle} object.
     * @param ackType a {@link org.opennms.web.notification.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNotices(SortStyle sortStyle, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        return (NoticeFactory.getNotices(sortStyle, ackType, new org.opennms.web.filter.Filter[0], servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by the
     * given sort style.
     *
     * @param sortStyle a {@link org.opennms.web.notification.SortStyle} object.
     * @param ackType a {@link org.opennms.web.notification.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNotices(SortStyle sortStyle, AcknowledgeType ackType, org.opennms.web.filter.Filter[] filters, ServletContext servletContext) throws SQLException {
        return (NoticeFactory.getNotices(sortStyle, ackType, filters, -1, -1, servletContext));
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
     * @param sortStyle a {@link org.opennms.web.notification.SortStyle} object.
     * @param ackType a {@link org.opennms.web.notification.AcknowledgeType} object.
     * @param filters an array of org$opennms$web$filter$Filter objects.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNotices(SortStyle sortStyle, AcknowledgeType ackType, org.opennms.web.filter.Filter[] filters, int limit, int offset, ServletContext servletContext) throws SQLException {
        if (sortStyle == null || ackType == null || filters == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        boolean useLimits = false;
        if (limit > 0 && offset > -1) {
            useLimits = true;
        }

        Notification[] notices = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT * FROM NOTIFICATIONS WHERE");
            select.append(ackType.getAcknowledgeTypeClause());

            for (Filter filter : filters) {
                select.append(" AND");
                select.append(filter.getParamSql());
            }

            select.append(sortStyle.getOrderByClause());

            if (useLimits) {
                select.append(" LIMIT ?");
                //select.append(limit);
                select.append(" OFFSET ?");
                //select.append(offset);
            }

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            
            int parameterIndex = 1;
            for (Filter filter : filters) {
            	parameterIndex += filter.bindParam(stmt, parameterIndex);
            }
            
            if (useLimits) {
            	stmt.setInt(parameterIndex++, limit);
            	stmt.setInt(parameterIndex, offset);
            }
            ResultSet rs = stmt.executeQuery();
            
//            PreparedStatement ps = conn.prepareStatement(select.toString());
            notices = rs2Notices(rs, servletContext);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return notices;
    }

    /**
     * Return all unacknowledged notices sorted by time for the given node.
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForNode(int nodeId, ServletContext servletContext) throws SQLException {
        return (getNoticesForNode(nodeId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by id
     * for the given node.
     *
     * @deprecated Replaced by
     *             {@link " #getNoticesForNode(int,SortStyle,AcknowledgeType) getNoticesForNode( int, SortStyle, AcknowledgeType )"}
     * @param nodeId a int.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForNode(int nodeId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = (includeAcknowledged) ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return (getNoticesForNode(nodeId, SortStyle.ID, ackType, servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by
     * given sort style for the given node.
     *
     * @param nodeId a int.
     * @param sortStyle a {@link org.opennms.web.notification.SortStyle} object.
     * @param ackType a {@link org.opennms.web.notification.AcknowledgeType} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        if (sortStyle == null || ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        org.opennms.web.filter.Filter[] filters = new org.opennms.web.filter.Filter[] { new NodeFilter(nodeId) };
        return (NoticeFactory.getNotices(sortStyle, ackType, filters, servletContext));
    }

    /**
     * Return all unacknowledged notices for the given interface.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForInterface(int nodeId, String ipAddress, ServletContext servletContext) throws SQLException {
        return (getNoticesForInterface(nodeId, ipAddress, false, servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by id
     * for the given interface.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForInterface(int nodeId, String ipAddress, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Notification[] notices = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT * FROM NOTIFICATIONS WHERE NODEID=? AND INTERFACEID=?");

            if (!includeAcknowledged) {
                select.append(" AND RESPONDTIME IS NULL");
            }

            select.append(" ORDER BY NOTIFYID DESC");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            ResultSet rs = stmt.executeQuery();

            notices = rs2Notices(rs, servletContext);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return notices;
    }

    /**
     * Return all unacknowledged notices sorted by time for that have the given
     * IP address, regardless of what node they belong to.
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForInterface(String ipAddress, ServletContext servletContext) throws SQLException {
        return (getNoticesForInterface(ipAddress, false, servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by id
     * that have the given IP address, regardless of what node they belong to.
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForInterface(String ipAddress, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Notification[] notices = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT * FROM NOTIFICATIONS WHERE INTERFACEID=?");

            if (!includeAcknowledged) {
                select.append(" AND RESPONDTIME IS NULL");
            }

            select.append(" ORDER BY NOTIFYID DESC");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();

            notices = rs2Notices(rs, servletContext);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return notices;
    }

    /**
     * Return all unacknowledged notices sorted by time for the given service.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForService(int nodeId, String ipAddress, int serviceId, ServletContext servletContext) throws SQLException {
        return (getNoticesForService(nodeId, ipAddress, serviceId, false, servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by
     * time for the given service.
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForService(int nodeId, String ipAddress, int serviceId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Notification[] notices = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT * FROM NOTIFICATIONS WHERE NODEID=? AND INTERFACEID=? AND SERVICEID=?");

            if (!includeAcknowledged) {
                select.append(" AND RESPONDTIME IS NULL");
            }

            select.append(" ORDER BY NOTIFYID DESC");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            stmt.setInt(3, serviceId);
            ResultSet rs = stmt.executeQuery();

            notices = rs2Notices(rs, servletContext);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return notices;
    }

    /**
     * Return all unacknowledged notices sorted by time for the given service
     * type, regardless of what node or interface they belong to.
     *
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForService(int serviceId, ServletContext servletContext) throws SQLException {
        return (getNoticesForService(serviceId, false, servletContext));
    }

    /**
     * Return all notices (optionally only unacknowledged notices) sorted by id
     * for the given service type, regardless of what node or interface they
     * belong to.
     *
     * @param serviceId a int.
     * @param includeAcknowledged a boolean.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Notification[] getNoticesForService(int serviceId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        Notification[] notices = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer select = new StringBuffer("SELECT * FROM NOTIFICATION WHERE SERVICEID=?");

            if (!includeAcknowledged) {
                select.append(" AND RESPONDTIME IS NULL");
            }

            select.append(" ORDER BY NOTIFIYID DESC");

            PreparedStatement stmt = conn.prepareStatement(select.toString());
            stmt.setInt(1, serviceId);
            ResultSet rs = stmt.executeQuery();

            notices = rs2Notices(rs, servletContext);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return notices;
    }

    /**
     * Acknowledge a list of notices with the given username
     *
     * @param notices an array of {@link org.opennms.web.notification.Notification} objects.
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Notification[] notices, String user) throws SQLException {
        acknowledge(notices, user, new Date());
    }

    /**
     * Acknowledge a list of notices with the given username and the given time.
     *
     * @param notices an array of {@link org.opennms.web.notification.Notification} objects.
     * @param user a {@link java.lang.String} object.
     * @param time a java$util$Date object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(Notification[] notices, String user, Date time) throws SQLException {
        if (notices == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int[] ids = new int[notices.length];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = notices[i].getId();
        }

        acknowledge(ids, user, time);
    }

    /**
     * Acknowledge a list of notices with the given username and the current
     * time.
     *
     * @param noticeIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(int[] noticeIds, String user) throws SQLException {
        acknowledge(noticeIds, user, new Date());
    }

    /**
     * Acknowledge a list of notices with the given username and the given time.
     *
     * @param noticeIds an array of int.
     * @param user a {@link java.lang.String} object.
     * @param time a java$util$Date object.
     * @throws java.sql.SQLException if any.
     */
    public static void acknowledge(int[] noticeIds, String user, Date time) throws SQLException {
        if (noticeIds == null || user == null || time == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (noticeIds.length > 0) {
            StringBuffer update = new StringBuffer("UPDATE NOTIFICATIONS SET RESPONDTIME=?, ANSWEREDBY=?");
            update.append(" WHERE NOTIFYID IN (");
            update.append(noticeIds[0]);

            for (int i = 1; i < noticeIds.length; i++) {
                update.append(",");
                update.append(noticeIds[i]);
            }

            update.append(")");
            update.append(" AND RESPONDTIME IS NULL");

            Connection conn = Vault.getDbConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement(update.toString());
                stmt.setTimestamp(1, new Timestamp(time.getTime()));
                stmt.setString(2, user);

                stmt.executeUpdate();
                stmt.close();
            } finally {
                Vault.releaseDbConnection(conn);
            }
        }
    }

    /**
     * Convenience method for translating a <code>java.sql.ResultSet</code>
     * containing notice information into an array of <code>Notification</code>
     * objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Notification[] rs2Notices(ResultSet rs, ServletContext servletContext) throws SQLException {
        Notification[] notices = null;
        Vector<Notification> vector = new Vector<Notification>();

        while (rs.next()) {
            Notification notice = new Notification();

            Integer id = Integer.valueOf(rs.getInt("notifyid"));
            notice.m_notifyID = id.intValue();

            Timestamp timestamp = rs.getTimestamp("pagetime");
            notice.m_timeSent = (timestamp != null) ? timestamp.getTime() : null;

            timestamp = rs.getTimestamp("respondtime");
            notice.m_timeReply = (timestamp != null) ? timestamp.getTime() : null;

            String str = rs.getString("textmsg");
            notice.m_txtMsg = str;

            str = rs.getString("numericmsg");
            notice.m_numMsg = str;

            str = rs.getString("answeredby");
            notice.m_responder = str;

            id = Integer.valueOf(rs.getInt("nodeid"));
            notice.m_nodeID = id.intValue();

            str = rs.getString("interfaceid");
            notice.m_interfaceID = str;

            id = Integer.valueOf(rs.getInt("eventid"));
            notice.m_eventId = id.intValue();

            id = Integer.valueOf(rs.getInt("serviceid"));
            if (id != null) {
                notice.m_serviceId = id.intValue();
                str = NetworkElementFactory.getInstance(servletContext).getServiceNameFromId(notice.m_serviceId);
                notice.m_serviceName = str;
            }

            vector.addElement(notice);
        }

        notices = new Notification[vector.size()];

        for (int i = 0; i < notices.length; i++) {
            notices[i] = vector.elementAt(i);
        }

        return notices;
    }
}
