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
package org.opennms.netmgt.alarmd;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.core.messagebus.MessageHandler;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.alarmd.drools.DroolsAlarmContext;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.ThreadAwareEventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Alarm management Daemon
 *
 * @author jwhite
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@EventListener(name=Alarmd.NAME, logPrefix="alarmd")
public class Alarmd extends AbstractServiceDaemon implements ThreadAwareEventListener, MessageHandler {
    private static final Logger LOG = LoggerFactory.getLogger(Alarmd.class);

    /** Constant <code>NAME="alarmd"</code> */
    public static final String NAME = "alarmd";

    protected static final Integer THREADS = SystemProperties.getInteger("org.opennms.alarmd.threads", 4);

    /** MessageBus type derived from uei.opennms.org/internal/reloadDaemonConfig */
    private static final String MSG_TYPE_RELOAD_DAEMON_CONFIG = "reloadDaemonConfig";

    private AlarmPersister m_persister;

    @Autowired
    private AlarmLifecycleListenerManager m_alm;

    @Autowired
    private DroolsAlarmContext m_droolsAlarmContext;

    @Autowired
    private NorthbounderManager m_northbounderManager;

    @Autowired(required = false)
    private MessageBus m_messageBus;

    public Alarmd() {
        super(NAME);
    }

    /**
     * Listens for all events.
     *
     * This method is thread-safe.
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventHandler.ALL_UEIS)
    public void onEvent(IEvent e) {
    	m_persister.persist(Event.copyFrom(e));
    }

    // --- MessageHandler interface ---
    // getName() already provided by AbstractServiceDaemon

    @Override
    public void onMessage(IpcMessage message) {
        LOG.debug("Received IPC message: type={} source={}", message.getType(), message.getSource());
        if (MSG_TYPE_RELOAD_DAEMON_CONFIG.equals(message.getType())) {
            String daemonName = message.getParameter(EventConstants.PARM_DAEMON_NAME);
            handleReload(daemonName);
        }
    }

    private synchronized void handleReload(String daemonName) {
        // Forward to NorthbounderManager — it matches daemonName against NBI names
        m_northbounderManager.onReloadDaemonConfig(daemonName);
        // Also reload Drools context when targeted at alarmd specifically
        if (NAME.equalsIgnoreCase(daemonName)) {
            m_droolsAlarmContext.reload();
        }
    }

	/**
     * <p>setPersister</p>
     *
     * @param persister a {@link org.opennms.netmgt.alarmd.AlarmPersister} object.
     */
    public void setPersister(AlarmPersister persister) {
        this.m_persister = persister;
    }

    /**
     * <p>getPersister</p>
     *
     * @return a {@link org.opennms.netmgt.alarmd.AlarmPersister} object.
     */
    public AlarmPersister getPersister() {
        return m_persister;
    }

    @Override
    protected synchronized void onInit() {
        if (m_messageBus != null) {
            m_messageBus.subscribe(MSG_TYPE_RELOAD_DAEMON_CONFIG, this);
            LOG.info("Alarmd subscribed to MessageBus for IPC events");
        } else {
            LOG.warn("MessageBus not available — Alarmd will not receive IPC events via MessageBus");
        }
    }

    @Override
    public synchronized void onStart() {
        // Start the Drools context
        m_droolsAlarmContext.start();
    }

    @Override
    public synchronized void onStop() {
        // Stop the northbound interfaces
        m_northbounderManager.stop();
        // Stop the Drools context
        m_droolsAlarmContext.stop();
    }

    @Override
    public int getNumThreads() {
        return THREADS;
    }

    public void setMessageBus(MessageBus messageBus) {
        m_messageBus = messageBus;
    }

}
