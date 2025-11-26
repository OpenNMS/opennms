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
package org.opennms.config.upgrade;

import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.JsonUtils;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.xml.eventconf.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EventConfUpgrader {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfUpgrader.class);

    private final EventConfEventDao eventConfEventDao;

    public EventConfUpgrader(EventConfEventDao eventConfEventDao) {
        this.eventConfEventDao = eventConfEventDao;
    }

    /**
     * Run upgrade process on all enabled EventConfEvent entries.
     */
    public void runContentUpgrade() {
        LOG.info("Starting EventConfEvent upgrade...");

        List<EventConfEvent> eventConfEventList = eventConfEventDao.findEnabledEvents();
        LOG.info("Found {} enabled event definitions.", eventConfEventList.size());

        AtomicInteger counter = new AtomicInteger();
        final int batchSize = 50;

        eventConfEventList.forEach(event -> {
            try {
                updateEventConfEvent(event);

                // Batch flush every 50 records
                if (counter.incrementAndGet() % batchSize == 0) {
                    LOG.info("Flushing Hibernate session at record {}", counter.get());
                    eventConfEventDao.flush();
                    eventConfEventDao.clear();
                }

            } catch (Exception e) {
                LOG.warn("Failed to upgrade EventConfEvent with ID {}. Skipping this record.",
                        event.getId(), e);
            }
        });

        // Final flush after loop
        eventConfEventDao.flush();
        eventConfEventDao.clear();

        LOG.info("EventConfEvent upgrade completed successfully.");
    }

    /**
     * Update a single EventConfEvent:
     *  - Unmarshal XML into Event
     *  - Marshal Event into JSON
     *  - Update entity & save
     */
    public void updateEventConfEvent(EventConfEvent event) {
        LOG.debug("Upgrading EventConfEvent id={} uei={}", event.getId(), event.getUei());

        // Unmarshal XML
        Event xmlEvent = JaxbUtils.unmarshal(Event.class, event.getXmlContent());

        // Convert to JSON
        String json = JsonUtils.marshal(xmlEvent);

        // Update entity
        event.setContent(json);
        event.setLastModified(new Date());
        event.setModifiedBy("event-upgradation-job");

        // Save to DB
        eventConfEventDao.saveOrUpdate(event);

        LOG.debug("Successfully upgraded EventConfEvent id={}", event.getId());
    }
}