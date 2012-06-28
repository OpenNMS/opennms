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

package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.RowProcessor;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.TimeConverter;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DestinationPathManager;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.PollOutagesConfigManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.destinationPaths.Escalate;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.notifd.AutoAcknowledge;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.datablock.EventUtil;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

/**
 * <p>BroadcastEventProcessor class.</p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class BroadcastEventProcessor implements EventListener {
    /**
     */
    private volatile Map<String, NoticeQueue> m_noticeQueues;
    private volatile EventIpcManager m_eventManager;
    private volatile PollOutagesConfigManager m_pollOutagesConfigManager;
    private volatile NotificationManager m_notificationManager;
    private volatile NotifdConfigManager m_notifdConfigManager;
    private volatile DestinationPathManager m_destinationPathManager;
    private volatile UserManager m_userManager;
    private volatile GroupManager m_groupManager;
    private volatile NotificationCommandManager m_notificationCommandManager;

    /**
     * <p>Constructor for BroadcastEventProcessor.</p>
     */
    public BroadcastEventProcessor() {
    }

    /**
     * An event listener is created and this instance is setup as the
     * endpoint for broadcast events. When a new event arrives it is processed
     * and the appropriate action is taken.
     */
    protected void init() {
        assertPropertiesSet();
        
        // start to listen for events
        getEventManager().addEventListener(this);
    }
    
    private void assertPropertiesSet() {
        if (m_noticeQueues == null) {
            throw new IllegalStateException("property noticeQueues not set");
        }
        if (m_eventManager == null) {
            throw new IllegalStateException("property eventManager not set");
        }
        if (m_pollOutagesConfigManager == null) {
            throw new IllegalStateException("property pollOutagesConfigManager not set");
        }
        if (m_notificationManager == null) {
            throw new IllegalStateException("property notificationManager not set");
        }
        if (m_notifdConfigManager == null) {
            throw new IllegalStateException("property notifdConfigManager not set");
        }
        if (m_destinationPathManager == null) {
            throw new IllegalStateException("property destinationPathManager not set");
        }
        if (m_userManager == null) {
            throw new IllegalStateException("property userManager not set");
        }
        if (m_groupManager == null) {
            throw new IllegalStateException("property groupManager not set");
        }
        if (m_notificationCommandManager == null) {
            throw new IllegalStateException("property notificationCommandManager not set");
        }

    }

    /**
     * Unsubscribe from eventd
     */
    public void close() {
        getEventManager().removeEventListener(this);
    }


    /**
     * {@inheritDoc}
     *
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing.
     */
    public void onEvent(Event event) {
        if (event == null) return;

        if (isReloadConfigEvent(event)) {
            log().info("onEvent: handling reload configuration event...");
            EventBuilder ebldr = null;
            try {
                m_userManager.update();
                m_groupManager.update();
                m_notificationManager.update();
                m_destinationPathManager.update();
                m_notificationCommandManager.update();
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Notifd");
            } catch (Throwable e) {
                log().debug("onEvent: could not reload notifd configuration: "+e, e);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Notifd");
                ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
            }
            m_eventManager.sendNow(ebldr.getEvent());
            log().info("onEvent: reload configuration event handled.");
            return;
        }

        boolean notifsOn = computeNullSafeStatus();

        if (notifsOn && (checkCriticalPath(event, notifsOn))) {
            scheduleNoticesForEvent(event);
        } else if (!notifsOn) {
            if (log().isDebugEnabled()) {
                log().debug("discarding event " + event.getUei() + ", notifd status on = " + notifsOn);
            }
        }
        automaticAcknowledge(event, notifsOn);
    }

    private boolean isReloadConfigEvent(Event event) {
        boolean isTarget = false;

        if (EventConstants.RELOAD_DAEMON_CONFIG_UEI.equals(event.getUei())) {
            List<Parm> parmCollection = event.getParmCollection();

            for (Parm parm : parmCollection) {
                if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Notifd".equalsIgnoreCase(parm.getValue().getContent())) {
                    isTarget = true;
                    break;
                }
            }

            log().debug("isReloadConfigEventTarget: Notifd was target of reload event: "+isTarget);
        }
        return isTarget;
    }

    /**
     * <p>computeNullSafeStatus</p>
     *
     * @return false if status is not defined in configuration as "on".
     */
    public boolean computeNullSafeStatus() {
        String notificationStatus = null;
        
        try {
            notificationStatus = getNotifdConfigManager().getNotificationStatus();
        } catch (MarshalException e) {
            log().error("onEvent: problem marshalling configuration", e);
        } catch (ValidationException e) {
            log().error("onEvent: problem validating marsharled configuraion", e);
        } catch (IOException e) {
            log().error("onEvent: IO problem marshalling configuration", e);
        }

        return "on".equalsIgnoreCase(notificationStatus);
    }

    /**
     * @author <a href="mailto:billayers@opennms.org">Bill Ayers</a>
     * @param event
     * @param notifsOn
     * @return boolean representing whether event is not relative to a critical path
     */
    private boolean checkCriticalPath(Event event, boolean notifsOn) {
        boolean isPathOk = true;
        Long nodeid = event.getNodeid();

        try {
            // If this is a nodeDown event, see if the critical path was down
            if (event.getUei().equals(EventConstants.NODE_DOWN_EVENT_UEI)) {
                String reason = EventUtils.getParm(event, EventConstants.PARM_LOSTSERVICE_REASON);
                if (reason != null && reason.equals(EventConstants.PARM_VALUE_PATHOUTAGE)) {
                    isPathOk = false;
                    String cip = EventUtils.getParm(event, EventConstants.PARM_CRITICAL_PATH_IP);
                    String csvc = EventUtils.getParm(event, EventConstants.PARM_CRITICAL_PATH_SVC);
                    if (log().isDebugEnabled()) {
                        log().debug("Critical Path " + cip + " " + csvc + " for nodeId " + nodeid + " did not respond. Checking to see if notice would have been sent...");
                    }
                    boolean mapsToNotice = false;
                    boolean noticeSupressed = false;
                    Notification[] notifications = null;
                    mapsToNotice = getNotificationManager().hasUei(event.getUei());
                    notifications = getNotificationManager().getNotifForEvent(event);

                    if (notifsOn && mapsToNotice && continueWithNotice(event) && notifications != null) {
                        noticeSupressed = true;
                    }
                    createPathOutageEvent(nodeid.intValue(), EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL), cip, csvc, noticeSupressed);
                }
            }
        } catch (MarshalException e) {
            log().error("onEvent: problem marshalling configuration", e);
        } catch (ValidationException e) {
            log().error("onEvent: problem validating marshalled configuration", e);
        } catch (IOException e) {
            log().error("onEvent: IO problem marshalling configuration", e);
        }
        return isPathOk;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    private void automaticAcknowledge(Event event, boolean notifsOn) {
        try {
            Collection<AutoAcknowledge> autoAcks = getNotifdConfigManager().getAutoAcknowledges();

            // see if this event has an auto acknowledge for a notice
            for (AutoAcknowledge curAck : autoAcks) {
                if (curAck.getUei().equals(event.getUei())) {
                    try {
                        if (log().isDebugEnabled()) {
                            log().debug("Acknowledging event " + curAck.getAcknowledge() + " " + event.getNodeid() + ":" + event.getInterface() + ":" + event.getService());
                        }
                        
                        Collection<Integer> notifIDs = getNotificationManager().acknowledgeNotice(event, curAck.getAcknowledge(), curAck.getMatch());
                        try {
                            // only send resolution notifications if notifications are globally turned on
                            if (curAck.getNotify() && notifsOn) {
                                sendResolvedNotifications(notifIDs, event, curAck.getAcknowledge(), curAck.getMatch(), curAck.getResolutionPrefix(), getNotifdConfigManager().getConfiguration().isNumericSkipResolutionPrefix());
                            }
                        } catch (Throwable e) {
                            log().error("Failed to send resolution notifications.", e);
                        }
                    } catch (SQLException e) {
                        log().error("Failed to auto acknowledge notice.", e);
                    }
                }

            }
        } catch (Throwable e) {
            log().error("Unable to auto acknowledge notice due to exception.", e);
        }
    }

    private void sendResolvedNotifications(Collection<Integer> notifIDs, Event event, String acknowledge, 
            String[] match, String resolutionPrefix, boolean skipNumericPrefix) throws Exception {
        for (int notifId : notifIDs) {
            boolean wa = false;
            if(notifId < 0) {
                notifId *= -1;
                wa = true;
                if (log().isDebugEnabled()) {
                    log().debug("Conditional autoNotify for notifId " + notifId);
                }
            }
            final boolean wasAcked = wa;
            final Map<String, String> parmMap = rebuildParameterMap(notifId, resolutionPrefix, skipNumericPrefix);
            
            NotificationManager.expandMapValues(parmMap, 
                    getNotificationManager().getEvent(Integer.parseInt(parmMap.get("eventID"))));
            
            String queueID = getNotificationManager().getQueueForNotification(notifId);

            final Map<String, List<String>> userNotifications = new HashMap<String, List<String>>();
            RowProcessor ackNotifProcessor = new RowProcessor() {
                public void processRow(ResultSet rs) throws SQLException {
                    String userID = rs.getString("userID");
                    String contactInfo = rs.getString("contactinfo");
                    String autoNotifyChar = rs.getString("autonotify");
                    if(userID.equals("email-address")) {
                        userID = contactInfo;
                    }
                    String cmd = rs.getString("media");
                    if(autoNotifyChar == null) {
                        autoNotifyChar = "C";
                    }
                    if(autoNotifyChar.equals("Y") || (autoNotifyChar.equals("C") && !wasAcked)) {
                        List<String> cmdList = userNotifications.get(userID);
                        if (cmdList == null) {
                            cmdList = new ArrayList<String>();
                            userNotifications.put(userID, cmdList);
                        }
                        cmdList.add(cmd);
                    }
                }
            };
            getNotificationManager().forEachUserNotification(notifId, ackNotifProcessor);

            for (String userID : userNotifications.keySet()) {
                List<String> cmdList = userNotifications.get(userID);
                String[] cmds = cmdList.toArray(new String[cmdList.size()]);
                if (log().isDebugEnabled()) {
                    log().debug("Sending " + resolutionPrefix + " notification to userID = " + userID + " for notice ID " + notifId);
                }
                sendResolvedNotificationsToUser(queueID, userID, cmds, parmMap);
            }

        }
    }

    /**
     * <p>sendResolvedNotificationsToUser</p>
     *
     * @param queueID a {@link java.lang.String} object.
     * @param targetName a {@link java.lang.String} object.
     * @param commands an array of {@link java.lang.String} objects.
     * @param params a {@link java.util.Map} object.
     * @throws java.lang.Exception if any.
     */
    protected void sendResolvedNotificationsToUser(String queueID, String targetName, String[] commands, Map<String, String> params) throws Exception {
        int noticeId = -1;
        NoticeQueue noticeQueue = null;
        if (m_noticeQueues != null) {
            synchronized (m_noticeQueues) {
                noticeQueue = m_noticeQueues.get(queueID);
            }
        }
        long now = System.currentTimeMillis();

        if (getUserManager().hasUser(targetName)) {
            NotificationTask newTask = makeUserTask(now, params, noticeId, targetName, commands, null, null);

            if (newTask != null) {
                noticeQueue.putItem(now, newTask);
            }
        } else if (targetName.indexOf("@") > -1) {
            NotificationTask newTask = makeEmailTask(now, params, noticeId, targetName, commands, null, null);

            if (newTask != null) {
                synchronized (noticeQueue) {
                    noticeQueue.putItem(now, newTask);
                }
            }
        } else {
            log().warn("Unrecognized target '" + targetName + "' contained in destinationPaths.xml. Please check the configuration.");
        }
    }

    /**
     * This method determines if the notice should continue based on the status
     * of the notify
     */
    private boolean continueWithNotice(Event event) {
        String nodeID = event.hasNodeid() ? String.valueOf(event.getNodeid()) : null;
        String ipAddr = event.getInterface();
        String service = event.getService();

        boolean continueNotice = false;

        // can't check the database if any of these are null, so let the notice
        // continue
        if (nodeID == null || ipAddr == null || service == null || ipAddr.equals("0.0.0.0")) {
            if (log().isDebugEnabled()) {
                log().debug("nodeID=" + nodeID + " ipAddr=" + ipAddr + " service=" + service + ". Not checking DB, continuing...");
            }
            return true;
        }

        try {
            // check the database to see if notices were turned off for this
            // service
            String notify = getNotificationManager().getServiceNoticeStatus(nodeID, ipAddr, service);
            if ("Y".equals(notify)) {
                continueNotice = true;
                if (log().isDebugEnabled()) {
                    log().debug("notify status for service " + service + " on interface/node " + ipAddr + "/" + nodeID + " is 'Y', continuing...");
                }
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("notify status for service " + service + " on interface/node " + ipAddr + "/" + nodeID + " is " + notify + ", not continuing...");
                }
            }
        } catch (Throwable e) {
            continueNotice = true;
            log().error("Not able to get notify status for service " + service + " on interface/node " + ipAddr + "/" + nodeID + ". Continuing notice... " + e.getMessage());
        }

        // in case of a error we will return false
        return continueNotice;
    }

    /**
     * Returns true if an auto acknowledgment exists for the specificed event,
     * such that the arrival of some second, different event will auto
     * acknowledge the event passed as an argument. E.g. if there is an auto ack
     * set up to acknowledge a nodeDown when a nodeUp is received, passing
     * nodeDown to this method will return true. Should this method be in
     * NotifdConfigFactory?
     */
    private boolean autoAckExistsForEvent(String eventUei) {
        try {
            Collection<AutoAcknowledge> autoAcks = getNotifdConfigManager().getAutoAcknowledges();
            for (AutoAcknowledge curAck : autoAcks) {
                if (curAck.getAcknowledge().equals(eventUei)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            log().error("Unable to find if an auto acknowledge exists for event " + eventUei + " due to exception.", e);
            return false;
        }
    }

    /**
     */
    private void scheduleNoticesForEvent(Event event) {

        boolean mapsToNotice = false;

        try {
            mapsToNotice = getNotificationManager().hasUei(event.getUei());
        } catch (Throwable e) {
            log().error("Couldn't map uei " + event.getUei() + " to a notification entry, not scheduling notice.", e);
            return;
        }

        if (mapsToNotice) {
            // check to see if notices are turned on for the interface/service
            // in the event
            if (continueWithNotice(event)) {
                Notification[] notifications = null;

                try {
                    notifications = getNotificationManager().getNotifForEvent(event);
                } catch (Throwable e) {
                    log().error("Couldn't get notification mapping for event " + event.getUei() + ", not scheduling notice.", e);
                    return;
                }

                long nodeid = event.getNodeid();
                String ipaddr = event.getInterface();
                if (notifications != null) {
                    for (Notification notification : notifications) {
                        int noticeId = 0;

                        try {
                            noticeId = getNotificationManager().getNoticeId();
                        } catch (Throwable e) {
                            log().error("Failed to get a unique id # for notification, exiting this notification", e);
                            continue;
                        }

                        Map<String, String> paramMap = buildParameterMap(notification, event, noticeId);
                        String queueID = (notification.getNoticeQueue() != null ? notification.getNoticeQueue() : "default");

                        if (log().isDebugEnabled()) {
                            log().debug("destination : " + notification.getDestinationPath());
                            log().debug("text message: " + paramMap.get(NotificationManager.PARAM_TEXT_MSG));
                            log().debug("num message : " + paramMap.get(NotificationManager.PARAM_NUM_MSG));
                            log().debug("subject     : " + paramMap.get(NotificationManager.PARAM_SUBJECT));
                            log().debug("node        : " + paramMap.get(NotificationManager.PARAM_NODE));
                            log().debug("interface   : " + paramMap.get(NotificationManager.PARAM_INTERFACE));
                            log().debug("service     : " + paramMap.get(NotificationManager.PARAM_SERVICE));
                        }

                        // get the target and escalation information
                        Path path = null;
                        try {
                            path = getDestinationPathManager().getPath(notification.getDestinationPath());
                            if (path == null) {
                                log().warn("Unknown destination path " + notification.getDestinationPath() + ". Please check the <destinationPath> tag for the notification " + notification.getName() + " in the notifications.xml file.");

                                // changing posted by Wiktor Wodecki
                                // return;
                                continue;
                            }
                        } catch (Throwable e) {
                            log().error("Could not get destination path for " + notification.getDestinationPath() + ", please check the destinationPath.xml for errors.", e);
                            return;
                        }
                        String initialDelay = (path.getInitialDelay() == null ? "0s" : path.getInitialDelay());
                        Target[] targets = path.getTarget();
                        Escalate[] escalations = path.getEscalate();

                        // now check to see if any users are to receive the
                        // notification, if none then generate an event a exit
                        try {
                            if (getUserCount(targets, escalations) == 0) {
                                log().warn("The path " + notification.getDestinationPath() + " assigned to notification " + notification.getName() + " has no targets or escalations specified, not sending notice.");
                                sendNotifEvent(EventConstants.NOTIFICATION_WITHOUT_USERS, "The path " + notification.getDestinationPath() + " assigned to notification " + notification.getName() + " has no targets or escalations specified.", "The message of the notification is as follows: " + paramMap.get(NotificationManager.PARAM_TEXT_MSG));
                                return;
                            }
                        } catch (Throwable e) {
                            log().error("Failed to get count of users in destination path " + notification.getDestinationPath() + ", exiting notification.", e);
                            return;
                        }

                        try {
                            log().info(String.format("Inserting notification #%d into database: %s", noticeId, paramMap.get(NotificationManager.PARAM_SUBJECT)));
                            getNotificationManager().insertNotice(noticeId, paramMap, queueID, notification);
                        } catch (SQLException e) {
                            log().error("Failed to enter notification into database, exiting this notification", e);
                            return;
                        }

                        long startTime = System.currentTimeMillis() + TimeConverter.convertToMillis(initialDelay);
                        // Find the first outage which applies at this time

                        String scheduledOutageName = scheduledOutage(nodeid, ipaddr);
                        if (scheduledOutageName != null) {
                            // This event occurred during a scheduled outage.
                            // Must decide what to do
                            if (autoAckExistsForEvent(event.getUei())) {
                                // Defer starttime till the given outage ends -
                                // if the auto ack catches the other event
                                // before then,
                                // then the page will not be sent
                                Calendar endOfOutage = getPollOutagesConfigManager().getEndOfOutage(scheduledOutageName);
                                startTime = endOfOutage.getTime().getTime();
                            } else {
                                // No auto-ack exists - there's no point
                                // delaying the page, so just drop it (but leave
                                // the database entry)
                                continue; // with the next notification (for
                                            // loop)
                            }
                        }

                        List<NotificationTask> targetSiblings = new ArrayList<NotificationTask>();

                        try {
                            synchronized(m_noticeQueues) {
                                NoticeQueue noticeQueue = m_noticeQueues.get(queueID);
                                processTargets(targets, targetSiblings, noticeQueue, startTime, paramMap, noticeId);
                                processEscalations(escalations, targetSiblings, noticeQueue, startTime, paramMap, noticeId);
                            }
                        } catch (Throwable e) {
                            log().error("notice not scheduled due to error: ", e);
                        }

                    }
                } else {
                    if (log().isDebugEnabled()) {
                        log().debug("Event doesn't match a notice: " + event.getUei() + " : " + nodeid + " : " + ipaddr + " : " + event.getService());
                    }
                }
            }
        } else {
            if (log().isDebugEnabled()) {
                log().debug("No notice match for uei: " + event.getUei());
            }
        }
    }

    /**
     * Detemines the number of users assigned to a list of Target and Escalate
     * lists. Group names may be specified in these objects and the users will
     * have to be extracted from those groups
     * 
     * @param targets
     *            the list of Target objects
     * @param escalations
     *            the list of Escalate objects
     * @return the total # of users assigned in each Target and Escalate
     *         objecst.
     */
    private int getUserCount(Target[] targets, Escalate[] escalations) throws IOException, MarshalException, ValidationException {
        int totalUsers = 0;

        for (int i = 0; i < targets.length; i++) {
            totalUsers += getUsersInTarget(targets[i]);
        }

        for (int j = 0; j < escalations.length; j++) {
            Target[] escalationTargets = escalations[j].getTarget();
            for (int k = 0; k < escalationTargets.length; k++) {
                totalUsers += getUsersInTarget(escalationTargets[k]);
            }
        }

        return totalUsers;
    }

    /**
     * 
     */
    private int getUsersInTarget(Target target) throws IOException, MarshalException, ValidationException {
        int count = 0;
        String targetName = target.getName();

        if (getGroupManager().hasGroup(targetName)) {
            count = getGroupManager().getGroup(targetName).getUserCount();
        } else if (getUserManager().hasRole(targetName)) {
            count = getUserManager().countUsersWithRole(targetName);
        } else if (getUserManager().hasUser(targetName)) {
            count = 1;
        } else if (targetName.indexOf("@") > -1) {
            count = 1;
        }

        return count;
    }


    /**
     * Sends and event related to a notification
     * 
     * @param uei
     *            the UEI for the event
     */
    private void sendNotifEvent(String uei, String logMessage, String description) {
        try {
            
            EventBuilder bldr = new EventBuilder(uei, "notifd");
            bldr.setLogMessage(logMessage);
            bldr.setDescription(description);
            
            getEventManager().sendNow(bldr.getEvent());
        } catch (Throwable t) {
            log().error("Could not send event " + uei, t);
        }
    }

    /**
     * 
     */
    static Map<String, String> buildParameterMap(Notification notification, Event event, int noticeId) {
        Map<String, String> paramMap = new HashMap<String, String>();
        
        NotificationManager.addNotificationParams(paramMap, notification);
        
        // expand the event parameters for the messages
        // call the notif expansion method before the event expansion because
        // event expansion will
        // throw away any expansion strings it doesn't recognize!

        paramMap.put("noticeid", Integer.toString(noticeId));
        // Replace the %noticeid% param
        String textMessage = NotificationManager.expandNotifParms((nullSafeTextMsg(notification)), paramMap);
        String numericMessage = NotificationManager.expandNotifParms((nullSafeNumerMsg(notification, noticeId)), paramMap);
        String subjectLine = NotificationManager.expandNotifParms((nullSafeSubj(notification, noticeId)), paramMap);
        
        nullSafeExpandedPut(NotificationManager.PARAM_TEXT_MSG, textMessage, event, paramMap);
        nullSafeExpandedPut(NotificationManager.PARAM_NUM_MSG, numericMessage, event, paramMap);
        nullSafeExpandedPut(NotificationManager.PARAM_SUBJECT, subjectLine, event, paramMap);
        paramMap.put(NotificationManager.PARAM_NODE, event.hasNodeid() ? String.valueOf(event.getNodeid()) : "");
        paramMap.put(NotificationManager.PARAM_INTERFACE, event.getInterface());
        paramMap.put(NotificationManager.PARAM_SERVICE, event.getService());
        paramMap.put("eventID", String.valueOf(event.getDbid()));
        paramMap.put("eventUEI", event.getUei());

        NotificationManager.expandMapValues(paramMap, event);

        return Collections.unmodifiableMap(paramMap);
        
    }

    private static void nullSafeExpandedPut(final String key, final String value, final Event event, Map<String, String> paramMap) {
        String result = EventUtil.expandParms(value, event);
        paramMap.put(key, (result == null ? value : result));
    }

    private static String nullSafeSubj(Notification notification, int noticeId) {
        return notification.getSubject() != null ? notification.getSubject() : "Notice #" + noticeId;
    }

    private static String nullSafeNumerMsg(Notification notification, int noticeId) {
        return notification.getNumericMessage() != null ? notification.getNumericMessage() : "111-" + noticeId;
    }

    private static String nullSafeTextMsg(Notification notification) {
        return notification.getTextMessage() != null ? notification.getTextMessage() : "No text message supplied.";
    }

    /**
     * 
     */
    private void processTargets(Target[] targets, List<NotificationTask> targetSiblings, NoticeQueue noticeQueue, long startTime, Map<String, String> params, int noticeId) throws IOException, MarshalException, ValidationException {
        for (int i = 0; i < targets.length; i++) {
            String interval = (targets[i].getInterval() == null ? "0s" : targets[i].getInterval());

            String targetName = targets[i].getName();
            String autoNotify = targets[i].getAutoNotify();
            if(autoNotify != null) {
                if(autoNotify.equalsIgnoreCase("on")) {
                    autoNotify = "Y";
                } else if(autoNotify.equalsIgnoreCase("off")) {
                    autoNotify = "N";
                } else {
                    autoNotify = "C";
                }
            } else {
                autoNotify = "C";
            }
            if (log().isDebugEnabled()) {
                log().debug("Processing target " + targetName + ":" + interval);
            }
            
            NotificationTask[] tasks = null;
            
            if (getGroupManager().hasGroup((targetName))) {
                tasks = makeGroupTasks(startTime, params, noticeId, targetName, targets[i].getCommand(), targetSiblings, autoNotify, TimeConverter.convertToMillis(interval));
            } else if (getUserManager().hasRole(targetName)) {
                tasks = makeRoleTasks(startTime, params, noticeId, targetName, targets[i].getCommand(), targetSiblings, autoNotify, TimeConverter.convertToMillis(interval));
            } else if (getUserManager().hasUser(targetName)) {
                NotificationTask[] userTasks = { makeUserTask(startTime, params, noticeId, targetName, targets[i].getCommand(), targetSiblings, autoNotify) };
                tasks = userTasks;
            } else if (targetName.indexOf("@") > -1) {
            	// Bug 2027 -- get the command name from the Notifd config instead of using default of "email"
            	String[] emailCommands = { getNotifdConfigManager().getConfiguration().getEmailAddressCommand() };
                NotificationTask[] emailTasks = { makeEmailTask(startTime, params, noticeId, targetName, emailCommands, targetSiblings, autoNotify) };
                tasks = emailTasks;
            }
             
            if (tasks != null) {
                for (int index = 0; index < tasks.length; index++) {
                    NotificationTask task = tasks[index];
                    if (task != null) {
                        synchronized(noticeQueue) {
                            noticeQueue.putItem(task.getSendTime(), task);
                        }
                        targetSiblings.add(task);
                    }
                }
            } else {
                log().warn("Unrecognized target '" + targetName + "' contained in destinationPaths.xml. Please check the configuration.");
            }
        }
    }

    NotificationTask[] makeGroupTasks(long startTime, Map<String, String> params, int noticeId, String targetName, String[] command, List<NotificationTask> targetSiblings, String autoNotify, long interval) throws IOException, MarshalException, ValidationException {
        Group group = getGroupManager().getGroup(targetName);

        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startTime);
        long next = getGroupManager().groupNextOnDuty(group.getName(), startCal);
        
        // it the group is not on duty
        if (next < 0) {
            if (log().isDebugEnabled()) {
                log().debug("The group " + group.getName() + " is not scheduled to come back on duty. No notification will be sent to this group.");
            }
            return null;
        }

        if (log().isDebugEnabled()) {
            log().debug("The group " + group.getName() + " is on duty in " + next + " millisec.");
        }
        String[] users = group.getUser();
        
        // There are no users in the group
        if (users == null || users.length == 0) {
            if (log().isDebugEnabled()) {
                log().debug("Not sending notice, no users specified for group " + group.getName());
            }
            return null;
        }

        return constructTasksFromUserList(users, startTime, next, params, noticeId, command, targetSiblings, autoNotify, interval);
    }

    private NotificationTask[] constructTasksFromUserList(String[] users, long startTime, long offset, Map<String, String> params, int noticeId, String[] command, List<NotificationTask> targetSiblings, String autoNotify, long interval) throws IOException, MarshalException, ValidationException {
        List<NotificationTask> taskList = new ArrayList<NotificationTask>(users.length);
        long curSendTime = 0;
        for (int j = 0; j < users.length; j++) {
            NotificationTask newTask = makeUserTask(offset + startTime + curSendTime, params, noticeId, users[j], command, targetSiblings, autoNotify);

            if (newTask != null) {
                taskList.add(newTask);
                curSendTime += interval; 
            }
        }
        return taskList.toArray(new NotificationTask[taskList.size()]);
    }
    
    
    NotificationTask[] makeRoleTasks(long startTime, Map<String, String> params, int noticeId, String targetName, String[] command, List<NotificationTask> targetSiblings, String autoNotify, long interval) throws MarshalException, ValidationException, IOException {
        String[] users = getUserManager().getUsersScheduledForRole(targetName, new Date(startTime));
        
        // There are no users in the group
        if (users == null || users.length == 0) {
            if (log().isDebugEnabled()) {
                log().debug("Not sending notice, no users scheduled for role  " + targetName);
            }
            return null;
        }
        
        return constructTasksFromUserList(users, startTime, 0, params, noticeId, command, targetSiblings, autoNotify, interval);

       
    }


    /**
     * 
     */
    private void processEscalations(Escalate[] escalations, List<NotificationTask> targetSiblings, NoticeQueue noticeQueue, long startTime, Map<String, String> params, int noticeId) throws IOException, MarshalException, ValidationException {
        for (int i = 0; i < escalations.length; i++) {
            Target[] targets = escalations[i].getTarget();
            startTime += TimeConverter.convertToMillis(escalations[i].getDelay());
            processTargets(targets, targetSiblings, noticeQueue, startTime, params, noticeId);
        }
    }

    /**
     * 
     */
    NotificationTask makeUserTask(long sendTime, Map<String, String> parameters, int noticeId, String targetName, String[] commandList, List<NotificationTask> siblings, String autoNotify) throws IOException, MarshalException, ValidationException {
        NotificationTask task = null;

        task = new NotificationTask(getNotificationManager(), getUserManager(), sendTime, parameters, siblings, autoNotify);

        User user = getUserManager().getUser(targetName);

        Command commands[] = new Command[commandList.length];
        for (int i = 0; i < commandList.length; i++) {
            commands[i] = getNotificationCommandManager().getCommand(commandList[i]);
        }

        // if either piece of information is missing don't add the task to
        // the notifier
        if (user == null) {
            log().error("user " + targetName + " is not a valid user, not adding this user to escalation thread");
            return null;
        }

        task.setUser(user);
        task.setCommands(commands);
        task.setNoticeId(noticeId);
        task.setAutoNotify(autoNotify);

        return task;
    }

    /**
     * 
     */
    NotificationTask makeEmailTask(long sendTime, Map<String, String> parameters, int noticeId, String address, String[] commandList, List<NotificationTask> siblings, String autoNotify) throws IOException, MarshalException, ValidationException {
        NotificationTask task = null;

        task = new NotificationTask(getNotificationManager(), getUserManager(), sendTime, parameters, siblings, autoNotify);

        User user = new User();
        user.setUserId(address);
        Contact contact = new Contact();
        contact.setType("email");
        if (log().isDebugEnabled()) {
            log().debug("email address = " + address + ", using contact type " + contact.getType());
        }
        contact.setInfo(address);
        user.addContact(contact);

        Command commands[] = new Command[commandList.length];
        for (int i = 0; i < commandList.length; i++) {
            commands[i] = getNotificationCommandManager().getCommand(commandList[i]);
        }

        task.setUser(user);
        task.setCommands(commands);
        task.setNoticeId(noticeId);
        task.setAutoNotify(autoNotify);

        return task;
    }

    /**
     * Return an id for this event listener
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return "Notifd:BroadcastEventProcessor";
    }

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
        return getNotificationManager().rebuildParameterMap(notifId, resolutionPrefix, skipNumericPrefix);

    }

    /**
     * Checks the package information for the pollable service and determines if
     * any of the calendar outages associated with the package apply to the
     * current time and the service's interface. If an outage applies it's name
     * is returned...otherwise null is returned.
     *
     * @return null if no outage found (indicating a notification may be sent)
     *         or the outage name, if an applicable outage is found (indicating
     *         notification should not be sent).
     * @throws IOException if any.
     * @throws ValidationException if any.
     * @throws MarshalException if any.
     * @param nodeId a long.
     * @param theInterface a {@link java.lang.String} object.
     */
    public String scheduledOutage(long nodeId, String theInterface) {
        try {

            PollOutagesConfigManager outageFactory = getPollOutagesConfigManager();

            // Iterate over the outage names
            // For each outage...if the outage contains a calendar entry which
            // applies to the current time and the outage applies to this
            // interface then break and return true. Otherwise process the
            // next outage.
            //
            Collection<String> outageCalendarNames = getNotifdConfigManager().getOutageCalendarNames();
            for (String outageName : outageCalendarNames) {

                // Does the outage apply to the current time?
                if (outageFactory.isCurTimeInOutage(outageName)) {
                    // Does the outage apply to this interface or node?

                    if ((outageFactory.isNodeIdInOutage(nodeId, outageName)) || (outageFactory.isInterfaceInOutage(theInterface, outageName)) || (outageFactory.isInterfaceInOutage("match-any", outageName))) {
                        if (log().isDebugEnabled()) {
                            log().debug("scheduledOutage: configured outage '" + outageName + "' applies, notification for interface " + theInterface + " on node " + nodeId + " will not be sent");
                        }
                        return outageName;
                    }
                }
            }
        } catch (Throwable e) {
            log().error("Error determining current outages", e);
        }

        return null;
    }

    /**
     * This method is responsible for generating a pathOutage event and
     * sending it
     *
     * @param nodeEntry Entry of node which was rescanned
     */
    private void createPathOutageEvent(int nodeid, String nodeLabel, String intfc, String svc, boolean noticeSupressed) {
        if (log().isDebugEnabled()) {
            log().debug("nodeid = " + nodeid + ", nodeLabel = " + nodeLabel + ", noticeSupressed = " + noticeSupressed);
        }
        
        EventBuilder bldr = new EventBuilder(EventConstants.PATH_OUTAGE_EVENT_UEI, "OpenNMS.notifd");
        bldr.setNodeid(nodeid);
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeLabel == null ? "" : nodeLabel);
        bldr.addParam(EventConstants.PARM_CRITICAL_PATH_IP, intfc);
        bldr.addParam(EventConstants.PARM_CRITICAL_PATH_SVC, svc);
        bldr.addParam(EventConstants.PARM_CRITICAL_PATH_NOTICE_SUPRESSED, noticeSupressed);

        // Send the event
        if (log().isDebugEnabled()) {
            log().debug("Creating pathOutageEvent for nodeid: " + nodeid);
        }
        
	try {
            EventIpcManagerFactory.getIpcManager().sendNow(bldr.getEvent());
        } catch (Throwable t) {
            log().warn("run: unexpected throwable exception caught during event send", t);
        }
    }

    /**
     * <p>getDestinationPathManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.DestinationPathManager} object.
     */
    public DestinationPathManager getDestinationPathManager() {
        return m_destinationPathManager;
    }

    /**
     * <p>setDestinationPathManager</p>
     *
     * @param destinationPathManager a {@link org.opennms.netmgt.config.DestinationPathManager} object.
     */
    public void setDestinationPathManager(
            DestinationPathManager destinationPathManager) {
        m_destinationPathManager = destinationPathManager;
    }

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventManager;
    }

    /**
     * <p>setEventManager</p>
     *
     * @param eventManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }

    /**
     * <p>getGroupManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public GroupManager getGroupManager() {
        return m_groupManager;
    }

    /**
     * <p>setGroupManager</p>
     *
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public void setGroupManager(GroupManager groupManager) {
        m_groupManager = groupManager;
    }

    /**
     * <p>getNotifdConfigManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotifdConfigManager} object.
     */
    public NotifdConfigManager getNotifdConfigManager() {
        return m_notifdConfigManager;
    }

    /**
     * <p>setNotifdConfigManager</p>
     *
     * @param notifdConfigManager a {@link org.opennms.netmgt.config.NotifdConfigManager} object.
     */
    public void setNotifdConfigManager(NotifdConfigManager notifdConfigManager) {
        m_notifdConfigManager = notifdConfigManager;
    }

    /**
     * <p>getNotificationCommandManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotificationCommandManager} object.
     */
    public NotificationCommandManager getNotificationCommandManager() {
        return m_notificationCommandManager;
    }

    /**
     * <p>setNotificationCommandManager</p>
     *
     * @param notificationCommandManager a {@link org.opennms.netmgt.config.NotificationCommandManager} object.
     */
    public void setNotificationCommandManager(
            NotificationCommandManager notificationCommandManager) {
        m_notificationCommandManager = notificationCommandManager;
    }

    /**
     * <p>getNotificationManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotificationManager} object.
     */
    public NotificationManager getNotificationManager() {
        return m_notificationManager;
    }

    /**
     * <p>setNotificationManager</p>
     *
     * @param notificationManager a {@link org.opennms.netmgt.config.NotificationManager} object.
     */
    public void setNotificationManager(NotificationManager notificationManager) {
        m_notificationManager = notificationManager;
    }

    /**
     * <p>getPollOutagesConfigManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.PollOutagesConfigManager} object.
     */
    public PollOutagesConfigManager getPollOutagesConfigManager() {
        return m_pollOutagesConfigManager;
    }

    /**
     * <p>setPollOutagesConfigManager</p>
     *
     * @param pollOutagesConfigManager a {@link org.opennms.netmgt.config.PollOutagesConfigManager} object.
     */
    public void setPollOutagesConfigManager(
            PollOutagesConfigManager pollOutagesConfigManager) {
        m_pollOutagesConfigManager = pollOutagesConfigManager;
    }

    /**
     * <p>getUserManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.UserManager} object.
     */
    public UserManager getUserManager() {
        return m_userManager;
    }

    /**
     * <p>setUserManager</p>
     *
     * @param userManager a {@link org.opennms.netmgt.config.UserManager} object.
     */
    public void setUserManager(UserManager userManager) {
        m_userManager = userManager;
    }

    /**
     * <p>getNoticeQueues</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public synchronized Map<String, NoticeQueue> getNoticeQueues() {
        return m_noticeQueues;
    }

    /**
     * <p>setNoticeQueues</p>
     *
     * @param noticeQueues a {@link java.util.Map} object.
     */
    public void setNoticeQueues(Map<String, NoticeQueue> noticeQueues) {
        m_noticeQueues = noticeQueues;
    }

} // end class
