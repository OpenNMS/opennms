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

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.opennms.core.spring.BeanUtils;
import org.opennms.features.events.store.EventStore;
import org.opennms.features.events.store.StoredEvent;
import org.opennms.web.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates all querying functionality for events.
 *
 * <p>Delegates to {@link EventStore} which queries the {@code events_archive}
 * materialized view. The static factory methods preserve the original API
 * for backward compatibility with JSP pages.</p>
 *
 * @deprecated Use an injected {@link EventStore} implementation instead
 */
public class EventFactory {

    private static final Logger LOG = LoggerFactory.getLogger(EventFactory.class);

    private static volatile EventStore s_eventStore;

    private EventFactory() {
    }

    /**
     * Set the EventStore instance. Called during Spring initialization.
     */
    public static void setEventStore(EventStore eventStore) {
        s_eventStore = eventStore;
    }

    private static EventStore getEventStore() {
        EventStore store = s_eventStore;
        if (store == null) {
            store = BeanUtils.getBean("soaContext", "eventStore", EventStore.class);
            if (store != null) {
                s_eventStore = store;
            }
        }
        if (store == null) {
            throw new IllegalStateException("EventStore not initialized. " +
                    "Ensure the event-store module is deployed and EventFactory.setEventStore() is called.");
        }
        return store;
    }

    // -----------------------------------------------------------------------
    // Count methods
    // -----------------------------------------------------------------------

    public static int getEventCount() throws SQLException {
        return getEventCount(AcknowledgeType.UNACKNOWLEDGED, new Filter[0]);
    }

    public static int getEventCount(AcknowledgeType ackType, Filter[] filters) throws SQLException {
        org.opennms.features.events.store.EventCriteria criteria = buildStoreCriteria(
                filters, SortStyle.TIME, -1, -1);
        return (int) getEventStore().count(criteria);
    }

    // -----------------------------------------------------------------------
    // Single event lookup
    // -----------------------------------------------------------------------

    public static Event getEvent(long eventId) throws SQLException {
        return getEventStore().getByTsid(eventId)
                .map(EventStoreWebEventRepository::toWebEvent)
                .orElse(null);
    }

    public static Map<String, String> getParmsForEventId(long eventId) throws SQLException {
        return getEventStore().getByTsid(eventId)
                .map(StoredEvent::getEventData)
                .orElse(Collections.emptyMap());
    }

    // -----------------------------------------------------------------------
    // General event queries
    // -----------------------------------------------------------------------

    public static Event[] getEvents() throws SQLException {
        return getEvents(SortStyle.TIME, AcknowledgeType.UNACKNOWLEDGED);
    }

    public static Event[] getEvents(AcknowledgeType ackType) throws SQLException {
        return getEvents(SortStyle.TIME, ackType);
    }

    public static Event[] getEvents(SortStyle sortStyle) throws SQLException {
        return getEvents(sortStyle, AcknowledgeType.UNACKNOWLEDGED);
    }

    public static Event[] getEvents(SortStyle sortStyle, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEvents(sortStyle, ackType);
    }

    public static Event[] getEvents(SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        return getEvents(sortStyle, ackType, new Filter[0]);
    }

    public static Event[] getEvents(SortStyle sortStyle, AcknowledgeType ackType, Filter[] filters) throws SQLException {
        return getEvents(sortStyle, ackType, filters, -1, -1);
    }

    public static Event[] getEvents(SortStyle sortStyle, AcknowledgeType ackType, Filter[] filters, int limit, int offset) throws SQLException {
        org.opennms.features.events.store.EventCriteria criteria = buildStoreCriteria(
                filters, sortStyle, limit, offset);
        List<StoredEvent> results = getEventStore().findByCriteria(criteria);
        return results.stream()
                .map(EventStoreWebEventRepository::toWebEvent)
                .toArray(Event[]::new);
    }

    // -----------------------------------------------------------------------
    // Node event queries
    // -----------------------------------------------------------------------

    public static Event[] getEventsForNode(int nodeId, ServletContext servletContext) throws SQLException {
        return getEventsForNode(nodeId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1, servletContext);
    }

    public static Event[] getEventsForNode(int nodeId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForNode(nodeId, SortStyle.ID, ackType, -1, -1, servletContext);
    }

