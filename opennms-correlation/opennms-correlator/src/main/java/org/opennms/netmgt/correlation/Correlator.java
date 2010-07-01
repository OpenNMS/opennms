/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Mar 20: System.err.println -> log().info. - dj@opennms.org
 * Created January 26, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.correlation;

import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.util.Assert;

/**
 * <p>Correlator class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
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

	/**
	 * <p>Constructor for Correlator.</p>
	 */
	protected Correlator() {
		super("OpenNMS.Correlator");
	}

	/** {@inheritDoc} */
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

	/**
	 * <p>setCorrelationEngines</p>
	 *
	 * @param engines a {@link java.util.List} object.
	 */
	public void setCorrelationEngines(List<CorrelationEngine> engines) {
		m_engines = engines;
	}

	/**
	 * <p>setEventIpcManager</p>
	 *
	 * @param eventIpcManager a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
	 */
	public void setEventIpcManager(EventIpcManager eventIpcManager) {
		m_eventIpcManager = eventIpcManager;
	}
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.correlation.CorrelationEngineRegistrar#addCorrelationEngine(org.opennms.netmgt.correlation.CorrelationEngine)
     */
    /** {@inheritDoc} */
    public void addCorrelationEngine(CorrelationEngine engine) {
        m_engines.add(engine);
        if (m_initialized) {
            m_adapters.add(new EngineAdapter(engine));
        }
    }

    /** {@inheritDoc} */
    public CorrelationEngine findEngineByName(String name) {
        for (CorrelationEngine engine : m_engines) {
            if (name.equals(engine.getName())) {
                return engine;
            }
        }
        return null;
    }

    /**
     * <p>getEngines</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<CorrelationEngine> getEngines() {
        return m_engines;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
