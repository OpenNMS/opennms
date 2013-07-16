/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Alarm management Daemon
 * 
 * TODO: Create configuration for Alarm to enable forwarding.
 * TODO: Application Context for wiring in forwarders???
 * TODO: Change this class to use AbstractServiceDaemon instead of SpringServiceDaemon
 * 
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
@EventListener(name=Alarmd.NAME, logPrefix="alarmd")
public class Alarmd implements SpringServiceDaemon, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(Alarmd.class);

    /** Constant <code>NAME="Alarmd"</code> */
    public static final String NAME = "Alarmd";

    private EventForwarder m_eventForwarder;
    
    private List<Northbounder> m_northboundInterfaces;

    private AlarmPersister m_persister;
    
    
    
    
    //Get all events
    /**
     * <p>onEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventHandler.ALL_UEIS)
    public void onEvent(Event e) {
    	
    	if (e.getUei().equals("uei.opennms.org/internal/reloadDaemonConfig")) {
    		return;
    	}
    	
        OnmsAlarm alarm = m_persister.persist(e);
        
        if (alarm != null) {
        	NorthboundAlarm a = new NorthboundAlarm(alarm);

            for (Northbounder nbi : m_northboundInterfaces) {
                nbi.onAlarm(a);
            }
        }
        
    }

    @EventHandler(uei = "uei.opennms.org/internal/reloadDaemonConfig")
    private void handleReloadEvent(Event e) {
    	LOG.info("Received reload configuration event: {}", e);

    	//Currently, Alarmd has no configuration... I'm sure this will change soon.


    	List<Parm> parmCollection = e.getParmCollection();
    	for (Parm parm : parmCollection) {

    		String parmName = parm.getParmName();
    		if("daemonName".equals(parmName)) {
    			if (parm.getValue() == null || parm.getValue().getContent() == null) {
    				LOG.warn("The daemonName parameter has no value, ignoring.");
    				return;
    			}

    			List<Northbounder> nbis = getNorthboundInterfaces();
    			for (Northbounder nbi : nbis) {
    				if (parm.getValue().getContent().contains(nbi.getName())) {
    					LOG.debug("Handling reload event for NBI: {}", nbi.getName());
    					LOG.debug("Reloading NBI configuration for interface {} not yet implemented.", nbi.getName());
    					return;
    				}
    			}
    		}
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

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * 
     * TODO: use onInit() instead
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (getNorthboundInterfaces() != null) {
            for (final Northbounder nb : getNorthboundInterfaces()) {
                nb.start();
            }
        }
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void destroy() throws Exception {
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return NAME;
    }

    /**
     * <p>start</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void start() throws Exception {
    }

    public void onNorthbounderRegistered(final Northbounder northbounder, final Map<String,String> properties) {
        northbounder.start();
    }
    
    public void onNorthbounderUnregistered(final Northbounder northbounder, final Map<String,String> properties) {
        northbounder.stop();
    }
    
    public List<Northbounder> getNorthboundInterfaces() {
        return m_northboundInterfaces;
    }

    public void setNorthboundInterfaces(List<Northbounder> northboundInterfaces) {
        m_northboundInterfaces = northboundInterfaces;
    }

}
