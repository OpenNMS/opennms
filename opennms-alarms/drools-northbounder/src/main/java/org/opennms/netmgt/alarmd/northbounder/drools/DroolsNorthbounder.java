/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.drools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

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
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Process alarms via Drools.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class DroolsNorthbounder extends AbstractNorthbounder implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DroolsNorthbounder.class);

    /** The Constant NBI_NAME. */
    protected static final String NBI_NAME = "DroolsNBI";

    /** The Drools Configuration DAO. */
    private DroolsNorthbounderConfigDao m_configDao;

    /** The Drools Engine. */
    private DroolsEngine m_engine;

    /** The kie base. */
    private KieBase m_kieBase;

    /** The kie session. */
    private KieSession m_kieSession;

    /** The event proxy. */
    private EventProxy m_eventProxy;

    /** The initialized flag (it will be true when the NBI is properly initialized). */
    private boolean initialized = false;

    private ApplicationContext m_context;

    /**
     * Instantiates a new Drools northbounder.
     *
     * @param configDao the configuration DAO
     * @param engineName the engine name
     */
    public DroolsNorthbounder(ApplicationContext context, DroolsNorthbounderConfigDao configDao, EventProxy eventProxy, String engineName) {
        super(NBI_NAME + '-' + engineName);
        m_context = context;
        m_configDao = configDao;
        m_eventProxy = eventProxy;
        m_engine = configDao.getConfig().getEngine(engineName);
    }


    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        setNaglesDelay(getConfig().getNaglesDelay());
        setMaxBatchSize(getConfig().getBatchSize());
        setMaxPreservedAlarms(getConfig().getQueueSize());
        if (m_engine == null) {
            LOG.error("Drools Northbounder {} is currently disabled because it has not been initialized correctly or there is a problem with the configuration.", getName());
            initialized = false;
            return;
        }
        initializeDroolsEngine();
        initialized = true;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder#onStop()
     */
    @Override
    protected void onStop() {
        marshallStateToDisk(true);
    }

    /**
     * Initialize drools engine.
     *
     * @throws Exception the exception
     */
    private void initializeDroolsEngine() throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kFileSystem = ks.newKieFileSystem();

        for (String ruleFile : m_engine.getRuleFiles()) {
            LOG.debug("Loading rules file: {}", ruleFile);
            kFileSystem.write("src/main/resources/" + ruleFile, ks.getResources().newFileSystemResource(new File(ruleFile)));
        }

        KieBuilder kbuilder = ks.newKieBuilder(kFileSystem);
        kbuilder.buildAll();
        if (kbuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            LOG.warn("Unable to initialize Drools engine: {}", kbuilder.getResults().getMessages(Level.ERROR));
            throw new IllegalStateException("Unable to initialize Drools engine: " + kbuilder.getResults().getMessages(Level.ERROR));
        }
        KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

        AssertBehaviour behaviour = AssertBehaviour.determineAssertBehaviour(m_engine.getAssertBehaviour());
        RuleBaseConfiguration ruleBaseConfig = new RuleBaseConfiguration();
        ruleBaseConfig.setAssertBehaviour(behaviour);
        ruleBaseConfig.setEventProcessingMode(EventProcessingOption.STREAM);

        m_kieBase = kContainer.newKieBase(ruleBaseConfig);
        m_kieSession = m_kieBase.newKieSession();
        m_kieSession.setGlobal("engine", this);

        unmarshallStateFromDisk(true);

        ApplicationContext ctx = m_context;
        if (m_engine.getAppContext() != null) {
            ctx = new FileSystemXmlApplicationContext(new String[] { m_engine.getAppContext() },  m_context);
        } 
        for (Global global : m_engine.getGlobals()) {
            m_kieSession.setGlobal(global.getName(), global.constructValue(ctx));
        }

        new Thread(() -> {
            Logging.putPrefix(getName());
            LOG.debug("Starting task thread for {}", getName());
            m_kieSession.fireUntilHalt();
            LOG.debug("Stopping task thread for {}", getName());
        }, "FireTask-" + getName()).start();

    }

    /**
     * The abstraction makes a call here to determine if the alarm should be placed on the queue of alarms to be sent northerly.
     *
     * @param alarm the alarm
     * @return true, if successful
     */
    @Override
    public boolean accepts(NorthboundAlarm alarm) {
        if (!initialized) {
            LOG.warn("Drools Northbounder {} has not been properly initialized, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }
        if (!getConfig().isEnabled()) {
            LOG.warn("Drools Northbounder {} is currently disabled, rejecting alarm {}.", getName(), alarm.getUei());
            return false;
        }

        LOG.debug("Validating UEI of alarm: {}", alarm.getUei());
        if (getConfig().getUeis() == null || getConfig().getUeis().contains(alarm.getUei())) {
            LOG.debug("UEI: {}, accepted.", alarm.getUei());
            boolean passed = m_engine.accepts(alarm);
            LOG.debug("Filters: {}, passed ? {}.", alarm.getUei(), passed);
            return passed;
        }
        LOG.debug("UEI: {}, rejected.", alarm.getUei());
        return false;
    }

    /**
     * Each implementation of the AbstractNorthbounder has a nice queue (Nagle's algorithmic) and the worker thread that processes the queue
     * calls this method to send alarms to the northern NMS.
     *
     * @param alarms the alarms
     * @throws NorthbounderException the northbounder exception
     */
    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        if (alarms == null) {
            String errorMsg = "No alarms in alarms list for drools forwarding.";
            NorthbounderException e = new NorthbounderException(errorMsg);
            LOG.error(errorMsg, e);
            throw e;
        }
        LOG.info("Forwarding {} alarms to engine {}", alarms.size(), m_engine.getName());
        alarms.forEach(a -> {
            LOG.debug("Begin correlation for alarm {} uei: {}", a.getId(), a.getUei());
            m_kieSession.insert(a);   
            LOG.debug("End correlation for alarm {} uei: {}", a.getId(), a.getUei());
        });
    }

    /**
     * Gets the kie session objects.
     *
     * @return the kie session objects
     */
    public Collection<? extends Object> getKieSessionObjects() {
        return m_kieSession.getObjects();
    }

    /**
     * Gets the kie session.
     *
     * @return the kie session
     */
    public KieSession getKieSession() {
        return m_kieSession;
    }

    /**
     * Send event.
     *
     * @param event the event
     */
    public void sendEvent(final Event event) {
        try {
            m_eventProxy.send(event);
        } catch (EventProxyException e) {
            LOG.error("Can't send event", e);
        }
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    protected DroolsNorthbounderConfig getConfig() {
        return m_configDao.getConfig();
    }

    /**
     * Gets the path to state.
     *
     * @return the path to state
     */
    private Path getPathToState() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "opennms.drools.nbi." + getName() + ".state");
    }

    /**
     * Marshall state to disk.
     *
     * @param serialize the serialize
     */
    private void marshallStateToDisk(boolean serialize) {
        final File stateFile = getPathToState().toFile();
        LOG.debug("Saving state for engine {} in {} ...", getName(), stateFile);
        final KieMarshallers kMarshallers = KieServices.Factory.get().getMarshallers();
        final ObjectMarshallingStrategy oms = serialize ? kMarshallers.newSerializeMarshallingStrategy() : kMarshallers.newIdentityMarshallingStrategy();
        final Marshaller marshaller = kMarshallers.newMarshaller( m_kieBase, new ObjectMarshallingStrategy[]{ oms } );
        try (FileOutputStream fos = new FileOutputStream(stateFile)) {
            m_kieSession.halt();
            marshaller.marshall( fos, m_kieSession );
            m_kieSession.dispose();
            m_kieSession.destroy();
            LOG.info("Sucessfully save state for engine {} in {}. There are {} elements on the working memory.", getName(), stateFile, m_kieSession.getObjects().size());
        } catch (IOException e) {
            LOG.error("Failed to save state for engine {} in {}.", getName(), stateFile, e);
        }
    }

    /**
     * Unmarshall state from disk.
     *
     * @param serialize the serialize
     */
    private void unmarshallStateFromDisk(boolean serialize) {
        final File stateFile = getPathToState().toFile();
        LOG.debug("Restoring state for engine {} from {} ...", getName(), stateFile);
        if (!stateFile.exists()) return;
        final KieMarshallers kMarshallers = KieServices.Factory.get().getMarshallers();
        final ObjectMarshallingStrategy oms = serialize ? kMarshallers.newSerializeMarshallingStrategy() : kMarshallers.newIdentityMarshallingStrategy();
        final Marshaller marshaller = kMarshallers.newMarshaller( m_kieBase, new ObjectMarshallingStrategy[]{ oms } );
        try (FileInputStream fin = new FileInputStream(stateFile)) {
            marshaller.unmarshall( fin, m_kieSession );
            stateFile.delete();
            LOG.info("Sucessfully restored state for engine {} from {}. There are {} elements on the working memory.", getName(), stateFile, m_kieSession.getObjects().size());
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Failed to restore state for engine {} from {}.", getName(), stateFile, e);
        }
    }

    @Override
    public boolean isReady() {
        return initialized && getConfig().isEnabled();
    }
}
