/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.correlation;

import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.model.events.EventIpcManager;
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
	private final List<EngineAdapter> m_adapters = new LinkedList<EngineAdapter>();
    private boolean m_initialized = false;
	
	
	private class EngineAdapter implements EventListener {
		
		private final String m_name;
		private final CorrelationEngine m_engine;

		public EngineAdapter(final CorrelationEngine engine) {
			m_engine = engine;
			m_name = m_engine.getClass().getSimpleName() + '-' + m_engine.getName() ;
			m_eventIpcManager.addEventListener(this, m_engine.getInterestingEvents());
		}

		public String getName() {
			return m_name;
		}

		public void onEvent(final Event e) {
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
        
		for(final CorrelationEngine engine : m_engines) {
			LogUtils.infof(this, "Registering correlation engine: %s", engine);
			m_adapters.add(new EngineAdapter(engine));
		}
        
        m_initialized = true;
		
	}

	/**
	 * <p>setCorrelationEngines</p>
	 *
	 * @param engines a {@link java.util.List} object.
	 */
	public void setCorrelationEngines(final List<CorrelationEngine> engines) {
		m_engines = engines;
	}

	/**
	 * <p>setEventIpcManager</p>
	 *
	 * @param eventIpcManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
	 */
	public void setEventIpcManager(final EventIpcManager eventIpcManager) {
		m_eventIpcManager = eventIpcManager;
	}
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.correlation.CorrelationEngineRegistrar#addCorrelationEngine(org.opennms.netmgt.correlation.CorrelationEngine)
     */
    /** {@inheritDoc} */
    public void addCorrelationEngine(final CorrelationEngine engine) {
        m_engines.add(engine);
        if (m_initialized) {
            m_adapters.add(new EngineAdapter(engine));
        }
    }
    
    

    @Override
	public void addCorrelationEngines(CorrelationEngine... engines) {
    	for(CorrelationEngine engine : engines) {
    		addCorrelationEngine(engine);
    	}
	}

	/** {@inheritDoc} */
    public CorrelationEngine findEngineByName(final String name) {
    	for (final CorrelationEngine engine : m_engines) {
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
}
