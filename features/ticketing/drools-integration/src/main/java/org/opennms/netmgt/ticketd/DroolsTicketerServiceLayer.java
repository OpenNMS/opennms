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
package org.opennms.netmgt.ticketd;

import java.util.Map;

import org.drools.io.FileSystemResource;
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
