//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
//

package org.opennms.web.notification;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.log4j.Category;

import org.opennms.core.resource.Vault;


public class NotificationModel extends Object 
{
    // The whole bunch of constants.
    private final String USERID           = "userID";
    private final String NOTICE_TIME      = "notifytime";
    private final String TXT_MESG         = "textMsg";
    private final String NUM_MESG         = "numericMsg";
    private final String NOTIFY           = "notifyID"; 
    private final String TIME             = "pageTime";
    private final String REPLYTIME        = "respondTime";
    private final String ANS_BY           = "answeredBy";
    private final String CONTACT          = "contactInfo";
    private final String NODE             = "nodeID";
    private final String INTERFACE        = "interfaceID";
    private final String SERVICE          = "serviceID";
    private final String MEDIA            = "media";
    private final String EVENTID          = "eventid";
    private final String SELECT           = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid from NOTIFICATIONS;";
    private final String NOTICE_ID        = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid from NOTIFICATIONS where NOTIFYID = ?";
    private final String SENT_TO          = "SELECT userid, notifytime, media, contactinfo FROM usersnotified WHERE notifyid=?";
    private final String INSERT_NOTIFY    = "INSERT INTO NOTIFICATIONS (notifyid, textmsg, numericmsg, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid) VALUES (NEXTVAL('notifyNxtId'), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    //private final String UPDATE_NOTIFY    = "UPDATE NOTIFICATIONS SET " + REPLYTIME + "  = ? , " + ANS_BY + " = ? WHERE " + NOTIFY + " = ? ";
    private final String OUTSTANDING      = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid FROM NOTIFICATIONS WHERE respondTime is NULL";
    private final String OUTSTANDING_COUNT = "SELECT COUNT(*) AS TOTAL FROM NOTIFICATIONS WHERE respondTime is NULL";
    private final String USER_OUTSTANDING = "SELECT textmsg, numericmsg, notifyid, pagetime, respondtime, answeredby, nodeid, interfaceid, serviceid, eventid FROM NOTIFICATIONS WHERE (respondTime is NULL) AND notifications.notifyid in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)";
    private final String USER_OUTSTANDING_COUNT = "SELECT COUNT(*) AS TOTAL FROM NOTIFICATIONS WHERE (respondTime is NULL) AND AND userID LIKE ?";

    /**
     * Static Log4j logging category
     */
    private static Category m_logger = Category.getInstance(NotificationModel.class.getName());

    public Notification getNoticeInfo(int id) throws SQLException {
        Notification nbean = new Notification();
        
        Connection conn = Vault.getDbConnection();

        try {
            //create the list of users the page was sent to
            PreparedStatement sentTo = conn.prepareStatement(SENT_TO);
            sentTo.setInt(1, id);
            ResultSet sentToResults = sentTo.executeQuery();
            
            List sentToList = new ArrayList();
            sentToResults.beforeFirst();
            while(sentToResults.next())
            {
                    NoticeSentTo newSentTo = new NoticeSentTo();
                    newSentTo.setUserId( sentToResults.getString(USERID) );
                    Timestamp ts = sentToResults.getTimestamp(NOTICE_TIME);
		    if (ts != null)
                    	newSentTo.setTime(ts.getTime());
		    else
                    	newSentTo.setTime(0);
                    newSentTo.setMedia( sentToResults.getString(MEDIA) );
                    newSentTo.setContactInfo( sentToResults.getString(CONTACT) );
                    sentToList.add(newSentTo);
            }
            
            nbean.m_sentTo = sentToList;
            
            //fill out the rest of the bean
            PreparedStatement pstmt = conn.prepareStatement(NOTICE_ID);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            rs.beforeFirst();
            while(rs.next()) {
                    Object element = rs.getString(TXT_MESG);
                    nbean.m_txtMsg = (String)element;
                    
                    element = rs.getString(NUM_MESG);
                    nbean.m_numMsg = (String)element;
                    
                    element = new Integer(rs.getInt(NOTIFY));
                    nbean.m_notifyID = ((Integer)element).intValue();
                    
                    element = rs.getTimestamp(TIME);
                    nbean.m_timeSent = ((Timestamp)element).getTime();
                    
                    element = rs.getTimestamp(REPLYTIME);
		    if (element != null)
                	nbean.m_timeReply = ((Timestamp)element).getTime();
		    else
                	nbean.m_timeReply = 0;
                    
                    element = rs.getString(ANS_BY);
                    nbean.m_responder = (String)element;
                    
                    element = new Integer(rs.getInt(NODE));
                    nbean.m_nodeID = ((Integer)element).intValue();
                    
                    element = rs.getString(INTERFACE);
                    nbean.m_interfaceID = (String)element;
                    
                    element = new Integer(rs.getInt(SERVICE));
                    nbean.m_serviceId = ((Integer)element).intValue();
                    
                    element = new Integer(rs.getInt(EVENTID));
                    nbean.m_eventId = ((Integer)element).intValue();
                    
                    Statement stmttmp = conn.createStatement();
                    ResultSet rstmp = stmttmp.executeQuery("SELECT servicename from service where serviceid = " + nbean.m_serviceId);
                    
                    if(rstmp.next()) {
                            element = rstmp.getString("servicename");
                            if(element != null) {
                                    nbean.m_serviceName = (String)element;
                            }
                    }
            }
            
            rs.close();
            pstmt.close();
        }
        catch( SQLException e ) {
            m_logger.debug("Problem getting data from the notifications table.");
            m_logger.debug("Error: " + e.getLocalizedMessage());

            throw e;
        }
        finally {
            Vault.releaseDbConnection( conn );
        }
        
        return nbean;
    }


