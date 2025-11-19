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
package org.opennms.netmgt.dao.support;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.JsonUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.xml.eventconf.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * Helper class containing common utility methods for EventConf services.
 * This class provides shared functionality for persisting and loading event configurations
 * to avoid code duplication across different service implementations.
 */
public class EventConfServiceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfServiceHelper.class);

    /**
     * Creates and persists an EventConfEvent entity from an Event XML object.
     *
     * @param eventConfEventDao The DAO for persisting EventConfEvent entities
     * @param source The EventConfSource to associate with this event
     * @param event The Event XML object to persist
     * @param username The username of the user creating the event
     * @param timestamp The timestamp for creation and modification
     * @return The ID of the saved EventConfEvent
     */
    public static Long saveEvent(EventConfEventDao eventConfEventDao, EventConfSource source,
                                  Event event, String username, Date timestamp) {
        EventConfEvent eventConfEvent = new EventConfEvent();
        eventConfEvent.setSource(source);
        eventConfEvent.setUei(event.getUei());
        eventConfEvent.setEventLabel(event.getEventLabel());
        eventConfEvent.setDescription(event.getDescr());
        eventConfEvent.setEnabled(true);
        eventConfEvent.setXmlContent(JaxbUtils.marshal(event));
        eventConfEvent.setContent(JsonUtils.marshal(event));
        eventConfEvent.setCreatedTime(timestamp);
        eventConfEvent.setLastModified(timestamp);
        eventConfEvent.setModifiedBy(username);
        return eventConfEventDao.save(eventConfEvent);
    }

    /**
     * Reloads all enabled events from the database into memory synchronously.
     *
     * @param eventConfEventDao The DAO for retrieving EventConfEvent entities
     * @param eventConfDao      The DAO for loading events into memory
     */
    public static void reloadEventsFromDB(EventConfEventDao eventConfEventDao, EventConfDao eventConfDao) {
        final long startTime = System.currentTimeMillis();
        List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
        eventConfDao.loadEventsFromDB(dbEvents);
        final long endTime = System.currentTimeMillis();
        LOG.info("Time to reload {} events from DB: {} ms", dbEvents.size(), (endTime - startTime));
    }

    /**
     * Reloads all enabled events from the database into memory asynchronously.
     *
     * @param eventConfEventDao The DAO for retrieving EventConfEvent entities
     * @param eventConfDao The DAO for loading events into memory
     * @param executor The ExecutorService to use for async execution
     */
    public static void reloadEventsFromDBAsync(EventConfEventDao eventConfEventDao,
                                                 EventConfDao eventConfDao,
                                                 ExecutorService executor) {
        executor.execute(() -> reloadEventsFromDB(eventConfEventDao, eventConfDao));
    }

    /**
     * Creates a single-threaded executor with a custom thread factory for EventConf operations.
     *
     * @param threadNameFormat The format string for thread names (e.g., "load-eventConf-%d")
     * @return A configured ExecutorService
     */
    public static ExecutorService createEventConfExecutor(String threadNameFormat) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(threadNameFormat)
                .build();
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    /**
     * Creates EventConfEvent entities from a list of Event XML objects.
     * This is useful for bulk operations where multiple events need to be persisted.
     *
     * @param source The EventConfSource to associate with these events
     * @param events The list of Event XML objects to convert
     * @param username The username of the user creating these events
     * @param timestamp The timestamp for creation and modification
     * @return A list of EventConfEvent entities ready to be persisted
     */
    public static List<EventConfEvent> createEventConfEventEntities(EventConfSource source,
                                                                      List<Event> events,
                                                                      String username,
                                                                      Date timestamp) {
        return events.stream().map(parsed -> {
            EventConfEvent event = new EventConfEvent();
            event.setSource(source);
            event.setUei(parsed.getUei());
            event.setEventLabel(parsed.getEventLabel());
            event.setDescription(parsed.getDescr());
            event.setEnabled(true);
            event.setXmlContent(JaxbUtils.marshal(parsed));
            event.setContent(JsonUtils.marshal(parsed));
            event.setCreatedTime(timestamp);
            event.setLastModified(timestamp);
            event.setModifiedBy(username);
            return event;
        }).collect(Collectors.toList());
    }
}
