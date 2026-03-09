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
package org.opennms.core.event.forwarder.kafka;

import java.util.List;

import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates and populates an in-memory {@link EventConfDao} from the database
 * on startup. The populated DAO is exposed via {@link #getEventConfDao()} for
 * injection into {@link KafkaEventForwarder}, enabling producer-side enrichment
 * of events with severity and alarm-data from eventconf definitions.
 */
public class EventConfInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfInitializer.class);

    private final EventConfEventDao eventConfEventDao;
    private EventConfDao eventConfDao;

    public EventConfInitializer(EventConfEventDao eventConfEventDao) {
        this.eventConfEventDao = eventConfEventDao;
    }

    public void init() {
        final long startTime = System.currentTimeMillis();
        DefaultEventConfDao dao = new DefaultEventConfDao();
        List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
        dao.loadEventsFromDB(dbEvents);
        this.eventConfDao = dao;
        final long elapsed = System.currentTimeMillis() - startTime;
        LOG.info("Loaded {} event definitions from database in {} ms", dbEvents.size(), elapsed);
    }

    public EventConfDao getEventConfDao() {
        return eventConfDao;
    }
}
