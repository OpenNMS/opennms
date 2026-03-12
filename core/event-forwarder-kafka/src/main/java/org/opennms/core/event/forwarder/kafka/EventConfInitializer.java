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
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads event definitions from the database on startup and injects the
 * resulting {@link EventConfDao} into {@link KafkaEventForwarder} for
 * producer-side enrichment of events with severity and alarm-data.
 *
 * <p>Gracefully degrades if {@link EventConfEventDao} is unavailable
 * (e.g., distributed-dao-impl failed to restart during a Karaf feature
 * refresh cycle). In that case, events are forwarded without enrichment.</p>
 */
public class EventConfInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfInitializer.class);

    private final EventConfEventDao eventConfEventDao;
    private final KafkaEventForwarder kafkaEventForwarder;

    public EventConfInitializer(EventConfEventDao eventConfEventDao, KafkaEventForwarder kafkaEventForwarder) {
        this.eventConfEventDao = eventConfEventDao;
        this.kafkaEventForwarder = kafkaEventForwarder;
    }

    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 3000;

    public void init() {
        // Start retry loop in a daemon thread to avoid blocking Blueprint context creation.
        // EventConfEventDao (from distributed-dao-impl) may not be registered yet when
        // init() runs — there's a 2-5 second race window during container startup.
        Thread initThread = new Thread(() -> {
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    final long startTime = System.currentTimeMillis();
                    DefaultEventConfDao dao = new DefaultEventConfDao();
                    List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
                    dao.loadEventsFromDB(dbEvents);
                    kafkaEventForwarder.setEventConfDao(dao);
                    final long elapsed = System.currentTimeMillis() - startTime;
                    LOG.info("Loaded {} event definitions from database in {} ms (attempt {})",
                            dbEvents.size(), elapsed, attempt);
                    return;
                } catch (Throwable t) {
                    if (attempt < MAX_RETRIES) {
                        LOG.info("EventConfEventDao not yet available (attempt {}/{}), retrying in {} ms: {}",
                                attempt, MAX_RETRIES, RETRY_DELAY_MS, t.getMessage());
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            LOG.warn("EventConfInitializer interrupted during retry wait");
                            return;
                        }
                    } else {
                        LOG.warn("EventConfEventDao is not available after {} attempts — event enrichment "
                                + "will be disabled. Cause: {} ({})", MAX_RETRIES, t.getMessage(),
                                t.getClass().getSimpleName());
                    }
                }
            }
        }, "eventconf-initializer");
        initThread.setDaemon(true);
        initThread.start();
    }
}
