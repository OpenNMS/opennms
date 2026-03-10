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
package org.opennms.core.daemon.loader;

import java.util.List;

import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Loads event definitions from the database into the in-memory EventConfDao
 * on startup. In the monolith, this is done by EventConfPersistenceService
 * in the webapp context. In standalone daemon containers, this initializer
 * replaces that role.
 */
public class EventConfInitializer implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfInitializer.class);

    private final EventConfEventDao eventConfEventDao;
    private final EventConfDao eventConfDao;

    public EventConfInitializer(EventConfEventDao eventConfEventDao, EventConfDao eventConfDao) {
        this.eventConfEventDao = eventConfEventDao;
        this.eventConfDao = eventConfDao;
    }

    @Override
    public void afterPropertiesSet() {
        final long startTime = System.currentTimeMillis();
        List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
        eventConfDao.loadEventsFromDB(dbEvents);
        final long elapsed = System.currentTimeMillis() - startTime;
        LOG.info("Loaded {} event definitions from database in {} ms", dbEvents.size(), elapsed);
    }
}
