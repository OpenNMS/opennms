/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.compiler.compiler.PackageBuilder;
import org.drools.compiler.compiler.PackageBuilderConfiguration;
import org.drools.core.RuleBase;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.RuleBaseConfiguration.AssertBehaviour;
import org.drools.core.RuleBaseFactory;
import org.drools.core.StatefulSession;
import org.drools.core.WorkingMemory;
import org.kie.api.conf.EventProcessingOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * <p>DroolsCorrelationEngine class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DroolsCorrelationEngine extends AbstractCorrelationEngine {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsCorrelationEngine.class);

    private StatefulSession m_workingMemory;
    private List<String> m_interestingEvents;
    private List<Resource> m_rules;
    private Map<String, Object> m_globals = new HashMap<>();
    private String m_name;
    private String m_assertBehaviour;
    private String m_eventProcessingMode;

    private final Meter m_eventsMeter;
    private boolean m_isStreaming = false;
    
    public DroolsCorrelationEngine(final String name, final MetricRegistry metricRegistry) {
        this.m_name = name;
        final Gauge<Long> factCount = () -> { return getWorkingMemory().getFactCount(); };
        metricRegistry.register(MetricRegistry.name(name, "fact-count"), factCount);
        final Gauge<Integer> pendingTasksCount = () -> { return getPendingTasksCount(); };
        metricRegistry.register(MetricRegistry.name(name, "pending-tasks-count"), pendingTasksCount);
        m_eventsMeter = metricRegistry.meter(MetricRegistry.name(name, "events"));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void correlate(final Event e) {
        LOG.debug("Begin correlation for Event {} uei: {}", e.getDbid(), e.getUei());
        m_workingMemory.insert(e);
        if (!m_isStreaming) m_workingMemory.fireAllRules();
        m_eventsMeter.mark();
        LOG.debug("End correlation for Event {} uei: {}", e.getDbid(), e.getUei());
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void timerExpired(final Integer timerId) {
	LOG.info("Begin correlation for Timer {}", timerId);
        TimerExpired expiration  = new TimerExpired(timerId);
        m_workingMemory.insert(expiration);
        if (!m_isStreaming) m_workingMemory.fireAllRules();
        LOG.debug("Begin correlation for Timer {}", timerId);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getInterestingEvents() {
        return m_interestingEvents;
    }
    
    /**
     * <p>setInterestingEvents</p>
     *
     * @param ueis a {@link java.util.List} object.
     */
    public void setInterestingEvents(final List<String> ueis) {
        m_interestingEvents = ueis;
    }
    
    /**
     * <p>setRulesResources</p>
     *
     * @param rules a {@link java.util.List} object.
     */
    public void setRulesResources(final List<Resource> rules) {
        m_rules = rules;
    }
    
    /**
     * <p>setGlobals</p>
     *
     * @param globals a {@link java.util.Map} object.
     */
    public void setGlobals(final Map<String, Object> globals) {
        m_globals = globals;
    }

    /**
     * <p>initialize</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void initialize() throws Exception {
    	final Properties props = new Properties();
        
        props.setProperty("drools.dialect.java.compiler.lnglevel", "1.6");

        final PackageBuilderConfiguration packageBuilderConfig = new PackageBuilderConfiguration(props);
        final PackageBuilder builder = new PackageBuilder( packageBuilderConfig );
        
        loadRules(builder);
        
        AssertBehaviour behaviour = AssertBehaviour.determineAssertBehaviour(m_assertBehaviour);
        RuleBaseConfiguration ruleBaseConfig = new RuleBaseConfiguration();
        ruleBaseConfig.setAssertBehaviour(behaviour);
        
        EventProcessingOption eventProcessingOption = EventProcessingOption.CLOUD;
        if (m_eventProcessingMode != null && m_eventProcessingMode.toLowerCase().equals("stream")) {
            eventProcessingOption = EventProcessingOption.STREAM;
            m_isStreaming = true;
        }
        ruleBaseConfig.setEventProcessingMode(eventProcessingOption);
        
        final RuleBase ruleBase = RuleBaseFactory.newRuleBase( ruleBaseConfig );

        if (builder.hasErrors()) {
            LOG.warn("Unable to initialize Drools engine: {}", builder.getErrors());
            throw new IllegalStateException("Unable to initialize Drools engine: " + builder.getErrors());
        }

        ruleBase.addPackage( builder.getPackage() );

        m_workingMemory = ruleBase.newStatefulSession();
        m_workingMemory.setGlobal("engine", this);
        
        for (final Map.Entry<String, Object> entry : m_globals.entrySet()) {
            m_workingMemory.setGlobal(entry.getKey(), entry.getValue());
        }

        if (m_isStreaming) {
            new Thread(() -> {
                Logging.putPrefix(getClass().getSimpleName() + '-' + getName());
                m_workingMemory.fireUntilHalt();
            }, "FireTask").start();
        }
    }

    private void loadRules(final PackageBuilder builder) throws DroolsParserException, IOException {
        
        for (final Resource rulesFile : m_rules) {
            Reader rdr = null;
            try {
                LOG.debug("Loading rules file: {}", rulesFile);
                rdr = new InputStreamReader( rulesFile.getInputStream(), "UTF-8" );
                builder.addPackageFromDrl( rdr );
            } finally {
                IOUtils.closeQuietly(rdr);
            }
        }
    }
    
    /**
     * <p>getMemorySize</p>
     *
     * @return a int.
     */
    public int getMemorySize() {
        int count = 0;
        for(final Iterator<?> it = m_workingMemory.iterateObjects(); it.hasNext(); it.next()) {
            count++;
        }
    	return count;
    }
    
    /**
     * <p>getMemoryObjects</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Object> getMemoryObjects() {
    	final List<Object> objects = new LinkedList<>();
        for(Iterator<?> it = m_workingMemory.iterateObjects(); it.hasNext(); ) {
        	objects.add(it.next());
        }
        return objects;
    }
    
    public WorkingMemory getWorkingMemory() {
    	return m_workingMemory;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * <p>setGlobal</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     */
    public void setGlobal(final String name, final Object value) {
        m_workingMemory.setGlobal(name, value);
    }

    public void setAssertBehaviour(String assertBehaviour) {
            m_assertBehaviour = assertBehaviour;
    }

    public String getEventProcessingMode() {
        return m_eventProcessingMode;
    }

    public void setEventProcessingMode(String eventProcessingMode) {
        this.m_eventProcessingMode = eventProcessingMode;
    }
}
