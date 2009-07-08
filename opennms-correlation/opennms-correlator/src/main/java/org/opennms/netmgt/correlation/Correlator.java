/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.correlation;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class Correlator extends AbstractServiceDaemon implements CorrelationEngineRegistrar {

	private EventIpcManager m_eventIpcManager;
	private List<CorrelationEngine> m_engines;
	private List<EngineAdapter> m_adapters = new LinkedList<EngineAdapter>();
    private boolean m_initialized = false;
	
	
	private class EngineAdapter implements EventListener {
		
		private final CorrelationEngine m_engine;

		public EngineAdapter(CorrelationEngine engine) {
			m_engine = engine;
			m_eventIpcManager.addEventListener(this, m_engine.getInterestingEvents());
		}

		public String getName() {
			return m_engine.getClass().getSimpleName();
		}

		public void onEvent(Event e) {
			m_engine.correlate(e);
		}
		
	}

	protected Correlator() {
		super("OpenNMS.Correlator");
	}

	@Override
	protected void onInit() {
		Assert.notNull(m_eventIpcManager, "property eventIpcManager must be set");
		Assert.notNull(m_engines, "property engines must be set");
        
        log().info("m_engines.size = " + m_engines.size());
		
		for(CorrelationEngine engine : m_engines) {
            log().info("Registering engine "+engine);
			m_adapters.add(new EngineAdapter(engine));
		}
        
        m_initialized = true;
		
	}

	public void setCorrelationEngines(List<CorrelationEngine> engines) {
		m_engines = engines;
	}

	public void setEventIpcManager(EventIpcManager eventIpcManager) {
		m_eventIpcManager = eventIpcManager;
	}
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.correlation.CorrelationEngineRegistrar#addCorrelationEngine(org.opennms.netmgt.correlation.CorrelationEngine)
     */
    public void addCorrelationEngine(CorrelationEngine engine) {
        m_engines.add(engine);
        if (m_initialized) {
            m_adapters.add(new EngineAdapter(engine));
        }
    }

    public CorrelationEngine findEngineByName(String name) {
        for (CorrelationEngine engine : m_engines) {
            if (name.equals(engine.getName())) {
                return engine;
            }
        }
        return null;
    }

    public List<CorrelationEngine> getEngines() {
        return m_engines;
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
