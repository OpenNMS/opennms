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

import java.io.IOException;
import java.io.InputStream;
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
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.common.io.ByteStreams;

/**
 * <p>DroolsCorrelationEngine class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DroolsCorrelationEngine extends AbstractCorrelationEngine {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsCorrelationEngine.class);

    private KieSession m_kieSession;
    private List<String> m_interestingEvents;
    private List<Resource> m_rules;
    private Map<String, Object> m_globals = new HashMap<>();
    private String m_name;
    private String m_assertBehaviour;
    private String m_eventProcessingMode;
    
    /** {@inheritDoc} */
    @Override
    public synchronized void correlate(final Event e) {
        LOG.debug("Begin correlation for Event {} uei: {}", e.getDbid(), e.getUei());
        m_kieSession.insert(e);
        m_kieSession.fireAllRules();
        LOG.debug("End correlation for Event {} uei: {}", e.getDbid(), e.getUei());
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void timerExpired(final Integer timerId) {
        LOG.info("Begin correlation for Timer {}", timerId);
        TimerExpired expiration  = new TimerExpired(timerId);
        m_kieSession.insert(expiration);
        m_kieSession.fireAllRules();
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
        }
        ruleBaseConfig.setEventProcessingMode(eventProcessingOption);

        KieBase kieBase = kContainer.newKieBase(ruleBaseConfig);
        m_kieSession = kieBase.newKieSession();
        m_kieSession.setGlobal("engine", this);

        for (final Map.Entry<String, Object> entry : m_globals.entrySet()) {
            m_kieSession.setGlobal(entry.getKey(), entry.getValue());
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

    public Collection<? extends Object> getKieSessionObjects() {
        return m_kieSession.getObjects();
    }

    public KieSession getKieSession() {
        return m_kieSession;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(final String name) {
        m_name = name;
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
}
