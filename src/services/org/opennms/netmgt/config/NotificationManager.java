//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.notifications.Header;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.utils.RowProcessor;
import org.opennms.netmgt.utils.SingleResultQuerier;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author David Hustace <david@opennms.org>
 * This base class was refactored from NotificationFactory to support non-global
 * references during JUnit testing and later to support distributed processes.
 */
public abstract class NotificationManager {

    /**
     * Object containing all Notification objects parsed from the xml file
     */
    public Notifications m_notifications;
    /**
     * 
     */
    private Header oldHeader;
    public static final String PARAM_TYPE = "-t";
    public static final String PARAM_DESTINATION = "-d";
    public static final String PARAM_TEXT_MSG = "-tm";
    public static final String PARAM_NUM_MSG = "-nm";
    public static final String PARAM_RESPONSE = "-r";
    public static final String PARAM_NODE = "-nodeid";
    public static final String PARAM_INTERFACE = "-interface";
    public static final String PARAM_SERVICE = "-service";
    public static final String PARAM_SUBJECT = "-subject";
    public static final String PARAM_EMAIL = "-email";
    public static final String PARAM_PAGER_EMAIL = "-pemail";
    public static final String PARAM_TEXT_PAGER_PIN = "-tp";
    public static final String PARAM_NUM_PAGER_PIN = "-np";
    NotifdConfigManager m_configManager;
    private DbConnectionFactory m_dbConnectionFactory;
    /**
     * @param configIn
     * @throws MarshalException
     * @throws ValidationException
     */
    protected NotificationManager(NotifdConfigManager configManager, DbConnectionFactory dcf) {
        m_configManager = configManager;
        m_dbConnectionFactory = dcf;
    }

    public synchronized void parseXML(Reader reader) throws MarshalException, ValidationException {
        m_notifications = (Notifications) Unmarshaller.unmarshal(Notifications.class, reader);
        oldHeader = m_notifications.getHeader();
    }

    public boolean hasUei(String uei) throws IOException, MarshalException, ValidationException {
        update();
    
        for (Enumeration e = m_notifications.enumerateNotification(); e.hasMoreElements();) {
            Notification notif = (Notification) e.nextElement();
    
            if (uei.equals(notif.getUei()) || "MATCH-ANY-UEI".equals(notif.getUei())) {
                return true;
            }
        }
    
        return false;
    }
    public Notification[] getNotifForEvent(Event event) throws IOException, MarshalException, ValidationException {

        update();
        Category log = ThreadCategory.getInstance(getClass());
    
        ArrayList notifList = new ArrayList();
        Notification[] notif = null;
        boolean matchAll = getConfigManager().getNotificationMatch();
    
        for (Enumeration e = m_notifications.enumerateNotification(); e.hasMoreElements();) {
            Notification curNotif = (Notification) e.nextElement();
    
            if (curNotif.getStatus().equals("on") && (event.getUei().equals(curNotif.getUei()) || "MATCH-ANY-UEI".equals(curNotif.getUei())) && nodeInterfaceServiceValid(curNotif, event)) {

                boolean parmsmatched = getConfigManager().matchNotificationParameters(event, curNotif);

                if (!parmsmatched) {
                    log.debug("Event " + event.getUei() + " did not match parameters for notice " + curNotif.getName());
                    continue;
                }
                notifList.add(curNotif);

                log.debug("Event " + event.getUei() + " matched notice " + curNotif.getName());
                
                if (!matchAll)
                    break;
            }
        }
    
        if (!notifList.isEmpty()) {
            notif = (Notification[]) notifList.toArray(new Notification[0]);
        }
        return notif;
    }
    /**
     * @return
     */
    protected NotifdConfigManager getConfigManager() {
        return m_configManager;
    }
        
