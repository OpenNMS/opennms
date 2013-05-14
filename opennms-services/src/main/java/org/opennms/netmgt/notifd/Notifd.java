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

package org.opennms.netmgt.notifd;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.DestinationPathManager;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationCommandManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.PollOutagesConfigManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.notifd.Queue;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.events.EventIpcManager;

/**
 * This class is used to represent the notification execution service. When an
 * event is received by this service that has one of either a notification,
 * trouble ticket, or auto action then a process is launched to execute the
 * appropriate commands.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * @version $Id: $
 */
public final class Notifd extends AbstractServiceDaemon {

    /**
     * The singleton instance.
     */
    private static final Notifd m_singleton = new Notifd();

    /**
     * The map for holding different notice queues
     */
    private final Map<String, NoticeQueue> m_noticeQueues = new HashMap<String, NoticeQueue>();

    /**
     * 
     */
    private final Map<String, NotifdQueueHandler> m_queueHandlers = new HashMap<String, NotifdQueueHandler>();

    /**
     * The broadcast event receiver.
     */
    private volatile BroadcastEventProcessor m_eventReader;

    // Would be better if these were final but the are initialized in setters 
    private volatile EventIpcManager m_eventManager;

    private volatile NotifdConfigManager m_configManager;

    private volatile NotificationManager m_notificationManager;
    
    private volatile GroupManager m_groupManager;

    private volatile UserManager m_userManager;

    private volatile DestinationPathManager m_destinationPathManager;

    private volatile NotificationCommandManager m_notificationCommandManager;

    private volatile PollOutagesConfigManager m_pollOutagesConfigManager;
    
    private volatile NodeDao m_nodeDao;

    /**
     * Constructs a new Notifd service daemon.
     */
    protected Notifd() {
    	super("OpenNMS.Notifd");
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        
        m_eventReader = new BroadcastEventProcessor();

        try {
            log().info("Notification status = " + getConfigManager().getNotificationStatus());

            Queue queues[] = getConfigManager().getConfiguration().getQueue();
            for (Queue queue : queues) {
                NoticeQueue curQueue = new NoticeQueue();

                Class<?> handlerClass = Class.forName(queue.getHandlerClass().getName());
                NotifdQueueHandler handlerQueue = (NotifdQueueHandler) handlerClass.newInstance();

                handlerQueue.setQueueID(queue.getQueueId());
                handlerQueue.setNoticeQueue(curQueue);
                handlerQueue.setInterval(queue.getInterval());

                m_noticeQueues.put(queue.getQueueId(), curQueue);
                m_queueHandlers.put(queue.getQueueId(), handlerQueue);
            }
        } catch (Throwable t) {
            log().error("start: Failed to load notifd queue handlers.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        m_eventReader.setDestinationPathManager(getDestinationPathManager());
        m_eventReader.setEventManager(getEventManager());
        m_eventReader.setGroupManager(getGroupManager());
        m_eventReader.setNoticeQueues(m_noticeQueues);
        m_eventReader.setNotifdConfigManager(getConfigManager());
        m_eventReader.setNotificationCommandManager(getNotificationCommandManager());
        m_eventReader.setNotificationManager(getNotificationManager());
        m_eventReader.setPollOutagesConfigManager(getPollOutagesConfigManager());
        m_eventReader.setUserManager(getUserManager());

        // start the event reader
        try {
            m_eventReader.init();
        } catch (Throwable e) {
            log().error("Failed to setup event receiver", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * <p>getConfigManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotifdConfigManager} object.
     */
    public NotifdConfigManager getConfigManager() {
        return m_configManager;
    }
    
    /**
     * <p>setConfigManager</p>
     *
     * @param manager a {@link org.opennms.netmgt.config.NotifdConfigManager} object.
     */
    public void setConfigManager(NotifdConfigManager manager ) {
        m_configManager = manager;
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
     * @param manager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public void setGroupManager(GroupManager manager) {
        m_groupManager = manager;
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
     * @param manager a {@link org.opennms.netmgt.config.UserManager} object.
     */
    public void setUserManager(UserManager manager) {
        m_userManager = manager;
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
     * @param manager a {@link org.opennms.netmgt.config.DestinationPathManager} object.
     */
    public void setDestinationPathManager(DestinationPathManager manager) {
        m_destinationPathManager = manager;
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
     * @param manager a {@link org.opennms.netmgt.config.NotificationCommandManager} object.
     */
    public void setNotificationCommandManager(NotificationCommandManager manager) {
        m_notificationCommandManager = manager;
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
     * <p>getBroadcastEventProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.notifd.BroadcastEventProcessor} object.
     */
    public BroadcastEventProcessor getBroadcastEventProcessor() {
        return m_eventReader;
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        for (NotifdQueueHandler curHandler : m_queueHandlers.values()) {
            curHandler.start();
        }
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        try {
            for (NotifdQueueHandler curHandler : m_queueHandlers.values()) {
                curHandler.stop();
            }
        } catch (Throwable e) {
        }

        if (m_eventReader != null) {
            m_eventReader.close();
        }

        m_eventReader = null;
    }

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
        for (NotifdQueueHandler curHandler : m_queueHandlers.values()) {
            curHandler.pause();
        }
    }

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
        for (NotifdQueueHandler curHandler : m_queueHandlers.values()) {
            curHandler.resume();
        }
    }

    /**
     * Returns the singular instance of the Notifd daemon. There can be only
     * one instance of this service per virtual machine.
     *
     * @return a {@link org.opennms.netmgt.notifd.Notifd} object.
     */
    public static Notifd getInstance() {
        return m_singleton;
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
     * @param eventManager The eventManager to set.
     */
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }

    /**
     * <p>setPollOutagesConfigManager</p>
     *
     * @param configManager a {@link org.opennms.netmgt.config.PollOutagesConfigManager} object.
     */
    public void setPollOutagesConfigManager(PollOutagesConfigManager configManager) {
        m_pollOutagesConfigManager = configManager;
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
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

}
