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
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.model.events.EventConfSrcEnableDisablePayload;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

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
        // Asynchronously load events from DB.
        eventConfExecutor.execute(this::loadEventConfFromDB);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistEventConfFile(final Events events, final EventConfSourceMetadataDto eventConfSourceMetadataDto) {
        EventConfSource source = createOrUpdateSource(eventConfSourceMetadataDto);
        eventConfEventDao.deleteBySourceId(source.getId());
        saveEvents(source, events, eventConfSourceMetadataDto.getUsername(), eventConfSourceMetadataDto.getNow());
        // Asynchronously load event conf from DB.
        eventConfExecutor.execute(this::loadEventConfFromDB);
    }

    public List<EventConfEvent>  findEventConfByFilters(String uei, String vendor, String sourceName, int offset, int limit) {
        return eventConfEventDao.filterEventConf(uei, vendor, sourceName, offset, limit);
    }

    @Transactional
    public void updateSourceAndEventEnabled(final EventConfSrcEnableDisablePayload eventConfSrcEnableDisablePayload) {
        eventConfSourceDao.updateEnabledFlag(eventConfSrcEnableDisablePayload.getSourceIds(),eventConfSrcEnableDisablePayload.getEnabled(),eventConfSrcEnableDisablePayload.getCascadeToEvents());
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

    protected synchronized void loadEventConfFromDB() {
        List<EventConfEvent> dbEvents = eventConfEventDao.findEnabledEvents();
        if (dbEvents.isEmpty()) {
            return;
        }
        // Group events by source and sort by source fileOrder
        Map<String, List<EventConfEvent>> eventsBySource = dbEvents.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getSource().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Sort sources by fileOrder
        List<Map.Entry<String, List<EventConfEvent>>> sortedSources = eventsBySource.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((events1, events2) -> {
                    Integer order1 = events1.get(0).getSource().getFileOrder();
                    Integer order2 = events2.get(0).getSource().getFileOrder();
                    return Integer.compare(order1 != null ? order1 : 0, order2 != null ? order2 : 0);
                }))
                .toList();

        Events rootEvents = new Events();

        // Create Events objects per source and add to m_loadedEventFiles
        for (Map.Entry<String, List<EventConfEvent>> sourceEntry : sortedSources) {
            String sourceName = sourceEntry.getKey();
            List<EventConfEvent> sourceEvents = sourceEntry.getValue();

            Events eventsForSource = new Events();

            for (EventConfEvent dbEvent : sourceEvents) {
                String xmlContent = dbEvent.getXmlContent();
                if (xmlContent != null && !xmlContent.trim().isEmpty()) {
                    try {
                        Event event = JaxbUtils.unmarshal(Event.class, xmlContent);
                        eventsForSource.addEvent(event);
                    } catch (Exception e) {
                        LOG.warn("Failed to parse event XML content for UEI {}: {}", dbEvent.getUei(), e.getMessage());
                    }
                }
            }

            rootEvents.addLoadedEventFile(sourceName, eventsForSource);
        }

        eventConfDao.loadEventsFromDB(rootEvents);
    }

    @PreDestroy
    public void shutdown(){
        eventConfExecutor.shutdown();
    }
}