    private boolean nodeInterfaceServiceValid(Notification notif, Event event) {
        boolean result = false;
    
        Connection connection = null;
        try {
    
            // Get the Interface and Service from the Event
    
            long eventNode = event.getNodeid();
            String eventIf = (String) event.getInterface();
            String eventSrv = (String) event.getService();
    
            if (eventNode == 0 || eventIf == null || eventSrv == null) {
                return true;
            }
    
            // ThreadCategory.getInstance(getClass()).debug("Notification Event
            // Interface: " + eventIf + " Service: " + eventSrv);
    
            // Get the Notification Rule
    
            String sql = getInterfaceFilter(notif.getRule());
    
            ThreadCategory.getInstance(getClass()).debug("getSQL Returned SQL for Notification: " + notif.getName() + ": " + sql);
    
            connection = getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rows = stmt.executeQuery(sql);
    
            // Loop through the rows returned from the SQL query and return true
            // if they match event
    
            while (rows.next()) {
                String notifIf = rows.getString(1);
                String notifSrv = rows.getString(2);
                long notifNode = rows.getLong(3);
    
                // ThreadCategory.getInstance(getClass()).debug("Notification
                // Notif Interface: " + notifIf + " Service: " + notifSrv);
    
                // if there is no If with the event, there can be no service,
                // thus check only if the node matches
                if (eventIf == null || eventIf.equals("0.0.0.0")) {
                    if (eventNode == notifNode) {
                        result = true;
                        break;
                    }
                }
                // If there is no Srv with the event, check and see if the If
                // matches
                else if (eventSrv == null) {
                    if (eventIf.equals(notifIf)) {
                        result = true;
                        break;
                    }
                }
                // Otherwise, insure that both the Srv and If match
                else if (eventSrv.equals(notifSrv) && eventIf.equals(notifIf)) {
                    result = true;
                    break;
                }
            }
    
            try {
                rows.close();
            } catch (SQLException e) {
            }
    
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        } catch (SQLException e) {
            ThreadCategory.getInstance(getClass()).error("Filter query threw exception: " + notif.getName() + ": " + notif.getRule(), e);
            return true;
        } catch (FilterParseException e) {
            ThreadCategory.getInstance(getClass()).error("Invalid filter rule for notification " + notif.getName() + ": " + notif.getRule(), e);
    
            // go ahead and send the notification
            return true;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    
        return result;
    }
    /**
     * @param rule
     * @return
     */
    protected String getInterfaceFilter(String rule) {
        Filter filter = new Filter(rule);
   
        // Select the Interfaces and Services that match the rule
   
        String sql = filter.getInterfaceWithServiceStatement();
        return sql;
    }

    /**
     * @return
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        return m_dbConnectionFactory.getConnection();
    }

    /**
     * This method wraps the call to the database to get a sequence notice ID
     * from the database.
     * 
     * @return int, the sequence id from the database, 0 by default if there is
     *         database trouble
     */
    public synchronized int getNoticeId() throws SQLException, IOException, MarshalException, ValidationException {
        int id = 0;
    
        Connection connection = null;
    
        try {
            connection = getConnection();
            Statement stmt = connection.createStatement();
            ResultSet results = stmt.executeQuery(m_configManager.getNextNotifIdSql());
    
            results.next();
    
            id = results.getInt(1);
    
            stmt.close();
            results.close();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return id;
    }
    /**
     * This method returns a boolean indicating if the page has been responded
     * to by any member of the group the page was sent to.
     */
    public boolean noticeOutstanding(int noticeId) throws IOException, MarshalException, ValidationException {
        boolean outstanding = false;
    
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(getConfigManager().getConfiguration().getOutstandingNoticesSql());
    
            statement.setInt(1, noticeId);
    
            ResultSet results = statement.executeQuery();
    
            // count how many rows were returned, if there is even one then the
            // page
            // has been responded too.
            int count = 0;
            while (results.next()) {
                count++;
            }
    
            if (count == 0) {
                outstanding = true;
            }
    
            statement.close();
            results.close();
        } catch (SQLException e) {
            ThreadCategory.getInstance(getClass()).error("Error getting notice status: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    
        return outstanding;
    }
    /**
     * 
     */
    public Collection acknowledgeNotice(Event event, String uei, String[] matchList) throws SQLException, IOException, MarshalException, ValidationException {
        // get the notification id and see if only one is returned
        Connection connection = null;
        Collection notifIDs = new LinkedList();

        try {
            connection = getConnection();
            StringBuffer sql = new StringBuffer("SELECT notifyid FROM notifications WHERE eventuei=? AND respondTime is null ");
    
            for (int i = 0; i < matchList.length; i++) {
                sql.append("AND ").append(matchList[i]).append("=? ");
            }
    
            sql.append("ORDER BY pagetime");
    
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            statement.setString(1, uei);
    
            for (int i = 0; i < matchList.length; i++) {
                if (matchList[i].equals("nodeid")) {
                    statement.setLong(i + 2, event.getNodeid());
                }
    
                if (matchList[i].equals("interfaceid")) {
                    statement.setString(i + 2, event.getInterface());
                }
    
                if (matchList[i].equals("serviceid")) {
                    statement.setInt(i + 2, getServiceId(event.getService()));
                }
            }
    
            ResultSet results = statement.executeQuery();
    
            // count how many rows were returned, if there is even one then the
            // page
            // has been responded too.
    
            if (results != null) {
                while (results.next()) {
                    int notifID = results.getInt(1);
                    notifIDs.add(new Integer(notifID));
                    PreparedStatement update = connection.prepareStatement(getConfigManager().getConfiguration().getAcknowledgeUpdateSql());
    
                    update.setString(1, "auto-acknowledged");
                    update.setTimestamp(2, new Timestamp((new Date()).getTime()));
                    update.setInt(3, notifID);
    
                    update.executeUpdate();
                    update.close();
                }
            }
    
            statement.close();
            results.close();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return notifIDs;
    }
    /**
     */
    public List getActiveNodes() throws SQLException {
        String NODE_QUERY = "SELECT   n.nodeid " + "FROM     node n " + "WHERE    n.nodetype != 'D' " + "ORDER BY n.nodelabel";
    
        java.sql.Connection connection = null;
        List allNodes = new ArrayList();
    
        try {
            connection = getConnection();
    
            Statement stmt = connection.createStatement();
            ResultSet rset = stmt.executeQuery(NODE_QUERY);
    
            if (rset != null) {
                // Iterate through the result and build the array list
                while (rset.next()) {
                    int nodeID = rset.getInt(1);
    
                    allNodes.add(new Integer(nodeID));
                }
            }
            return allNodes;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    /**
     * 
     */
    public String getServiceNoticeStatus(String nodeID, String ipaddr, String service) throws SQLException {
        String notify = "Y";
    
        String query = "SELECT notify FROM ifservices, service WHERE nodeid=? AND ipaddr=? AND ifservices.serviceid=service.serviceid AND service.servicename=?";
        java.sql.Connection connection = null;
    
        try {
            connection = getConnection();
    
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, Integer.parseInt(nodeID));
            statement.setString(2, ipaddr);
            statement.setString(3, service);
    
            ResultSet rs = statement.executeQuery();
    
            if (rs.next() && rs.getString("notify") != null) {
                notify = rs.getString("notify");
                if (notify == null)
                    notify = "Y";
            }
            return notify;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    /**
     * 
     */
    public void updateNoticeWithUserInfo(String userId, int noticeId, String media, String contactInfo) throws SQLException {
        if (noticeId < 0) return;
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement insert = connection.prepareStatement("INSERT INTO usersNotified (userid, notifyid, notifytime, media, contactinfo) values (?,?,?,?,?)");
    
            insert.setString(1, userId);
            insert.setInt(2, noticeId);
    
            insert.setTimestamp(3, new Timestamp((new Date()).getTime()));
    
            insert.setString(4, media);
            insert.setString(5, contactInfo);
    
            insert.executeUpdate();
            insert.close();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    /**
     * This method inserts a row into the notifications table in the database.
     * This row indicates that the page has been sent out.
     * @param queueID
     */
    public void insertNotice(int notifyId, Map params, String queueID) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO notifications (textmsg, numericmsg, notifyid, pagetime, nodeid, interfaceid, serviceid, eventid, eventuei, subject, queueID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    
            // notifications textMsg field
            statement.setString(1, (String) params.get(NotificationManager.PARAM_TEXT_MSG));
    
            // notifications numericMsg field
            statement.setString(2, (String) params.get(NotificationManager.PARAM_NUM_MSG));
    
            // notifications notifyID field
            statement.setInt(3, notifyId);
    
            // notifications pageTime field
            statement.setTimestamp(4, new Timestamp((new Date()).getTime()));
    
            // notifications nodeID field
            String node = (String) params.get(NotificationManager.PARAM_NODE);
            if (node != null && !node.trim().equals("") && !node.toLowerCase().equals("null") && !node.toLowerCase().equals("%nodeid%")) {
                statement.setInt(5, Integer.parseInt(node));
            } else {
                statement.setNull(5, Types.INTEGER);
            }
    
            // notifications interfaceID field
            String ipaddr = (String) params.get(NotificationManager.PARAM_INTERFACE);
            if (ipaddr != null && !ipaddr.trim().equals("") && !ipaddr.toLowerCase().equals("null") && !ipaddr.toLowerCase().equals("%interface%")) {
                statement.setString(6, ipaddr);
            } else {
                statement.setString(6, null);
            }
    
            // notifications serviceID field
            String service = (String) params.get(NotificationManager.PARAM_SERVICE);
            if (service != null && !service.trim().equals("") && !service.toLowerCase().equals("null") && !service.toLowerCase().equals("%service%")) {
                statement.setInt(7, getServiceId(service));
            } else {
                statement.setNull(7, Types.INTEGER);
            }
    
            // eventID field
            String eventID = (String) params.get("eventID");
            statement.setInt(8, Integer.parseInt(eventID));
    
            statement.setString(9, (String) params.get("eventUEI"));
            
            // notifications subject field
            statement.setString(10, (String) params.get(NotificationManager.PARAM_SUBJECT));
            
            // the queue this will be sent on
            statement.setString(11, queueID);
    
            statement.executeUpdate();
            statement.close();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    /**
     * This method queries the database in search of a service id for a given
     * serivice name
     * 
     * @param service
     *            the name of the service
     * @return the serviceID of the service
     */
    private int getServiceId(String service) throws SQLException {
        int serviceID = 0;
    
        Connection connection = null;
        try {
            connection = getConnection();
    
            PreparedStatement statement = connection.prepareStatement("SELECT serviceID from service where serviceName = ?");
            statement.setString(1, service);
    
            ResultSet results = statement.executeQuery();
            results.next();
    
            serviceID = results.getInt(1);
    
            results.close();
            statement.close();
    
            return serviceID;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }
    /**
     * 
     */
    public Map getNotifications() throws IOException, MarshalException, ValidationException {
        update();
    
        Map newMap = new HashMap();
    
        Notification notices[] = m_notifications.getNotification();
        for (int i = 0; i < notices.length; i++) {
            newMap.put(notices[i].getName(), notices[i]);
        }
    
        return newMap;
    }
    /**
     * 
     */
    public List getServiceNames() throws SQLException {
        Connection connection = null;
        List services = new ArrayList();
        try {
            connection = getConnection();
    
            Statement stmt = connection.createStatement();
            ResultSet rset = stmt.executeQuery("SELECT servicename FROM service");
            if (rset != null) {
                // Iterate through the result and build the array list
                while (rset.next()) {
                    services.add(rset.getString(1));
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    
        return services;
    }
    /**
     */
    public Notification getNotification(String name) throws IOException, MarshalException, ValidationException {
        update();
    
        return (Notification) getNotifications().get(name);
    }
    /**
     */
    public List getNotificationNames() throws IOException, MarshalException, ValidationException {
        update();
    
        List notificationNames = new ArrayList();
    
        for (Enumeration e = m_notifications.enumerateNotification(); e.hasMoreElements();) {
            Notification curNotif = (Notification) e.nextElement();
            notificationNames.add(curNotif.getName());
        }
    
        return notificationNames;
    }
    /**
     * 
     */
    public synchronized void removeNotification(String name) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        m_notifications.removeNotification(getNotification(name));
        saveCurrent();
    }
    /**
     * Handles adding a new Notification.
     * 
     * @param notice
     *            The Notification to add.
     */
    public synchronized void addNotification(Notification notice) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        // remove any existing notice with the same name
        m_notifications.removeNotification(getNotification(notice.getName()));
    
        m_notifications.addNotification(notice);
        saveCurrent();
    }
    /**
     * 
     */
    public synchronized void replaceNotification(String oldName, Notification notice) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        Notification oldNotice = getNotification(oldName);
        if (oldNotice != null)
            m_notifications.removeNotification(oldNotice);
    
        addNotification(notice);
    }
    /**
     * Sets the status on an individual notification configuration and saves to
     * xml.
     * 
     * @param name
     *            The name of the notification.
     * @param status
     *            The status (either "on" or "off").
     */
    public synchronized void updateStatus(String name, String status) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        if ("on".equals(status) || "off".equals(status)) {
            Notification notice = getNotification(name);
            notice.setStatus(status);
    
            saveCurrent();
        } else
            throw new IllegalArgumentException("Status must be on|off, not " + status);
    }
    /**
     * 
     */
    public synchronized void saveCurrent() throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        m_notifications.setHeader(rebuildHeader());
    
        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_notifications, stringWriter);
        String xmlString = stringWriter.toString();
        saveXML(xmlString);
    
        update();
    }
    /**
     * @param xmlString
     * @throws IOException
     */
    protected abstract void saveXML(String xmlString) throws IOException;
    
    /**
     * 
     */
    private Header rebuildHeader() {
        Header header = oldHeader;
    
        header.setCreated(EventConstants.formatToString(new Date()));
    
        return header;
    }
    /**
     * 
     */
    protected abstract void update() throws IOException, MarshalException, ValidationException;

    /**
     * @param notifId
     */
    public Map rebuildParamterMap(int notifId) throws Exception {
        final Map parmMap = new HashMap();
        Querier querier = new Querier(m_dbConnectionFactory, "select notifications.*, service.* from notifications left outer join service on notifications.serviceID = service.serviceID  where notifyId = ?") {
            public void processRow(ResultSet rs) throws SQLException {
                parmMap.put(NotificationManager.PARAM_TEXT_MSG, rs.getObject("textMsg"));
                parmMap.put(NotificationManager.PARAM_NUM_MSG, rs.getObject("numericMsg"));
                parmMap.put(NotificationManager.PARAM_SUBJECT, "RESOLVED: "+rs.getObject("subject"));
                parmMap.put(NotificationManager.PARAM_NODE, rs.getObject("nodeID").toString());
                parmMap.put(NotificationManager.PARAM_INTERFACE, rs.getObject("interfaceID"));
                parmMap.put(NotificationManager.PARAM_SERVICE, rs.getObject("serviceName"));
                parmMap.put("noticeid", rs.getObject("notifyID").toString());
                parmMap.put("eventID", rs.getObject("eventID").toString());
                parmMap.put("eventUEI", rs.getObject("eventUEI"));

            }
        };
        querier.execute(new Integer(notifId));
        return parmMap;
    }

    /**
     * @param notifId
     * @return
     */
    public void forEachUserNotification(int notifId, RowProcessor rp) {
        Querier querier = new Querier(m_dbConnectionFactory, "select * from usersNotified where notifyId = ?", rp);
        querier.execute(new Integer(notifId));
    }

    /**
     * @param notifId
     * @return
     */
    public String getQueueForNotification(int notifId) {
        SingleResultQuerier querier = new SingleResultQuerier(m_dbConnectionFactory, "select queueID from notifications where notifyId = ?");
        querier.execute(new Integer(notifId));
        return (String)querier.getResult();
    }
}
