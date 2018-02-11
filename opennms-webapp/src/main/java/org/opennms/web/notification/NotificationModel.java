/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Use the {@link NotificationDao} directly or use the {@link WebNotificationRepository}
 * instead of this class.
 */
public class NotificationModel extends Object {
	
	private static final Logger LOG = LoggerFactory.getLogger(NotificationModel.class);

    private static final String TXT_MESG = "textMsg";

    private static final String NUM_MESG = "numericMsg";

    private static final String NOTIFY = "notifyID";

    private static final String TIME = "pageTime";

    private static final String REPLYTIME = "respondTime";

    private static final String ANS_BY = "answeredBy";

    private static final String NODE = "nodeID";

    private static final String INTERFACE = "interfaceID";

    private static final String SERVICE = "serviceID";

    private static final String EVENTID = "eventid";

    private static final String SELECT = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid from NOTIFICATIONS";

    /**
     * <p>allNotifications</p>
     *
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public Notification[] allNotifications() throws SQLException {
        return this.allNotifications(null);
    }

    /**
     * Return all notifications, both outstanding and acknowledged.
     *
     * @param order a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public Notification[] allNotifications(String order) throws SQLException {
        Notification[] notices = null;

        final Connection conn = DataSourceFactory.getInstance().getConnection();
        final DBUtils d = new DBUtils(getClass(), conn);

        try {
            final Statement stmt = conn.createStatement();
            d.watch(stmt);

            // oh man this is lame, but it'll be a DAO soon right?  right?  :P
            String query = SELECT;
            if (order != null) {
                if (order.equalsIgnoreCase("asc")) {
                    query += " ORDER BY pagetime ASC";
                } else if (order.equalsIgnoreCase("desc")) {
                    query += " ORDER BY pagetime DESC";
                }
            }
            query += ";";

            final ResultSet rs = stmt.executeQuery(query);
            d.watch(rs);

            notices = rs2NotifyBean(conn, rs);
        } catch (SQLException e) {
            LOG.error("allNotifications: Problem getting data from the notifications table: {}", e, e);
            throw e;
        } finally {
            d.cleanUp();
        }

        return (notices);
    }

    protected static String getServiceName(Connection conn, Integer id) {
        if (id == null) {
            return null;
        }

        String serviceName = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        final DBUtils d = new DBUtils(NotificationModel.class);
        try {
            ps = conn.prepareStatement("SELECT servicename from service where serviceid = ?");
            d.watch(ps);
            ps.setInt(1, id);

            rs = ps.executeQuery();
            d.watch(rs);

            if (rs.next()) {
                serviceName = rs.getString("servicename");
            }
        } catch (SQLException e) {
            LOG.warn("unable to get service name for service ID '{}'", id, e);
        } finally {
            d.cleanUp();
        }
        return serviceName;
    }
    /**
     * Returns the data from the result set as an array of
     * Notification objects.  The ResultSet must be positioned before
     * the first result before calling this method (this is the case right
     * after calling java.sql.Connection#createStatement and friends or
     * after calling java.sql.ResultSet#beforeFirst).
     *
     * @param conn a {@link java.sql.Connection} object.
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Notification[] rs2NotifyBean(Connection conn, ResultSet rs) throws SQLException {
        List<Notification> vector = new ArrayList<>();

        try {

            while (rs.next()) {
                Notification nbean = new Notification();
                nbean.m_timeReply = 0;

                nbean.m_txtMsg = rs.getString(TXT_MESG);
                nbean.m_numMsg = rs.getString(NUM_MESG);
                nbean.m_notifyID = rs.getInt(NOTIFY);
                if (rs.getTimestamp(TIME) != null) {
                    nbean.m_timeSent = rs.getTimestamp(TIME).getTime();
                }
                if (rs.getTimestamp(REPLYTIME) != null) {
                    nbean.m_timeReply = rs.getTimestamp(REPLYTIME).getTime();
                }
                nbean.m_responder = rs.getString(ANS_BY);
                nbean.m_nodeID = rs.getInt(NODE);
                nbean.m_interfaceID = rs.getString(INTERFACE);
                nbean.m_serviceId = rs.getInt(SERVICE);
                nbean.m_eventId = rs.getInt(EVENTID);
                nbean.m_serviceName = getServiceName(conn, nbean.m_serviceId);
                vector.add(nbean);
            }
        } catch (SQLException e) {
            LOG.error("Error occurred in rs2NotifyBean: {}", e, e);
            throw e;
        }

        return vector.toArray(new Notification[vector.size()]);
    }
}
