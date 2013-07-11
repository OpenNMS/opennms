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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opennms.core.resource.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NotificationModel class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NotificationModel extends Object {
	
	private static final Logger LOG = LoggerFactory.getLogger(NotificationModel.class);

    private static final String USERID = "userID";

    private static final String NOTICE_TIME = "notifytime";

    private static final String TXT_MESG = "textMsg";

    private static final String NUM_MESG = "numericMsg";

    private static final String NOTIFY = "notifyID";

    private static final String TIME = "pageTime";

    private static final String REPLYTIME = "respondTime";

    private static final String ANS_BY = "answeredBy";

    private static final String CONTACT = "contactInfo";

    private static final String NODE = "nodeID";

    private static final String INTERFACE = "interfaceID";

    private static final String SERVICE = "serviceID";

    private static final String MEDIA = "media";

    private static final String EVENTID = "eventid";

    private static final String SELECT = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid from NOTIFICATIONS";

    private static final String NOTICE_ID = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid from NOTIFICATIONS where NOTIFYID = ?";

    private static final String SENT_TO = "SELECT userid, notifytime, media, contactinfo FROM usersnotified WHERE notifyid=?";

    private static final String INSERT_NOTIFY = "INSERT INTO NOTIFICATIONS (notifyid, textmsg, numericmsg, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid) VALUES (NEXTVAL('notifyNxtId'), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String OUTSTANDING = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid FROM NOTIFICATIONS WHERE respondTime is NULL";

    private static final String OUTSTANDING_COUNT = "SELECT COUNT(notifyid) AS TOTAL FROM NOTIFICATIONS WHERE respondTime is NULL";

    private static final String USER_OUTSTANDING = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid FROM NOTIFICATIONS WHERE (respondTime is NULL) AND notifications.notifyid in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)";

    private static final String USER_OUTSTANDING_COUNT = "SELECT COUNT(notifyid) AS TOTAL FROM NOTIFICATIONS WHERE (respondTime is NULL) AND notifications.notifyid in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)";

    

    /**
     * <p>getNoticeInfo</p>
     *
     * @param id a int.
     * @return a {@link org.opennms.web.notification.Notification} object.
     * @throws java.sql.SQLException if any.
     */
    public Notification getNoticeInfo(int id) throws SQLException {
        Notification nbean = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        Connection conn = Vault.getDbConnection();

        try {
            pstmt = conn.prepareStatement(NOTICE_ID);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            Notification[] n = rs2NotifyBean(conn, rs);
            if (n.length > 0) {
                nbean = n[0];
            } else {
                nbean = new Notification();
            }

            rs.close();
            pstmt.close();

            // create the list of users the page was sent to
            PreparedStatement sentTo = conn.prepareStatement(SENT_TO);
            sentTo.setInt(1, id);
            ResultSet sentToResults = sentTo.executeQuery();

            List<NoticeSentTo> sentToList = new ArrayList<NoticeSentTo>();
            while (sentToResults.next()) {
                NoticeSentTo newSentTo = new NoticeSentTo();
                newSentTo.setUserId(sentToResults.getString(USERID));
                Timestamp ts = sentToResults.getTimestamp(NOTICE_TIME);
                if (ts != null) {
                    newSentTo.setTime(ts.getTime());
                } else {
                    newSentTo.setTime(0);
                }
                newSentTo.setMedia(sentToResults.getString(MEDIA));
                newSentTo.setContactInfo(sentToResults.getString(CONTACT));
                sentToList.add(newSentTo);
            }

            nbean.m_sentTo = sentToList;
        } catch (SQLException e) {
            LOG.error("Problem getting data from the notifications table: {}", e, e);
            throw e;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                throw(e);
            } finally {
                try {
                    if (pstmt != null) {
                        pstmt.close();
                    }
                } catch (SQLException e) {
                    throw(e);
                }
            }
            Vault.releaseDbConnection(conn);
        }

        return nbean;
    }

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

        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();

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
            
            ResultSet rs = stmt.executeQuery(query);
            notices = rs2NotifyBean(conn, rs);

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOG.error("allNotifications: Problem getting data from the notifications table: {}", e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return (notices);
    }

    private String getServiceName(Connection conn, Integer id) {
        if (id == null) {
            return null;
        }
        
        String serviceName = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            ps = conn.prepareStatement("SELECT servicename from service where serviceid = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                serviceName = rs.getString("servicename");
            }
        } catch (SQLException e) {
            LOG.warn("unable to get service name for service ID '{}'", id, e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                LOG.warn("unable to close result set while getting service name for service ID '{}'", id, e);
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException e) {
                    LOG.warn("unable to close prepared statement while getting service name for service ID '{}'", id, e);
                }
            }
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
    protected Notification[] rs2NotifyBean(Connection conn, ResultSet rs) throws SQLException {
        Notification[] notices = null;
        Vector<Notification> vector = new Vector<Notification>();
        
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
                vector.addElement(nbean);
            }
        } catch (SQLException e) {
            LOG.error("Error occurred in rs2NotifyBean: {}", e, e);
            throw e;
        }

        notices = new Notification[vector.size()];

        for (int i = 0; i < notices.length; i++) {
            notices[i] = vector.elementAt(i);
        }

        return notices;
    }

    /**
     * This method returns the count of all outstanding notices.
     *
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public Notification[] getOutstandingNotices() throws SQLException {
        Notification[] notices = null;

        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(OUTSTANDING);
            notices = rs2NotifyBean(conn, rs);

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOG.error("Problem getting data from the notifications table: {}", e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return notices;
    }

    /**
     * This method returns notices not yet acknowledged.
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public int getOutstandingNoticeCount() throws SQLException {
        int count = 0;

        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(OUTSTANDING_COUNT);

            if (rs.next()) {
                count = rs.getInt("TOTAL");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOG.error("Problem getting data from the notifications table: {}", e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return count;
    }

    /**
     * This method returns notices not yet acknowledged.
     *
     * @param username a {@link java.lang.String} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public int getOutstandingNoticeCount(String username) throws SQLException {
        if (username == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int count = 0;

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement(USER_OUTSTANDING_COUNT);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("TOTAL");
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            LOG.error("Problem getting data from the notifications table: {}", e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return (count);
    }

    /**
     * This method returns notices not yet acknowledged.
     *
     * @param name a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.notification.Notification} objects.
     * @throws java.sql.SQLException if any.
     */
    public Notification[] getOutstandingNotices(String name) throws SQLException {
        Notification[] notices = null;

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement(USER_OUTSTANDING);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            notices = rs2NotifyBean(conn, rs);

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            LOG.error("Problem getting data from the notifications table: {}", e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return (notices);
    }

    /**
     * This method updates the table when the user acknowledges the pager
     * information.
     *
     * @param name a {@link java.lang.String} object.
     * @param noticeId a int.
     * @throws java.sql.SQLException if any.
     */
    public void acknowledged(String name, int noticeId) throws SQLException {
        if (name == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE notifications SET respondtime = ? , answeredby = ? WHERE notifyid= ?");
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(2, name);
            pstmt.setInt(3, noticeId);
            pstmt.execute();

            pstmt.close();
        } catch (SQLException e) {
            LOG.error("Problem acknowledging notification {} as answered by '{}': {}", noticeId, name, e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }
    }

    /**
     * This method helps insert into the database.
     *
     * @param nbean a {@link org.opennms.web.notification.Notification} object.
     * @throws java.sql.SQLException if any.
     */
    public void insert(Notification nbean) throws SQLException {
        if (nbean == null || nbean.m_txtMsg == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement(INSERT_NOTIFY);

            pstmt.setString(1, nbean.m_txtMsg);
            pstmt.setString(2, nbean.m_numMsg);
            pstmt.setLong(3, nbean.m_timeSent);
            pstmt.setLong(4, nbean.m_timeReply);
            pstmt.setString(5, nbean.m_responder);
            pstmt.setInt(6, nbean.m_nodeID);
            pstmt.setString(7, nbean.m_interfaceID);
            pstmt.setInt(8, nbean.m_serviceId);
            pstmt.setInt(9, nbean.m_eventId);

            pstmt.execute();

            // Close prepared statement.
            pstmt.close();
        } catch (SQLException e) {
            LOG.error("Problem getting data from the notifications table: {}", e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }
    }

}
