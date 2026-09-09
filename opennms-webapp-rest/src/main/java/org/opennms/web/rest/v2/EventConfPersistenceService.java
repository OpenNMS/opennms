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


import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventConfGlobalSecurityDao;
import org.opennms.netmgt.dao.support.EventConfServiceHelper;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;
import org.opennms.netmgt.model.events.EnableDisableConfSourceEventsPayload;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.model.events.EventConfSrcEnableDisablePayload;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.web.rest.v2.model.EventConfEventDeletePayload;
import org.opennms.web.rest.v2.model.EventConfEventEditRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventConfPersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfPersistenceService.class);

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Autowired
    private EventConfEventDao eventConfEventDao;

    @Autowired
    private EventConfGlobalSecurityDao eventConfGlobalSecurityDao;

    @Autowired
    private EventConfDao eventConfDao;

    private final ExecutorService eventConfExecutor =
            EventConfServiceHelper.createEventConfExecutor("load-eventConf-%d");

    @PostConstruct
    public void init() {
        // Asynchronously load events from DB in order to not to block startup
        EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao, eventConfExecutor);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistEventConfFile(final Events events, final EventConfSourceMetadataDto eventConfSourceMetadataDto) {
        // An existing source comes back row-locked (see createOrUpdateSource), which serializes us with
        // concurrent appenders (nextEventOrder locks the same row) so the 1..N numbering below cannot
        // interleave with a MAX+1 computed against the old events. A new source is invisible to other
        // transactions until we commit, so it needs no lock.
        EventConfSource source = createOrUpdateSource(eventConfSourceMetadataDto);
        eventConfEventDao.deleteBySourceId(source.getId());
        saveEvents(source, events, eventConfSourceMetadataDto.getUsername(), eventConfSourceMetadataDto.getNow());
    }

    @Transactional
    public Long addEventConfSourceEvent(final Long sourceId, final String userName, Event event) {
        final Date now = new Date();
        // Lock (and re-read) the source before touching it, so the count below is not a lost update
        EventConfSource eventConfSource = eventConfSourceDao.lockForUpdate(sourceId);
        Long eventConfId = EventConfServiceHelper.saveEvent(eventConfEventDao, eventConfSource, event, userName, now);
        eventConfSource.setEventCount(eventConfEventDao.countBySourceId(sourceId));
        eventConfSourceDao.saveOrUpdate(eventConfSource);
        return eventConfId;
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


    @Transactional
    public void updateEventConfEvent(final Long sourceId, final Long eventId, EventConfEventEditRequest payload) {

        try {
            // Keep deleteEventsForSource's compaction out while this event is rewritten, so the
            // eventOrder we hold cannot be stale by the time it is flushed
            eventConfSourceDao.lockForUpdate(sourceId);
            EventConfEvent eventConfEvent = eventConfEventDao.findBySourceIdAndEventId(sourceId,eventId);
            if (eventConfEvent == null) {
                throw new EntityNotFoundException(String.format("EventConfEvent not found for eventId=%d", eventId));
            }
            eventConfEvent.setUei(payload.getEvent().getUei());
            eventConfEvent.setEventLabel(payload.getEvent().getEventLabel());
            eventConfEvent.setDescription(payload.getEvent().getDescr());
            eventConfEvent.setEnabled(payload.getEnabled());
            eventConfEvent.setXmlContent(JaxbUtils.marshal(payload.getEvent()));
            eventConfEvent.setLastModified(new Date());
            eventConfEvent.setSeverity(EventConfServiceHelper.getValidSeverity(payload.getEvent().getSeverity()));
            eventConfEventDao.saveOrUpdate(eventConfEvent);

        } catch (Exception e) {
            throw new RuntimeException("Failed to update EventConfEvent XML for eventId=" + eventId, e);
        }
    }

    /**
     * Persists a new source. Its {@code fileOrder} is always allocated here (see {@link #allocateFileOrder}),
     * whatever the caller set, so the value is unique and the catch-all cannot end up anywhere but last.
     */
    @Transactional
    public Long createEventConfSource(final EventConfSource eventConfSource) {
        eventConfSource.setFileOrder(allocateFileOrder(eventConfSource.getName()));
        return eventConfSourceDao.save(eventConfSource);
    }

    /**
     * The one place a new source gets its {@code fileOrder}: the catch-all is pinned at 1 (the slot the
     * migration reserved for it, so it is evaluated after everything else however it is re-created),
     * every other source takes the next sequence value and is evaluated before all existing ones.
     */
    private Integer allocateFileOrder(final String sourceName) {
        return EventConfSource.CATCH_ALL_SOURCE_NAME.equals(sourceName) ? 1 : eventConfSourceDao.nextFileOrder();
    }

    /**
     * Renumbers every existing source according to an eventconf.xml {@code <event-file>} list, in one
     * transaction: referenced sources take the file's order (first listed = evaluated first), sources
     * not listed keep their relative order but are evaluated before all referenced ones (like any
     * freshly uploaded file), and the catch-all stays pinned at 1.
     * <p>
     * REQUIRES_NEW and executed under {@link EventConfSourceDao#lockFileOrders()}: the upload is one
     * non-transactional REST call made of independent steps; the uploaded files are persisted first
     * (new sources get a locked MAX+1) and this step then rewrites all values from the committed table,
     * so no value is ever reserved outside a transaction. The unique constraint on fileOrder is deferred,
     * so intermediate collisions while the rows are rewritten are allowed and only the end state is checked.
     *
     * @param eventConfOrder source names in eventconf.xml order
     * @return the resulting fileOrder per source name
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String, Integer> reorderSourcesFromEventConf(final List<String> eventConfOrder) {
        eventConfSourceDao.lockFileOrders();

        final List<EventConfSource> existing = eventConfSourceDao.findAllByFileOrder(); // ascending
        final Map<String, EventConfSource> byName = new LinkedHashMap<>();
        existing.forEach(source -> byName.put(source.getName(), source));
        final Set<String> referenced = new HashSet<>(eventConfOrder);

        final Map<String, Integer> assigned = new LinkedHashMap<>();
        int nextOrder = 2;
        // referenced, walked from the last entry so the first listed ends up highest
        for (int i = eventConfOrder.size() - 1; i >= 0; i--) {
            final String name = eventConfOrder.get(i);
            if (!EventConfSource.CATCH_ALL_SOURCE_NAME.equals(name) && byName.containsKey(name) && !assigned.containsKey(name)) {
                assigned.put(name, nextOrder++);
            }
        }
        // unreferenced, keeping their current relative order, above every referenced one
        for (EventConfSource source : existing) {
            final String name = source.getName();
            if (!EventConfSource.CATCH_ALL_SOURCE_NAME.equals(name) && !referenced.contains(name)) {
                assigned.put(name, nextOrder++);
            }
        }
        if (byName.containsKey(EventConfSource.CATCH_ALL_SOURCE_NAME)) {
            assigned.put(EventConfSource.CATCH_ALL_SOURCE_NAME, 1);
        }

        final Date now = new Date();
        assigned.forEach((name, fileOrder) -> {
            final EventConfSource source = byName.get(name);
            if (!fileOrder.equals(source.getFileOrder())) {
                source.setFileOrder(fileOrder);
                source.setLastModified(now);
                eventConfSourceDao.saveOrUpdate(source);
            }
        });
        LOG.info("Renumbered {} event-conf sources from an eventconf.xml with {} entries", assigned.size(), eventConfOrder.size());
        return assigned;
    }

    private EventConfSource createOrUpdateSource(final EventConfSourceMetadataDto eventConfSourceMetadataDto) {
        EventConfSource source = eventConfSourceDao.findByName(eventConfSourceMetadataDto.getFilename());
        if (source == null) {
            source = new EventConfSource();
            source.setCreatedTime(eventConfSourceMetadataDto.getNow());
        } else {
            // Lock first, then re-read: only then may the row be modified. Otherwise the fileOrder of
            // the findByName snapshot would be flushed back over a renumbering committed in between.
            source = eventConfSourceDao.lockForUpdate(source.getId());
        }
        // an existing source keeps the position it holds under the lock, a new one is allocated
        if (source.getFileOrder() == null) {
            source.setFileOrder(allocateFileOrder(eventConfSourceMetadataDto.getFilename()));
        }
        source.setName(eventConfSourceMetadataDto.getFilename());
        source.setEventCount(eventConfSourceMetadataDto.getEventCount());
        source.setEnabled(true);
        source.setUploadedBy(eventConfSourceMetadataDto.getUsername());
        source.setLastModified(eventConfSourceMetadataDto.getNow());
        source.setVendor(eventConfSourceMetadataDto.getVendor());
        source.setDescription(eventConfSourceMetadataDto.getDescription());
        eventConfSourceDao.saveOrUpdate(source);
        return source;
    }

    private void saveEvents(EventConfSource source, Events events, String username, Date now) {
        // All events of the source were just deleted, so numbering restarts at 1 in file order
        List<EventConfEvent> eventEntities = EventConfServiceHelper.createEventConfEventEntities(
                source, events.getEvents(), username, now, 1);
        eventConfEventDao.saveAll(eventEntities);
    }

    @PreDestroy
    public void shutdown() {
        eventConfExecutor.shutdown();
    }

    public  void reloadEventsIntoMemory() {
        // Schedule reload only AFTER transaction commits
        EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao, eventConfExecutor);
    }

    public Map<String, Object> filterConfEventsBySourceId(Long sourceId, String eventFilter, String eventSortBy,
                                                          String eventOrder, Integer totalRecords,  Integer offset,
                                                          Integer limit) {
        return eventConfEventDao.findBySourceId(sourceId, eventFilter, eventSortBy, eventOrder, totalRecords,  offset, limit);
    }

    public Map<String, Object> filterEventConfSource(String filter, String sortBy, String order, Integer totalRecords, Integer offset, Integer limit) {
        return eventConfSourceDao.filterEventConfSource(filter, sortBy, order, totalRecords, offset, limit);
    }

    @Transactional
    public void deleteEventsForSource(final Long sourceId, final EventConfEventDeletePayload eventConfEventDeletePayload) throws Exception {
        if (eventConfEventDeletePayload.getEventIds() == null || eventConfEventDeletePayload.getEventIds().isEmpty()) {
            throw new IllegalArgumentException("Event IDs to delete must not be empty");
        }

        // Lock the source before looking at anything (throws EntityNotFoundException if it is gone):
        // appenders and other deleters wait here, so the event set read below is the one we decide on
        final EventConfSource source = eventConfSourceDao.lockForUpdate(sourceId);
        final Set<Long> databaseEventIds = source.getEvents()
                .stream()
                .map(EventConfEvent::getId)
                .collect(Collectors.toSet());

        final var requestEventIds = eventConfEventDeletePayload.getEventIds();
        final var existingEventIds = requestEventIds.stream()
                .filter(databaseEventIds::contains)
                .toList();

        if (existingEventIds.isEmpty()) {
            throw new EntityNotFoundException("No matching events found in database for deletion. Request IDs: " + requestEventIds);
        }
        final int deleteCount = existingEventIds.size();

        // Decide on the rows actually present under the lock, never on the eventCount column
        if (deleteCount == databaseEventIds.size()) {
            LOG.info("Deleting entire sourceId={} as all {} events are removed.", sourceId, deleteCount);
            eventConfSourceDao.delete(source);
        } else {
            eventConfEventDao.deleteByEventIds(sourceId, existingEventIds);
            eventConfEventDao.compactEventOrder(sourceId);
            final int remaining = eventConfEventDao.countBySourceId(sourceId);
            LOG.info("Deleted {} events from sourceId={} (remaining count={})", deleteCount, sourceId, remaining);
            source.setEventCount(remaining);
            eventConfSourceDao.saveOrUpdate(source);
        }
    }
}