    /**
     * Return all notifications, both outstanding and acknowledged.
     */
    public Notification[] allNotifications() throws SQLException {
        Notification[] notices = null;

        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SELECT);
            notices = rs2NotifyBean( conn, rs );

            rs.close();
            stmt.close();
        }
        catch( SQLException e) {
            m_logger.debug("allNotifications: Problem getting data from the notifications table.");
            m_logger.debug("allNotifications: Error: " + e.getLocalizedMessage());

            throw e;
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return( notices );
    }


    /**
     * This method returns the data from the result set as an array of Notification objects.
     */
    protected Notification[] rs2NotifyBean( Connection conn, ResultSet rs ) throws SQLException {
        Notification[] notices = null;
        Vector vector = new Vector();
        
        // Format the results.
        try {
            rs.beforeFirst();
            while(rs.next()) {
                Notification nbean = new Notification();
                
                Object element = rs.getString(TXT_MESG);
                nbean.m_txtMsg = (String)element;

                element = rs.getString(NUM_MESG);
                nbean.m_numMsg = (String)element;

                element = new Integer(rs.getInt(NOTIFY));
                nbean.m_notifyID = ((Integer)element).intValue();

                element = rs.getTimestamp(TIME);
                nbean.m_timeSent = ((Timestamp)element).getTime();

                element = rs.getTimestamp(REPLYTIME);
		if (element != null)
                	nbean.m_timeReply = ((Timestamp)element).getTime();
		else
                	nbean.m_timeReply = 0;

                element = rs.getString(ANS_BY);
                nbean.m_responder = (String)element;

                element = new Integer(rs.getInt(NODE));
                nbean.m_nodeID = ((Integer)element).intValue();

                element = rs.getString(INTERFACE);
                nbean.m_interfaceID = (String)element;

                element = new Integer(rs.getInt(SERVICE));
                nbean.m_serviceId = ((Integer)element).intValue();

                element = new Integer(rs.getInt(EVENTID));
                nbean.m_eventId = ((Integer)element).intValue();
                
                Statement stmttmp = conn.createStatement();
                ResultSet rstmp = stmttmp.executeQuery("SELECT servicename from service where serviceid = " + nbean.m_serviceId);

                if(rstmp.next()) {
                    element = rstmp.getString("servicename");
                    if(element != null) {
                        nbean.m_serviceName = (String)element;
                    }
                }
                
                rstmp.close();
                stmttmp.close();

                vector.addElement( nbean ) ;
            }
        }
        catch( SQLException e) {
            m_logger.debug("Error:" + e.getLocalizedMessage());
            m_logger.debug("Error occured in rs2NotifyBean");
            throw e;
        }
                
        notices = new Notification[vector.size()];
        
        for (int i = 0;i < notices.length; i++) {
            notices[i] = (Notification)vector.elementAt(i);
        }

        return notices;
    }


    /**
     * This method returns the count of all outstanding notices. 
     */
    public Notification[] getOutstandingNotices() throws SQLException {
        Notification[] notices = null;

        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(OUTSTANDING);
            notices = rs2NotifyBean( conn, rs );

            rs.close();
            stmt.close();
        }
        catch( SQLException e ) {
            m_logger.debug("Problem getting data from the notifications table.");
            m_logger.debug("Error: " + e.getLocalizedMessage());
            throw e;
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return( notices );
    }


    /**
     * This method returns notices not yet acknowledged.
     */
    public int getOutstandingNoticeCount() throws SQLException {
        int count = 0;

        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery( OUTSTANDING_COUNT );

            if( rs.next() ) {
                count = rs.getInt( "TOTAL" );
            }

            rs.close();
            stmt.close();
        }
        catch( SQLException e ) {
            m_logger.debug("Problem getting data from the notifications table.");
            m_logger.debug("Error: " + e.getLocalizedMessage());
            throw e;
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return( count );
    }


    /**
     * This method returns notices not yet acknowledged.
     */
    public int getOutstandingNoticeCount( String username) throws SQLException {
        if( username == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        int count = 0;

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement( USER_OUTSTANDING_COUNT );
            pstmt.setString( 1, username );
            ResultSet rs = pstmt.executeQuery();

            if( rs.next() ) {
                count = rs.getInt( "TOTAL" );
            }

            rs.close();
            pstmt.close();
        }
        catch( SQLException e ) {
            m_logger.debug("Problem getting data from the notifications table.");
            m_logger.debug("Error: " + e.getLocalizedMessage());
            throw e;
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return( count );
    }


    /**
     * This method returns notices not yet acknowledged.
     */
    public Notification[] getOutstandingNotices(String name) throws SQLException {
        Notification[] notices = null;

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement(USER_OUTSTANDING);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            notices = rs2NotifyBean( conn, rs );

            rs.close();
            pstmt.close();
        }
        catch( SQLException e ) {
            m_logger.debug("Problem getting data from the notifications table.");
            m_logger.debug("Error: " + e.getLocalizedMessage());
            throw e;
        }
        finally {
            Vault.releaseDbConnection( conn );
        }

        return( notices );
    }


    /**
     * This method updates the table when the user acknowledges the pager information.
     */
    public void acknowledged(String name, int noticeId) throws SQLException {
        if( name == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE notifications SET respondtime = ? , answeredby = ? WHERE notifyid= ?");
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(2, name);
            pstmt.setInt(3, noticeId);
            pstmt.execute();
            
            pstmt.close();
        }
        catch( SQLException e ) {
            m_logger.debug("Problem acknowledging.");
            m_logger.debug("Error: " + e.getLocalizedMessage());
            throw e;
        }
        finally {
            Vault.releaseDbConnection( conn );
        }
    }


    /**
     * This method helps insert into the database.
     */
    public void insert(Notification nbean) throws SQLException {
        if( nbean == null || nbean.m_txtMsg == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
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
        }
        catch( SQLException e ) {
            m_logger.debug("Problem getting data from the notifications table.");
            m_logger.debug("Error: " + e.getLocalizedMessage());
            throw e;
        }
        finally {
            Vault.releaseDbConnection( conn );
        }
    }

    /**
     * This method may be used to insert / update the database.
     */
   /* public void execute(String query)
    {
            if(m_dbConn == null)
            {
                    m_logger.debug("Problem executing query.");
                    m_logger.debug("We do not have a database connection yet");
            }
            else
            {
                    try
                    {
                            Statement stmt = m_dbConn.createStatement();
                            stmt.executeQuery(query);

                            // Close statement.
                            if(stmt != null)
                                    stmt.close();
                    }
                    catch(Exception e)
                    {
                            m_logger.debug("Problem inserting");
                            m_logger.debug("Error: " + e.getLocalizedMessage());
                    }
            }
    }*/

    /**
     * This method is used for formatting the date attributes in a proper format.
     */
    private String appendZero(String values)
    {
            int len = values.length();
            if(values != null)
            {
                    if(len >= 0 && len < 2)
                    {
                            while(2 - len > 0)
                            {
                                    values = "0" + values;
                                    len++;
                            }   
                    }
                    else
                            if(len > 2)
                                    m_logger.debug("Incorrect attributes for time");
            }
            return values;
    }

}
