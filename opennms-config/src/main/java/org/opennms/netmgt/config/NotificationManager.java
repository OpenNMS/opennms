/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.RowProcessor;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.notifications.Header;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.config.notifications.Parameter;
import org.opennms.netmgt.eventd.datablock.EventUtil;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Tticket;
import org.springframework.util.Assert;

/**
 * <p>Abstract NotificationManager class.</p>
 *
 * @author David Hustace <david@opennms.org>
 * This base class was refactored from NotificationFactory to support non-global
 * references during JUnit testing and later to support distributed processes.
 * @version $Id: $
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
    /** Constant <code>PARAM_TYPE="-t"</code> */
    public static final String PARAM_TYPE = "-t";
    /** Constant <code>PARAM_DESTINATION="-d"</code> */
    public static final String PARAM_DESTINATION = "-d";
    /** Constant <code>PARAM_TEXT_MSG="-tm"</code> */
    public static final String PARAM_TEXT_MSG = "-tm";
    /** Constant <code>PARAM_NUM_MSG="-nm"</code> */
    public static final String PARAM_NUM_MSG = "-nm";
    /** Constant <code>PARAM_RESPONSE="-r"</code> */
    public static final String PARAM_RESPONSE = "-r";
    /** Constant <code>PARAM_NODE="-nodeid"</code> */
    public static final String PARAM_NODE = "-nodeid";
    /** Constant <code>PARAM_INTERFACE="-interface"</code> */
    public static final String PARAM_INTERFACE = "-interface";
    /** Constant <code>PARAM_SERVICE="-service"</code> */
    public static final String PARAM_SERVICE = "-service";
    /** Constant <code>PARAM_SUBJECT="-subject"</code> */
    public static final String PARAM_SUBJECT = "-subject";
    /** Constant <code>PARAM_EMAIL="-email"</code> */
    public static final String PARAM_EMAIL = "-email";
    /** Constant <code>PARAM_PAGER_EMAIL="-pemail"</code> */
    public static final String PARAM_PAGER_EMAIL = "-pemail";
    /** Constant <code>PARAM_XMPP_ADDRESS="-xmpp"</code> */
    public static final String PARAM_XMPP_ADDRESS = "-xmpp";
    /** Constant <code>PARAM_TEXT_PAGER_PIN="-tp"</code> */
    public static final String PARAM_TEXT_PAGER_PIN = "-tp";
    /** Constant <code>PARAM_NUM_PAGER_PIN="-np"</code> */
    public static final String PARAM_NUM_PAGER_PIN = "-np";
    /** Constant <code>PARAM_WORK_PHONE="-wphone"</code> */
    public static final String PARAM_WORK_PHONE = "-wphone";
    /** Constant <code>PARAM_HOME_PHONE="-hphone"</code> */
    public static final String PARAM_HOME_PHONE = "-hphone";
    /** Constant <code>PARAM_MOBILE_PHONE="-mphone"</code> */
    public static final String PARAM_MOBILE_PHONE = "-mphone";
    /** Constant <code>PARAM_TUI_PIN="-tuipin"</code> */
    public static final String PARAM_TUI_PIN = "-tuipin";
    /** Constant <code>PARAM_MICROBLOG_USERNAME="-ublog"</code> */
    public static final String PARAM_MICROBLOG_USERNAME = "-ublog";
    
    NotifdConfigManager m_configManager;
    private DataSource m_dataSource;
    
    /**
     * A regular expression for matching an expansion parameter delimited by
     * percent signs.
     */
    private static final String NOTIFD_EXPANSION_PARM = "%(noticeid)%";

    private static RE m_expandRE;

    /**
     * Initializes the expansion regular expression. The exception is going to
     * be thrown away if the RE can't be compiled, thus the compilation should
     * be tested prior to runtime.
     */
    static {
        try {
            m_expandRE = new RE(NOTIFD_EXPANSION_PARM);
        } catch (RESyntaxException e) {
            // this shouldn't throw an exception, should be tested prior to
            // runtime
            LogUtils.errorf(NotificationManager.class, e, "failed to compile RE %s", NOTIFD_EXPANSION_PARM);
            // FIXME: wrap this in runtime exception since SOMETIMES we are using
            // an incorrect version of regexp pulled from xalan that is doesn't
            // extend RuntimeException only Exception.  We really need to fix that.
            // See Bug# 1736 in Bugzilla.
            throw new RuntimeException(e);
        }
    }

    /**
     * A parameter expansion algorithm, designed to replace strings delimited by
     * percent signs '%' with a value supplied by a Map object.
     *
     * <p>NOTE: This function only replaces one particular parameter, the
     * <code>%noticeid%</code> parameter.</p>
     *
     * @param input
     *            the input string
     * @param paramMap
     *            a map that will supply the substitution values
     * @return a {@link java.lang.String} object.
     */
    public static String expandNotifParms(final String input, final Map<String, String> paramMap) {
        String expanded = input;

        if (m_expandRE.match(expanded)) {
            String key = m_expandRE.getParen(1);
            Assert.isTrue("noticeid".equals(key));
            String replace = paramMap.get(key);
            if (replace != null) {
                expanded = m_expandRE.subst(expanded, replace);
            }
        }
        return expanded;
    }

    /**
     * <p>Constructor for NotificationManager.</p>
     *
     * @throws MarshalException if any.
     * @throws ValidationException if any.
     * @param configManager a {@link org.opennms.netmgt.config.NotifdConfigManager} object.
     * @param dcf a {@link javax.sql.DataSource} object.
     */
    protected NotificationManager(final NotifdConfigManager configManager, final DataSource dcf) {
        m_configManager = configManager;
        m_dataSource = dcf;
    }

    /**
     * <p>parseXML</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @Deprecated
    public synchronized void parseXML(final Reader reader) throws MarshalException, ValidationException {
        m_notifications = CastorUtils.unmarshal(Notifications.class, reader, true);
        oldHeader = m_notifications.getHeader();
    }

    /**
     * <p>parseXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public synchronized void parseXML(final InputStream stream) throws MarshalException, ValidationException {
        m_notifications = CastorUtils.unmarshal(Notifications.class, stream, true);
        oldHeader = m_notifications.getHeader();
    }

    /**
     * <p>hasUei</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
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
    
    /**
     * <p>getNotifForEvent</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return an array of {@link org.opennms.netmgt.config.notifications.Notification} objects.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public Notification[] getNotifForEvent(final Event event) throws IOException, MarshalException, ValidationException {
        update();
        List<Notification> notifList = new ArrayList<Notification>();
        boolean matchAll = getConfigManager().getNotificationMatch();
        ThreadCategory log = this.log();
  
        // This if statement will check to see if notification should be suppressed for this event.

        if (event == null) {
            log.warn("unable to get notification for null event!");
            return null;
        } else if (event.getLogmsg() != null && !(event.getLogmsg().getNotify())) {
            if (log.isDebugEnabled())
                log.debug("Event " + event.getUei() + " is configured to suppress notifications.");
            return null;
        }
    
        for (Notification curNotif : m_notifications.getNotificationCollection()) {
            if (log.isDebugEnabled())
                log.debug("Checking " + event.getUei() + " against " + curNotif.getUei());
 
            if (event.getUei().equals(curNotif.getUei()) || "MATCH-ANY-UEI".equals(curNotif.getUei())) {
               // Match!
            } else if (curNotif.getUei().charAt(0) == '~') {
               if (event.getUei().matches(curNotif.getUei().substring(1))) {
                    // Match!
               } else {
                   if (log.isDebugEnabled())
                       log.debug("Notification regex " + curNotif.getUei() + " failed to match event UEI: " + event.getUei());
                   continue;
               }
            } else {
                if (log.isDebugEnabled())
                    log.debug("Event UEI " + event.getUei() + " did not match " + curNotif.getUei());
                continue;
            }

            /**
             * Check if event severity matches pattern in notification
             */
            if (log.isDebugEnabled())
                log.debug("Checking event severity: " + event.getSeverity() + " against notification severity: " + curNotif.getEventSeverity());
            // parameter is optional, return true if not set
            if (curNotif.getEventSeverity() == null) {
                // Skip matching on severity
            } else if (event.getSeverity().toLowerCase().matches(curNotif.getEventSeverity().toLowerCase())) {
                // Severities match
            } else {
                if (log.isDebugEnabled())
                    log.debug("Event severity: " + event.getSeverity() + " did not match notification severity: " + curNotif.getEventSeverity());
                continue;
            }
           
            // The notice has to be "on"
            // The notice has to match a severity if configured - currHasSeverity should be true if there is no severity rule 
            // The notice has to match the UEI of the event or MATCH-ANY-UEI
            // If all those things are true:
            // Then the service has to match if configured, the interface if configured, and the node if configured.

            if (curNotif.getStatus().equals("on")) {
                if (nodeInterfaceServiceValid(curNotif, event)) {
                    boolean parmsmatched = getConfigManager().matchNotificationParameters(event, curNotif);
    
                    if (!parmsmatched) {
                        if (log.isDebugEnabled())
                            log.debug("Event " + event.getUei() + " did not match parameters for notice " + curNotif.getName());
                        continue;
                    }
                    // Add this notification to the return value
                    notifList.add(curNotif);
    
                    if (log.isDebugEnabled())
                        log.debug("Event " + event.getUei() + " matched notice " + curNotif.getName());
                    
                    if (!matchAll)
                        break;
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Node/interface/service combination in the event was invalid");
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("Current notification is turned off.");
            }
        }
    
        if (!notifList.isEmpty()) {
            return notifList.toArray(new Notification[0]);
        } else {
            return null;
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    /**
     * <p>getConfigManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotifdConfigManager} object.
     */
    protected NotifdConfigManager getConfigManager() {
        return m_configManager;
    }
    
    /**
     * <p>nodeInterfaceServiceValid</p>
     *
     * @param notif a {@link org.opennms.netmgt.config.notifications.Notification} object.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    protected boolean nodeInterfaceServiceValid(final Notification notif, final Event event) {
        Assert.notNull(notif, "notif argument must not be null");
        Assert.notNull(event, "event argument must not be null");
        Assert.notNull(notif.getRule(), "getRule() on notif argument must not return null");
        
        /*
         *  If the event doesn't have a nodeId, interface, or service,
         *  return true since there is nothing on which to filter.
         */
        if (event.getNodeid() == 0 && event.getInterface() == null && event.getService() == null) {
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
     * @throws java.sql.SQLException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public int getNoticeId() throws SQLException, IOException, MarshalException, ValidationException {
        return getNxtId(m_configManager.getNextNotifIdSql());
    }

    /**
     * <p>getUserNotifId</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
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
     *
     * @param noticeId a int.
     * @return a boolean.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public boolean noticeOutstanding(final int noticeId) throws IOException, MarshalException, ValidationException {
        boolean outstanding = false;
    
        Connection connection = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            connection = getConnection();
            d.watch(connection);
            final PreparedStatement statement = connection.prepareStatement(getConfigManager().getConfiguration().getOutstandingNoticesSql());
            d.watch(statement);
    
            statement.setInt(1, noticeId);
    
            ResultSet results = statement.executeQuery();
            d.watch(results);
    
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
        } catch (SQLException e) {
            log().error("Error getting notice status: " + e.getMessage(), e);
        } finally {
            d.cleanUp();
        }
    
        return outstanding;
    }
    /**
     * <p>acknowledgeNotice</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param uei a {@link java.lang.String} object.
     * @param matchList an array of {@link java.lang.String} objects.
     * @return a {@link java.util.Collection} object.
     * @throws java.sql.SQLException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public Collection<Integer> acknowledgeNotice(final Event event, final String uei, final String[] matchList) throws SQLException, IOException, MarshalException, ValidationException {
        Connection connection = null;
        List<Integer> notifIDs = new LinkedList<Integer>();
        final DBUtils d = new DBUtils(getClass());
        ThreadCategory log = this.log();

        try {
            // First get most recent event ID from notifications 
            // that match the matchList, then get all notifications
            // with this event ID
            connection = getConnection();
            d.watch(connection);
            int eventID = 0;
            boolean wasAcked = false;
            StringBuffer sql = new StringBuffer("SELECT eventid FROM notifications WHERE eventuei=? ");
            for (int i = 0; i < matchList.length; i++) {
                sql.append("AND ").append(matchList[i]).append("=? ");
            }
            sql.append("ORDER BY eventid desc limit 1");
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            d.watch(statement);
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
            d.watch(results);
            if (results != null && results.next()) {
                eventID = results.getInt(1);
                if (log.isDebugEnabled())
                    log.debug("EventID for notice(s) to be acked: " + eventID);


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
                            if (log.isDebugEnabled())
                                log.debug("Notice has previously been auto-acknowledged. Skipping...");
                            continue;
                        } else {
                            wasAcked = true;
                            ansBy = ansBy + "/auto-acknowledged";
                        }
                        if (log.isDebugEnabled())
                            log.debug("Matching DOWN notifyID = " + notifID + ", was acked by user = " + wasAcked + ", ansBy = " +ansBy);
                        final PreparedStatement update = connection.prepareStatement(getConfigManager().getConfiguration().getAcknowledgeUpdateSql());
                        d.watch(update);
    
                        update.setString(1, ansBy);
                        update.setTimestamp(2, ts);
                        update.setInt(3, notifID);
    
                        update.executeUpdate();
                        update.close();
                        if(wasAcked) {
                            notifIDs.add(-1 * notifID);
                        } else {
                            notifIDs.add(notifID);
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("No matching DOWN eventID found");
            }
        } finally {
            d.cleanUp();
        }
        return notifIDs;
    }
    /**
     * <p>getActiveNodes</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    public List<Integer> getActiveNodes() throws SQLException {
        String NODE_QUERY = "SELECT   n.nodeid " + "FROM     node n " + "WHERE    n.nodetype != 'D' " + "ORDER BY n.nodelabel";
    
        java.sql.Connection connection = null;
        final List<Integer> allNodes = new ArrayList<Integer>();
        final DBUtils d = new DBUtils(getClass());

        try {
            connection = getConnection();
            d.watch(connection);
    
            final Statement stmt = connection.createStatement();
            d.watch(stmt);
            final ResultSet rset = stmt.executeQuery(NODE_QUERY);
            d.watch(rset);
    
            if (rset != null) {
                // Iterate through the result and build the array list
                while (rset.next()) {
                    int nodeID = rset.getInt(1);
    
                    allNodes.add(nodeID);
                }
            }
            return allNodes;
        } finally {
            d.cleanUp();
        }
    }
    /**
     * <p>getServiceNoticeStatus</p>
     *
     * @param nodeID a {@link java.lang.String} object.
     * @param ipaddr a {@link java.lang.String} object.
     * @param service a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public String getServiceNoticeStatus(final String nodeID, final String ipaddr, final String service) throws SQLException {
        String notify = "Y";
    
        final String query = "SELECT notify FROM ifservices, service WHERE nodeid=? AND ipaddr=? AND ifservices.serviceid=service.serviceid AND service.servicename=?";
        java.sql.Connection connection = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            connection = getConnection();
            d.watch(connection);
    
            final PreparedStatement statement = connection.prepareStatement(query);
            d.watch(statement);
            statement.setInt(1, Integer.parseInt(nodeID));
            statement.setString(2, ipaddr);
            statement.setString(3, service);
    
            final ResultSet rs = statement.executeQuery();
            d.watch(rs);
    
            if (rs.next() && rs.getString("notify") != null) {
                notify = rs.getString("notify");
                if (notify == null)
                    notify = "Y";
            }
            return notify;
        } finally {
            d.cleanUp();
        }
    }
    /**
     * <p>updateNoticeWithUserInfo</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @param userId a {@link java.lang.String} object.
     * @param noticeId a int.
     * @param media a {@link java.lang.String} object.
     * @param contactInfo a {@link java.lang.String} object.
     * @param autoNotify a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public void updateNoticeWithUserInfo(final String userId, final int noticeId, final String media, final String contactInfo, final String autoNotify) throws SQLException, MarshalException, ValidationException, IOException {
        if (noticeId < 0) return;
        int userNotifId = getUserNotifId();
        ThreadCategory log = this.log();
        if (log.isDebugEnabled()) {
            log.debug("updating usersnotified: ID = " + userNotifId+ " User = " + userId + ", notice ID = " + noticeId + ", conctactinfo = " + contactInfo + ", media = " + media + ", autoNotify = " + autoNotify);
        }
        Connection connection = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            connection = getConnection();
            d.watch(connection);
            final PreparedStatement insert = connection.prepareStatement("INSERT INTO usersNotified (id, userid, notifyid, notifytime, media, contactinfo, autonotify) values (?,?,?,?,?,?,?)");
            d.watch(insert);
    
            insert.setInt(1, userNotifId);
            insert.setString(2, userId);
            insert.setInt(3, noticeId);
    
            insert.setTimestamp(4, new Timestamp((new Date()).getTime()));
    
            insert.setString(5, media);
            insert.setString(6, contactInfo);
            insert.setString(7, autoNotify);
    
            insert.executeUpdate();
        } finally {
            d.cleanUp();
        }
    }
    /**
     * This method inserts a row into the notifications table in the database.
     * This row indicates that the page has been sent out.
     *
     * @param queueID a {@link java.lang.String} object.
     * @param notification TODO
     * @param notifyId a int.
     * @param params a {@link java.util.Map} object.
     * @throws java.sql.SQLException if any.
     */
    public void insertNotice(final int notifyId, final Map<String, String> params, final String queueID, final Notification notification) throws SQLException {
        Connection connection = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            connection = getConnection();
            d.watch(connection);
            final PreparedStatement statement = connection.prepareStatement("INSERT INTO notifications (" +
                    "textmsg, numericmsg, notifyid, pagetime, nodeid, interfaceid, serviceid, eventid, " +
                    "eventuei, subject, queueID, notifConfigName) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            d.watch(statement);
    
            // notifications textMsg field
            String textMsg = params.get(NotificationManager.PARAM_TEXT_MSG);
            if (textMsg != null && textMsg.length() > 4000) {
                log().warn("textmsg too long, it will be truncated");
                textMsg = textMsg.substring(0, 4000);
            }
            statement.setString(1, textMsg);
    
            // notifications numericMsg field
            String numMsg = params.get(NotificationManager.PARAM_NUM_MSG);
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
            String node = params.get(NotificationManager.PARAM_NODE);
            if (node != null && !node.trim().equals("") && !node.toLowerCase().equals("null") && !node.toLowerCase().equals("%nodeid%")) {
                statement.setInt(5, Integer.parseInt(node));
            } else {
                statement.setNull(5, Types.INTEGER);
            }
    
            // notifications interfaceID field
            String ipaddr = params.get(NotificationManager.PARAM_INTERFACE);
            if (ipaddr != null && !ipaddr.trim().equals("") && !ipaddr.toLowerCase().equals("null") && !ipaddr.toLowerCase().equals("%interface%")) {
                statement.setString(6, ipaddr);
            } else {
                statement.setString(6, null);
            }
    
            // notifications serviceID field
            String service = params.get(NotificationManager.PARAM_SERVICE);
            if (service != null && !service.trim().equals("") && !service.toLowerCase().equals("null") && !service.toLowerCase().equals("%service%")) {
                statement.setInt(7, getServiceId(service));
            } else {
                statement.setNull(7, Types.INTEGER);
            }
    
            // eventID field
            final String eventID = params.get("eventID");
            statement.setInt(8, Integer.parseInt(eventID));
    
            statement.setString(9, params.get("eventUEI"));
            
            // notifications subject field
            statement.setString(10, params.get(NotificationManager.PARAM_SUBJECT));
            
            // the queue this will be sent on
            statement.setString(11, queueID);
            
            statement.setString(12, notification.getName());
    
            statement.executeUpdate();
        } finally {
            d.cleanUp();
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
        final DBUtils d = new DBUtils(getClass());
        try {
            connection = getConnection();
            d.watch(connection);
    
            final PreparedStatement statement = connection.prepareStatement("SELECT serviceID from service where serviceName = ?");
            d.watch(statement);
            statement.setString(1, service);
    
            final ResultSet results = statement.executeQuery();
            d.watch(results);
            results.next();
    
            serviceID = results.getInt(1);
    
            return serviceID;
        } finally {
            d.cleanUp();
        }
    }
    /**
     * <p>getNotifications</p>
     *
     * @return a {@link java.util.Map} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public Map<String, Notification> getNotifications() throws IOException, MarshalException, ValidationException {
        update();
    
        Map<String, Notification> newMap = new HashMap<String, Notification>();
    
        Notification notices[] = m_notifications.getNotification();
        for (int i = 0; i < notices.length; i++) {
            newMap.put(notices[i].getName(), notices[i]);
        }
    
        return Collections.unmodifiableMap(newMap);
    }
    /**
     * <p>getServiceNames</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
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
     * <p>getNotification</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.notifications.Notification} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public Notification getNotification(final String name) throws IOException, MarshalException, ValidationException {
        update();
    
        return getNotifications().get(name);
    }
    /**
     * <p>getNotificationNames</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
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
     * <p>removeNotification</p>
     *
     * @param name a {@link java.lang.String} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
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
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public synchronized void addNotification(final Notification notice) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        // remove any existing notice with the same name
        m_notifications.removeNotification(getNotification(notice.getName()));
    
        m_notifications.addNotification(notice);
        saveCurrent();
    }
    /**
     * <p>replaceNotification</p>
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newNotice a {@link org.opennms.netmgt.config.notifications.Notification} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public synchronized void replaceNotification(final String oldName, final Notification newNotice) throws MarshalException, ValidationException, IOException, ClassNotFoundException {
        //   In order to preserve the order of the notices, we have to replace "in place".

        Notification notice = getNotification(oldName);
	if (notice != null) {
       	notice.setWriteable(newNotice.getWriteable());
       	notice.setName(newNotice.getName());
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
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
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
     * <p>saveCurrent</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
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
     * <p>saveXML</p>
     *
     * @param xmlString a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
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
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public abstract void update() throws IOException, MarshalException, ValidationException;

    /**
     * <p>rebuildParameterMap</p>
     *
     * @param notifId a int.
     * @param resolutionPrefix a {@link java.lang.String} object.
     * @param skipNumericPrefix a boolean.
     * @return a {@link java.util.Map} object.
     * @throws java.lang.Exception if any.
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
                parmMap.put(
                    NotificationManager.PARAM_TEXT_MSG, 
                    expandNotifParms(
                        resolutionPrefix, 
                        Collections.singletonMap("noticeid", String.valueOf(notifId))
                    ) + rs.getString("textMsg")
                );
                if (skipNumericPrefix) {
                    parmMap.put(
                        NotificationManager.PARAM_NUM_MSG, 
                        rs.getString("numericMsg")
                    );
                } else {
                    parmMap.put(
                        NotificationManager.PARAM_NUM_MSG, 
                        expandNotifParms(
                            resolutionPrefix, 
                            Collections.singletonMap("noticeid", String.valueOf(notifId))
                        ) + rs.getString("numericMsg")
                    );
                }
                parmMap.put(
                    NotificationManager.PARAM_SUBJECT, 
                    expandNotifParms(
                        resolutionPrefix, 
                        Collections.singletonMap("noticeid", String.valueOf(notifId))
                    ) + rs.getString("subject")
                );
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
        querier.execute(notifId);
        return parmMap;
    }

    /**
     * Adds additional parameters defined by the user in the notificaiton
     * configuration XML.
     *
     * @param paramMap a {@link java.util.Map} object.
     * @param notification a {@link org.opennms.netmgt.config.notifications.Notification} object.
     */
    public static void addNotificationParams(final Map<String, String> paramMap, final Notification notification) {
        Collection<Parameter> parameters = notification.getParameterCollection();
        
        for (Parameter parameter : parameters) {
            paramMap.put(parameter.getName(), parameter.getValue());
        }
    }

    /**
     * <p>forEachUserNotification</p>
     *
     * @param notifId a int.
     * @param rp a {@link org.opennms.netmgt.utils.RowProcessor} object.
     */
    public void forEachUserNotification(final int notifId, final RowProcessor rp) {
        final Querier querier = new Querier(m_dataSource, "select * from usersNotified where notifyId = ? order by notifytime", rp);
        querier.execute(notifId);
    }

    /**
     * <p>getQueueForNotification</p>
     *
     * @param notifId a int.
     * @return a {@link java.lang.String} object.
     */
    public String getQueueForNotification(final int notifId) {
        final SingleResultQuerier querier = new SingleResultQuerier(m_dataSource, "select queueID from notifications where notifyId = ?");
        querier.execute(notifId);
        return (String)querier.getResult();
    }
    
    /**
     * <p>expandMapValues</p>
     *
     * @param map a {@link java.util.Map} object.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static void expandMapValues(Map<String, String> map, final Event event) {
        for (String key : map.keySet()) {
            String mapValue = map.get(key);
            if (mapValue == null) {
                continue;
            }
            String expandedValue = EventUtil.expandParms(map.get(key), event);
            if (expandedValue == null) {
                // Don't use this value to replace the existing value if it's null
            } else {
                map.put(key, expandedValue);
            }
        }
    }

    /**
     * In the absence of DAOs and ORMs this creates an Event object from the persisted
     * record.
     *
     * @param eventid a int.
     * @return a populated Event object
     */
    public Event getEvent(final int eventid) {
        // don't switch using event builder since this event is read from the database
        final Event event = new Event();
        Querier querier = new Querier(m_dataSource, "select * from events where eventid = ?", new RowProcessor() {

            public void processRow(ResultSet rs) throws SQLException {
                event.setDbid(rs.getInt("eventid"));
                event.setUei(rs.getString("eventuei"));
                event.setNodeid(rs.getLong("nodeid"));
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
        querier.execute(eventid);
        return event;
    }

}
