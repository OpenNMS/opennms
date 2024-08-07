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
package org.opennms.netmgt.correlation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
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

	// when reloading daemon, set this event param to "false" to not to reload state ( default, it reloads previous state.)
	public static final String EVENT_PARM_PERSIST_STATE = "persistState";
	private EventIpcManager m_eventIpcManager;
	private Map<String,CorrelationEngine> m_engines = new HashMap<>();
	private final List<EngineAdapter> m_adapters = new LinkedList<>();
	private boolean m_initialized = false;
	
	
	class EngineAdapter implements EventListener {
		
		private final String m_name;
		private final CorrelationEngine m_engine;

		public EngineAdapter(final CorrelationEngine engine) {
			m_engine = engine;
			m_name = m_engine.getClass().getSimpleName() + '-' + m_engine.getName() ;
			Map<String,String> mdc = Logging.getCopyOfContextMap();
			Logging.putPrefix(m_name);
			registerEventListeners();
			Logging.setContextMap(mdc);
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public void onEvent(final IEvent e) {
		    if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_UEI)) {
		        handleReloadEvent(e);
		        return;
		    }
		    m_engine.correlate(Event.copyFrom(e));
		}

		public void tearDown() {
		    m_eventIpcManager.removeEventListener(this);
		}

		private void registerEventListeners() {
                    final List<String> interesting = m_engine.getInterestingEvents();
                    if (interesting.contains(EventHandler.ALL_UEIS)) {
                            LOG.warn("Registering engine {} for ALL events", m_engine.getName());
                            m_eventIpcManager.addEventListener(this);
                    } else {
                            m_eventIpcManager.addEventListener(this, interesting);
                            m_eventIpcManager.addEventListener(this, EventConstants.RELOAD_DAEMON_CONFIG_UEI);
                    }
		}

		private void handleReloadEvent(IEvent e) {
			boolean engineNameMatched = false;
			// By default always persist state.
			boolean persistState = true;
		    List<IParm> parmCollection = e.getParmCollection();
		    for (IParm parm : parmCollection) {
		        String parmName = parm.getParmName();
		        if(EventConstants.PARM_DAEMON_NAME.equals(parmName)) {
		            if (parm.getValue() == null || parm.getValue().getContent() == null) {
		                LOG.warn("The daemonName parameter has no value, ignoring.");
		                return;
		            }
		            if (parm.getValue().getContent().contains(getName())) {
						engineNameMatched = true;
		            }
		        }
		        if(parmName.equals(EVENT_PARM_PERSIST_STATE)) {
		        	if(parm.getValue() != null && parm.getValue().getContent() != null && parm.getValue().getContent().equals("false")) {
						persistState = false;
					}
				}
		    }
		    if (engineNameMatched) {
				m_eventIpcManager.removeEventListener(this);
				m_engine.reloadConfig(persistState);
				registerEventListeners();
			}
			return;
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

	/** {@inheritDoc} */
    @Override
	protected void onStop() {
        for(final CorrelationEngine engine : m_engines.values()) {
            LOG.info("Tearing down correlation engine: {}", engine);
            engine.tearDown();
        }
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
            LOG.debug("addCorrelationEngine: adding engine {}", engine.getName());
            m_adapters.add(new EngineAdapter(engine));
        }
    }
    
    public void removeCorrelationEngine(final String engineName) {
        m_adapters.stream().filter(a -> a.getName().endsWith(engineName)).findFirst().ifPresent(a -> {
            LOG.debug("removeCorrelationEngine: removing engine {}", engineName);
            a.tearDown();
            m_adapters.remove(a);
            m_engines.get(engineName).tearDown();
            m_engines.remove(engineName);
        });
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
