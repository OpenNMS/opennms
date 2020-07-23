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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.drools.compiler.compiler.DroolsParserException;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.RuleBaseConfiguration.AssertBehaviour;
import org.drools.core.util.DroolsStreamUtils;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.type.FactType;
import org.kie.api.marshalling.KieMarshallers;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.opennms.core.logging.Logging;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.correlation.drools.config.EngineConfiguration;
import org.opennms.netmgt.correlation.drools.config.RuleSet;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

/**
 * <p>DroolsCorrelationEngine class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DroolsCorrelationEngine extends AbstractCorrelationEngine {

    private static final Logger LOG = LoggerFactory.getLogger(DroolsCorrelationEngine.class);
    // If state need to be reloaded in case of engine being reloaded because of exception in rules engine, set this system property to true.
    public static final String RELOAD_STATE_AFTER_EXCEPTION = "org.opennms.netmgt.correlation.drools.reloadStateAfterException";

    private static final ExecutorService s_sessionDisposeExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("DroolsCorrelationEngine-Dispose-Pool-%d").build());
    private static TimeLimiter s_timeLimiter = new SimpleTimeLimiter(s_sessionDisposeExecutor);

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
    private MetricRegistry m_metricRegistry;
    private Boolean m_persistState;
    private Resource m_configPath;
    private ApplicationContext m_configContext;
    private Map<byte[], Class<?>> factObjects = new HashMap<>();

    /**
     * Holds a reference to the thread that calls {@link KieSession#fireUntilHalt()}
     */
    private Thread m_streamThread;

    /**
     * Used to let the "stream thread" known that we're shutting down and that it should not
     * treat {@link InterruptedException}s as errors.
     */
    private final AtomicBoolean m_shuttingDownStreamThread = new AtomicBoolean(false);

    /**
     * Used to marshall/unmarshall the session.
     */
    private Marshaller m_marshaller;

    public DroolsCorrelationEngine(final String name, final MetricRegistry metricRegistry, final Resource configPath, final ApplicationContext configContext) {
        this.m_name = name;
        this.m_configPath = configPath;
        this.m_configContext = configContext;
        this.m_metricRegistry = metricRegistry;
        final Gauge<Long> factCount = () -> { return getKieSession().getFactCount(); };
        metricRegistry.register(MetricRegistry.name(name, "fact-count"), factCount);
        final Gauge<Integer> pendingTasksCount = this::getPendingTasksCount;
        metricRegistry.register(MetricRegistry.name(name, "pending-tasks-count"), pendingTasksCount);
        m_eventsMeter = metricRegistry.meter(MetricRegistry.name(name, "events"));
    }

    public Resource getConfigPath() {
        return m_configPath;
    }

    public ApplicationContext getConfigContext() {
        return m_configContext;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void correlate(final Event e) {
        if (m_kieSession == null) {
            LOG.info("No valid session, Event with id: {} and UEI: {} will not be added as a fact.", e.getDbid(), e.getUei());
            return;
        }
        LOG.debug("Begin correlation for Event {} uei: {}", e.getDbid(), e.getUei());
        m_kieSession.insert(e);
        try {
            if (!m_isStreaming) m_kieSession.fireAllRules();
        } catch (Exception e1) {
            LOG.error("Exception while firing rules ", e1);
        }
        m_eventsMeter.mark();
        LOG.debug("End correlation for Event {} uei: {}", e.getDbid(), e.getUei());
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void timerExpired(final Integer timerId) {
        if (m_kieSession == null) {
            LOG.info("No valid session, Timer with Id {} will not be added as a fact.", timerId);
            return;
        }
        LOG.info("Begin correlation for Timer {}", timerId);
        TimerExpired expiration  = new TimerExpired(timerId);
        m_kieSession.insert(expiration);
        try {
            if (!m_isStreaming) m_kieSession.fireAllRules();
        } catch (Exception e) {
            LOG.error("Exception while firing rules ", e);
        }
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

        final KieMarshallers kMarshallers = KieServices.Factory.get().getMarshallers();
        final ObjectMarshallingStrategy oms = kMarshallers.newSerializeMarshallingStrategy();
        m_marshaller = kMarshallers.newMarshaller( m_kieBase, new ObjectMarshallingStrategy[]{ oms } );

        m_kieSession = m_kieBase.newKieSession();
        m_kieSession.setGlobal("engine", this);

        for (final Map.Entry<String, Object> entry : m_globals.entrySet()) {
            m_kieSession.setGlobal(entry.getKey(), entry.getValue());
        }

        if (m_persistState != null && m_persistState) {
            unmarshallStateFromDisk();
        }

        if (factObjects != null) {
            factObjects.forEach(this::unmarshalAndInsert);
            factObjects.clear();
        }

        if (m_isStreaming) {
            m_shuttingDownStreamThread.set(false);
            m_streamThread = new Thread(() -> {
                Logging.putPrefix(getClass().getSimpleName() + '-' + getName());
                try {
                    m_kieSession.fireUntilHalt();
                } catch (Exception e) {
                    if (m_shuttingDownStreamThread.get()) {
                        // We're shutting down, don't trigger a reload!
                        return;
                    }
                    LOG.error("Exception while running rules, reloading engine ", e);
                    doReload(e);
                }
            }, "FireTask [" + m_name + "]");
            m_streamThread.start();
        }

    }

    // This will send drools exception event which should result into Alarm and send reload event.
    private void doReload(Exception exception) {
        // Trigger an alarm with the specific exception
        EventBuilder eventBldr = new EventBuilder(EventConstants.DROOLS_ENGINE_ENCOUNTERED_EXCEPTION, getName());
        eventBldr.addParam("enginename", getName());
        eventBldr.addParam("stacktrace", ExceptionUtils.getStackTrace(exception));
        sendEvent(eventBldr.getEvent());
        // Send reload daemon event.
        EventBuilder reloadEventBldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, getName());
        // Correlator.EngineAdapter uses this pattern for the engine name.
        reloadEventBldr.addParam(EventConstants.PARM_DAEMON_NAME, this.getClass().getSimpleName() + "-" + getName());
        // By default, no need to persist state after exception, but if this property is set, persist state.
        if(!Boolean.getBoolean(RELOAD_STATE_AFTER_EXCEPTION)) {
            reloadEventBldr.addParam("persistState", "false");
        }
        sendEvent(reloadEventBldr.getEvent());
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
        getScheduler().shutdown();
        m_metricRegistry.remove(MetricRegistry.name(getName(), "pending-tasks-count"));
        m_metricRegistry.remove(MetricRegistry.name(getName(), "fact-count"));
        m_metricRegistry.remove(MetricRegistry.name(getName(), "events"));
        if (m_persistState != null && m_persistState) {
            if (getPendingTasksCount() > 0) {
                LOG.error("Cannot marshall state because there are pending time based tasks running.");
                shutDownKieSession();
            } else {
                marshallStateToDisk();
            }
        } else {
            shutDownKieSession();
        }
    }

    private synchronized void shutDownKieSession() {
        shutDownKieSession(null);
    }

    private synchronized void shutDownKieSession(Runnable postHaltPreDispose) {
        if (m_kieSession == null) {
            return;
        }
        m_shuttingDownStreamThread.set(true);
        m_kieSession.halt();
        if (postHaltPreDispose != null) {
            postHaltPreDispose.run();
        }

        try {
            LOG.debug("Disposing KieSession for engine: {}", m_name);
            // Calls to dispose have been known to cause us deadlocks - see NMS-12201
            // Wrap it with a timeout to make sure this doesn't happen
            s_timeLimiter.callWithTimeout(() -> {
                m_kieSession.dispose();
                m_kieSession.destroy();
                LOG.debug("Successfully disposed KieSession for engine: {}", m_name);
                return null;
            },  10, TimeUnit.SECONDS, true);
        }  catch (UncheckedTimeoutException e) {
            LOG.info("KieSession for engine named '{}' was not disposed within the given timeout.", m_name);
            if (m_streamThread != null) {
                // If we're streaming, interrupt the thread, this has been found to clean things up properly
                // when calling dispose() blocks.
                LOG.info("Interrupting the stream thread for engine: {}", m_name);
                m_streamThread.interrupt();
            }
        } catch (Exception e) {
            LOG.warn("Error occurred while disposing KieSession for engine: {}", m_name, e);
        }

        m_kieSession = null;
        m_streamThread = null;
    }

    private Path getPathToState() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "opennms.drools." + m_name + ".state");
    }

    private synchronized void marshallStateToDisk() {
        if (m_kieSession == null) {
            return;
        }
        final File stateFile = getPathToState().toFile();
        LOG.debug("Saving state for engine {} in {} ...", m_name, stateFile);
        try (FileOutputStream fos = new FileOutputStream(stateFile)) {
            shutDownKieSession(() -> {
                try {
                    m_marshaller.marshall(fos, m_kieSession);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            LOG.info("Successfully saved state for engine {} in {}.", m_name, stateFile);
        } catch (Exception e) {
            LOG.error("Failed to save state for engine {} in {}.", m_name, stateFile, e);
        }
    }

    private void unmarshallStateFromDisk() {
        final File stateFile = getPathToState().toFile();
        if (!stateFile.exists()) {
            LOG.error("Can't restore state from {} because the file doesn't exist", stateFile);
            return;
        }
        LOG.debug("Restoring state for engine {} from {} ...", m_name, stateFile);
        try (FileInputStream fin = new FileInputStream(stateFile)) {
            m_marshaller.unmarshall( fin, m_kieSession );
            stateFile.delete();
            LOG.info("Successfully restored state for engine {} from {}.", m_name, stateFile);
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

    @Override
    public void reloadConfig(boolean persistState) {
        EventBuilder ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
        ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "DroolsCorrelationEngine-" + m_name);
        try {
            LOG.info("Reloading configuration for engine {}", m_name);
            EngineConfiguration cfg = JaxbUtils.unmarshal(EngineConfiguration.class, m_configPath);
            Optional<RuleSet> opt = cfg.getRuleSetCollection().stream().filter(rs -> rs.getName().equals(getName())).findFirst();
            if (opt.isPresent()) {
                if (persistState) {
                    saveFacts();
                } else {
                    shutDownKieSession();
                }
                opt.get().updateEngine(this);
                initialize();
            } else {
                ebldr.setUei(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI);
                ebldr.addParam(EventConstants.PARM_REASON, "RuleSet not found on " + m_configPath);
            }
        } catch (Exception e) {
            ebldr.setUei(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI);
            ebldr.addParam(EventConstants.PARM_REASON, e.getMessage());
        } finally {
            sendEvent(ebldr.getEvent());
        }
    }

    void saveFacts( ) {
        shutDownKieSession(() -> {
            try {
                // Capture the current set of facts
                m_kieSession.getFactHandles().forEach(this::marshalAndSaveFact);
            } catch (Exception e) {
                LOG.warn("Failed to save facts", e);
            }
        });
    }

    Map<byte[], Class<?>> getFactObjects() {
        return factObjects;
    }

    /**
     * This checks if fact is declared in drl.
     * Facts which are inserted in the session will throw exception.
     * Currently, there is no better way to find if the fact is a declared fact or not.
     */
    private FactType getDeclaredFactType(String packageName, String className) {
        try {
            return m_kieBase.getFactType(packageName, className);
        } catch (Exception e) {
            // Any objects that are not declared in drl will throw exception.
        }
        return null;
    }

    private void marshalAndSaveFact(FactHandle factHandle) {
        Object factObject = m_kieSession.getObject(factHandle);
        try {
            factObjects.put(DroolsStreamUtils.streamOut(factObject), factObject.getClass());
        } catch (IOException e) {
            LOG.error("Exception while marshalling fact {} with Class {}", factObject, factObject.getClass().getCanonicalName(), e);
        }
    }


    private void unmarshalAndInsert(byte[] factBytes, Class<?> clazz) {

        try {
            String packageName = clazz.getPackage().getName();
            String className = clazz.getSimpleName();
            // Check if this is a declared fact in drl.
            FactType factType = getDeclaredFactType(packageName, className);
            if (factType != null) {
                m_kieSession.insert(DroolsStreamUtils.streamIn(factBytes, factType.getFactClass().getClassLoader()));
            } else {
                m_kieSession.insert(DroolsStreamUtils.streamIn(factBytes, clazz.getClassLoader()));
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Exception while unmarshalling fact of size {} with Class {}", factBytes.length, clazz.getCanonicalName());
        }
    }

}
