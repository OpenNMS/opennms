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

package org.opennms.netmgt.translator;

import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.EventTranslatorConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;

/**
 * <p>EventTranslator class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class EventTranslator extends AbstractServiceDaemon implements EventListener {
    
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
     * @param eventMgr a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
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
    public synchronized static void setInstance(EventTranslator psk) {
        s_instance = psk;
    }
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.translator.EventTranslator} object.
     */
    public synchronized static EventTranslator getInstance() {
        return s_instance;
    }

    
    /**
     * <p>onInit</p>
     */
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
    public void onEvent(Event e) {

        if (isReloadConfigEvent(e)) {
            handleReloadEvent(e);
            return;
        }

        if (getName().equals(e.getSource())) {
            log().debug("onEvent: ignoring event with EventTranslator as source");
            return;
        }

        if (!m_config.isTranslationEvent(e)) {
            log().debug("onEvent: received event that matches no translations: \n"+EventUtils.toString(e));
            return;
        }

        log().debug("onEvent: received valid registered translation event: \n"+EventUtils.toString(e));
        
        List<Event> translated = m_config.translateEvent(e);
        if (translated != null) {
            Log log = new Log();
            Events events = new Events();
            for (Iterator<Event> iter = translated.iterator(); iter.hasNext();) {
                Event event = iter.next();
                events.addEvent(event);
                log().debug("onEvent: sended translated event: \n"+EventUtils.toString(event));
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
        log().info("onEvent: reloading configuration....");
        EventBuilder ebldr = null;
        try {
            List<String> previousUeis = m_config.getUEIList();
            m_config.update();
            
            //need to re-register the UEIs not including those the daemon
            //registered separate from the config (i.e. reloadDaemonConfig)
            getEventManager().removeEventListener(this, previousUeis);
            getEventManager().addEventListener(this, m_config.getUEIList());
            
            log().debug("onEvent: configuration reloaded.");
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Translator");
        } catch (Throwable exception) {
            log().error("onEvent: reload config failed:"+e, exception);
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Translator");
            ebldr.addParam(EventConstants.PARM_REASON, exception.getLocalizedMessage().substring(1, 128));
        }
        if (ebldr != null) {
            m_eventMgr.sendNow(ebldr.getEvent());
        }
        
        log().info("onEvent: reload configuration: reload configuration contains "+m_config.getUEIList().size()+" UEI specs.");
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

            log().debug("isReloadConfigEventTarget: Event Translator was target of reload event: "+isTarget);
        }
        return isTarget;
    }

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    /**
     * <p>setEventManager</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
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
