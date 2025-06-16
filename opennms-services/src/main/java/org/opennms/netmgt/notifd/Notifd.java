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