    public static Event[] getEventsForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        return getEventsForNode(nodeId, sortStyle, ackType, -1, -1, servletContext);
    }

    public static Event[] getEventsForNode(int nodeId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        org.opennms.features.events.store.EventCriteria criteria =
                org.opennms.features.events.store.EventCriteria.builder()
                        .nodeId((long) nodeId)
                        .eventDisplayFilter("Y")
                        .sortOrder(toSortOrder(sortStyle))
                        .limit(throttle > 0 ? throttle : 100)
                        .offset(Math.max(offset, 0))
                        .build();
        return getEventStore().findByCriteria(criteria).stream()
                .map(EventStoreWebEventRepository::toWebEvent)
                .toArray(Event[]::new);
    }

    public static int getEventCountForNode(int nodeId, AcknowledgeType ackType) throws SQLException {
        org.opennms.features.events.store.EventCriteria criteria =
                org.opennms.features.events.store.EventCriteria.builder()
                        .nodeId((long) nodeId)
                        .eventDisplayFilter("Y")
                        .build();
        return (int) getEventStore().count(criteria);
    }

    // -----------------------------------------------------------------------
    // Interface event queries
    // -----------------------------------------------------------------------

    public static Event[] getEventsForInterface(int nodeId, String ipAddress, ServletContext servletContext) throws SQLException {
        return getEventsForInterface(nodeId, ipAddress, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1, servletContext);
    }

    public static Event[] getEventsForInterface(int nodeId, String ipAddress, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForInterface(nodeId, ipAddress, SortStyle.ID, ackType, -1, -1, servletContext);
    }

    public static Event[] getEventsForInterface(int nodeId, String ipAddress, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        org.opennms.features.events.store.EventCriteria criteria =
                org.opennms.features.events.store.EventCriteria.builder()
                        .nodeId((long) nodeId)
                        .ipAddress(ipAddress)
                        .eventDisplayFilter("Y")
                        .sortOrder(toSortOrder(sortStyle))
                        .limit(throttle > 0 ? throttle : 100)
                        .offset(Math.max(offset, 0))
                        .build();
        return getEventStore().findByCriteria(criteria).stream()
                .map(EventStoreWebEventRepository::toWebEvent)
                .toArray(Event[]::new);
    }

    public static Event[] getEventsForInterface(int nodeId, int ifIndex, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        // The archive doesn't store ifIndex — fall back to node-only query
        return getEventsForNode(nodeId, sortStyle, ackType, throttle, offset, servletContext);
    }

    public static Event[] getEventsForInterface(String ipAddress) throws SQLException {
        return getEventsForInterface(ipAddress, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1);
    }

    public static Event[] getEventsForInterface(String ipAddress, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForInterface(ipAddress, SortStyle.ID, ackType, -1, -1);
    }

    public static Event[] getEventsForInterface(String ipAddress, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset) throws SQLException {
        org.opennms.features.events.store.EventCriteria criteria =
                org.opennms.features.events.store.EventCriteria.builder()
                        .ipAddress(ipAddress)
                        .eventDisplayFilter("Y")
                        .sortOrder(toSortOrder(sortStyle))
                        .limit(throttle > 0 ? throttle : 100)
                        .offset(Math.max(offset, 0))
                        .build();
        return getEventStore().findByCriteria(criteria).stream()
                .map(EventStoreWebEventRepository::toWebEvent)
                .toArray(Event[]::new);
    }

    // -----------------------------------------------------------------------
    // Service event queries
    // -----------------------------------------------------------------------

    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId, ServletContext servletContext) throws SQLException {
        return getEventsForService(nodeId, ipAddress, serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1, servletContext);
    }

    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForService(nodeId, ipAddress, serviceId, SortStyle.ID, ackType, -1, -1, servletContext);
    }

    public static Event[] getEventsForService(int nodeId, String ipAddress, int serviceId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        // The archive stores service_name, not service_id. Query by node+interface
        // which is the best match we can do without a service ID → name lookup.
        org.opennms.features.events.store.EventCriteria criteria =
                org.opennms.features.events.store.EventCriteria.builder()
                        .nodeId((long) nodeId)
                        .ipAddress(ipAddress)
                        .eventDisplayFilter("Y")
                        .sortOrder(toSortOrder(sortStyle))
                        .limit(throttle > 0 ? throttle : 100)
                        .offset(Math.max(offset, 0))
                        .build();
        return getEventStore().findByCriteria(criteria).stream()
                .map(EventStoreWebEventRepository::toWebEvent)
                .toArray(Event[]::new);
    }

    public static Event[] getEventsForService(int serviceId, ServletContext servletContext) throws SQLException {
        return getEventsForService(serviceId, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, -1, -1, servletContext);
    }

    public static Event[] getEventsForService(int serviceId, boolean includeAcknowledged, ServletContext servletContext) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForService(serviceId, SortStyle.ID, ackType, -1, -1, servletContext);
    }

    public static Event[] getEventsForService(int serviceId, SortStyle sortStyle, AcknowledgeType ackType, int throttle, int offset, ServletContext servletContext) throws SQLException {
        // The archive stores service_name, not service_id. Return an empty result
        // since we cannot resolve service ID to name without additional context.
        LOG.debug("getEventsForService(serviceId={}) cannot map service ID to name for events_archive", serviceId);
        return new Event[0];
    }

    public static int getEventCountForNode(int nodeId, String ipAddress, int serviceId, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        org.opennms.features.events.store.EventCriteria criteria =
                org.opennms.features.events.store.EventCriteria.builder()
                        .nodeId((long) nodeId)
                        .ipAddress(ipAddress)
                        .eventDisplayFilter("Y")
                        .build();
        return (int) getEventStore().count(criteria);
    }

    public static int getEventCountForService(int nodeId, String ipAddress, int serviceId, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        return getEventCountForNode(nodeId, ipAddress, serviceId, ackType, servletContext);
    }

    public static int getEventCountForService(int serviceId, AcknowledgeType ackType, ServletContext servletContext) throws SQLException {
        LOG.debug("getEventCountForService(serviceId={}) cannot map service ID to name for events_archive", serviceId);
        return 0;
    }

    // -----------------------------------------------------------------------
    // Severity event queries
    // -----------------------------------------------------------------------

    public static Event[] getEventsForSeverity(int severity) throws SQLException {
        return getEventsForSeverity(severity, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED);
    }

    public static Event[] getEventsForSeverity(int severity, boolean includeAcknowledged) throws SQLException {
        AcknowledgeType ackType = includeAcknowledged ? AcknowledgeType.BOTH : AcknowledgeType.UNACKNOWLEDGED;
        return getEventsForSeverity(severity, SortStyle.ID, ackType);
    }

    public static Event[] getEventsForSeverity(int severity, SortStyle sortStyle, AcknowledgeType ackType) throws SQLException {
        org.opennms.features.events.store.EventCriteria criteria =
                org.opennms.features.events.store.EventCriteria.builder()
                        .severityGte(severity)
                        .severityLte(severity)
                        .eventDisplayFilter("Y")
                        .sortOrder(toSortOrder(sortStyle))
                        .build();
        return getEventStore().findByCriteria(criteria).stream()
                .map(EventStoreWebEventRepository::toWebEvent)
                .toArray(Event[]::new);
    }

    // -----------------------------------------------------------------------
    // Poller event queries
    // -----------------------------------------------------------------------

    public static Event[] getEventsForPoller(String poller) throws SQLException {
        return getEventsForPoller(poller, false);
    }

    public static Event[] getEventsForPoller(String poller, boolean includeAcknowledged) throws SQLException {
        // The archive doesn't store poller/system label — return all events
        LOG.debug("getEventsForPoller is not supported by events_archive, returning empty result");
        return new Event[0];
    }

    // -----------------------------------------------------------------------
    // Acknowledge/unacknowledge (no-ops for read-only archive)
    // -----------------------------------------------------------------------

    public static void acknowledge(Event[] events, String user) throws SQLException {
        LOG.warn("acknowledge is a no-op: events_archive is read-only");
    }

    public static void acknowledge(Event[] events, String user, Date time) throws SQLException {
        LOG.warn("acknowledge is a no-op: events_archive is read-only");
    }

    public static void acknowledge(long[] eventIds, String user) throws SQLException {
        LOG.warn("acknowledge is a no-op: events_archive is read-only");
    }

    public static void acknowledge(long[] eventIds, String user, Date time) throws SQLException {
        LOG.warn("acknowledge is a no-op: events_archive is read-only");
    }

    public static void acknowledge(Filter[] filters, String user) throws SQLException {
        LOG.warn("acknowledge is a no-op: events_archive is read-only");
    }

    public static void acknowledge(Filter[] filters, String user, Date time) throws SQLException {
        LOG.warn("acknowledge is a no-op: events_archive is read-only");
    }

    public static void acknowledgeAll(String user) throws SQLException {
        LOG.warn("acknowledgeAll is a no-op: events_archive is read-only");
    }

    public static void acknowledgeAll(String user, Date time) throws SQLException {
        LOG.warn("acknowledgeAll is a no-op: events_archive is read-only");
    }

    public static void unacknowledge(Event[] events) throws SQLException {
        LOG.warn("unacknowledge is a no-op: events_archive is read-only");
    }

    public static void unacknowledge(long[] eventIds) throws SQLException {
        LOG.warn("unacknowledge is a no-op: events_archive is read-only");
    }

    public static void unacknowledge(Filter[] filters) throws SQLException {
        LOG.warn("unacknowledge is a no-op: events_archive is read-only");
    }

    public static void unacknowledgeAll() throws SQLException {
        LOG.warn("unacknowledgeAll is a no-op: events_archive is read-only");
    }

    // -----------------------------------------------------------------------
    // Sort/criteria helpers
    // -----------------------------------------------------------------------

    protected static String getOrderByClause(SortStyle sortStyle) {
        if (sortStyle == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        return sortStyle.getOrderByClause();
    }

    protected static String getAcknowledgeTypeClause(AcknowledgeType ackType) {
        if (ackType == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        return ackType.getAcknowledgeTypeClause();
    }

    private static org.opennms.features.events.store.EventCriteria.SortOrder toSortOrder(SortStyle sortStyle) {
        if (sortStyle == null) {
            return org.opennms.features.events.store.EventCriteria.SortOrder.DESC;
        }
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
                return org.opennms.features.events.store.EventCriteria.SortOrder.ASC;
            default:
                return org.opennms.features.events.store.EventCriteria.SortOrder.DESC;
        }
    }

    /**
     * Build an EventStore criteria from legacy filter array, sort style, and pagination.
     * Extracts semantic filter values using the public typed APIs on each filter class.
     */
    private static org.opennms.features.events.store.EventCriteria buildStoreCriteria(
            Filter[] filters, SortStyle sortStyle, int limit, int offset) {
        org.opennms.features.events.store.EventCriteria.Builder builder =
                org.opennms.features.events.store.EventCriteria.builder()
                        .eventDisplayFilter("Y")
                        .sortOrder(toSortOrder(sortStyle));

        if (limit > 0) {
            builder.limit(limit);
        }
        if (offset > 0) {
            builder.offset(offset);
        }

        if (filters != null) {
            for (Filter filter : filters) {
                applyFilter(builder, filter);
            }
        }

        return builder.build();
    }

    private static void applyFilter(org.opennms.features.events.store.EventCriteria.Builder builder, Filter filter) {
        if (filter instanceof org.opennms.web.event.filter.NodeFilter) {
            builder.nodeId((long) ((org.opennms.web.event.filter.NodeFilter) filter).getNodeId());
        } else if (filter instanceof org.opennms.web.event.filter.InterfaceFilter) {
            builder.ipAddress(((org.opennms.web.event.filter.InterfaceFilter) filter).getIpAddress());
        } else if (filter instanceof org.opennms.web.event.filter.ExactUEIFilter) {
            builder.uei(((org.opennms.web.event.filter.ExactUEIFilter) filter).getUEI());
        } else if (filter instanceof org.opennms.web.event.filter.SeverityFilter) {
            int sev = ((org.opennms.web.event.filter.SeverityFilter) filter).getSeverity();
            builder.severityGte(sev);
            builder.severityLte(sev);
        } else if (filter instanceof org.opennms.web.event.filter.AfterDateFilter) {
            Date date = ((org.opennms.web.event.filter.AfterDateFilter) filter).getDate();
            if (date != null) {
                builder.afterTime(date.toInstant());
            }
        } else if (filter instanceof org.opennms.web.event.filter.BeforeDateFilter) {
            Date date = ((org.opennms.web.event.filter.BeforeDateFilter) filter).getDate();
            if (date != null) {
                builder.beforeTime(date.toInstant());
            }
        } else {
            LOG.debug("Unsupported filter type for EventStore: {} ({})",
                    filter.getClass().getSimpleName(), filter.getDescription());
        }
    }
}
