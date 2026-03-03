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
package org.opennms.web.event;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.features.events.store.EventStore;
import org.opennms.features.events.store.StoredEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.event.filter.AfterDateFilter;
import org.opennms.web.event.filter.BeforeDateFilter;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventCriteria.EventCriteriaVisitor;
import org.opennms.web.event.filter.ExactUEIFilter;
import org.opennms.web.event.filter.InterfaceFilter;
import org.opennms.web.event.filter.NodeFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WebEventRepository} implementation backed by the {@link EventStore},
 * which queries the {@code events_archive} materialized view instead of the
 * legacy {@code events} table.
 *
 * <p>The events archive is read-only — acknowledge/unacknowledge operations
 * are no-ops with a warning log, since event acknowledgement is handled at
 * the alarm level in the new architecture.</p>
 */
public class EventStoreWebEventRepository implements WebEventRepository {

    private static final Logger LOG = LoggerFactory.getLogger(EventStoreWebEventRepository.class);

    private final EventStore eventStore;

    public EventStoreWebEventRepository(EventStore eventStore) {
        this.eventStore = Objects.requireNonNull(eventStore);
    }

    @Override
    public int countMatchingEvents(EventCriteria criteria) {
        org.opennms.features.events.store.EventCriteria storeCriteria = convertCriteria(criteria);
        return (int) eventStore.count(storeCriteria);
    }

    @Override
    public int[] countMatchingEventsBySeverity(EventCriteria criteria) {
        int[] eventCounts = new int[8];
        for (int severity = OnmsSeverity.INDETERMINATE.getId(); severity <= OnmsSeverity.CRITICAL.getId(); severity++) {
            org.opennms.features.events.store.EventCriteria storeCriteria = convertCriteriaWithSeverity(criteria, severity);
            eventCounts[severity] = (int) eventStore.count(storeCriteria);
        }
        return eventCounts;
    }

    @Override
    public Event getEvent(long eventId) {
        Optional<StoredEvent> stored = eventStore.getByTsid(eventId);
        return stored.map(EventStoreWebEventRepository::toWebEvent).orElse(null);
    }

    @Override
    public Event[] getMatchingEvents(EventCriteria criteria) {
        org.opennms.features.events.store.EventCriteria storeCriteria = convertCriteria(criteria);
        List<StoredEvent> results = eventStore.findByCriteria(storeCriteria);
        return results.stream()
                .map(EventStoreWebEventRepository::toWebEvent)
                .toArray(Event[]::new);
    }

    @Override
    public void acknowledgeMatchingEvents(String user, Date timestamp, EventCriteria criteria) {
        LOG.warn("acknowledgeMatchingEvents is a no-op: events_archive is read-only. " +
                "Event acknowledgement is handled at the alarm level.");
    }

    @Override
    public void acknowledgeAll(String user, Date timestamp) {
        LOG.warn("acknowledgeAll is a no-op: events_archive is read-only. " +
                "Event acknowledgement is handled at the alarm level.");
    }

    @Override
    public void unacknowledgeMatchingEvents(EventCriteria criteria) {
        LOG.warn("unacknowledgeMatchingEvents is a no-op: events_archive is read-only. " +
                "Event acknowledgement is handled at the alarm level.");
    }

    @Override
    public void unacknowledgeAll() {
        LOG.warn("unacknowledgeAll is a no-op: events_archive is read-only. " +
                "Event acknowledgement is handled at the alarm level.");
    }

    /**
     * Convert the legacy webapp EventCriteria (visitor pattern with Hibernate filters)
     * to the new EventStore EventCriteria (builder pattern for JDBC queries).
     */
    private static org.opennms.features.events.store.EventCriteria convertCriteria(EventCriteria webCriteria) {
        CriteriaConverter converter = new CriteriaConverter();
        webCriteria.visit(converter);
        return converter.build();
    }

    private static org.opennms.features.events.store.EventCriteria convertCriteriaWithSeverity(
            EventCriteria webCriteria, int severity) {
        CriteriaConverter converter = new CriteriaConverter();
        webCriteria.visit(converter);
        converter.setSeverityExact(severity);
        return converter.build();
    }

