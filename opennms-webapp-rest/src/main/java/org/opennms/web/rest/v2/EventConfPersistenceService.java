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
import org.opennms.web.rest.v2.model.EventConfEventMoveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.annotation.PostConstruct;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
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

    @PostConstruct
    public void init() {
        // Asynchronously load events from DB in order to not to block startup
        EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao);
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

    /**
     * Rewrites the complete evaluation order of the sources from an explicit id list: the first id is
     * evaluated first ({@code fileOrder = N+1}), the last gets 2, and the catch-all stays pinned at 1.
     * The list must name every non-catch-all source exactly once; a source added concurrently surfaces
     * as a missing-source error.
     *
     * @return the number of sources whose position actually changed
     * @throws IllegalArgumentException on duplicates, a listed catch-all, or missing sources
     * @throws EntityNotFoundException  on an id that matches no source
     */
    @Transactional
    public int reorderEventConfSources(final List<Long> orderedSourceIds) {
        if (orderedSourceIds == null || orderedSourceIds.isEmpty()) {
            throw new IllegalArgumentException("sourceIds must not be empty");
        }
        final Set<Long> unique = new HashSet<>(orderedSourceIds);
        if (unique.size() != orderedSourceIds.size()) {
            throw new IllegalArgumentException("sourceIds must not contain duplicates");
        }

        // same lock (and lock order) as the eventconf.xml renumbering and the bulk source operations
        eventConfSourceDao.lockFileOrders();
        final List<EventConfSource> existing = eventConfSourceDao.findAllByFileOrder();
        final Map<Long, EventConfSource> byId = new LinkedHashMap<>();
        existing.forEach(source -> byId.put(source.getId(), source));

        for (final Long id : orderedSourceIds) {
            final EventConfSource source = byId.get(id);
            if (source == null) {
                throw new EntityNotFoundException("EventConfSource not found for id: " + id);
            }
            if (EventConfSource.CATCH_ALL_SOURCE_NAME.equals(source.getName())) {
                throw new IllegalArgumentException(EventConfSource.CATCH_ALL_SOURCE_NAME
                        + " is pinned as the last evaluated source and cannot be reordered");
            }
        }
        final List<String> missing = existing.stream()
                .filter(source -> !EventConfSource.CATCH_ALL_SOURCE_NAME.equals(source.getName()))
                .filter(source -> !unique.contains(source.getId()))
                .map(EventConfSource::getName)
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("The order must list every source exactly once; missing: "
                    + String.join(", ", missing));
        }

        final Date now = new Date();
        int nextOrder = orderedSourceIds.size() + 1; // down to 2; 1 stays the catch-all's slot
        int updated = 0;
        for (final Long id : orderedSourceIds) {
            final EventConfSource source = byId.get(id);
            final Integer fileOrder = nextOrder--;
            if (!fileOrder.equals(source.getFileOrder())) {
                source.setFileOrder(fileOrder);
                source.setLastModified(now);
                eventConfSourceDao.saveOrUpdate(source);
                updated++;
            }
        }
        LOG.info("Reordered {} event-conf sources ({} positions changed)", orderedSourceIds.size(), updated);
        return updated;
    }

    /**
     * Moves one event within its source's evaluation order. Range shifts and the single-row set are
     * bulk updates against the {@code (source_id, event_order)} index, never a whole-source renumbering;
     * the deferred unique constraint tolerates the intermediate duplicates. Runs under the source's row
     * lock, so uploads, appends, deletes and other moves on the same source are serialized with it.
     *
     * @return the event's resulting position (unchanged for an edge no-op)
     * @throws EntityNotFoundException  on an unknown source or event
     * @throws IllegalArgumentException on an unknown mode or an out-of-range position
     */
    @Transactional
    public int moveEventConfEvent(final Long sourceId, final Long eventId, final EventConfEventMoveRequest request) {
        final EventConfEventMoveRequest.Mode mode = request.resolveMode();
        eventConfSourceDao.lockForUpdate(sourceId);
        final EventConfEvent event = eventConfEventDao.findBySourceIdAndEventId(sourceId, eventId);
        if (event == null) {
            throw new EntityNotFoundException(String.format("EventConfEvent not found for sourceId=%d, eventId=%d", sourceId, eventId));
        }
        final int current = event.getEventOrder();
        final int count = eventConfEventDao.countBySourceId(sourceId);

        switch (mode) {
            case UP: {
                final EventConfEvent neighbour = eventConfEventDao.findNeighbourByOrder(sourceId, current, true);
                return neighbour == null ? current : swapEventOrder(sourceId, event, neighbour);
            }
            case DOWN: {
                final EventConfEvent neighbour = eventConfEventDao.findNeighbourByOrder(sourceId, current, false);
                return neighbour == null ? current : swapEventOrder(sourceId, event, neighbour);
            }
            case TOP:
                return moveEventToPosition(sourceId, eventId, current, 1);
            case BOTTOM:
                return moveEventToPosition(sourceId, eventId, current, count);
            case POSITION: {
                final Integer target = request.getPosition();
                if (target == null || target < 1 || target > count) {
                    throw new IllegalArgumentException("position must be between 1 and " + count);
                }
                return moveEventToPosition(sourceId, eventId, current, target);
            }
            default:
                throw new IllegalArgumentException("Unsupported mode: " + mode);
        }
    }

    private int moveEventToPosition(final Long sourceId, final Long eventId, final int current, final int target) {
        if (target == current) {
            return current;
        }
        if (target < current) {
            eventConfEventDao.shiftEventOrder(sourceId, target, current - 1, 1);
        } else {
            eventConfEventDao.shiftEventOrder(sourceId, current + 1, target, -1);
        }
        eventConfEventDao.updateEventOrder(sourceId, eventId, target);
        return target;
    }

    private int swapEventOrder(final Long sourceId, final EventConfEvent event, final EventConfEvent neighbour) {
        final int target = neighbour.getEventOrder();
        eventConfEventDao.updateEventOrder(sourceId, event.getId(), target);
        eventConfEventDao.updateEventOrder(sourceId, neighbour.getId(), event.getEventOrder());
        return target;
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

    public  void reloadEventsIntoMemory() {
        // Schedule reload only AFTER transaction commits
        EventConfServiceHelper.reloadEventsFromDBAsync(eventConfEventDao, eventConfDao, eventConfGlobalSecurityDao);
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
