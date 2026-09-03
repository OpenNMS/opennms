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
package org.opennms.netmgt.eventd;

import java.util.List;

import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfGlobalSecurityDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfGlobalSecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Loads the event configuration during context refresh, so Eventd never expands
 * an event against a configuration that has not arrived yet.
 *
 * The DAOs are optional: this context is also built by tests with no database.
 */
public class EventConfDbBootstrap implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfDbBootstrap.class);

    private EventConfEventDao m_eventConfEventDao;
    private EventConfGlobalSecurityDao m_eventConfGlobalSecurityDao;
    private EventConfDao m_eventConfDao;

    @Autowired(required = false)
    public void setEventConfEventDao(final EventConfEventDao eventConfEventDao) {
        m_eventConfEventDao = eventConfEventDao;
    }

    @Autowired(required = false)
    public void setEventConfGlobalSecurityDao(final EventConfGlobalSecurityDao dao) {
        m_eventConfGlobalSecurityDao = dao;
    }

    @Autowired(required = false)
    public void setEventConfDao(final EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }

    @Override
    public void afterPropertiesSet() {
        if (m_eventConfEventDao == null || m_eventConfGlobalSecurityDao == null || m_eventConfDao == null) {
            LOG.debug("No database-backed event configuration in this context; nothing to load.");
            return;
        }
        try {
            final long started = System.currentTimeMillis();
            final List<EventConfEvent> events = m_eventConfEventDao.findEnabledEvents();
            final List<EventConfGlobalSecurity> security = m_eventConfGlobalSecurityDao.findAll();
            m_eventConfDao.loadEventsFromDB(events, security);
            LOG.info("Loaded {} events from the database in {} ms", events.size(),
                    System.currentTimeMillis() - started);
        } catch (final Exception e) {
            // Starting with no event configuration loses events, but refusing to
            // start loses the ability to fix it: the web application reloads the
            // configuration when it comes up.
            LOG.error("Failed to load the event configuration from the database. Events will be "
                    + "discarded until it loads.", e);
        }
    }
}
