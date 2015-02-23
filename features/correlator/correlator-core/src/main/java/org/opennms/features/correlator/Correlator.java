/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
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
package org.opennms.features.correlator;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Markus Neumann
 */
@EventListener(name = "Correlator", logPrefix = "Correlator-LogPrefix")
public class Correlator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Correlator.class);
    private EventProxy eventProxy;
    private KieSession kSession;

    public void start() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer(getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        KieBase kBase = kieContainer.getKieBase();
        this.kSession = kBase.newKieSession();
    }

    public void stop() {
        if (kSession != null) {
            kSession.dispose();
            LOGGER.debug("Correlator - disposing kSession: {}", kSession);
        } else {
            LOGGER.debug("Correlator - no kSession to dispose: {}", kSession);
        }
    }

    @EventHandler(uei = EventHandler.ALL_UEIS)
    public void handleEventAll(Event e) {
        LOGGER.debug("Correlator!! ksession='{}' \t event='{}'", kSession, e);
        kSession.insert(e);
        int amountOfRulesFired = kSession.fireAllRules();
        LOGGER.debug("Correlator - Received event: {} \t fired {} rules against it. UIE was {}", e.getDbid(), amountOfRulesFired, e.getUei());
    }

    public EventProxy getEventProxy() {
        return eventProxy;
    }

    public void setEventProxy(EventProxy eventProxy) {
        this.eventProxy = eventProxy;
    }
}
