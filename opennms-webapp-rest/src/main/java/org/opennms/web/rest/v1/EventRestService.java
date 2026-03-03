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
package org.opennms.web.rest.v1;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opennms.features.events.store.EventCriteria;
import org.opennms.features.events.store.EventStore;
import org.opennms.features.events.store.StoredEvent;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.web.rest.mapper.v2.StoredEventMapper;
import org.opennms.web.rest.model.v2.EventCollectionDTO;
import org.opennms.web.rest.model.v2.EventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("eventRestService")
@Path("events")
@Tag(name = "Events", description = "Events API")
public class EventRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(EventRestService.class);
    private static final DateTimeFormatter ISO8601_FORMATTER_MILLIS = ISODateTimeFormat.dateTime();
    private static final DateTimeFormatter ISO8601_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final int DEFAULT_LIMIT = 10;

    @Autowired
    private EventStore m_eventStore;

    @Autowired
    private EventConfDao m_eventConfDao;

    @Autowired
    private EventIpcManager m_eventForwarder;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{eventId}")
    public EventDTO getEvent(@PathParam("eventId") final Long eventId) {
        Optional<StoredEvent> event = m_eventStore.getByTsid(eventId);
        if (event.isEmpty()) {
            throw getException(Status.NOT_FOUND, "Event object {} was not found.", Long.toString(eventId));
        }
        return getMapper().toEventDTO(event.get());
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    public String getCount() {
        return Long.toString(m_eventStore.count(EventCriteria.builder().build()));
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public EventCollectionDTO getEvents(@Context final UriInfo uriInfo) {
        EventCriteria criteria = buildCriteria(uriInfo.getQueryParameters(), EventCriteria.SortOrder.ASC);

        List<StoredEvent> events = m_eventStore.findByCriteria(criteria);
        long totalCount = m_eventStore.count(criteria);

        StoredEventMapper mapper = getMapper();
        List<EventDTO> dtos = events.stream()
                .map(mapper::toEventDTO)
                .collect(Collectors.toList());

        EventCollectionDTO collection = new EventCollectionDTO(dtos);
        collection.setTotalCount((int) totalCount);
        return collection;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("between")
    public EventCollectionDTO getEventsBetween(@Context final UriInfo uriInfo) {
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        Instant begin;
        if (params.containsKey("begin")) {
            begin = parseDate(params.getFirst("begin")).toInstant();
        } else {
            begin = Instant.EPOCH;
        }

        Instant end;
        if (params.containsKey("end")) {
            end = parseDate(params.getFirst("end")).toInstant();
        } else {
            end = Instant.now();
        }

        EventCriteria.Builder builder = EventCriteria.builder()
                .afterTime(begin)
                .beforeTime(end);

        applyCommonParams(builder, params);

        EventCriteria criteria = builder.build();
        List<StoredEvent> events = m_eventStore.findByCriteria(criteria);
        long totalCount = m_eventStore.count(criteria);

        StoredEventMapper mapper = getMapper();
        List<EventDTO> dtos = events.stream()
                .map(mapper::toEventDTO)
                .collect(Collectors.toList());

        EventCollectionDTO collection = new EventCollectionDTO(dtos);
        collection.setTotalCount((int) totalCount);
        return collection;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response publishEvent(final org.opennms.netmgt.xml.event.Event event) {
        if (event.getSource() == null) {
            event.setSource("ReST");
        }
        if (event.getTime() == null) {
            event.setTime(new Date());
        }
        try {
            final Validator validator = factory.getValidator();
            final Set<ConstraintViolation<org.opennms.netmgt.xml.event.Event>> errors = validator.validate(event);
            LOG.debug("got errors: {}", errors);
            if (errors.size() > 0) {
                final StringBuilder sb = new StringBuilder("Error validating event:\n");
                for (final ConstraintViolation<?> error : errors) {
                    sb.append(error.toString()).append("\n");
                }
                LOG.debug(sb.toString());
                throw getException(Status.BAD_REQUEST, errors.size() + " errors found while validating event.");
            }
            m_eventForwarder.sendNow(event);
            return Response.accepted().build();
        } catch (final Exception e) {
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }
    }

    private EventCriteria buildCriteria(MultivaluedMap<String, String> params, EventCriteria.SortOrder defaultSort) {
        EventCriteria.Builder builder = EventCriteria.builder()
                .sortOrder(defaultSort);

        applyCommonParams(builder, params);

        return builder.build();
    }

    private void applyCommonParams(EventCriteria.Builder builder, MultivaluedMap<String, String> params) {
        if (params.containsKey("limit")) {
            builder.limit(Integer.parseInt(params.getFirst("limit")));
        } else {
            builder.limit(DEFAULT_LIMIT);
        }
        if (params.containsKey("offset")) {
            builder.offset(Integer.parseInt(params.getFirst("offset")));
        }
        if (params.containsKey("eventUei")) {
            builder.uei(params.getFirst("eventUei"));
        }
        if (params.containsKey("node.id")) {
            builder.nodeId(Long.parseLong(params.getFirst("node.id")));
        }
        if (params.containsKey("ipAddr")) {
            builder.ipAddress(params.getFirst("ipAddr"));
        }
        if (params.containsKey("eventDisplay")) {
            builder.eventDisplayFilter(params.getFirst("eventDisplay"));
        }
    }

    private Date parseDate(String dateStr) {
        try {
            return ISO8601_FORMATTER.parseLocalDateTime(dateStr).toDate();
        } catch (final Throwable t1) {
            try {
                return ISO8601_FORMATTER_MILLIS.parseDateTime(dateStr).toDate();
            } catch (final Throwable t2) {
                throw getException(Status.BAD_REQUEST, "Can't parse date: " + dateStr);
            }
        }
    }

    private StoredEventMapper getMapper() {
        return new StoredEventMapper(m_eventConfDao);
    }
}
