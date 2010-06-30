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
// 2007 Jul 24: Java 5 generics, refactor logging. - dj@opennms.org
// 2003 Apr 01: Fixed bug with number of notifications outstanding.
// 2003 Jan 08: Changed SQL "= null" to "is null" to work with Postgres 7.2.
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

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.opennms.core.resource.Vault;

/**
 * <p>NotificationModel class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class NotificationModel extends Object {
    // The whole bunch of constants.
    private final String USERID = "userID";

    private final String NOTICE_TIME = "notifytime";

    private final String TXT_MESG = "textMsg";

    private final String NUM_MESG = "numericMsg";

    private final String NOTIFY = "notifyID";

    private final String TIME = "pageTime";

    private final String REPLYTIME = "respondTime";

    private final String ANS_BY = "answeredBy";

    private final String CONTACT = "contactInfo";

    private final String NODE = "nodeID";

    private final String INTERFACE = "interfaceID";

    private final String SERVICE = "serviceID";

    private final String MEDIA = "media";

    private final String EVENTID = "eventid";

    private final String SELECT = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid from NOTIFICATIONS";

    private final String NOTICE_ID = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid from NOTIFICATIONS where NOTIFYID = ?";

    private final String SENT_TO = "SELECT userid, notifytime, media, contactinfo FROM usersnotified WHERE notifyid=?";

    private final String INSERT_NOTIFY = "INSERT INTO NOTIFICATIONS (notifyid, textmsg, numericmsg, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid) VALUES (NEXTVAL('notifyNxtId'), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String OUTSTANDING = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid FROM NOTIFICATIONS WHERE respondTime is NULL";

    private final String OUTSTANDING_COUNT = "SELECT COUNT(notifyid) AS TOTAL FROM NOTIFICATIONS WHERE respondTime is NULL";

    private final String USER_OUTSTANDING = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid FROM NOTIFICATIONS WHERE (respondTime is NULL) AND notifications.notifyid in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)";

    private final String USER_OUTSTANDING_COUNT = "SELECT COUNT(notifyid) AS TOTAL FROM NOTIFICATIONS WHERE (respondTime is NULL) AND notifications.notifyid in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)";

    private Category log() {
        return Logger.getLogger(getClass());
    }

    /**
     * <p>getNoticeInfo</p>
     *
     * @param id a int.
     * @return a {@link org.opennms.web.notification.Notification} object.
     * @throws java.sql.SQLException if any.
     */
    public Notification getNoticeInfo(int id) throws SQLException {
        Notification nbean = new Notification();

        Connection conn = Vault.getDbConnection();

        try {
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

            // fill out the rest of the bean
            PreparedStatement pstmt = conn.prepareStatement(NOTICE_ID);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            // FIXME: Don't reuse the "element" variable for different objects.
            while (rs.next()) {
                Object element = rs.getString(TXT_MESG);
                nbean.m_txtMsg = (String) element;

                element = rs.getString(NUM_MESG);
                nbean.m_numMsg = (String) element;

                element = new Integer(rs.getInt(NOTIFY));
                nbean.m_notifyID = ((Integer) element).intValue();

                element = rs.getTimestamp(TIME);
                nbean.m_timeSent = ((Timestamp) element).getTime();

                element = rs.getTimestamp(REPLYTIME);
                if (element != null) {
                    nbean.m_timeReply = ((Timestamp) element).getTime();
                } else {
                    nbean.m_timeReply = 0;
                }

                element = rs.getString(ANS_BY);
                nbean.m_responder = (String) element;

                element = new Integer(rs.getInt(NODE));
                nbean.m_nodeID = ((Integer) element).intValue();

                element = rs.getString(INTERFACE);
                nbean.m_interfaceID = (String) element;

                element = new Integer(rs.getInt(SERVICE));
                nbean.m_serviceId = ((Integer) element).intValue();

                element = new Integer(rs.getInt(EVENTID));
                nbean.m_eventId = ((Integer) element).intValue();

                PreparedStatement stmttmp = conn.prepareStatement("SELECT servicename from service where serviceid = ?");
                stmttmp.setInt(1, nbean.m_serviceId);
                ResultSet rstmp = stmttmp.executeQuery();

                if (rstmp.next()) {
                    element = rstmp.getString("servicename");
                    if (element != null) {
                        nbean.m_serviceName = (String) element;
                    }
                }
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            log().error("Problem getting data from the notifications table: " + e, e);
            throw e;
        } finally {
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
            log().error("allNotifications: Problem getting data from the notifications table: " + e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return (notices);
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

        // Format the results.
        try {
            while (rs.next()) {
                Notification nbean = new Notification();

                Object element = rs.getString(TXT_MESG);
                nbean.m_txtMsg = (String) element;

                element = rs.getString(NUM_MESG);
                nbean.m_numMsg = (String) element;

                element = new Integer(rs.getInt(NOTIFY));
                nbean.m_notifyID = ((Integer) element).intValue();

                element = rs.getTimestamp(TIME);
                nbean.m_timeSent = ((Timestamp) element).getTime();

                element = rs.getTimestamp(REPLYTIME);
                if (element != null) {
                    nbean.m_timeReply = ((Timestamp) element).getTime();
                } else {
                    nbean.m_timeReply = 0;
                }

                element = rs.getString(ANS_BY);
                nbean.m_responder = (String) element;

                element = new Integer(rs.getInt(NODE));
                nbean.m_nodeID = ((Integer) element).intValue();

                element = rs.getString(INTERFACE);
                nbean.m_interfaceID = (String) element;

                element = new Integer(rs.getInt(SERVICE));
                nbean.m_serviceId = ((Integer) element).intValue();

                element = new Integer(rs.getInt(EVENTID));
                nbean.m_eventId = ((Integer) element).intValue();

                PreparedStatement stmttmp = conn.prepareStatement("SELECT servicename from service where serviceid = ?");
                stmttmp.setInt(1, nbean.m_serviceId);
                ResultSet rstmp = stmttmp.executeQuery();

                if (rstmp.next()) {
                    element = rstmp.getString("servicename");
                    if (element != null) {
                        nbean.m_serviceName = (String) element;
                    }
                }

                rstmp.close();
                stmttmp.close();

                vector.addElement(nbean);
            }
        } catch (SQLException e) {
            log().error("Error occurred in rs2NotifyBean: " + e, e);
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
            log().error("Problem getting data from the notifications table: " + e, e);
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
            log().error("Problem getting data from the notifications table: " + e, e);
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
            log().error("Problem getting data from the notifications table: " + e, e);
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
            log().error("Problem getting data from the notifications table: " + e, e);
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
            log().error("Problem acknowledging notification " + noticeId + " as answered by '" + name + "': " + e, e);
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
            log().error("Problem getting data from the notifications table: " + e, e);
            throw e;
        } finally {
            Vault.releaseDbConnection(conn);
        }
    }

    /**
     * This method may be used to insert / update the database.
     */
    /*
     * public void execute(String query) { if(m_dbConn == null) {
     * m_logger.debug("Problem executing query."); m_logger.debug("We do not
     * have a database connection yet"); } else { try { Statement stmt =
     * m_dbConn.createStatement(); stmt.executeQuery(query);
     *  // Close statement. if(stmt != null) stmt.close(); } catch(Exception e) {
     * m_logger.debug("Problem inserting"); m_logger.debug("Error: " +
     * e.getLocalizedMessage()); } } }
     */

    /**
     * This method is used for formatting the date attributes in a proper
     * format.
     */
    // FIXME: This is unused; if we don't need it, remove it
//    private String appendZero(String values) {
//        int len = values.length();
//        if (values != null) {
//            if (len >= 0 && len < 2) {
//                while (2 - len > 0) {
//                    values = "0" + values;
//                    len++;
//                }
//            } else if (len > 2) {
//                log().debug("Incorrect attributes for time: len > 2; len = " + len);
//            }
//        }
//        return values;
//    }

}
