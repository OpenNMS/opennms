/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.compiler.DroolsParserException;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.RuleBaseConfiguration.AssertBehaviour;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.marshalling.KieMarshallers;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.io.ByteStreams;

/**
 * <p>DroolsCorrelationEngine class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DroolsCorrelationEngine extends AbstractCorrelationEngine {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsCorrelationEngine.class);

    private KieBase m_kieBase;
    private KieSession m_kieSession;
    private List<String> m_interestingEvents;
    private List<Resource> m_rules;
    private Map<String, Object> m_globals = new HashMap<>();
    private String m_name;
    private String m_assertBehaviour;
    private String m_eventProcessingMode;
    private boolean m_isStreaming = false;
    private final Meter m_eventsMeter;
    private Boolean m_persistState;
    
    public DroolsCorrelationEngine(final String name, final MetricRegistry metricRegistry) {
        this.m_name = name;
        final Gauge<Long> factCount = () -> { return getKieSession().getFactCount(); };
        metricRegistry.register(MetricRegistry.name(name, "fact-count"), factCount);
        final Gauge<Integer> pendingTasksCount = () -> { return getPendingTasksCount(); };
        metricRegistry.register(MetricRegistry.name(name, "pending-tasks-count"), pendingTasksCount);
        m_eventsMeter = metricRegistry.meter(MetricRegistry.name(name, "events"));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void correlate(final Event e) {
        LOG.debug("Begin correlation for Event {} uei: {}", e.getDbid(), e.getUei());
        m_kieSession.insert(e);
        if (!m_isStreaming) m_kieSession.fireAllRules();
        m_eventsMeter.mark();
        LOG.debug("End correlation for Event {} uei: {}", e.getDbid(), e.getUei());
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void timerExpired(final Integer timerId) {
        LOG.info("Begin correlation for Timer {}", timerId);
        TimerExpired expiration  = new TimerExpired(timerId);
        m_kieSession.insert(expiration);
        if (!m_isStreaming) m_kieSession.fireAllRules();
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
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kFileSystem = ks.newKieFileSystem();
        loadRules(kFileSystem);

        KieBuilder kbuilder = ks.newKieBuilder( kFileSystem );
        kbuilder.buildAll();
        if (kbuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            LOG.warn("Unable to initialize Drools engine: {}", kbuilder.getResults().getMessages(Level.ERROR));
            throw new IllegalStateException("Unable to initialize Drools engine: " + kbuilder.getResults().getMessages(Level.ERROR));
        }
        KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

        AssertBehaviour behaviour = AssertBehaviour.determineAssertBehaviour(m_assertBehaviour);
        RuleBaseConfiguration ruleBaseConfig = new RuleBaseConfiguration();
        ruleBaseConfig.setAssertBehaviour(behaviour);

        EventProcessingOption eventProcessingOption = EventProcessingOption.CLOUD;
        if (m_eventProcessingMode != null && m_eventProcessingMode.toLowerCase().equals("stream")) {
            eventProcessingOption = EventProcessingOption.STREAM;
            m_isStreaming = true;
        }
        ruleBaseConfig.setEventProcessingMode(eventProcessingOption);

        m_kieBase = kContainer.newKieBase(ruleBaseConfig);
        m_kieSession = m_kieBase.newKieSession();
        m_kieSession.setGlobal("engine", this);

        for (final Map.Entry<String, Object> entry : m_globals.entrySet()) {
            m_kieSession.setGlobal(entry.getKey(), entry.getValue());
        }

        if (m_persistState != null && m_persistState) {
            unmarshallStateFromDisk(true);
        }

        if (m_isStreaming) {
            new Thread(() -> {
                Logging.putPrefix(getClass().getSimpleName() + '-' + getName());
                m_kieSession.fireUntilHalt();
            }, "FireTask").start();
        }
    }

    private void loadRules(final KieFileSystem kfs) throws DroolsParserException, IOException {
        int k = 0;
        for (final Resource rulesFile : m_rules) {
            try (InputStream is = rulesFile.getInputStream()) {
                LOG.debug("Loading rules file: {}", rulesFile);
                kfs.write(String.format("src/main/resources/" + rulesFile.getFilename(), ++k), ByteStreams.toByteArray(is));
            }
        }
    }

    @Override
    public void tearDown() {
        if (m_persistState != null && m_persistState) {
            if (getPendingTasksCount() > 0) {
                LOG.error("Cannot marshall state because there are pending time based tasks running.");
            } else {
                marshallStateToDisk(true);
            }
        }
    }

    private Path getPathToState() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "opennms.drools." + m_name + ".state");
    }

    private void marshallStateToDisk(boolean serialize) {
        final File stateFile = getPathToState().toFile();
        LOG.debug("Saving state for engine {} in {} ...", m_name, stateFile);
        final KieMarshallers kMarshallers = KieServices.Factory.get().getMarshallers();
        final ObjectMarshallingStrategy oms = serialize ?
                kMarshallers.newSerializeMarshallingStrategy() : kMarshallers.newIdentityMarshallingStrategy();
        final Marshaller marshaller = kMarshallers.newMarshaller( m_kieBase, new ObjectMarshallingStrategy[]{ oms } );
        try (FileOutputStream fos = new FileOutputStream(stateFile)) {
            m_kieSession.halt();
            marshaller.marshall( fos, m_kieSession );
            m_kieSession.dispose();
            m_kieSession.destroy();
            LOG.info("Sucessfully save state for engine {} in {}.", m_name, stateFile);
        } catch (IOException e) {
            LOG.error("Failed to save state for engine {} in {}.", m_name, stateFile, e);
        }
    }

    private void unmarshallStateFromDisk(boolean serialize) {
        final File stateFile = getPathToState().toFile();
        LOG.debug("Restoring state for engine {} from {} ...", m_name, stateFile);
        final KieMarshallers kMarshallers = KieServices.Factory.get().getMarshallers();
        final ObjectMarshallingStrategy oms = serialize ?
                kMarshallers.newSerializeMarshallingStrategy() : kMarshallers.newIdentityMarshallingStrategy();
        final Marshaller marshaller = kMarshallers.newMarshaller( m_kieBase, new ObjectMarshallingStrategy[]{ oms } );

        try (FileInputStream fin = new FileInputStream(stateFile)) {
            marshaller.unmarshall( fin, m_kieSession );
            stateFile.delete();
            LOG.info("Sucessfully restored state for engine {} from {}.", m_name, stateFile);
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Failed to restore state for engine {} from {}.", m_name, stateFile, e);
        }
    }

    public Collection<? extends Object> getKieSessionObjects() {
        return m_kieSession.getObjects();
    }

    public KieSession getKieSession() {
        return m_kieSession;
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
        m_kieSession.setGlobal(name, value);
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

    public void setPersistState(Boolean persistState) {
        m_persistState = persistState;
    }

    public Boolean getPersistState() {
        return m_persistState;
    }

    @Override
    public String toString() {
        return String.format("DroolsCorrelationEngine[%s]", m_name);
    }

}
