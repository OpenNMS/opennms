/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 16, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.translator;

import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.config.EventTranslatorConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

/**
 * <p>EventTranslator class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 * @version $Id: $
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
     * @param eventMgr a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
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
        getEventManager().addEventListener(this, m_config.getUEIList());
    }

    /** {@inheritDoc} */
    public void onEvent(Event e) {
    	
    		if (getName().equals(e.getSource())) {
    			log().debug("onEvent: ignoring event with EventTranslator as source");
    			return;
    		}
    		
    		if (!m_config.isTranslationEvent(e)) {
    			log().debug("onEvent: received event that matches no translations: \n"+EventUtils.toString(e));
    			return;
    		}
        
    		log().debug("onEvent: received valid registered translation event: \n"+EventUtils.toString(e));
    		List translated = m_config.translateEvent(e);
    		if (translated != null) {
    			Log log = new Log();
    			Events events = new Events();
    			for (Iterator iter = translated.iterator(); iter.hasNext();) {
    				Event event = (Event) iter.next();
    				events.addEvent(event);
    				log().debug("onEvent: sended translated event: \n"+EventUtils.toString(event));
    			}
    			log.setEvents(events);
    			getEventManager().sendNow(log);
    		}
    }

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    /**
     * <p>setEventManager</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
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
