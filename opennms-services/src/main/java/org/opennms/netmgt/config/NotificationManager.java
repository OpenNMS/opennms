//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// 2007 Jun 29: Move filter matching code to JdbcFilterDao and organize imports. - dj@opennms.org
// 2006 Apr 07: Changed replaceNotification to handle preserving the order.
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
import java.io.InputStream;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.common.Header;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.config.notifications.Parameter;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.utils.RowProcessor;
import org.opennms.netmgt.utils.SingleResultQuerier;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Tticket;
import org.springframework.util.Assert;

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
    public static final String PARAM_XMPP_ADDRESS = "-xmpp";
    public static final String PARAM_TEXT_PAGER_PIN = "-tp";
    public static final String PARAM_NUM_PAGER_PIN = "-np";
    public static final String PARAM_WORK_PHONE = "-wphone";
    public static final String PARAM_HOME_PHONE = "-hphone";
    public static final String PARAM_MOBILE_PHONE = "-mphone";
    public static final String PARAM_TUI_PIN = "-tuipin";
    
    NotifdConfigManager m_configManager;
    private DataSource m_dataSource;
    
    
    /**
     * @param configIn
     * @throws MarshalException
     * @throws ValidationException
     */
    protected NotificationManager(final NotifdConfigManager configManager, final DataSource dcf) {
        m_configManager = configManager;
        m_dataSource = dcf;
    }

    @Deprecated
    public synchronized void parseXML(final Reader reader) throws MarshalException, ValidationException {
        m_notifications = CastorUtils.unmarshal(Notifications.class, reader);
        oldHeader = m_notifications.getHeader();
    }

    public synchronized void parseXML(final InputStream stream) throws MarshalException, ValidationException {
        m_notifications = CastorUtils.unmarshal(Notifications.class, stream);
        oldHeader = m_notifications.getHeader();
    }

    public boolean hasUei(final String uei) throws IOException, MarshalException, ValidationException {
        update();
    
        for (Notification notif : m_notifications.getNotificationCollection()) {
            if (uei.equals(notif.getUei()) || "MATCH-ANY-UEI".equals(notif.getUei())) {
                 return true;
            } else if (notif.getUei().charAt(0) == '~') {
               if (uei.matches(notif.getUei().substring(1))) {
                       return true;
               }
    
            }
        }
    
        return false;
    }
    
    public Notification[] getNotifForEvent(final Event event) throws IOException, MarshalException, ValidationException {
        update();
        List<Notification> notifList = new ArrayList<Notification>();
        Notification[] notif = null;
        boolean matchAll = getConfigManager().getNotificationMatch();
        Category log = ThreadCategory.getInstance(getClass());
  
        // This if statement will check to see if notification should be suppressed for this event.

        if (event == null) {
            log.warn("unable to get notification for null event!");
        }

        if (event.getLogmsg() != null && !(event.getLogmsg().getNotify())) {
            log.debug("Event " + event.getUei() + " is configured to supress notifications.");
            return notif;
        }
    
        for (Notification curNotif : m_notifications.getNotificationCollection()) {
            boolean curHasUei = false;
            boolean curHasSeverity = false;

            log.debug("Checking " + event.getUei() + " against " + curNotif.getUei());
 
            if (event.getUei().equals(curNotif.getUei()) || "MATCH-ANY-UEI".equals(curNotif.getUei())) {
               curHasUei = true;
            } else if (curNotif.getUei().charAt(0) == '~') {
               if (event.getUei().matches(curNotif.getUei().substring(1))) {
                       curHasUei = true;
               }
            }

            /**
             * Check if event severity matches pattern in notification
             */
            log.debug("Checking event severity: " + event.getSeverity() + " against notification severity: " + curNotif.getEventSeverity());
            // parameter is optional, return true if not set
            if (curNotif.getEventSeverity() == null) {
               curHasSeverity = true;
            } else if (event.getSeverity().toLowerCase().matches(curNotif.getEventSeverity().toLowerCase())) {
               curHasSeverity = true;
            }
           
            // The notice has to be "on"
            // The notice has to match a severity if configured - currHasSeverity should be true if there is no severity rule 
            // The notice has to match the UEI of the event or MATCH-ANY-UEI
            // If all those things are true:
            // Then the service has to match if configured, the interface if configured, and the node if configured.

            if (curNotif.getStatus().equals("on") && curHasSeverity && curHasUei && nodeInterfaceServiceValid(curNotif, event)) {
                boolean parmsmatched = getConfigManager().matchNotificationParameters(event, curNotif);

                if (!parmsmatched) {
                    log().debug("Event " + event.getUei() + " did not match parameters for notice " + curNotif.getName());
                    continue;
                }
                notifList.add(curNotif);

                log().debug("Event " + event.getUei() + " matched notice " + curNotif.getName());
                
                if (!matchAll)
                    break;
            }
        }
    
        if (!notifList.isEmpty()) {
            notif = (Notification[]) notifList.toArray(new Notification[0]);
        }
        return notif;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    /**
     * @return
     */
    protected NotifdConfigManager getConfigManager() {
        return m_configManager;
    }
    
    protected boolean nodeInterfaceServiceValid(final Notification notif, final Event event) {
        Assert.notNull(notif, "notif argument must not be null");
        Assert.notNull(event, "event argument must not be null");
        Assert.notNull(notif.getRule(), "getRule() on notif argument must not return null");
        
        /*
         *  If the event doesn't have a nodeId, interface, or service,
         *  return true since there is nothing on which to filter.
         */
        if (event.getNodeid() == 0 && event.getInterface() == null &&
                event.getService() == null) {
            if ("MATCH-ANY-UEI".equals(notif.getUei())) {
               if ("ipaddr != '0.0.0.0'".equals(notif.getRule().toLowerCase()) || "ipaddr iplike *.*.*.*".equals(notif.getRule().toLowerCase())) {
                   return true;
               } else {
                   return false;
               }
            }
            return true;
        }

        StringBuffer constraints = new StringBuffer();
        if (event.getNodeid() != 0) {
            constraints.append(" & (nodeId == " + event.getNodeid() + ")");
        }
        
        if (event.getInterface() != null
                && !"0.0.0.0".equals(event.getInterface())) {
            constraints.append(" & (ipAddr == '" + event.getInterface() + "')");
            if (event.getService() != null) {
                constraints.append(" & (serviceName == '" + event.getService() + "')");
            }
        }
        
        String rule = "((" + notif.getRule() + ")" + constraints + ")";

        return isRuleMatchingFilter(notif, rule);
    }
    
    private boolean isRuleMatchingFilter(final Notification notif, final String rule) {
        try {
            return FilterDaoFactory.getInstance().isRuleMatching(rule);
        } catch (FilterParseException e) {
            log().error("Invalid filter rule for notification " + notif.getName() + ": " + notif.getRule(), e);
            throw e;
        }
    }

    /**
     * @return
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        return m_dataSource.getConnection();
    }

    /**
     * This method wraps the call to the database to get a sequence notice ID
     * from the database.
     * 
     * @return int, the sequence id from the database, 0 by default if there is
     *         database trouble
     */
    public int getNoticeId() throws SQLException, IOException, MarshalException, ValidationException {
        return getNxtId(m_configManager.getNextNotifIdSql());
    }

    public int getUserNotifId() throws SQLException, IOException, MarshalException, ValidationException {
        return getNxtId(m_configManager.getNextUserNotifIdSql());
    }

	private int getNxtId(final String sql) throws SQLException {
		int id = 0;
		Connection connection = null;
		try {
            connection = getConnection();
            Statement stmt = connection.createStatement();
            ResultSet results = stmt.executeQuery(sql);
    
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
    public boolean noticeOutstanding(final int noticeId) throws IOException, MarshalException, ValidationException {
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
            log().error("Error getting notice status: " + e.getMessage(), e);
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
    public Collection<Integer> acknowledgeNotice(final Event event, final String uei, final String[] matchList) throws SQLException, IOException, MarshalException, ValidationException {
        Connection connection = null;
        List<Integer> notifIDs = new LinkedList<Integer>();

        try {
            // First get most recent event ID from notifications 
            // that match the matchList, then get all notifications
            // with this event ID
            connection = getConnection();
            int eventID = 0;
            boolean wasAcked = false;
            StringBuffer sql = new StringBuffer("SELECT eventid FROM notifications WHERE eventuei=? ");
            for (int i = 0; i < matchList.length; i++) {
                sql.append("AND ").append(matchList[i]).append("=? ");
            }
            sql.append("ORDER BY eventid desc limit 1");
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
            if (results != null && results.next()) {
                eventID = results.getInt(1);
                log().debug("EventID for notice(s) to be acked: " + eventID);


                sql = new StringBuffer("SELECT notifyid, answeredby, respondtime FROM notifications WHERE eventID=?");
    
                statement = connection.prepareStatement(sql.toString());
                statement.setInt(1, eventID);
    
                results = statement.executeQuery();
    
                if (results != null) {
                    while (results.next()) {
                        int notifID = results.getInt(1);
                        String ansBy = results.getString(2);
                        Timestamp ts = results.getTimestamp(3);
                        if(ansBy == null) {
                            ansBy = "auto-acknowledged";
                            ts = new Timestamp((new Date()).getTime());
                        } else if(ansBy.indexOf("auto-acknowledged") > -1) {
                            log().debug("Notice has previously been auto-acknowledged. Skipping...");
                            continue;
                        } else {
                            wasAcked = true;
                            ansBy = ansBy + "/auto-acknowledged";
                        }
                        log().debug("Matching DOWN notifyID = " + notifID + ", was acked by user = " + wasAcked + ", ansBy = " +ansBy);
                        PreparedStatement update = connection.prepareStatement(getConfigManager().getConfiguration().getAcknowledgeUpdateSql());
    
                        update.setString(1, ansBy);
                        update.setTimestamp(2, ts);
                        update.setInt(3, notifID);
    
                        update.executeUpdate();
                        update.close();
                        if(wasAcked) {
                            notifIDs.add(new Integer(-1 * notifID));
                        } else {
                            notifIDs.add(new Integer(notifID));
                        }
                    }
                }
            } else {
                log().debug("No matching DOWN eventID found");
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
    public List<Integer> getActiveNodes() throws SQLException {
        String NODE_QUERY = "SELECT   n.nodeid " + "FROM     node n " + "WHERE    n.nodetype != 'D' " + "ORDER BY n.nodelabel";
    
        java.sql.Connection connection = null;
        List<Integer> allNodes = new ArrayList<Integer>();
    
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
    public String getServiceNoticeStatus(final String nodeID, final String ipaddr, final String service) throws SQLException {
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
     * @throws IOException 
     * @throws ValidationException 
     * @throws MarshalException 
     * 
     */
    public void updateNoticeWithUserInfo(final String userId, final int noticeId, final String media, final String contactInfo, final String autoNotify) throws SQLException, MarshalException, ValidationException, IOException {
        Category log = log();
        if (noticeId < 0) return;
        int userNotifId = getUserNotifId();
        if (log.isDebugEnabled()) {
            log.debug("updating usersnotified: ID = " + userNotifId+ " User = " + userId + ", notice ID = " + noticeId + ", contactinfo = " + contactInfo + ", media = " + media + ", autoNotify = " + autoNotify);
        }
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement insert = connection.prepareStatement("INSERT INTO usersNotified (id, userid, notifyid, notifytime, media, contactinfo, autonotify) values (?,?,?,?,?,?,?)");
    
            insert.setInt(1, userNotifId);
            insert.setString(2, userId);
            insert.setInt(3, noticeId);
    
            insert.setTimestamp(4, new Timestamp((new Date()).getTime()));
    
            insert.setString(5, media);
            insert.setString(6, contactInfo);
            insert.setString(7, autoNotify);
    
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
     * @param notification TODO
     */
    public void insertNotice(final int notifyId, final Map<String, String> params, final String queueID, final Notification notification) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO notifications (" +
                    "textmsg, numericmsg, notifyid, pagetime, nodeid, interfaceid, serviceid, eventid, " +
                    "eventuei, subject, queueID, notifConfigName) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    
            // notifications textMsg field
            String textMsg = (String) params.get(NotificationManager.PARAM_TEXT_MSG);
            if (textMsg != null && textMsg.length() > 4000) {
                log().warn("textmsg too long, it will be truncated");
                textMsg = textMsg.substring(0, 4000);
            }
            statement.setString(1, textMsg);
    
            // notifications numericMsg field
            String numMsg = (String) params.get(NotificationManager.PARAM_NUM_MSG);
            if (numMsg != null && numMsg.length() > 256) {
                log().warn("numericmsg too long, it will be truncated");
                numMsg = numMsg.substring(0, 256);
            }
            statement.setString(2, numMsg);
    
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
            
            statement.setString(12, notification.getName());
    
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
    private int getServiceId(final String service) throws SQLException {
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
    public Map<String, Notification> getNotifications() throws IOException, MarshalException, ValidationException {
        update();
    
        Map<String, Notification> newMap = new HashMap<String, Notification>();
    
        Notification notices[] = m_notifications.getNotification();
        for (int i = 0; i < notices.length; i++) {
            newMap.put(notices[i].getName(), notices[i]);
        }
    
        return newMap;
    }
    /**
     * 
     */
    public List<String> getServiceNames() throws SQLException {
        Connection connection = null;
        List<String> services = new ArrayList<String>();
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
    public Notification getNotification(final String name) throws IOException, MarshalException, ValidationException {
        update();
    
        return (Notification) getNotifications().get(name);
    }
    /**
     */
    public List<String> getNotificationNames() throws IOException, MarshalException, ValidationException {
        update();
    
        List<String> notificationNames = new ArrayList<String>();
    
        for (Notification curNotif : m_notifications.getNotificationCollection()) {
            notificationNames.add(curNotif.getName());
        }
    
        return notificationNames;
    }
    /**
     * 
     */
    public synchronized void removeNotification(final String name) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        m_notifications.removeNotification(getNotification(name));
        saveCurrent();
    }
    /**
     * Handles adding a new Notification.
     * 
     * @param notice
     *            The Notification to add.
     */
    public synchronized void addNotification(final Notification notice) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        // remove any existing notice with the same name
        m_notifications.removeNotification(getNotification(notice.getName()));
    
        m_notifications.addNotification(notice);
        saveCurrent();
    }
    /**
     * 
     */
    public synchronized void replaceNotification(final String oldName, final Notification newNotice) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        //   In order to preserve the order of the notices, we have to replace "in place".

        Notification notice = getNotification(oldName);
	if (notice != null) {
       	notice.setWriteable(newNotice.getWriteable());
	        notice.setDescription(newNotice.getDescription());
	        notice.setUei(newNotice.getUei());
	        notice.setRule(newNotice.getRule());
	        notice.setDestinationPath(newNotice.getDestinationPath());
	        notice.setNoticeQueue(newNotice.getNoticeQueue());
	        notice.setTextMessage(newNotice.getTextMessage());
	        notice.setSubject(newNotice.getSubject());
	        notice.setNumericMessage(newNotice.getNumericMessage());
	        notice.setStatus(newNotice.getStatus());
	        notice.setVarbind(newNotice.getVarbind());
           
	        Parameter parameters[] = newNotice.getParameter();
	        for (int i = 0; i < parameters.length; i++) {
		            Parameter newParam = new Parameter();
		            newParam.setName(parameters[i].getName());
		            newParam.setValue(parameters[i].getValue());

		            notice.addParameter(newParam);
	        } 
                saveCurrent();
	}
	else	
        	addNotification(newNotice);
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
    public synchronized void updateStatus(final String name, final String status) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
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
    
        // Marshal to a string first, then write the string to the file. This
        // way the original configuration
        // isn't lost if the XML from the marshal is hosed.
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
    public Map<String, String> rebuildParameterMap(final int notifId, final String resolutionPrefix, final boolean skipNumericPrefix) throws Exception {
        final Map<String, String> parmMap = new HashMap<String, String>();
        Querier querier = new Querier(m_dataSource, "select notifications.*, service.* from notifications left outer join service on notifications.serviceID = service.serviceID  where notifyId = ?") {
            public void processRow(ResultSet rs) throws SQLException {
                
                /*
                 * Note, getString on results is valid for any SQL data type except the new SQL types:
                 *    Blog, Clob, Array, Struct, Ref
                 * of which we have none in this table so this row processor is using getString
                 * to correctly align with annotated types in the map.
                 */
                parmMap.put(NotificationManager.PARAM_TEXT_MSG, resolutionPrefix+rs.getString("textMsg"));
                if (skipNumericPrefix) {
                    parmMap.put(NotificationManager.PARAM_NUM_MSG, rs.getString("numericMsg"));
                } else {
                    parmMap.put(NotificationManager.PARAM_NUM_MSG, resolutionPrefix+rs.getString("numericMsg"));
                }
                parmMap.put(NotificationManager.PARAM_SUBJECT, resolutionPrefix+rs.getString("subject"));
                parmMap.put(NotificationManager.PARAM_NODE, rs.getString("nodeID"));
                parmMap.put(NotificationManager.PARAM_INTERFACE, rs.getString("interfaceID"));
                parmMap.put(NotificationManager.PARAM_SERVICE, rs.getString("serviceName"));
                parmMap.put("noticeid", rs.getString("notifyID"));
                parmMap.put("eventID", rs.getString("eventID"));
                parmMap.put("eventUEI", rs.getString("eventUEI"));
                
                Notification notification = null;
                try {
                    notification = getNotification(rs.getObject("notifConfigName").toString());
                } catch (MarshalException e) {
                } catch (ValidationException e) {
                } catch (IOException e) {
                }
                
                if (notification != null) {
                    addNotificationParams(parmMap, notification);
                }
            }
        };
        querier.execute(new Integer(notifId));
        return parmMap;
    }

    /**
     * Adds additional parameters defined by the user in the notificaiton
     * configuration XML.
     * 
     * @param paramMap
     * @param notification
     */
    public static void addNotificationParams(final Map<String, String> paramMap, final Notification notification) {
        Collection<Parameter> parameters = notification.getParameterCollection();
        
        for (Parameter parameter : parameters) {
            paramMap.put(parameter.getName(), parameter.getValue());
        }
    }

    /**
     * @param notifId
     * @return
     */
    public void forEachUserNotification(final int notifId, final RowProcessor rp) {
        Querier querier = new Querier(m_dataSource, "select * from usersNotified where notifyId = ? order by notifytime", rp);
        querier.execute(new Integer(notifId));
    }

    /**
     * @param notifId
     * @return
     */
    public String getQueueForNotification(final int notifId) {
        SingleResultQuerier querier = new SingleResultQuerier(m_dataSource, "select queueID from notifications where notifyId = ?");
        querier.execute(new Integer(notifId));
        return (String)querier.getResult();
    }
    
    public static void expandMapValues(Map<String, String> map, final Event event) {
        for (String key : map.keySet()) {
            String mapValue = map.get(key);
            if (mapValue == null) {
                continue;
            }
            String expandedValue = EventUtil.expandParms(map.get(key), event);
            map.put(key, (expandedValue != null ? expandedValue : map.get(key)));
        }
        
    }

    /**
     * In the absence of DAOs and ORMs this creates an Event object from the persisted
     * record.
     * 
     * @param eventid
     * @return a populated Event object
     */
    public Event getEvent(final int eventid) {
        final Event event = new Event();
        Querier querier = new Querier(m_dataSource, "select * from events where eventid = ?", new RowProcessor() {

            public void processRow(ResultSet rs) throws SQLException {
                event.setDbid(rs.getInt("eventid"));
                event.setUei(rs.getString("eventuei"));
                event.setNodeid(rs.getInt("nodeid"));
                event.setTime(rs.getString("eventtime"));
                event.setHost(rs.getString("eventhost"));
                event.setInterface(rs.getString("ipaddr"));
                event.setSnmphost(rs.getString("eventsnmphost"));
                event.setService(getServiceName(rs.getInt("serviceid")));
                event.setCreationTime(rs.getString("eventcreatetime"));
                event.setSeverity(rs.getString("eventseverity"));
                event.setPathoutage(rs.getString("eventpathoutage"));
                Tticket tticket = new Tticket();
                tticket.setContent(rs.getString("eventtticket"));
                tticket.setState(rs.getString("eventtticketstate"));
                event.setTticket(tticket);
                event.setSource(rs.getString("eventsource"));
            }

            private String getServiceName(int serviceid) {
                SingleResultQuerier querier = new SingleResultQuerier(m_dataSource, "select servicename from service where serviceid = ?");
                return (String)querier.getResult();
            }
            
        });
        querier.execute(new Integer(eventid));
        return event;
    }

}
