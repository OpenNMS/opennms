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
package org.opennms.web.services;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.xml.eventconf.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing programmatically generated events (e.g., from ThresholdController).
 * This service persists events to the database under the "opennms.programmatic.events" source
 * and reloads them into memory via EventConfDao.
 */
public class EventConfProgrammaticService {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfProgrammaticService.class);

    private static final String PROGRAMMATIC_SOURCE_NAME = "opennms.programmatic.events";
    private static final String VENDOR = "opennms";

    private EventConfSourceDao eventConfSourceDao;
    private EventConfEventDao eventConfEventDao;
    private EventConfDao eventConfDao;

    private final ThreadFactory eventConfThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("load-eventConf-programmatic-%d")
            .build();

    private final ExecutorService eventConfExecutor = Executors.newSingleThreadExecutor(eventConfThreadFactory);

    /**
     * Saves an event to the programmatic events source in the database.
     * Creates the source if it doesn't exist.
     *
     * @param event The event to save
     * @param username The username of the user creating the event
     */
    @Transactional
    public void saveEventToDB(Event event, String username) {
        EventConfSource source = getOrCreateProgrammaticSource();
        saveEvent(source, event, username, new Date());

        // Update event count
        source.setEventCount(source.getEventCount() + 1);
        eventConfSourceDao.save(source);

        // Reload events into memory
        reloadEventsFromDB();
    }

    /**
     * Gets or creates the programmatic events source.
     *
     * @return The EventConfSource for programmatic events
     */
    private EventConfSource getOrCreateProgrammaticSource() {
        EventConfSource source = eventConfSourceDao.findByName(PROGRAMMATIC_SOURCE_NAME);
        if (source == null) {
            LOG.info("Creating new programmatic event source: {}", PROGRAMMATIC_SOURCE_NAME);
            source = new EventConfSource();
            source.setName(PROGRAMMATIC_SOURCE_NAME);
            source.setVendor(VENDOR);
            source.setDescription("Programmatically generated events (e.g., from thresholds)");
            source.setEnabled(true);
            source.setEventCount(0);
            source.setUploadedBy("system");
            Date now = new Date();
            source.setCreatedTime(now);
            source.setLastModified(now);

            // Get max file order and add 1 to ensure programmatic events are loaded last
            Integer maxFileOrder = eventConfSourceDao.findMaxFileOrder();
            source.setFileOrder(maxFileOrder != null ? maxFileOrder + 1 : 1);

            eventConfSourceDao.saveOrUpdate(source);
        }
        return source;
    }

    /**
     * Saves a single event to the database.
     *
     * @param source The EventConfSource to associate with this event
     * @param event The event to save
     * @param username The username of the user creating the event
     * @param now The current timestamp
     */
    private void saveEvent(EventConfSource source, Event event, String username, Date now) {
        EventConfEvent eventConfEvent = new EventConfEvent();
        eventConfEvent.setSource(source);
        eventConfEvent.setUei(event.getUei());
        eventConfEvent.setEventLabel(event.getEventLabel());
        eventConfEvent.setDescription(event.getDescr());
        eventConfEvent.setEnabled(true);
        eventConfEvent.setXmlContent(JaxbUtils.marshal(event));
        eventConfEvent.setCreatedTime(now);
        eventConfEvent.setLastModified(now);
        eventConfEvent.setModifiedBy(username);
        eventConfEventDao.save(eventConfEvent);
    }

    /**
     * Reloads all enabled events from the database into memory.
     */
    public void reloadEventsFromDB() {
        eventConfExecutor.execute(() -> {
            List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
            LOG.info("Reloading {} events from database into memory", dbEvents.size());
            eventConfDao.loadEventsFromDB(dbEvents);
        });
    }

    /**
     * Sets the EventConfSourceDao.
     *
     * @param eventConfSourceDao The DAO for EventConfSource
     */
    public void setEventConfSourceDao(EventConfSourceDao eventConfSourceDao) {
        this.eventConfSourceDao = eventConfSourceDao;
    }

    /**
     * Sets the EventConfEventDao.
     *
     * @param eventConfEventDao The DAO for EventConfEvent
     */
    public void setEventConfEventDao(EventConfEventDao eventConfEventDao) {
        this.eventConfEventDao = eventConfEventDao;
    }

    /**
     * Sets the EventConfDao.
     *
     * @param eventConfDao The DAO for EventConf
     */
    public void setEventConfDao(EventConfDao eventConfDao) {
        this.eventConfDao = eventConfDao;
    }

    public void shutdown() {
        eventConfExecutor.shutdown();
    }
}
