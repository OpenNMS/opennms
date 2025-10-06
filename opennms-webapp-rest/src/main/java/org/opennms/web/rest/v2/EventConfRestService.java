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

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.events.EnableDisableConfSourceEventsPayload;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfEventDto;
import org.opennms.netmgt.model.events.EventConfSourceMetadataDto;
import org.opennms.netmgt.model.events.EventConfSrcEnableDisablePayload;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.web.rest.v2.api.EventConfRestApi;
import org.opennms.web.rest.v2.model.EventConfSourceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class EventConfRestService implements EventConfRestApi {

    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Override
    @Transactional
    public Response uploadEventConfFiles(final List<Attachment> attachments, final SecurityContext securityContext) {
        final String username = getUsername(securityContext);
        final Date now = new Date();
        int maxFileOrder = Optional.ofNullable(eventConfSourceDao.findMaxFileOrder()).orElse(0);

        final Map<String, Attachment> fileMap = attachments.stream()
                .collect(Collectors.toMap(
                        a -> stripExtension(a.getContentDisposition().getParameter("filename")),
                        a -> a,
                        (a1, a2) -> a1, // keep first if duplicate
                        LinkedHashMap::new
                ));

        final Attachment eventConfXml = fileMap.remove("eventconf");
        final List<String> orderedFiles = determineFileOrder(eventConfXml, fileMap.keySet());

        final List<Map<String, Object>> successList = new ArrayList<>();
        final List<Map<String, Object>> errorList = new ArrayList<>();

        for (final String fileName : orderedFiles) {
            final Attachment attachment = fileMap.get(fileName);
            if (attachment == null) {
                continue;
            }

            Events fileEvents;
            try (InputStream stream = attachment.getObject(InputStream.class)) {
                fileEvents = parseEventFile(stream);
            } catch (Exception e) {
                errorList.add(buildErrorResponse(fileName, e));
                continue;
            }

            try {
                final EventConfSource existingSource = eventConfSourceDao.findByName(fileName);
                final int fileOrder = (existingSource != null)
                        ? existingSource.getFileOrder()
                        : ++maxFileOrder;

                eventConfPersistenceService.persistEventConfFile(
                        fileEvents,
                        buildMetadata(fileName, "", fileEvents, fileOrder, username, now));
                successList.add(buildSuccessResponse(fileName, fileEvents));
            } catch (Exception e) {
                errorList.add(buildErrorResponse(fileName, e));
            }
        }

        return Response.ok(Map.of("success", successList, "errors", errorList)).build();
    }

    @Override
    public Response filterEventConf(String uei, String vendor, String sourceName, int offset, int limit, SecurityContext securityContext) {

        // Return 400 Bad Request if offset is negative or limit is less than 1
        if (offset < 0 || limit < 1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Call the persistence service
        List<EventConfEvent> results = eventConfPersistenceService.findEventConfByFilters(uei, vendor, sourceName, offset, limit);
        if (results == null || results.isEmpty()) {
            // Return 204 No Content if no matching records found
            return Response.noContent().build();
        }

        List<EventConfEventDto> dtoList = EventConfEventDto.fromEntity(results);

        // Return the matching results
        return Response.ok(dtoList).build();
    }

    @Override
    @Transactional
    public Response enableDisableEventConfSources(final EventConfSrcEnableDisablePayload payload, SecurityContext securityContext) throws Exception {

        if (payload == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }

        if (payload.getEnabled() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("The 'enabled' flag must be provided (true/false).").build();
        }

        if (payload.getSourceIds() == null || payload.getSourceIds().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("At least one sourceId must be provided.").build();
        }

        try {
            eventConfPersistenceService.updateSourceAndEventEnabled(payload);
            return Response.ok().entity("EventConf sources updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("One or more sourceIds were not found: " + ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response filterConfEventsBySourceId(Long sourceId, Integer totalRecords, Integer offset, Integer limit,
                                               SecurityContext securityContext) {

        // Return 400 Bad Request if sourceId is null, invalid sourceId, offset < 0 or limit < 1
        if (Objects.requireNonNullElse(sourceId, 0L) <= 0L || Objects.requireNonNullElse(offset, 0) < 0
                || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid sourceId/offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        Map<String, Object> result = eventConfPersistenceService.filterConfEventsBySourceId(sourceId, totalRecords,
                offset, limit);

        // Check if no data found
        if (result == null
                || result.isEmpty()
                || (result.containsKey("totalRecords") && ((Integer) result.get("totalRecords")) == 0)) {
            return Response.noContent().build();  // 204 No Content
        }

        List<EventConfEventDto> dtoList =
                EventConfEventDto.fromEntity((List<EventConfEvent>) result.get("eventConfEventList"));

        // Build response
        return Response.ok(Map.of("totalRecords", result.get("totalRecords"), "eventConfSourceList", dtoList))
                .build();
    }

    @Override
    public Response filterEventConfSource(String filter, String sortBy, String order, Integer totalRecords,
                                          Integer offset, Integer limit, SecurityContext securityContext) {

        // Return 400 Bad Request if offset < 0 or limit < 1
        if (Objects.requireNonNullElse(offset, 0) < 0 || Objects.requireNonNullElse(limit, 0) < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        Map<String, Object> result = eventConfPersistenceService.filterEventConfSource(filter, sortBy, order,
                totalRecords, offset, limit);

        // Check if no data found
        if (result == null
                || result.isEmpty()
                || (result.containsKey("totalRecords") && ((Integer) result.get("totalRecords")) == 0)) {
            return Response.noContent().build();  // 204 No Content
        }

        List<EventConfSourceDto> dtoList =
                EventConfSourceDto.fromEntity((List<EventConfSource>) result.get("eventConfSourceList"));

        // Build response
        return Response.ok(Map.of("totalRecords", result.get("totalRecords"), "eventConfSourceList", dtoList))
                .build();
    }

    @Override
    public Response deleteEventConfSources(EventConfSourceDeletePayload payload, SecurityContext securityContext) throws Exception {

        if (payload == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }

        if (payload.getSourceIds() == null || payload.getSourceIds().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("At least one sourceId must be provided.").build();
        }

        try {
            eventConfPersistenceService.deleteEventConfSources(payload);
            return Response.ok().entity("EventConf sources deleted successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("One or more sourceIds were not found: " + ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }

    }

    @Override
    public Response enableDisableEventConfSourcesEvents(final Long sourceId, EnableDisableConfSourceEventsPayload payload, SecurityContext securityContext) throws Exception {

        if (payload == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }

        if (payload.getEventsIds() == null || payload.getEventsIds().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("At least one eventConfEventsIds must be provided.").build();
        }

        try {
            eventConfPersistenceService.enableDisableConfSourcesEvents(sourceId, payload);
            return Response.ok().entity("EventConfEvents updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("One or more eventConfEvents were not found: " + ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }
    @Override
    public Response getEventConfSourcesNames(SecurityContext securityContext) throws Exception {
        try {
            final var  sourceNames = eventConfSourceDao.findAllNames();
            return Response.ok(sourceNames).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to fetch EventConf source names: " + e.getMessage()).build();
        }
    }

    @Override
    public Response addEventConfSourceEvent(Long sourceId, Event event, SecurityContext securityContext) throws Exception {
        try {
            validateAddEvent(sourceId,event);
            final String username = getUsername(securityContext);
            final var id = eventConfPersistenceService.addEventConfSourceEvent(sourceId,username, event);
            return Response
                    .status(Response.Status.CREATED)
                    .entity(id)
                    .build();
        } catch (EntityNotFoundException ex) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Source with ID " + sourceId + " not found")
                    .build();
        } catch (IllegalArgumentException ex) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Invalid event payload: " + ex.getMessage())
                    .build();
        }
    }


    private List<String> determineFileOrder(final Attachment eventconfXmlAttachment, final Set<String> uploadedFiles) {
        List<String> ordered = new ArrayList<>();

        if (eventconfXmlAttachment != null) {
            try (InputStream stream = eventconfXmlAttachment.getObject(InputStream.class)) {
                List<String> fromXmlRaw = parseOrderingFromEventconfXml(stream);
                List<String> fromXml = fromXmlRaw.stream()
                        .map(path -> path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path).toList();

                // Identify files not listed in eventconf.xml
                List<String> extraFiles = uploadedFiles
                        .stream()
                        .filter(f -> !fromXml.contains(f))
                        .collect(Collectors.toList());

                // Add extra files first, then the ones in eventconf.xml
                ordered.addAll(extraFiles);
                ordered.addAll(fromXml);
            } catch (Exception e) {
                throw new RuntimeException("Invalid eventconf.xml format", e);
            }
        } else {
            // No eventconf.xml, preserve uploaded file order
            ordered.addAll(uploadedFiles);
        }

        return ordered;
    }


    private Events parseEventFile(final InputStream inputStream) throws Exception {
        return JaxbUtils.unmarshal(Events.class, inputStream);
    }

    private List<String> parseOrderingFromEventconfXml(final InputStream xmlStream) throws Exception {
        Events events = JaxbUtils.unmarshal(Events.class, xmlStream);
        return events.getEventFiles();
    }

    private String getUsername(final SecurityContext context) {
        return (context != null && context.getUserPrincipal() != null) ? context.getUserPrincipal().getName() : "unknown";
    }

    private Map<String, Object> buildSuccessResponse(String filename, Events events) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("file", filename);
        entry.put("eventCount", events.getEvents().size());
        entry.put("vendor", StringUtils.substringBefore(filename, "."));
        List<Map<String, ? extends Serializable>> eventSummaries = events
                .getEvents()
                .stream()
                .map(e -> Map.of("uei", e.getUei(), "label", e.getEventLabel(), "description", e.getEventLabel(), "enabled", true))
                .collect(Collectors.toList());
        entry.put("events", eventSummaries);

        return entry;
    }

    private Map<String, Object> buildErrorResponse(String filename, Exception ex) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("file", filename);
        entry.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return entry;
    }

    private EventConfSourceMetadataDto buildMetadata(String fileName, String description, Events events, int fileOrder,
                                                     String username, Date now) {
        return new EventConfSourceMetadataDto.Builder()
                .filename(fileName)
                .eventCount(events.getEvents().size())
                .fileOrder(fileOrder)
                .username(username)
                .now(now)
                .vendor(StringUtils.substringBefore(fileName, "."))
                .description(description)
                .build();
    }

    private String stripExtension(final String filename) {
        if (filename == null) return null;
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }

    private void validateAddEvent(Long sourceId, Event event) {
        if (sourceId == null || sourceId <= 0) {
            throw new IllegalArgumentException("Invalid sourceId: must be a positive number");
        }
        EventConfSource eventConfSource = eventConfSourceDao.get(sourceId);
        if (eventConfSource == null) {
            throw new EntityNotFoundException("Source with id " + sourceId + " does not exist");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event payload is missing");
        }
        requireNonBlank(event.getUei(), "Event 'uei' is required");
        requireNonBlank(event.getEventLabel(), "Event 'event-label' is required");
        requireNonBlank(event.getSeverity(), "Event 'severity' is required");
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
