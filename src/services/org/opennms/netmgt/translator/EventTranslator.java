//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.translator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.config.DbConnectionFactory;
import org.opennms.netmgt.config.EventTranslatorConfig;
import org.opennms.netmgt.config.PassiveStatusKey;
import org.opennms.netmgt.config.PassiveStatusValue;
import org.opennms.netmgt.daemon.ServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.poller.pollables.PollStatus;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;

public class EventTranslator extends ServiceDaemon implements EventListener {
    
    private static EventTranslator s_instance = new EventTranslator();

    private Map m_statusTable = null;
    private EventIpcManager m_eventMgr;
    private EventTranslatorConfig m_config;
    private boolean m_initialized = false;

    private DbConnectionFactory m_dbConnectionFactory;

    
    public EventTranslator() {
    }
    
    public EventTranslator(EventIpcManager eventMgr) {
        setEventManager(eventMgr);
    }
    
    public synchronized static void setInstance(EventTranslator psk) {
        s_instance = psk;
    }
    
    public synchronized static EventTranslator getInstance() {
        return s_instance;
    }

    
    public void init() {
        if (m_initialized) return;
        
        checkPreRequisites();
        createMessageSelectorAndSubscribe();
        
        m_statusTable = new HashMap();
        
        String sql = "select node.nodeLabel AS nodeLabel, outages.ipAddr AS ipAddr, service.serviceName AS serviceName " +
                "FROM outages " +
                "JOIN node ON outages.nodeId = node.nodeId " +
                "JOIN service ON outages.serviceId = service.serviceId " +
                "WHERE outages.ifRegainedService is NULL";
        
        Querier querier = new Querier(m_dbConnectionFactory, sql) {
        
            public void processRow(ResultSet rs) throws SQLException {
               
                PassiveStatusKey key = new PassiveStatusKey(rs.getString("nodeLabel"), rs.getString("ipAddr"), rs.getString("serviceName"));
                m_statusTable.put(key, PollStatus.STATUS_DOWN);
            }
        
        };
        querier.execute();
        
        
        
        m_initialized = true;
        setStatus(START_PENDING);
    }

    private void checkPreRequisites() {
        if (m_config == null)
            throw new IllegalStateException("config has not been set");
        if (m_eventMgr == null)
            throw new IllegalStateException("eventManager has not been set");
        if (m_dbConnectionFactory == null)
            throw new IllegalStateException("dbConnectionFactory has not been set");
    }

    public void start() {
        setStatus(RUNNING);
    }

    public void stop() {
        setStatus(STOP_PENDING);
        setStatus(STOPPED);
    }
    
    public void destroy() {
        setStatus(STOPPED);
        m_initialized = false;
        m_eventMgr = null;
        m_config = null;
        m_statusTable = null;
    }

    public String getName() {
        return EventTranslatorConfig.TRANSLATOR_NAME;
    }

    public void pause() {
        setStatus(PAUSED);
    }

    public void resume() {
        setStatus(RESUME_PENDING);
    }

    public void setStatus(String nodeLabel, String ipAddr, String svcName, PollStatus pollStatus) {
        checkInit();
        setStatus(new PassiveStatusKey(nodeLabel, ipAddr, svcName), pollStatus);
    }
    
    public void setStatus(PassiveStatusKey key, PollStatus pollStatus) {
        checkInit();
        m_statusTable.put(key, pollStatus);
    }

    private void checkInit() {
        if (!m_initialized)
            throw new IllegalStateException("the service has not been intialized");
    }

    public PollStatus getStatus(String nodeLabel, String ipAddr, String svcName) {
        //FIXME: Throw a log or exception here if this method is called and the this class hasn't been initialized
        PollStatus status = (PollStatus) (m_statusTable == null ? PollStatus.STATUS_UNKNOWN : m_statusTable.get(new PassiveStatusKey(nodeLabel, ipAddr, svcName)));
        return (status == null ? PollStatus.STATUS_UP : status);
    }

    private void createMessageSelectorAndSubscribe() {
        // Subscribe to eventd
        getEventManager().addEventListener(this, m_config.getUEIList());
    }

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

    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
    
    public EventTranslatorConfig getConfig() {
        return m_config;
    }
    
    public void setConfig(EventTranslatorConfig config) {
        m_config = config;
    }
    
    public DbConnectionFactory getDbConnectoinFactory() {
        return m_dbConnectionFactory;
    }
    
    public void setDbConnectionFactory(DbConnectionFactory dbConnectionFactory) {
        m_dbConnectionFactory = dbConnectionFactory;
    }
    
    private Category log() {
        return ThreadCategory.getInstance(EventTranslator.class);
    }

}
