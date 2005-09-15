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
// 2004 Nov 22: Fixed problem with notifications for threshold events on non-IP interfaces.
// 2004 Aug 26: Added the ability to trigger notifications on an event and a parameter.
// 2003 Sep 30: Added a change to support SNMP Thresholding notices.
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Nov 09: Added code to allow an event to match more than one notice.
// 2002 Oct 29: Added the ability to include event files in eventconf.xml.
// 2002 Oct 24: Option to auto-acknowledge "up" notifications as well as "down". Bug #544.
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
// Tab Size = 8
//

package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.utils.TimeConverter;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.PollOutagesConfigManager;
import org.opennms.netmgt.config.destinationPaths.Escalate;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.notifd.AutoAcknowledge;
import org.opennms.netmgt.config.notificationCommands.Command;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Parameter;
import org.opennms.netmgt.config.users.Contact;
import org.opennms.netmgt.config.users.User;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.utils.RowProcessor;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;

/**
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class BroadcastEventProcessor implements EventListener {
    /**
     */
    private Map m_noticeQueues;

    /**
     * A regular expression for matching an expansion parameter delimited by
     * percent signs.
     */
    private static final String NOTIFD_EXPANSION_PARM = "%(noticeid)%";

    private static RE notifdExpandRE;

    private Notifd m_notifd;

    /**
     * Initializes the expansion regular expression. The exception is going to
     * be thrown away if the RE can't be compiled, thus the complilation should
     * be tested prior to runtime.
     */
    static {
        try {
            notifdExpandRE = new RE(NOTIFD_EXPANSION_PARM);
        } catch (RESyntaxException e) {
            // this shouldn't throw an exception, should be tested prior to
            // runtime
            ThreadCategory.getInstance(BroadcastEventProcessor.class).error("failed to compile RE " + NOTIFD_EXPANSION_PARM, e);
        }
    }

    /**
     * This constructor is called to initilize the event receiver. A connection
     * to the message server is opened and this instance is setup as the
     * endpoint for broadcast events. When a new event arrives it is processed
     * and the appropriate action is taken.
     * 
     */
    BroadcastEventProcessor(Notifd notifd, Map noticeQueues) {
        // set up the exectuable queue first
        //
        m_notifd = notifd;
        m_noticeQueues = noticeQueues;

        // start to listen for events
        getEventManager().addEventListener(this);
    }

    /**
     * Unsubscribe from eventd
     */
    public void close() {
        getEventManager().removeEventListener(this);
    }

    /**
     * @return
     */
    private EventIpcManager getEventManager() {
        return m_notifd.getEventManager();
    }

    /**
     * This method is invoked by the EventIpcManager when a new event is
     * available for processing.
     * 
     * @param event
     *            The event .
     */
    public void onEvent(Event event) {
        if (event == null)
            return;

        String status = "off";
        try {
            status = getConfigManager().getNotificationStatus();
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).error("error getting notifd status, assuming status = 'off' for now: ", e);
        }

        if (status.equals("on")) {
            scheduleNoticesForEvent(event);
        } else {
            Category log = ThreadCategory.getInstance(getClass());
            if (log.isDebugEnabled())
                log.debug("discarding event " + event.getUei() + ", notifd status = " + status);
        }

        automaticAcknowledge(event);

    } // end onEvent()

    /**
     * 
     */
    private void automaticAcknowledge(Event event) {
        try {
            Collection ueis = getConfigManager().getConfiguration().getAutoAcknowledgeCollection();

            // see if this event has an auto acknowledge for a notice
            Iterator i = ueis.iterator();
            while (i.hasNext()) {
                AutoAcknowledge curAck = (AutoAcknowledge) i.next();
                if (curAck.getUei().equals(event.getUei())) {
                    try {
                        ThreadCategory.getInstance(getClass()).debug("Acknowledging event " + curAck.getAcknowledge() + " " + event.getNodeid() + ":" + event.getInterface() + ":" + event.getService());
                        Collection notifIDs = getNotificationManager().acknowledgeNotice(event, curAck.getAcknowledge(), curAck.getMatch());
                        try {
                            if (curAck.getNotify()) {
                                sendResolvedNotifications(notifIDs, event, curAck.getAcknowledge(), curAck.getMatch(), curAck.getResolutionPrefix());
                            }
                        } catch (Exception e) {
                            ThreadCategory.getInstance(getClass()).error("Failed to send resolution notifications.", e);
                        }
                    } catch (SQLException e) {
                        ThreadCategory.getInstance(getClass()).error("Failed to auto acknowledge notice.", e);
                    }
                }

            }
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).error("Unable to auto acknowledge notice due to exception.", e);
        }
    }

    private void sendResolvedNotifications(Collection notifIDs, Event event, String acknowledge, String[] match, String resolutionPrefix) throws Exception {
        Category log = ThreadCategory.getInstance(getClass());
        for (Iterator it = notifIDs.iterator(); it.hasNext();) {
            int notifId = ((Integer) it.next()).intValue();
            boolean wa = false;
            if(notifId < 0) {
                notifId *= -1;
                wa = true;
                log.debug("Conditional autoNotify for notifId " + notifId);
            }
            final boolean wasAcked = wa;
            final Map parmMap = rebuildParameterMap(notifId, resolutionPrefix);

            String queueID = getNotificationManager().getQueueForNotification(notifId);

            final Map userNotifitcations = new HashMap();
            RowProcessor acknowledgeNotification = new RowProcessor() {
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
                        List cmdList = (List) userNotifitcations.get(userID);
                        if (cmdList == null) {
                            cmdList = new ArrayList();
                            userNotifitcations.put(userID, cmdList);
                        }
                        cmdList.add(cmd);
                    }
                }
            };
            getNotificationManager().forEachUserNotification(notifId, acknowledgeNotification);

            for (Iterator userIt = userNotifitcations.keySet().iterator(); userIt.hasNext();) {
                String userID = (String) userIt.next();
                List cmdList = (List) userNotifitcations.get(userID);
                String[] cmds = (String[]) cmdList.toArray(new String[cmdList.size()]);
                log.debug("Sending " + resolutionPrefix + " notification to userID = " + userID + " for notice ID " + notifId);
                sendResolvedNotificationsToUser(queueID, userID, cmds, parmMap);
            }

        }
    }

    protected void sendResolvedNotificationsToUser(String queueID, String targetName, String[] commands, Map params) throws Exception {
        int noticeId = -1;
        NoticeQueue noticeQueue = (NoticeQueue) m_noticeQueues.get(queueID);
        long now = System.currentTimeMillis();

        if (m_notifd.getUserManager().hasUser(targetName)) {
            NotificationTask newTask = makeUserTask(now, params, noticeId, targetName, commands, null, null);

            if (newTask != null) {
                noticeQueue.put(new Long(now), newTask);
            }
        } else if (targetName.indexOf("@") > -1) {
            NotificationTask newTask = makeEmailTask(now, params, noticeId, targetName, commands, null, null);

            if (newTask != null) {
                noticeQueue.put(new Long(now), newTask);
            }
        } else {
            Category log = ThreadCategory.getInstance(getClass());
            log.warn("Unrecognized target '" + targetName + "' contained in destinationPaths.xml. Please check the configuration.");
        }

    }

    /**
     * @return
     */
    private NotificationManager getNotificationManager() {
        return m_notifd.getNotificationManager();
    }

    /**
     * @return
     */
    private NotifdConfigManager getConfigManager() {
        return m_notifd.getConfigManager();
    }

    /**
     * This method determines if the notice should continue based on the status
     * of the notify
     */
    private boolean continueWithNotice(Event event) {
        String nodeID = String.valueOf(event.getNodeid());
        String ipAddr = event.getInterface();
        String service = event.getService();

        boolean continueNotice = false;

        // can't check the database if any of these are null, so let the notice
        // continue
        if (nodeID == null || ipAddr == null || service == null || ipAddr.equals("0.0.0.0")) {
            ThreadCategory.getInstance(getClass()).debug("nodeID=" + nodeID + " ipAddr=" + ipAddr + " service=" + service + ". Not checking DB, allowing notice to continue.");
            return true;
        }

        try {
            // check the database to see if notices were turned off for this
            // service
            String notify = getNotificationManager().getServiceNoticeStatus(nodeID, ipAddr, service);
            if ("Y".equals(notify)) {
                continueNotice = true;
                ThreadCategory.getInstance(getClass()).debug("notify status for service " + service + " on interface/node " + ipAddr + "/" + nodeID + " is 'Y', continuing...");
            } else {
                ThreadCategory.getInstance(getClass()).debug("notify status for service " + service + " on interface/node " + ipAddr + "/" + nodeID + " is " + notify + ", not continuing...");
            }
        } catch (Exception e) {
            continueNotice = true;
            ThreadCategory.getInstance(getClass()).error("Not able to get notify status for service " + service + " on interface/node " + ipAddr + "/" + nodeID + ". Continuing notice... " + e.getMessage());
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
            Collection ueis = getConfigManager().getConfiguration().getAutoAcknowledgeCollection();
            Iterator i = ueis.iterator();
            while (i.hasNext()) {
                AutoAcknowledge curAck = (AutoAcknowledge) i.next();
                if (curAck.getAcknowledge().equals(eventUei)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).error("Unable to find if an auto acknowledge exists for event " + eventUei + " due to exception.", e);
            return false;
        }
    }

    /**
     */
    private void scheduleNoticesForEvent(Event event) {

        Category log = ThreadCategory.getInstance(getClass());
        boolean mapsToNotice = false;

        try {
            mapsToNotice = getNotificationManager().hasUei(event.getUei());
        } catch (Exception e) {
            log.error("Couldn't map uei " + event.getUei() + " to a notification entry, not scheduling notice.", e);
            return;
        }

        if (mapsToNotice) {
            // check to see if notices are turned on for the interface/service
            // in the event
            if (continueWithNotice(event)) {
                Notification[] notifications = null;

                try {
                    notifications = getNotificationManager().getNotifForEvent(event);
                } catch (Exception e) {
                    log.error("Couldn't get notification mapping for event " + event.getUei() + ", not scheduling notice.", e);
                    return;
                }

                long nodeid = event.getNodeid();
                String ipaddr = event.getInterface();
                if (notifications != null) {
                    for (int i = 0; i < notifications.length; i++) {

                        Notification notification = notifications[i];
                        int noticeId = 0;

                        try {
                            noticeId = getNotificationManager().getNoticeId();
                        } catch (Exception e) {
                            log.error("Failed to get a unique id # for notification, exiting this notification", e);
                            continue;
                        }

                        Map paramMap = buildParameterMap(notification, event, noticeId);
                        String queueID = (notification.getNoticeQueue() != null ? notification.getNoticeQueue() : "default");

                        log.debug("destination : " + notification.getDestinationPath());
                        log.debug("text message: " + (String) paramMap.get(NotificationManager.PARAM_TEXT_MSG));
                        log.debug("num message : " + (String) paramMap.get(NotificationManager.PARAM_NUM_MSG));
                        log.debug("subject     : " + (String) paramMap.get(NotificationManager.PARAM_SUBJECT));
                        log.debug("node        : " + (String) paramMap.get(NotificationManager.PARAM_NODE));
                        log.debug("interface   : " + (String) paramMap.get(NotificationManager.PARAM_INTERFACE));
                        log.debug("service     : " + (String) paramMap.get(NotificationManager.PARAM_SERVICE));

                        // get the target and escalation information
                        Path path = null;
                        try {
                            path = m_notifd.getDestinationPathManager().getPath(notification.getDestinationPath());
                            if (path == null) {
                                log.warn("Unknown destination path " + notification.getDestinationPath() + ". Please check the <destinationPath> tag for the notification " + notification.getName() + " in the notifications.xml file.");

                                // changing posted by Wiktor Wodecki
                                // return;
                                continue;
                            }
                        } catch (Exception e) {
                            log.error("Could not get destination path for " + notification.getDestinationPath() + ", please check the destinationPath.xml for errors.", e);
                            return;
                        }
                        String initialDelay = (path.getInitialDelay() == null ? "0s" : path.getInitialDelay());
                        Target[] targets = path.getTarget();
                        Escalate[] escalations = path.getEscalate();

                        // now check to see if any users are to receive the
                        // notification, if none then generate an event a exit
                        try {
                            if (getUserCount(targets, escalations) == 0) {
                                log.warn("The path " + notification.getDestinationPath() + " assigned to notification " + notification.getName() + " has no targets or escalations specified, not sending notice.");
                                sendNotifEvent(EventConstants.NOTIFICATION_WITHOUT_USERS, "The path " + notification.getDestinationPath() + " assigned to notification " + notification.getName() + " has no targets or escalations specified.", "The message of the notification is as follows: " + (String) paramMap.get(NotificationManager.PARAM_TEXT_MSG));
                                return;
                            }
                        } catch (Exception e) {
                            log.error("Failed to get count of users in destination path " + notification.getDestinationPath() + ", exiting notification.", e);
                            return;
                        }

                        try {
                            getNotificationManager().insertNotice(noticeId, paramMap, queueID);
                        } catch (SQLException e) {
                            log.error("Failed to enter notification into database, exiting this notification", e);
                            return;
                        }

                        long startTime = System.currentTimeMillis() + TimeConverter.convertToMillis(initialDelay);
                        // Find the first outage which applies at this time

                        String scheduledOutageName = scheduledOutage(nodeid, ipaddr);
                        if (scheduledOutageName != null) {
                            // This event occured during a scheduled outage.
                            // Must decide what to do
                            if (autoAckExistsForEvent(event.getUei())) {
                                // Defer starttime till the given outage ends -
                                // if the auto ack catches the other event
                                // before then,
                                // then the page will not be sent
                                Calendar endOfOutage = m_notifd.getPollOutagesConfigManager().getEndOfOutage(scheduledOutageName);
                                startTime = endOfOutage.getTime().getTime();
                            } else {
                                // No auto-ack exists - there's no point
                                // delaying the page, so just drop it (but leave
                                // the database entry)
                                continue; // with the next notification (for
                                            // loop)
                            }
                        }

                        List targetSiblings = new ArrayList();

                        try {
                            NoticeQueue noticeQueue = (NoticeQueue) m_noticeQueues.get(queueID);
                            processTargets(targets, targetSiblings, noticeQueue, startTime, paramMap, noticeId);
                            processEscalations(escalations, targetSiblings, noticeQueue, startTime, paramMap, noticeId);
                        } catch (Exception e) {
                            log.error("notice not scheduled due to error: ", e);
                        }

                    }
                } else {
                    log.debug("Event doesn't match a notice: " + event.getUei() + " : " + nodeid + " : " + ipaddr + " : " + event.getService());
                }
            }
        } else {
            log.debug("No notice match for uei: " + event.getUei());
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

        if (m_notifd.getGroupManager().hasGroup(targetName)) {
            count = m_notifd.getGroupManager().getGroup(targetName).getUserCount();
        } else if (m_notifd.getUserManager().hasRole(targetName)) {
            count = m_notifd.getUserManager().countUsersWithRole(targetName);
        } else if (m_notifd.getUserManager().hasUser(targetName)) {
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
            Logmsg logMsg = new Logmsg();
            logMsg.setContent(logMessage);

            Event event = new Event();
            event.setUei(uei);
            event.setSource("notifd");
            event.setLogmsg(logMsg);
            event.setDescr(description);
            event.setTime(EventConstants.formatToString(new java.util.Date()));

            getEventManager().sendNow(event);
        } catch (Throwable t) {
            ThreadCategory.getInstance(getClass()).error("Could not send event " + uei, t);
        }
    }

    /**
     * 
     */
    Map buildParameterMap(Notification notification, Event event, int noticeId) {
        Map paramMap = new HashMap();
        Parameter[] parameters = notification.getParameter();
        for (int i = 0; i < parameters.length; i++) {
            paramMap.put(parameters[i].getName(), parameters[i].getValue());
        }
        
//        recursivelyExpandMapValues(paramMap);

        // expand the event parameters for the messages
        String text = (notification.getTextMessage() != null ? notification.getTextMessage() : "No text message supplied.");
        String numeric = (notification.getNumericMessage() != null ? notification.getNumericMessage() : "111-" + noticeId);
        String subject = (notification.getSubject() != null ? notification.getSubject() : "Notice #" + noticeId);

        paramMap.put("noticeid", Integer.toString(noticeId));
        paramMap.put(NotificationManager.PARAM_NODE, String.valueOf(event.getNodeid()));
        paramMap.put(NotificationManager.PARAM_INTERFACE, event.getInterface());
        paramMap.put(NotificationManager.PARAM_SERVICE, event.getService());
        paramMap.put("eventID", String.valueOf(event.getDbid()));
        paramMap.put("eventUEI", event.getUei());

        // call the notid expansion method before the event expansion because
        // event expansion will
        // throw away any expanion strings it doesn't recognize!
        String textMessage = expandNotifParms(text, paramMap);
        String numericMessage = expandNotifParms(numeric, paramMap);
        String subjectLine = expandNotifParms(subject, paramMap);

        String finalTextMessage = EventUtil.expandParms(textMessage, event);
        if (finalTextMessage == null)
            paramMap.put(NotificationManager.PARAM_TEXT_MSG, textMessage);
        else
            paramMap.put(NotificationManager.PARAM_TEXT_MSG, finalTextMessage);

        String finalNumericMessage = EventUtil.expandParms(numericMessage, event);
        if (finalNumericMessage == null)
            paramMap.put(NotificationManager.PARAM_NUM_MSG, numericMessage);
        else
            paramMap.put(NotificationManager.PARAM_NUM_MSG, finalNumericMessage);

        String finalSubjectLine = EventUtil.expandParms(subjectLine, event);
        if (finalSubjectLine == null)
            paramMap.put(NotificationManager.PARAM_SUBJECT, subjectLine);
        else
            paramMap.put(NotificationManager.PARAM_SUBJECT, finalSubjectLine);
        
        expandMapValues(paramMap, event);

        return paramMap;
        
    }

    /**
     * A parameter expansion algorithm, designed to replace strings delimited by
     * percent signs '%' with a value supplied by a Map object.
     * 
     * @param inp
     *            the input string
     * @param paramMap
     *            a map that will supply the substitution values
     */
    public static String expandNotifParms(String inp, Map paramMap) {
        String expanded = new String(inp);

        if (notifdExpandRE.match(expanded)) {
            String replace = (String) paramMap.get(notifdExpandRE.getParen(1));
            if (replace != null) {
                expanded = notifdExpandRE.subst(expanded, replace);
            }
        }

        return expanded;
    }

    private static void expandMapValues(Map map, Event event) {
        Set keySet = map.keySet();

        for (Iterator it = keySet.iterator(); it.hasNext();) {
            String key = (String) it.next();
            String mapValue = (String)map.get(key);
            if (mapValue == null) {
                continue;
            }
            String expandedValue = EventUtil.expandParms((String)map.get(key), event);
            map.put(key, (expandedValue != null ? expandedValue : map.get(key)));
        }
        
    }
    
    /**
     * 
     */
    private void processTargets(Target[] targets, List targetSiblings, NoticeQueue noticeQueue, long startTime, Map params, int noticeId) throws IOException, MarshalException, ValidationException {
        Category log = ThreadCategory.getInstance(getClass());
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
            log.debug("Processing target " + targetName + ":" + interval);
            
		    NotificationTask[] tasks = null;
            


            if (m_notifd.getGroupManager().hasGroup((targetName))) {
                
                tasks = makeGroupTasks(startTime, params, noticeId, targetName, targets[i].getCommand(), targetSiblings, autoNotify, TimeConverter.convertToMillis(interval));
                
            } else if (m_notifd.getUserManager().hasRole(targetName)) {
                
                tasks = makeRoleTasks(startTime, params, noticeId, targetName, targets[i].getCommand(), targetSiblings, autoNotify, TimeConverter.convertToMillis(interval));
                
            } else if (m_notifd.getUserManager().hasUser(targetName)) {
                
                NotificationTask[] userTasks = { makeUserTask(startTime, params, noticeId, targetName, targets[i].getCommand(), targetSiblings, autoNotify) };
                tasks = userTasks;
            } else if (targetName.indexOf("@") > -1) {
                
                NotificationTask[] emailTasks = { makeEmailTask(startTime, params, noticeId, targetName, targets[i].getCommand(), targetSiblings, autoNotify) };
                tasks = emailTasks;
            }
             
            if (tasks != null) {

                for (int index = 0; index < tasks.length; index++) {
                    NotificationTask task = tasks[index];
                    if (task != null) {
                        noticeQueue.put(task.getSendTime(), task);
                        targetSiblings.add(task);
                    }
                }
                
            } else {
                log.warn("Unrecognized target '" + targetName + "' contained in destinationPaths.xml. Please check the configuration.");
            }
            
        }
    }

    NotificationTask[] makeGroupTasks(long startTime, Map params, int noticeId, String targetName, String[] command, List targetSiblings, String autoNotify, long interval) throws IOException, MarshalException, ValidationException {
        Category log = ThreadCategory.getInstance(getClass());
        Group group = m_notifd.getGroupManager().getGroup(targetName);

        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startTime);
        long next = m_notifd.getGroupManager().groupNextOnDuty(group.getName(), startCal);
        
        // it the group is not on duty
        if (next < 0) {
            log.debug("The group " + group.getName() + " is not scheduled to come back on duty. No notification will be sent to this group.");
            return null;
        }

        log.debug("The group " + group.getName() + " is on duty in " + next + " millisec.");
        String[] users = group.getUser();
        
        // There are no users in the group
        if (users == null || users.length == 0) {
            log.debug("Not sending notice, no users specified for group " + group.getName());
            return null;
        }

        return constructTasksFromUserList(users, startTime, next, params, noticeId, command, targetSiblings, autoNotify, interval);
    }

    private NotificationTask[] constructTasksFromUserList(String[] users, long startTime, long offset, Map params, int noticeId, String[] command, List targetSiblings, String autoNotify, long interval) throws IOException, MarshalException, ValidationException {
        List taskList = new ArrayList(users.length);
        long curSendTime = 0;
        for (int j = 0; j < users.length; j++) {
            NotificationTask newTask = makeUserTask(offset + startTime + curSendTime, params, noticeId, users[j], command, targetSiblings, autoNotify);

            if (newTask != null) {
                taskList.add(newTask);
                curSendTime += interval; 
            }
        }
        return (NotificationTask[])taskList.toArray(new NotificationTask[taskList.size()]);
    }
    
    
    NotificationTask[] makeRoleTasks(long startTime, Map params, int noticeId, String targetName, String[] command, List targetSiblings, String autoNotify, long interval) throws MarshalException, ValidationException, IOException {
        Category log = ThreadCategory.getInstance(getClass());

        String[] users = m_notifd.getUserManager().getUsersScheduledForRole(targetName, new Date(startTime));
        
        // There are no users in the group
        if (users == null || users.length == 0) {
            log.debug("Not sending notice, no users scheduled for role  " + targetName);
            return null;
        }
        
        return constructTasksFromUserList(users, startTime, 0, params, noticeId, command, targetSiblings, autoNotify, interval);

       
    }


    /**
     * 
     */
    private void processEscalations(Escalate[] escalations, List targetSiblings, NoticeQueue noticeQueue, long startTime, Map params, int noticeId) throws IOException, MarshalException, ValidationException {
        for (int i = 0; i < escalations.length; i++) {
            Target[] targets = escalations[i].getTarget();
            startTime += TimeConverter.convertToMillis(escalations[i].getDelay());
            processTargets(targets, targetSiblings, noticeQueue, startTime, params, noticeId);
        }
    }

    /**
     * 
     */
    NotificationTask makeUserTask(long sendTime, Map parameters, int noticeId, String targetName, String[] commandList, List siblings, String autoNotify) throws IOException, MarshalException, ValidationException {
        NotificationTask task = null;

        try {
            task = new NotificationTask(m_notifd, sendTime, parameters, siblings, autoNotify);

            User user = m_notifd.getUserManager().getUser(targetName);

            Command commands[] = new Command[commandList.length];
            for (int i = 0; i < commandList.length; i++) {
                commands[i] = m_notifd.getNotificationCommandManager().getCommand(commandList[i]);
            }

            // if either piece of information is missing don't add the task to
            // the notifier
            if (user == null) {
                ThreadCategory.getInstance(getClass()).error("user " + targetName + " is not a valid user, not adding this user to escalation thread");
                return null;
            }

            task.setUser(user);
            task.setCommands(commands);
            task.setNoticeId(noticeId);
            task.setAutoNotify(autoNotify);
        } catch (SQLException e) {
            ThreadCategory.getInstance(getClass()).error("Couldn't create user notification task", e);
        }

        return task;
    }

    /**
     * 
     */
    NotificationTask makeEmailTask(long sendTime, Map parameters, int noticeId, String address, String[] commandList, List siblings, String autoNotify) throws IOException, MarshalException, ValidationException {
        NotificationTask task = null;

        try {
            task = new NotificationTask(m_notifd, sendTime, parameters, siblings, autoNotify);

            User user = new User();
            user.setUserId(address);
            Contact contact = new Contact();
            contact.setType("email");
            // contact.setType("javaEmail");
            ThreadCategory.getInstance(getClass()).debug("email address = " + address);
            contact.setInfo(address);
            user.addContact(contact);

            Command commands[] = new Command[commandList.length];
            for (int i = 0; i < commandList.length; i++) {
                commands[i] = m_notifd.getNotificationCommandManager().getCommand(commandList[i]);
            }

            task.setUser(user);
            task.setCommands(commands);
            task.setNoticeId(noticeId);
            task.setAutoNotify(autoNotify);
        } catch (SQLException e) {
            ThreadCategory.getInstance(getClass()).error("Couldn't create email notification task", e);
        }

        return task;
    }

    /**
     * Return an id for this event listener
     */
    public String getName() {
        return "Notifd:BroadcastEventProcessor";
    }

    /**
     * @param i
     * @return
     */
    public Map rebuildParameterMap(int notifId, String resolutionPrefix) throws Exception {
        return getNotificationManager().rebuildParamterMap(notifId, resolutionPrefix);

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
     * @throws IOException
     * @throws ValidationException
     * @throws MarshalException
     */
    public String scheduledOutage(long nodeId, String theInterface) {
        Category log = ThreadCategory.getInstance(getClass());
        try {

            PollOutagesConfigManager outageFactory = m_notifd.getPollOutagesConfigManager();

            // Iterate over the outage names
            // For each outage...if the outage contains a calendar entry which
            // applies to the current time and the outage applies to this
            // interface then break and return true. Otherwise process the
            // next outage.
            //
            Iterator iter = getConfigManager().getConfiguration().getOutageCalendarCollection().iterator();
            while (iter.hasNext()) {
                String outageName = (String) iter.next();

                // Does the outage apply to the current time?
                if (outageFactory.isCurTimeInOutage(outageName)) {
                    // Does the outage apply to this interface or node?

                    if ((outageFactory.isNodeIdInOutage(nodeId, outageName)) || (outageFactory.isInterfaceInOutage(theInterface, outageName)) || (outageFactory.isInterfaceInOutage("match-any", outageName))) {
                        if (log.isDebugEnabled())
                            log.debug("scheduledOutage: configured outage '" + outageName + "' applies, notification for interface " + theInterface + " on node " + nodeId + " will not be sent");
                        return outageName;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error determining current outages", e);
        }

        return null;
    }

} // end class
