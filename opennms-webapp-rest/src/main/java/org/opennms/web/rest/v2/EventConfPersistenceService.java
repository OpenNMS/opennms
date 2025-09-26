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
package org.opennms.web.rest.v2;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;
import org.opennms.netmgt.model.events.EnableDisableConfSourceEventsPayload;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.model.events.EventConfSrcEnableDisablePayload;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.web.rest.v2.model.EventConfEventDeletePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.Map;

@Service
public class EventConfPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfPersistenceService.class);

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Autowired
    private EventConfEventDao eventConfEventDao;

    @Autowired
    private EventConfDao eventConfDao;

    private final ThreadFactory eventConfThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("load-eventConf-%d")
            .build();

    private final ExecutorService eventConfExecutor = Executors.newSingleThreadExecutor(eventConfThreadFactory);

    @PostConstruct
    public void init() {
        // Asynchronously load events from DB in order to not to block startup
        // TODO: Uncomment when we are ready to disable loading from filesystem
        //eventConfExecutor.execute(this::reloadEventsFromDB);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistEventConfFile(final Events events, final EventConfSourceMetadataDto eventConfSourceMetadataDto) {
        EventConfSource source = createOrUpdateSource(eventConfSourceMetadataDto);
        eventConfEventDao.deleteBySourceId(source.getId());
        saveEvents(source, events, eventConfSourceMetadataDto.getUsername(), eventConfSourceMetadataDto.getNow());
        // Asynchronously load event conf from DB.
        //eventConfExecutor.execute(this::reloadEventsFromDB);
    }

    public List<EventConfEvent>  findEventConfByFilters(String uei, String vendor, String sourceName, int offset, int limit) {
        return eventConfEventDao.filterEventConf(uei, vendor, sourceName, offset, limit);
    }

    @Transactional
    public void updateSourceAndEventEnabled(final EventConfSrcEnableDisablePayload eventConfSrcEnableDisablePayload) {
        eventConfSourceDao.updateEnabledFlag(eventConfSrcEnableDisablePayload.getSourceIds(),eventConfSrcEnableDisablePayload.getEnabled(),eventConfSrcEnableDisablePayload.getCascadeToEvents());
    }


    @Transactional
    public void deleteEventConfSources(EventConfSourceDeletePayload eventConfSourceDeletePayload) throws Exception {
        eventConfSourceDao.deleteBySourceIds(eventConfSourceDeletePayload.getSourceIds());
    }

    @Transactional
    public void enableDisableConfSourcesEvents(final Long sourceId, final EnableDisableConfSourceEventsPayload enableDisableConfSourceEventsPayload) {

        eventConfEventDao.updateEventEnabledFlag(sourceId,enableDisableConfSourceEventsPayload.getEventsIds(),enableDisableConfSourceEventsPayload.isEnable());
    }

    private EventConfSource createOrUpdateSource(final EventConfSourceMetadataDto eventConfSourceMetadataDto) {
        EventConfSource source = eventConfSourceDao.findByName(eventConfSourceMetadataDto.getFilename());
        if (source == null) {
            source = new EventConfSource();
            source.setCreatedTime(eventConfSourceMetadataDto.getNow());
        }

        source.setName(eventConfSourceMetadataDto.getFilename());
        source.setFileOrder(eventConfSourceMetadataDto.getFileOrder());
        source.setEventCount(eventConfSourceMetadataDto.getEventCount());
        source.setEnabled(true);
        source.setUploadedBy(eventConfSourceMetadataDto.getUsername());
        source.setLastModified(eventConfSourceMetadataDto.getNow());
        source.setVendor(eventConfSourceMetadataDto.getVendor());
        source.setDescription(eventConfSourceMetadataDto.getDescription());
        eventConfSourceDao.saveOrUpdate(source);
        return eventConfSourceDao.get(source.getId());
    }

    private void saveEvents(EventConfSource source, Events events, String username, Date now) {
        List<EventConfEvent> eventEntities = events.getEvents().stream().map(parsed -> {
            EventConfEvent event = new EventConfEvent();
            event.setSource(source);
            event.setUei(parsed.getUei());
            event.setEventLabel(parsed.getEventLabel());
            event.setDescription(parsed.getDescr());
            event.setEnabled(true);
            event.setXmlContent(JaxbUtils.marshal(parsed));
            event.setCreatedTime(now);
            event.setLastModified(now);
            event.setModifiedBy(username);
            return event;
        }).toList();

        eventEntities.forEach(eventConfEventDao::save);
    }

    protected void reloadEventsFromDB() {
        List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
        if (dbEvents.isEmpty()) {
            return;
        }
        eventConfDao.loadEventsFromDB(dbEvents);
    }

    @PreDestroy
    public void shutdown() {
        eventConfExecutor.shutdown();
    }

    private void saveEventsToDatabase() {

        Map<String, Events> fileEventsMap = eventConfDao.getRootEvents().getLoadedEventFiles();
        int fileOrder = 1;
        for (Map.Entry<String, Events> entry : fileEventsMap.entrySet()) {
            String fileName = entry.getKey();
            if (fileName.startsWith("events/")) {
                String[] parts = fileName.split("/");
                fileName = parts[parts.length - 1];
            }
            Events events = entry.getValue();

            if (fileName.startsWith("opennms")) {
                String withoutExtension = fileName.endsWith(".xml")
                        ? fileName.substring(0, fileName.lastIndexOf(".xml"))
                        : fileName;
                EventConfSourceMetadataDto metadataDto = new EventConfSourceMetadataDto.Builder().filename(withoutExtension).now(new Date()).vendor(StringUtils.substringBefore(fileName, ".")).username("system-migration").description("").eventCount(events.getEvents().size()).fileOrder(fileOrder++).build();
                persistEventConfFile(events, metadataDto);
            }
        }
    }

    public Map<String, Object> filterConfEventsBySourceId(Long sourceId, Integer totalRecords,  Integer offset, Integer limit) {
        return eventConfEventDao.findBySourceId(sourceId, totalRecords,  offset, limit);
    }

    public Map<String, Object> filterEventConfSource(String filter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit) {
        return eventConfSourceDao.filterEventConfSource(filter, sortBy, order, totalRecords, offset, limit);
    }

    @Transactional
    public void deleteEventsForSource(Long sourceId,EventConfEventDeletePayload eventConfEventDeletePayload) throws Exception {
        eventConfEventDao.deleteByEventIds(sourceId,eventConfEventDeletePayload.getEventIds());
    }

}
