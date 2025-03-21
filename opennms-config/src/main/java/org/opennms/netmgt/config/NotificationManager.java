/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.RowProcessor;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.notifications.Header;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Notifications;
import org.opennms.netmgt.config.notifications.Parameter;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventDatetimeFormatter;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Tticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(NotificationManager.class);

    private static final EventDatetimeFormatter FORMATTER = EventConstants.getEventDatetimeFormatter();

    /**
     * Object containing all Notification objects parsed from the xml file
     */
    public Notifications m_notifications;

    /**
     * Counters for exposure via JMX
     */
    private long m_notifTasksQueued = 0;
    private long m_binaryNoticesAttempted = 0;
    private long m_javaNoticesAttempted = 0;
    private long m_binaryNoticesSucceeded = 0;
    private long m_javaNoticesSucceeded = 0;
    private long m_binaryNoticesFailed = 0;
    private long m_javaNoticesFailed = 0;
    private long m_binaryNoticesInterrupted = 0;
    private long m_javaNoticesInterrupted = 0;
    private long m_unknownNoticesInterrupted = 0;
    
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
        if (input.contains("%noticeid%")) {
            String noticeId = paramMap.get("noticeid");
            if (noticeId != null) {
                return input.replaceAll("%noticeid%", noticeId);
            }
        }
        return input;
    }

    /**
     * <p>Constructor for NotificationManager.</p>
     *
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
     */
    @Deprecated
    public synchronized void parseXML(final Reader reader) {
        m_notifications = JaxbUtils.unmarshal(Notifications.class, reader, true);
        oldHeader = m_notifications.getHeader();
    }

    /**
     * <p>parseXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws IOException 
     */
    public synchronized void parseXML(final InputStream stream) throws IOException {
        try (final Reader reader = new InputStreamReader(stream)) {
            m_notifications = JaxbUtils.unmarshal(Notifications.class, reader, true);
        }
        oldHeader = m_notifications.getHeader();
    }

    /**
     * <p>hasUei</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @return a boolean.
     * @throws java.io.IOException if any.
     */
    public boolean hasUei(final String uei) throws IOException {
        update();

        for (Notification notif : m_notifications.getNotifications()) {
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
     */
    public Notification[] getNotifForEvent(final Event event) throws IOException {
        update();
        List<Notification> notifList = new ArrayList<>();
        boolean matchAll = getConfigManager().getNotificationMatch();

        // This if statement will check to see if notification should be suppressed for this event.

        if (event == null) {
            LOG.warn("unable to get notification for null event!");
            return null;
        } else if (event.getLogmsg() != null && !(event.getLogmsg().getNotify())) {

            LOG.debug("Event {} is configured to suppress notifications.", event.getUei());
            return null;
        }

        for (Notification curNotif : m_notifications.getNotifications()) {

            LOG.trace("Checking notification {} against event {} with UEI {}", curNotif.getUei(), event.getDbid(), event.getUei());

            if (event.getUei().equals(curNotif.getUei()) || "MATCH-ANY-UEI".equals(curNotif.getUei())) {
                // Match!
            	LOG.debug("Exact match using notification UEI {} for event UEI: {}", curNotif.getUei(), event.getUei());
            } else if (curNotif.getUei().charAt(0) == '~') {
                if (event.getUei().matches(curNotif.getUei().substring(1))) {
                	//Match!
                    LOG.debug("Regex hit using notification UEI {} for event UEI: {}", curNotif.getUei(), event.getUei());
                } else {

                    LOG.trace("Notification regex {} failed to match event UEI: {}", event.getUei(), curNotif.getUei());
                    continue;
                }
            } else {

                LOG.debug("Notification UEI {} did not match UEI of event {}: {}", curNotif.getUei(), event.getDbid(), event.getUei());
                continue;
            }

            /**
             * Check if event severity matches pattern in notification
             */

            LOG.trace("Checking event severity: {} against notification severity: {}", curNotif.getEventSeverity().orElse(null), event.getSeverity());
            // parameter is optional, return true if not set
            if (!curNotif.getEventSeverity().isPresent()) {
                // Skip matching on severity
            } else if (event.getSeverity().toLowerCase().matches(curNotif.getEventSeverity().get().toLowerCase())) {
                // Severities match
            } else {

                LOG.debug("Event severity: {} did not match notification severity: {}", curNotif.getEventSeverity().orElse(null), event.getSeverity());
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

                        LOG.debug("Event {} did not match parameters for notice {}", event.getUei(), curNotif.getName());
                        continue;
                    }
                    // Add this notification to the return value
                    notifList.add(curNotif);


                    LOG.debug("Event {} matched notice {}", event.getUei(), curNotif.getName());

                    if (!matchAll)
                        break;
                } else {

                    LOG.debug("Node/interface/service combination in the event was invalid");
                }
            } else {

                LOG.debug("Current notification with UEI {} is turned off.", curNotif.getUei());
            }
        }

        if (!notifList.isEmpty()) {
            return notifList.toArray(new Notification[0]);
        } else {
            return null;
        }
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
                // TODO: Trim parentheses from the filter and trim whitespace from inside the
                // filter statement. This comparison is very brittle as it is.
                if ("ipaddr != '0.0.0.0'".equals(notif.getRule().getContent().toLowerCase()) || "ipaddr iplike *.*.*.*".equals(notif.getRule().getContent().toLowerCase())) {
                    return true;
                } else {
                    return false;
                }
            }
            // When rule is enforced to be strict and there is no nodeId, interface or service, discard the notice.
            if(notif.getRule().getStrict() != null && notif.getRule().getStrict()) {
                return false;
            }
            return true;
        }

        final StringBuilder constraints = new StringBuilder();
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

        String rule = "((" + notif.getRule().getContent() + ")" + constraints + ")";

        return isRuleMatchingFilter(notif, rule);
    }

    private boolean isRuleMatchingFilter(final Notification notif, final String rule) {
        try {
            return FilterDaoFactory.getInstance().isRuleMatching(rule);
        } catch (FilterParseException e) {
            LOG.error("Invalid filter rule for notification {}: {}", notif.getName(), notif.getRule().getContent(), e);
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
     */
    public int getNoticeId() throws SQLException, IOException {
        return getNxtId(m_configManager.getNextNotifIdSql());
    }

    /**
     * <p>getUserNotifId</p>
     *
     * @return a int.
     * @throws java.sql.SQLException if any.
     * @throws java.io.IOException if any.
     */
    public int getUserNotifId() throws SQLException, IOException {
        return getNxtId(m_configManager.getNextUserNotifIdSql());
    }

    /**
     * This method returns the next ID for a given sql.
     *
     * @param sql a java.lang.String.
     * @return a int.
     * @throws java.io.IOException if any.
     */
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
     */
    public boolean noticeOutstanding(final int noticeId) throws IOException {
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
            LOG.error("Error getting notice status", e);
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
     */
    public Collection<Integer> acknowledgeNotice(final Event event, final String uei, final String[] matchList) throws SQLException, IOException {
        List<Integer> notifIDs = new LinkedList<>();
        final DBUtils dbUtils = new DBUtils(getClass());

        try {
            // First get most recent event ID from notifications 
            // that match the matchList, then get all notifications
            // with this event ID

            // Verify if parameter matching is required
            boolean matchParameters = false;
            for (int i = 0; i < matchList.length; i++) {
                if (matchList[i].startsWith("parm[")) {
                    matchParameters = true;
                    break;
                }
            }

            final Map<String, String> eventParametersToMatch = new LinkedHashMap<>();
            final StringBuilder sql = new StringBuilder("SELECT n.eventid FROM notifications n ");
            if (matchParameters) {
                sql.append("INNER JOIN events as e on e.eventid = n.eventid ");
                for (int i = 0; i < matchList.length; i++) {
                    if (matchList[i].startsWith("parm[")) {
                        if (appendParameterNameAndValue(matchList[i], event, eventParametersToMatch)) {
                            sql.append(String.format("INNER JOIN event_parameters as ep%d on ep%d.eventid = n.eventid and ep%d.name=? and ep%d.value=? ",
                                    i, i, i, i));
                        } else {
                            // The given event does contain the specified parameter, so no match can be made
                            LOG.warn("No parameter matching {} was found on {}. No notices with UEI {} will be acknowledged.",
                                    matchList[i], event, uei);
                            // No DB connections have been acquired yet, so we can return immediately
                            return Collections.emptyList();
                        }
                    }
                }
                LOG.debug("Matching notices with UEI {} against event parameters: {}", uei, eventParametersToMatch);
            }
            sql.append("WHERE n.eventuei=? ");
            for (int i = 0; i < matchList.length; i++) {
                if (!matchList[i].startsWith("parm[")) {
                    sql.append("AND n.").append(matchList[i]).append("=? ");
                }
            }
            sql.append("ORDER BY eventid desc limit 1");

            Connection connection = getConnection();
            dbUtils.watch(connection);
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            dbUtils.watch(statement);

            int offset = 1;
            for (Map.Entry<String, String> eventParameterToMatch : eventParametersToMatch.entrySet()) {
                statement.setString(offset++, eventParameterToMatch.getKey());
                statement.setString(offset++, eventParameterToMatch.getValue());
            }

            statement.setString(offset++, uei);

            for (int i = 0; i < matchList.length; i++) {
                if (matchList[i].equals("nodeid")) {
                    statement.setLong(offset++, event.getNodeid());
                } else if (matchList[i].equals("interfaceid")) {
                    statement.setString(offset++, event.getInterface());
                } else if (matchList[i].equals("serviceid")) {
                    statement.setInt(offset++, getServiceId(event.getService()));
                } else if (matchList[i].startsWith("parm[")) {
                    // Ignore
                } else {
                    LOG.warn("Unknown match statement {} for UEI {}.", matchList[i], uei);
                }
            }

            ResultSet results = statement.executeQuery();
            dbUtils.watch(results);
            if (results != null && results.next()) {
                long eventID = results.getLong(1);
                notifIDs = doAcknowledgeNotificationsFromEvent(connection, dbUtils, eventID);
            } else {
                LOG.debug("No matching DOWN eventID found");
            }
        } finally {
            dbUtils.cleanUp();
        }
        return notifIDs;
    }

    /**
     * Parses the given match statement i.e. param[key] or parm[#99] and retrieves the corresponding
     * parameter from the given event.
     *
     * If the parameter is found, it is added to the map and <code>true</code> is returned, otherwise <code>false</code>
     * is returned.
     *
     * @param match notification match statement
     * @param event event to match
     * @param eventParametersToMatch ordered map of event parameters we need to match
     * @return <code>true</code> if the map was modified, <code>false</code> otherwise
     */
    private static boolean appendParameterNameAndValue(String match, Event event, Map<String, String> eventParametersToMatch) {
        String key = null;
        String param = null;
        String value = null;
        try {
            key = match.substring(match.indexOf('[') + 1, match.indexOf(']'));
        } catch (Exception e) {}
        if (key != null) {
            int numkey = 0;
            if (key.startsWith("#")) {
                try {
                    numkey = Integer.parseInt(key.substring(1));
                } catch (Exception e) {}
            }
            int idx = 1;
            for (Parm p : event.getParmCollection()) {
                if (numkey > 0) {
                    if (numkey == idx) {
                        param = p.getParmName();
                        value = p.getValue().getContent();
                    }
                } else {
                    if (p.getParmName().equalsIgnoreCase(key)) {
                        param = p.getParmName();
                        value = p.getValue().getContent();
                    }
                }
                idx++;
            }
        }
        if (param == null || value == null) {
            return false;
        }
        eventParametersToMatch.put(param, value);
        return true;
    }

    /**
     * <p>acknowledgeNoticeBasedOnAlarms</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link java.util.Collection} object.
     * @throws java.sql.SQLException if any.
     * @throws java.io.IOException if any.
     */
    public Collection<Integer> acknowledgeNoticeBasedOnAlarms(final Event event) throws SQLException, IOException {
        Set<Integer> notifIDs = new TreeSet<>();
        if (event.getAlarmData() == null || event.getAlarmData().getAlarmType() != 2) {
            return notifIDs;
        }
        final DBUtils dbUtils = new DBUtils(getClass());
        try {
            Connection connection = getConnection();
            dbUtils.watch(connection);
            PreparedStatement statement = connection.prepareStatement("SELECT e.eventId FROM events e, alarms a WHERE e.alarmid = a.alarmid AND a.reductionkey= ?");
            dbUtils.watch(statement);
            String resolvingKey = event.getAlarmData().getClearKey() == null ? event.getAlarmData().getReductionKey() : event.getAlarmData().getClearKey();
            statement.setString(1, resolvingKey);
            ResultSet results = statement.executeQuery();
            dbUtils.watch(results);
            while (results.next()) {
                int eventID = results.getInt(1);
                notifIDs.addAll(doAcknowledgeNotificationsFromEvent(connection, dbUtils, eventID));
            }
        } finally {
            dbUtils.cleanUp();
        }
        return notifIDs;
    }

    /**
     * <p>doAcknowledgeNotificationsFromEvent</p>
     *
     * @param connection a {@link java.sql.Connection} object.
     * @param dbUtils a {@link org.opennms.core.utils.DBUtils} object.
     * @param eventID a {@link java.lang.Integer} object.
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     * @throws java.io.IOException if any.
     */
    private List<Integer> doAcknowledgeNotificationsFromEvent(final Connection connection, final DBUtils dbUtils, long eventID)
            throws SQLException, IOException {
        List<Integer> notifIDs = new LinkedList<>();
        LOG.debug("EventID for notice(s) to be acked: {}", eventID);

        PreparedStatement statement = connection.prepareStatement("SELECT notifyid, answeredby, respondtime FROM notifications WHERE eventID=?");
        dbUtils.watch(statement);
        statement.setLong(1, eventID);

        ResultSet results = statement.executeQuery();
        boolean wasAcked = false;
        if (results != null) {
            dbUtils.watch(results);
            while (results.next()) {
                int notifID = results.getInt(1);
                String ansBy = results.getString(2);
                Timestamp ts = results.getTimestamp(3);
                if(ansBy == null) {
                    ansBy = "auto-acknowledged";
                    ts = new Timestamp((new Date()).getTime());
                } else if(ansBy.indexOf("auto-acknowledged") > -1) {
                    LOG.debug("Notice has previously been auto-acknowledged. Skipping...");
                    continue;
                } else {
                    wasAcked = true;
                    ansBy = ansBy + "/auto-acknowledged";
                }
                LOG.debug("Matching DOWN notifyID = {}, was acked by user = {}, ansBy = {}", notifID, wasAcked, ansBy);
                final PreparedStatement update = connection.prepareStatement(getConfigManager().getConfiguration().getAcknowledgeUpdateSql());
                dbUtils.watch(update);
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

        return notifIDs;
    }

    /**
     * <p>getActiveNodes</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    public List<Integer> getActiveNodes() throws SQLException {
        final List<Integer> allNodes = new ArrayList<>();
        Querier querier = new Querier(m_dataSource, "SELECT n.nodeid FROM node n WHERE n.nodetype != 'D' ORDER BY n.nodelabel", new RowProcessor() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                allNodes.add(rs.getInt(1));
            }
        });
        querier.execute(new Object[] {});
        return allNodes;
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

        final String query = "SELECT notify FROM ifservices, ipInterface, node, service WHERE ifServices.ipInterfaceId = ipInterface.id AND ipInterface.nodeId = node.nodeId AND node.nodeid=? AND ipInterface.ipaddr=? AND ifservices.serviceid=service.serviceid AND service.servicename=?";
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
     * @param userId a {@link java.lang.String} object.
     * @param noticeId a int.
     * @param media a {@link java.lang.String} object.
     * @param contactInfo a {@link java.lang.String} object.
     * @param autoNotify a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public void updateNoticeWithUserInfo(final String userId, final int noticeId, final String media, final String contactInfo, final String autoNotify) throws SQLException, IOException {
        if (noticeId < 0) return;
        int userNotifId = getUserNotifId();
        LOG.debug("updating usersnotified: ID = {} User = {}, notice ID = {}, contactinfo = {}, media = {}, autoNotify = {}", autoNotify, userNotifId, userId, noticeId, contactInfo, media);
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
            statement.setString(1, params.get(NotificationManager.PARAM_TEXT_MSG));
    
            // notifications numericMsg field
            String numMsg = params.get(NotificationManager.PARAM_NUM_MSG);
            if (numMsg != null && numMsg.length() > 256) {
                LOG.warn("numericmsg too long, it will be truncated");
                numMsg = numMsg.substring(0, 256);
            }
            statement.setString(2, numMsg);

            // notifications notifyID field
            statement.setInt(3, notifyId);

            // notifications pageTime field
            statement.setTimestamp(4, new Timestamp((new Date()).getTime()));

            // notifications nodeID field
            String node = params.get(NotificationManager.PARAM_NODE);
            if (node != null && !node.trim().equals("") && !node.equalsIgnoreCase("null") && !node.equalsIgnoreCase("%nodeid%")) {
                statement.setInt(5, Integer.parseInt(node));
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            // notifications interfaceID field
            String ipaddr = params.get(NotificationManager.PARAM_INTERFACE);
            if (ipaddr != null && !ipaddr.trim().equals("") && !ipaddr.equalsIgnoreCase("null") && !ipaddr.equalsIgnoreCase("%interface%")) {
                statement.setString(6, ipaddr);
            } else {
                statement.setString(6, null);
            }

            // notifications serviceID field
            String service = params.get(NotificationManager.PARAM_SERVICE);
            if (service != null && !service.trim().equals("") && !service.equalsIgnoreCase("null") && !service.equalsIgnoreCase("%service%")) {
                statement.setInt(7, getServiceId(service));
            } else {
                statement.setNull(7, Types.INTEGER);
            }

            // eventID field
            final String eventID = params.get("eventID");
            if (eventID != null && !eventID.trim().equals("") && !eventID.trim().equals("0") && !eventID.equalsIgnoreCase("null") && !eventID.equalsIgnoreCase("%eventid%")) {
                statement.setInt(8, Integer.parseInt(eventID));
            } else {
                statement.setNull(8, Types.INTEGER);
            }

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
     * This method queries the database in search of a service id for a given service name
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
            if (!results.next()) {
                throw new SQLException("No serviceID found for service with serviceName: " + service);
            }

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
     */
    public Map<String, Notification> getNotifications() throws IOException {
        update();

        Map<String, Notification> newMap = new HashMap<String, Notification>();

        for (final Notification notif : m_notifications.getNotifications()) {
            newMap.put(notif.getName(), notif);
        }

        return Collections.unmodifiableMap(newMap);
    }

    /**
     * <p>getServiceNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getServiceNames() throws SQLException {
        final List<String> services = new ArrayList<>();
        Querier querier = new Querier(m_dataSource, "SELECT servicename FROM service", new RowProcessor() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                services.add(rs.getString(1));
            }
        });
        querier.execute(new Object[] {});
        return services;
    }

    /**
     * <p>getNotification</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.notifications.Notification} object.
     * @throws java.io.IOException if any.
     */
    public Notification getNotification(final String name) throws IOException {
        update();

        return getNotifications().get(name);
    }

    /**
     * <p>getNotificationNames</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.io.IOException if any.
     */
    public List<String> getNotificationNames() throws IOException {
        update();

        List<String> notificationNames = new ArrayList<>();

        for (Notification curNotif : m_notifications.getNotifications()) {
            notificationNames.add(curNotif.getName());
        }

        return notificationNames;
    }

    /**
     * <p>removeNotification</p>
     *
     * @param name a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public synchronized void removeNotification(final String name) throws IOException, ClassNotFoundException {
        m_notifications.removeNotification(getNotification(name));
        saveCurrent();
    }

    /**
     * Handles adding a new Notification.
     *
     * @param notice
     *            The Notification to add.
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public synchronized void addNotification(final Notification notice) throws IOException, ClassNotFoundException {
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
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public synchronized void replaceNotification(final String oldName, final Notification newNotice) throws IOException, ClassNotFoundException {
        //   In order to preserve the order of the notices, we have to replace "in place".

        Notification notice = getNotification(oldName);
        if (notice != null) {
            notice.setWriteable(newNotice.getWriteable());
            notice.setName(newNotice.getName());
            notice.setDescription(newNotice.getDescription().orElse(null));
            notice.setUei(newNotice.getUei());
            notice.setRule(newNotice.getRule());
            notice.setDestinationPath(newNotice.getDestinationPath());
            notice.setNoticeQueue(newNotice.getNoticeQueue().orElse(null));
            notice.setTextMessage(newNotice.getTextMessage());
            notice.setSubject(newNotice.getSubject().orElse(null));
            notice.setNumericMessage(newNotice.getNumericMessage().orElse(null));
            notice.setStatus(newNotice.getStatus());
            notice.setVarbind(newNotice.getVarbind());
            notice.getParameters().clear(); // Required to avoid NMS-5948
            for (Parameter parameter : newNotice.getParameters()) {
                Parameter newParam = new Parameter();
                newParam.setName(parameter.getName());
                newParam.setValue(parameter.getValue());
                notice.addParameter(newParam);
            } 
            saveCurrent();
        }
        else	
            addNotification(newNotice);
    }

    /**
     * Sets the status on an individual notification configuration and saves to xml.
     *
     * @param name
     *            The name of the notification.
     * @param status
     *            The status (either "on" or "off").
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public synchronized void updateStatus(final String name, final String status) throws IOException, ClassNotFoundException {
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
     * @throws java.io.IOException if any.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public synchronized void saveCurrent() throws IOException, ClassNotFoundException {
        m_notifications.setHeader(rebuildHeader());

        // Marshal to a string first, then write the string to the file. This
        // way the original configuration
        // isn't lost if the XML from the marshal is hosed.
        final String xmlString = JaxbUtils.marshal(m_notifications);
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

        header.setCreated(FORMATTER.format(new Date()));

        return header;
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    public abstract void update() throws IOException;

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
            @Override
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
     * Adds additional parameters defined by the user in the notification
     * configuration XML.
     *
     * @param paramMap a {@link java.util.Map} object.
     * @param notification a {@link org.opennms.netmgt.config.notifications.Notification} object.
     */
    public static void addNotificationParams(final Map<String, String> paramMap, final Notification notification) {
        Collection<Parameter> parameters = notification.getParameters();

        for (Parameter parameter : parameters) {
            paramMap.put(parameter.getName(), parameter.getValue());
        }
    }

    /**
     * <p>forEachUserNotification</p>
     *
     * @param notifId a int.
     * @param rp a {@link org.opennms.core.utils.RowProcessor} object.
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

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                event.setDbid(rs.getLong("eventid"));
                event.setUei(rs.getString("eventuei"));
                event.setNodeid(rs.getLong("nodeid"));
                event.setTime(rs.getDate("eventtime"));
                event.setHost(rs.getString("eventhost"));
                event.setInterface(rs.getString("ipaddr"));
                event.setSnmphost(rs.getString("eventsnmphost"));
                event.setService(getServiceName(rs.getInt("serviceid")));
                event.setCreationTime(rs.getDate("eventcreatetime"));
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
    
    public void incrementTasksQueued() {
        m_notifTasksQueued++;
    }
    
    public void incrementAttempted(boolean isBinary) {
        if (isBinary) {
            m_binaryNoticesAttempted++;
        } else {
            m_javaNoticesAttempted++;
        }
    }
    
    public void incrementSucceeded(boolean isBinary) {
        if (isBinary) {
            m_binaryNoticesSucceeded++;
        } else {
            m_javaNoticesSucceeded++;
        }
    }
    
    public void incrementFailed(boolean isBinary) {
        if (isBinary) {
            m_binaryNoticesFailed++;
        } else {
            m_javaNoticesFailed++;
        }
    }
    public void incrementInterrupted(boolean isBinary) {
        if (isBinary) {
            m_binaryNoticesInterrupted++;
        } else {
            m_javaNoticesInterrupted++;
        }
    }
    
    public void incrementUnknownInterrupted() {
        m_unknownNoticesInterrupted++;
    }
    
    public long getNotificationTasksQueued() {
        return m_notifTasksQueued;
    }

    public long getBinaryNoticesAttempted() {
        return m_binaryNoticesAttempted;
    }

    public long getJavaNoticesAttempted() {
        return m_javaNoticesAttempted;
    }

    public long getBinaryNoticesSucceeded() {
        return m_binaryNoticesSucceeded;
    }

    public long getJavaNoticesSucceeded() {
        return m_javaNoticesSucceeded;
    }

    public long getBinaryNoticesFailed() {
        return m_binaryNoticesFailed;
    }

    public long getJavaNoticesFailed() {
        return m_javaNoticesFailed;
    }

    public long getBinaryNoticesInterrupted() {
        return m_binaryNoticesInterrupted;
    }

    public long getJavaNoticesInterrupted() {
        return m_javaNoticesInterrupted;
    }

    public long getUnknownNoticesInterrupted() {
        return m_unknownNoticesInterrupted;
    }
}