    /**
     * Map a {@link StoredEvent} from the archive to the legacy web {@link Event} DTO.
     */
    static Event toWebEvent(StoredEvent stored) {
        Event event = new Event();
        event.id = stored.getEventTsid();
        event.uei = stored.getEventUei();
        event.dpName = stored.getEventSource() != null ? stored.getEventSource() : "";
        event.severity = OnmsSeverity.get(stored.getEventSeverity());
        event.time = Date.from(stored.getEventTime());
        event.createTime = stored.getCreatedAt() != null ? Date.from(stored.getCreatedAt()) : event.time;
        event.nodeID = stored.getNodeId() != null ? stored.getNodeId().intValue() : 0;
        event.ipAddr = stored.getIpAddress();
        event.serviceName = stored.getServiceName() != null ? stored.getServiceName() : "";
        event.logMessage = stored.getEventLogMsg();
        event.description = stored.getEventDescr();
        event.eventDisplay = "Y".equals(stored.getEventDisplay());
        event.parms = stored.getEventData() != null ? stored.getEventData() : Collections.emptyMap();
        // Fields not available in the archive — leave as null/default
        event.serviceID = 0;
        event.alarmId = 0;
        return event;
    }

    /**
     * Visitor that extracts filter parameters from the legacy webapp EventCriteria
     * and builds an EventStore EventCriteria. Uses the public typed APIs on each
     * filter class (e.g. {@code NodeFilter.getNodeId()}) rather than reflection.
     */
    private static class CriteriaConverter implements EventCriteriaVisitor<RuntimeException> {

        private final org.opennms.features.events.store.EventCriteria.Builder builder =
                org.opennms.features.events.store.EventCriteria.builder();

        private Integer severityExact;

        void setSeverityExact(int severity) {
            this.severityExact = severity;
        }

        org.opennms.features.events.store.EventCriteria build() {
            builder.eventDisplayFilter("Y");
            if (severityExact != null) {
                builder.severityGte(severityExact);
                builder.severityLte(severityExact);
            }
            return builder.build();
        }

        @Override
        public void visitAckType(AcknowledgeType ackType) {
            // The archive doesn't track ack state — all events are returned regardless
        }

        @Override
        public void visitFilter(Filter filter) {
            if (filter instanceof NodeFilter) {
                builder.nodeId((long) ((NodeFilter) filter).getNodeId());
            } else if (filter instanceof InterfaceFilter) {
                builder.ipAddress(((InterfaceFilter) filter).getIpAddress());
            } else if (filter instanceof ExactUEIFilter) {
                builder.uei(((ExactUEIFilter) filter).getUEI());
            } else if (filter instanceof SeverityFilter) {
                int sev = ((SeverityFilter) filter).getSeverity();
                builder.severityGte(sev);
                builder.severityLte(sev);
            } else if (filter instanceof AfterDateFilter) {
                Date date = ((AfterDateFilter) filter).getDate();
                if (date != null) {
                    builder.afterTime(date.toInstant());
                }
            } else if (filter instanceof BeforeDateFilter) {
                Date date = ((BeforeDateFilter) filter).getDate();
                if (date != null) {
                    builder.beforeTime(date.toInstant());
                }
            } else {
                // ServiceFilter (stores serviceId, not serviceName — can't map directly),
                // EventIdFilter (handled via getEvent()), text filters, and other
                // advanced filters are not supported by the archive's simple criteria.
                LOG.debug("Unsupported filter type for EventStore: {} ({})",
                        filter.getClass().getSimpleName(), filter.getDescription());
            }
        }

        @Override
        public void visitSortStyle(SortStyle sortStyle) {
            if (sortStyle == null) return;
            // The EventStore only supports time-based sort. Map all "descending" styles
            // to DESC and all "ascending/reverse" styles to ASC.
            switch (sortStyle) {
                case REVERSE_TIME:
                case REVERSE_ID:
                case REVERSE_SEVERITY:
                case REVERSE_NODE:
                case REVERSE_INTERFACE:
                case REVERSE_SERVICE:
                case REVERSE_LOCATION:
                case REVERSE_SYSTEMID:
                case REVERSE_POLLER:
                    builder.sortOrder(org.opennms.features.events.store.EventCriteria.SortOrder.ASC);
                    break;
                default:
                    builder.sortOrder(org.opennms.features.events.store.EventCriteria.SortOrder.DESC);
                    break;
            }
        }

        @Override
        public void visitLimit(int limit, int offset) {
            builder.limit(limit);
            builder.offset(offset);
        }
    }
}
