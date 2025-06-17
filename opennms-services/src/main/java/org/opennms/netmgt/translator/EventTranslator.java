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
package org.opennms.netmgt.translator;

import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.netmgt.config.EventTranslatorConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>EventTranslator class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class EventTranslator extends AbstractServiceDaemon implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventTranslator.class);

    private static EventTranslator s_instance = new EventTranslator();

    private volatile EventIpcManager m_eventMgr;
    private volatile EventTranslatorConfig m_config;
    private volatile boolean m_initialized = false;

    private DataSource m_dataSource;


    /**
     * <p>Constructor for EventTranslator.</p>
     */
    public EventTranslator() {
        super(EventTranslatorConfig.TRANSLATOR_NAME);
    }

    /**
     * <p>Constructor for EventTranslator.</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public EventTranslator(EventIpcManager eventMgr) {
        this();
        setEventManager(eventMgr);
    }

    /**
     * <p>setInstance</p>
     *
     * @param psk a {@link org.opennms.netmgt.translator.EventTranslator} object.
     */
    public static synchronized void setInstance(EventTranslator psk) {
        s_instance = psk;
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.translator.EventTranslator} object.
     */
    public static synchronized EventTranslator getInstance() {
        return s_instance;
    }


    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        if (m_initialized) return;

        checkPreRequisites();
        createMessageSelectorAndSubscribe();

        m_initialized = true;
    }

    private void checkPreRequisites() {
        if (m_config == null)
            throw new IllegalStateException("config has not been set");
        if (m_eventMgr == null)
            throw new IllegalStateException("eventManager has not been set");
        if (m_dataSource == null)
            throw new IllegalStateException("dataSource has not been set");
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        m_initialized = false;
        m_eventMgr = null;
        m_config = null;
    }

    private void createMessageSelectorAndSubscribe() {
        // Subscribe to eventd
        List<String> ueiList = m_config.getUEIList();
        ueiList.add(EventConstants.RELOAD_DAEMON_CONFIG_UEI);
        getEventManager().addEventListener(this, ueiList);
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(IEvent ie) {
        Event e = Event.copyFrom(ie);
        if (isReloadConfigEvent(e)) {
            handleReloadEvent(e);
            return;
        }

        if (getName().equals(e.getSource())) {
            LOG.debug("onEvent: ignoring event with EventTranslator as source");
            return;
        }

        List<Event> translated = m_config.translateEvent(e);
        if (translated != null) {

            if (translated.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("onEvent: received event that matches no translations: {}", EventUtils.toString(e));
                }
                return;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("onEvent: received valid registered translation event: {}", EventUtils.toString(e));
            }

            Log log = new Log();
            Events events = new Events();
            for (Iterator<Event> iter = translated.iterator(); iter.hasNext();) {
                Event event = iter.next();
                events.addEvent(event);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("onEvent: sent translated event: {}", EventUtils.toString(event));
                }
            }
            log.setEvents(events);
            getEventManager().sendNow(log);
        }
    }

    /**
     * Re-marshals the translator specs into the factory's config member and
     * re-registers the UIEs with the eventProxy.
     *
     * @param e The reload daemon config event<code>Event</code>
     */
    protected void handleReloadEvent(Event e) {
        LOG.info("onEvent: reloading configuration....");
        EventBuilder ebldr = null;
        try {
            List<String> previousUeis = m_config.getUEIList();
            m_config.update();

            //need to re-register the UEIs not including those the daemon
            //registered separate from the config (i.e. reloadDaemonConfig)
            getEventManager().removeEventListener(this, previousUeis);
            getEventManager().addEventListener(this, m_config.getUEIList());

            LOG.debug("onEvent: configuration reloaded.");
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Translator");
        } catch (Throwable exception) {
            LOG.error("onEvent: reload config failed: {}", e, exception);
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Translator");
            ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));
        }
        if (ebldr != null) {
            m_eventMgr.sendNow(ebldr.getEvent());
        }

        LOG.info("onEvent: reload configuration: reload configuration contains {} UEI specs.", m_config.getUEIList().size());
    }

    private boolean isReloadConfigEvent(Event event) {
        boolean isTarget = false;

        if (EventConstants.RELOAD_DAEMON_CONFIG_UEI.equals(event.getUei())) {

            List<Parm> parmCollection = event.getParmCollection();

            for (Parm parm : parmCollection) {
                if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && 
                        "Translator".equalsIgnoreCase(parm.getValue().getContent())) {
                    isTarget = true;
                    break;
                }
            }

            LOG.debug("isReloadConfigEventTarget: Event Translator was target of reload event: {}", isTarget);
        }
        return isTarget;
    }

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    /**
     * <p>setEventManager</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.EventTranslatorConfig} object.
     */
    public EventTranslatorConfig getConfig() {
        return m_config;
    }

    /**
     * <p>setConfig</p>
     *
     * @param config a {@link org.opennms.netmgt.config.EventTranslatorConfig} object.
     */
    public void setConfig(EventTranslatorConfig config) {
        m_config = config;
    }

    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getDataSource() {
        return m_dataSource;
    }

    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }


}
