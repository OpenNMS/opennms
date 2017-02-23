/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.correlation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * <p>Correlator class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class Correlator extends AbstractServiceDaemon implements CorrelationEngineRegistrar {
	private static final Logger LOG = LoggerFactory.getLogger(Correlator.class);

	private EventIpcManager m_eventIpcManager;
	private Map<String,CorrelationEngine> m_engines = new HashMap<>();
	private final List<EngineAdapter> m_adapters = new LinkedList<EngineAdapter>();
	private boolean m_initialized = false;
	
	
	private class EngineAdapter implements EventListener {
		
		private final String m_name;
		private final CorrelationEngine m_engine;

		public EngineAdapter(final CorrelationEngine engine) {
			m_engine = engine;
			m_name = m_engine.getClass().getSimpleName() + '-' + m_engine.getName() ;
			Map<String,String> mdc = Logging.getCopyOfContextMap();
			Logging.putPrefix(m_name);
			final List<String> interesting = m_engine.getInterestingEvents();
			if (interesting.contains(EventHandler.ALL_UEIS)) {
				LOG.warn("Registering engine {} for ALL events", m_engine.getName());
				m_eventIpcManager.addEventListener(this);
			} else {
				m_eventIpcManager.addEventListener(this, interesting);
			}
			Logging.setContextMap(mdc);
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public void onEvent(final Event e) {
			m_engine.correlate(e);
		}
		
	}

	/**
	 * <p>Constructor for Correlator.</p>
	 */
	protected Correlator() {
		super("correlator");
	}

	/** {@inheritDoc} */
	@Override
	protected void onInit() {
		Assert.notNull(m_eventIpcManager, "property eventIpcManager must be set");
		Assert.notNull(m_engines, "property engines must be set");
        
		for(final CorrelationEngine engine : m_engines.values()) {
			LOG.info("Registering correlation engine: {}", engine);
			m_adapters.add(new EngineAdapter(engine));
		}
        
        m_initialized = true;
		
	}

	/**
	 * <p>setCorrelationEngines</p>
	 *
	 * @param engines a {@link java.util.List} object.
	 */
	public void setCorrelationEngines(final Collection<CorrelationEngine> engines) {
		m_engines.clear();
		engines.stream().forEach(engine -> m_engines.put(engine.getName(), engine));
	}

	/**
	 * <p>setEventIpcManager</p>
	 *
	 * @param eventIpcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
	 */
	public void setEventIpcManager(final EventIpcManager eventIpcManager) {
		m_eventIpcManager = eventIpcManager;
	}
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.correlation.CorrelationEngineRegistrar#addCorrelationEngine(org.opennms.netmgt.correlation.CorrelationEngine)
     */
    /** {@inheritDoc} */
    @Override
    public void addCorrelationEngine(final CorrelationEngine engine) {
        m_engines.put(engine.getName(), engine);
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
    @Override
    public CorrelationEngine findEngineByName(final String name) {
        return m_engines.get(name);
    }

    /**
     * <p>getEngines</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public Collection<CorrelationEngine> getEngines() {
        return m_engines.values();
    }
}
