/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketd;

import java.util.Map;

import org.drools.core.io.impl.FileSystemResource;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.netmgt.model.OnmsAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenNMS Trouble Ticket API implementation.
 *
 * @author jwhite
 */
public class DroolsTicketerServiceLayer extends DefaultTicketerServiceLayer {
    private static final Logger LOG = LoggerFactory.getLogger(DroolsTicketerServiceLayer.class);
    private final DroolsTicketerConfigDao m_configDao;
    private KieBase m_kieBase;

    public DroolsTicketerServiceLayer() {
        this(new DroolsTicketerConfigDao());
    }

    public DroolsTicketerServiceLayer(DroolsTicketerConfigDao configDao) {
        m_configDao = configDao;
        m_kieBase = createKieBase();
    }

    public DroolsTicketerConfigDao getConfigDao() {
        return m_configDao;
    }

    @Override
    public void reloadTicketer() {
        LOG.debug("reloadTicketer: Reloading ticketer");
        m_kieBase = createKieBase();
    }

    private KieBase createKieBase() {
        return new KieHelper()
            .addResource(new FileSystemResource(m_configDao.getRulesFile()))
            .build();
    }

    /**
     * Called from API implemented method after successful retrieval of Alarm.
     *
     * @param alarm OpenNMS Model class alarm
     * @param attributes
     * @return OpenNMS Ticket processed by Drools logic.
     */
    @Override
    protected Ticket createTicketFromAlarm(OnmsAlarm alarm, Map<String, String> attributes) {
        LOG.debug("Initializing ticket from alarm: {}", alarm);

        // Call superclass method if the knowledge-base was not properly created.
        if (m_kieBase == null) {
            LOG.error("KieContainer is NULL, creating basic ticket from alarm.");
            return super.createTicketFromAlarm(alarm, attributes);
        }

        Ticket ticket = new Ticket();
        KieSession session = m_kieBase.newKieSession();
        try {
            // Pass the ticket as a global - the logic will fill the appropriate fields
            session.setGlobal("ticket", ticket);
            // Pass the alarm and the node objects
            session.insert(alarm);
            session.insert(alarm.getNode());
            session.fireAllRules();
        } finally {
            session.dispose();
        }

        LOG.debug("Successfully initialized ticket: {} from alarm: {}.", ticket, alarm);
        return ticket;
    }
}
