/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.notifd.Queue;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.NodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    
    private static final Logger LOG = LoggerFactory.getLogger(Notifd.class);

    private static final String LOG4J_CATEGORY = "notifd";

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
    @Autowired
    private volatile BroadcastEventProcessor m_eventReader;

    @Autowired
    private volatile NotifdConfigManager m_configManager;

    @Autowired
    private volatile NodeDao m_nodeDao;

    @Autowired
    private volatile NotificationManager m_notificationManager;

    /**
     * Constructs a new Notifd service daemon.
     */
    protected Notifd() {
    	super("notifd");
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        try {
            LOG.info("Notification status = {}", getConfigManager().getNotificationStatus());

            final List<Queue> queues = getConfigManager().getConfiguration().getQueues();
            for (final Queue queue : queues) {
                final NoticeQueue curQueue = new NoticeQueue();

                Class<?> handlerClass = Class.forName(queue.getHandlerClass().getName());
                NotifdQueueHandler handlerQueue = (NotifdQueueHandler) handlerClass.newInstance();

                handlerQueue.setQueueID(queue.getQueueId());
                handlerQueue.setNoticeQueue(curQueue);
                handlerQueue.setInterval(queue.getInterval());

                m_noticeQueues.put(queue.getQueueId(), curQueue);
                m_queueHandlers.put(queue.getQueueId(), handlerQueue);
            }
        } catch (Throwable t) {
            LOG.error("start: Failed to load notifd queue handlers.", t);
            throw new UndeclaredThrowableException(t);
        }

        m_eventReader.setNoticeQueues(m_noticeQueues);

        // start the event reader
        try {
            m_eventReader.init();
        } catch (Throwable e) {
            LOG.error("Failed to setup event receiver", e);
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
     * <p>getBroadcastEventProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.notifd.BroadcastEventProcessor} object.
     */
    public BroadcastEventProcessor getBroadcastEventProcessor() {
        return m_eventReader;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public NotificationManager getNotificationManager() {
        return m_notificationManager;
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

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }
}
