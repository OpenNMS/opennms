/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.netmgt.correlation.drools;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Message.Level;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.correlation.AbstractCorrelationEngine;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

/**
 * <p>
 * DroolsCorrelationEngine class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DroolsCorrelationEngine extends AbstractCorrelationEngine {

    private static final Logger LOG = LoggerFactory.getLogger(DroolsCorrelationEngine.class);

    private KieSession kieSession;
    private List<String> m_interestingEvents;
    private List<Resource> m_rulesResouces;
    private Map<String, Object> m_globals = new HashMap<>();
    private String m_name;
    
    //TODO this assertBehaviour setting is not implemented for now
    private String m_assertBehaviour;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void correlate(final Event e) {
        LOG.debug("Begin correlation for Event {} uei: {}", e.getDbid(), e.getUei());
        kieSession.insert(e);
        kieSession.fireAllRules();
        LOG.debug("End correlation for Event {} uei: {}", e.getDbid(), e.getUei());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void timerExpired(final Integer timerId) {
        LOG.info("Begin correlation for Timer {}", timerId);
        TimerExpired expiration = new TimerExpired(timerId);
        kieSession.insert(expiration);
        kieSession.fireAllRules();
        LOG.debug("Begin correlation for Timer {}", timerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getInterestingEvents() {
        return m_interestingEvents;
    }

    /**
     * <p>
     * setInterestingEvents</p>
     *
     * @param ueis a {@link java.util.List} object.
     */
    public void setInterestingEvents(final List<String> ueis) {
        m_interestingEvents = ueis;
    }

    /**
     * <p>
     * setRulesResources</p>
     *
     * @param rules a {@link java.util.List} object.
     */
    public void setRulesResources(final List<Resource> rules) {
        m_rulesResouces = rules;
    }

    /**
     * <p>
     * setGlobals</p>
     *
     * @param globals a {@link java.util.Map} object.
     */
    public void setGlobals(final Map<String, Object> globals) {
        m_globals = globals;
    }

    /**
     * <p>getMemorySize</p>
     *
     * @return a int.
     */
    public int getMemorySize() {
        return (int) kieSession.getFactCount();
    }

    /**
     * <p>
     * getMemoryObjects</p>
     *
     * @return a {@link java.util.List} object.
     */
    public Collection<? extends Object> getMemoryObjects() {
        return kieSession.getObjects();
    }

    /**
     * <p>
     * setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(final String name) {
        m_name = name;
    }

    /**
     * <p>
     * getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * <p>
     * setGlobal</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     */
    public void setGlobal(final String name, final Object value) {
        kieSession.setGlobal(name, value);
    }

    public void setAssertBehaviour(String assertBehaviour) {
        m_assertBehaviour = assertBehaviour;
    }

    public void initialize() throws Exception {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        for (Resource ruleFile : m_rulesResouces) {
            LOG.info("Loading ruleFile: " + ruleFile.getFilename());
            kieFileSystem.write(ResourceFactory.newFileResource(ruleFile.getFile()));
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();

        if (kieBuilder.getResults().hasMessages(Level.ERROR)) {
            List<Message> errors = kieBuilder.getResults().getMessages(Level.ERROR);
            StringBuilder sb = new StringBuilder("Errors:");
            for (Message msg : errors) {
                sb.append("\n");
                sb.append(prettyBuildMessage(msg));
            }
            throw new Exception(sb.toString());
        }
        LOG.info("KieServices built: " + toString());

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        kieSession = kieContainer.newKieSession();
        
        kieSession.setGlobal("engine", this);
        for (final Map.Entry<String, Object> entry : m_globals.entrySet()) {
            kieSession.setGlobal(entry.getKey(), entry.getValue());
        }
    }

    private static String prettyBuildMessage(Message msg) {
        return "Message: {"
                + "id=" + msg.getId()
                + ", level=" + msg.getLevel()
                + ", path=" + msg.getPath()
                + ", line=" + msg.getLine()
                + ", column=" + msg.getColumn()
                + ", text=\"" + msg.getText() + "\""
                + "}";
    }
}
