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
// 2003 Jan 31: Cleaned up some unused imports.
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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DbConnectionFactory;
import org.opennms.netmgt.config.DestinationPathManager;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.notifd.Queue;
import org.opennms.netmgt.eventd.EventIpcManager;

/**
 * This class is used to represent the notification execution service. When an
 * event is received by this service that has one of either a notification,
 * trouble ticket, or auto action then a process is launched to execute the
 * appropriate commands.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * 
 */
public final class Notifd implements PausableFiber {
    /**
     * Logging categoyr for log4j
     */
    private static String LOG4J_CATEGORY = "OpenNMS.Notifd";

    /**
     * The signlton instance.
     */
    private static final Notifd m_singleton = new Notifd();

    /**
     * The map for holding different notice queues
     */
    private Map m_noticeQueues;

    /**
     * 
     */
    private Map m_queueHandlers;

    /**
     * The broadcast event receiver.
     */
    private BroadcastEventProcessor m_eventReader;

    /**
     * The current status of this fiber
     */
    private int m_status;

    private EventIpcManager m_eventManager;

    private NotifdConfigManager m_configManager;

    private DbConnectionFactory m_dbConnectionFactory;

    private NotificationManager m_notificationManager;
    
    private GroupManager m_groupManager;

    private UserManager m_userManager;

    private DestinationPathManager m_destinationPathManager;

    private NotificationCommandManager m_notificationCommandManager;

    /**
     * Constructs a new Notifd service daemon.
     */
    Notifd() {
    }

    public synchronized void init() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        m_noticeQueues = new HashMap();
        m_queueHandlers = new HashMap();
        m_eventReader = null;
        try {

            ThreadCategory.getInstance(getClass()).info("Notification status = " + getConfigManager().getNotificationStatus());

            Queue queues[] = getConfigManager().getConfiguration().getQueue();
            for (int i = 0; i < queues.length; i++) {
                NoticeQueue curQueue = new NoticeQueue();

                Class handlerClass = Class.forName(queues[i].getHandlerClass().getName());
                NotifdQueueHandler handlerQueue = (NotifdQueueHandler) handlerClass.newInstance();

                handlerQueue.setQueueID(queues[i].getQueueId());
                handlerQueue.setNoticeQueue(curQueue);
                handlerQueue.setInterval(queues[i].getInterval());

                m_noticeQueues.put(queues[i].getQueueId(), curQueue);
                m_queueHandlers.put(queues[i].getQueueId(), handlerQueue);
            }
        } catch (Throwable t) {
            ThreadCategory.getInstance(getClass()).warn("start: Failed to load notifd queue handlers.", t);
        }

        // start the event reader
        //
        try {
            m_eventReader = new BroadcastEventProcessor(this, m_noticeQueues);
        } catch (Exception ex) {
            ThreadCategory.getInstance(getClass()).error("Failed to setup event receiver", ex);
            throw new UndeclaredThrowableException(ex);
        }
    }

    /**
     * @return
     */
    public NotifdConfigManager getConfigManager() {
        return m_configManager;
    }
    
    public void setConfigManager(NotifdConfigManager manager ) {
        m_configManager = manager;
    }
    
    public GroupManager getGroupManager() {
        return m_groupManager;
    }
    
    public void setGroupManager(GroupManager manager) {
        m_groupManager = manager;
    }
    
    public UserManager getUserManager() {
        return m_userManager;
    }
    
    public void setUserManager(UserManager manager) {
        m_userManager = manager;
    }
    
    public DestinationPathManager getDestinationPathManager() {
        return m_destinationPathManager;
    }
    
    public void setDestinationPathManager(DestinationPathManager manager) {
        m_destinationPathManager = manager;
    }
    
    public NotificationCommandManager getNotificationCommandManager() {
        return m_notificationCommandManager;
    }

    public void setNotificationCommandManager(NotificationCommandManager manager) {
        m_notificationCommandManager = manager;
    }
    
    public NotificationManager getNotificationManager() {
        return m_notificationManager;
    }
    
    public void setNotificationManager(NotificationManager notificationManager) {
        m_notificationManager = notificationManager;
    }

    /**
     * Starts the <em>Notifd</em> service. The process of starting the service
     * involves starting the queue handlers and starting an event receiver.
     */
    public synchronized void start() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        Iterator i = m_queueHandlers.keySet().iterator();
        while (i.hasNext()) {
            NotifdQueueHandler curHandler = (NotifdQueueHandler) m_queueHandlers.get(i.next());
            curHandler.start();
        }

        // create the control receiver
        m_status = RUNNING;
    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     * 
     */
    public synchronized void stop() {
        m_status = STOP_PENDING;

        try {
            Iterator i = m_queueHandlers.keySet().iterator();
            while (i.hasNext()) {
                NotifdQueueHandler curHandler = (NotifdQueueHandler) m_queueHandlers.get(i.next());
                curHandler.stop();
            }
        } catch (Exception e) {
        }

        if (m_eventReader != null)
            m_eventReader.close();

        m_eventReader = null;

        m_status = STOPPED;

    }

    /**
     * Returns the current status of the service.
     * 
     * @return The service's status.
     */
    public synchronized int getStatus() {
        return m_status;
    }

    /**
     * Returns the name of the service.
     * 
     * @return The service's name.
     */
    public String getName() {
        return "OpenNMS.Notifd";
    }

    /**
     * Pauses the service if its currently running
     */
    public synchronized void pause() {
        if (m_status != RUNNING)
            return;

        m_status = PAUSE_PENDING;

        Iterator i = m_queueHandlers.keySet().iterator();
        while (i.hasNext()) {
            NotifdQueueHandler curHandler = (NotifdQueueHandler) m_queueHandlers.get(i.next());
            curHandler.pause();
        }

        m_status = PAUSED;
    }

    /**
     * Resumes the service if its currently paused
     */
    public synchronized void resume() {
        if (m_status != PAUSED)
            return;

        m_status = RESUME_PENDING;

        Iterator i = m_queueHandlers.keySet().iterator();
        while (i.hasNext()) {
            NotifdQueueHandler curHandler = (NotifdQueueHandler) m_queueHandlers.get(i.next());
            curHandler.resume();
        }

        m_status = RUNNING;
    }

    /**
     * Returns the singular instance of the Notifd daemon. There can be only one
     * instance of this service per virtual machine.
     */
    public static Notifd getInstance() {
        return m_singleton;
    }

    /**
     * @return
     */
    public EventIpcManager getEventManager() {
        return m_eventManager;
    }
    /**
     * @param eventManager The eventManager to set.
     */
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }

    /**
     * @param dbConnectionFactory
     */
    public void setDbConnectionFactory(DbConnectionFactory dbConnectionFactory) {
        m_dbConnectionFactory = dbConnectionFactory;
        
    }

}
