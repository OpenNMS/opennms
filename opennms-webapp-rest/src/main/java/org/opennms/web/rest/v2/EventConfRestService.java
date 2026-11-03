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
import org.opennms.netmgt.dao.api.EventConfEventDao;
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
import org.opennms.netmgt.model.events.EventConfEventOrderPayload;
import org.opennms.netmgt.model.events.EventConfSourceOrderPayload;
import org.opennms.web.rest.v2.model.AddEventConfSourceRequest;
import org.opennms.web.rest.v2.model.EventConfEventDeletePayload;
import org.opennms.web.rest.v2.model.EventConfEventEditRequest;
import org.opennms.web.rest.v2.model.EventConfEventMoveRequest;
import org.opennms.web.rest.v2.model.EventConfEventSummaryDto;
import org.opennms.web.rest.v2.model.EventConfSourceDto;
import org.opennms.web.rest.v2.model.SourceNameDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class EventConfRestService implements EventConfRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfRestService.class);

    /** "file" label of the upload report entry describing a failed eventconf.xml ordering step. */
    static final String EVENTCONF_ORDER_STEP = "eventconf.xml (source order)";

    private static final int VENDOR_MAX_LENGTH = 128;

    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Autowired
    private EventConfEventDao eventConfEventDao;

    @Override
    public Response uploadEventConfFiles(final List<Attachment> attachments, final SecurityContext securityContext) {
        final String username = getUsername(securityContext);
        final Date now = new Date();
        final List<Map<String, Object>> successList = new ArrayList<>();
        final List<Map<String, Object>> errorList = new ArrayList<>();
        final Map<String, Attachment> fileMap = new LinkedHashMap<>();
        for (Attachment attachment : attachments) {
            // a dropped attachment must show up in the report: a 200 with files missing from
            // both lists reads as success to the client
            String filename = attachment.getContentDisposition() == null
                    ? null : attachment.getContentDisposition().getParameter("filename");
            String basename = stripPathAndExtension(filename);

            if (basename == null || basename.isEmpty()) {
                LOG.warn("Skipping attachment with invalid filename: {}", filename);
                errorList.add(Map.of("file", String.valueOf(filename),
                        "error", "The attachment carries no usable filename and was not stored"));
                continue;
            }

            if (fileMap.containsKey(basename)) {
                String existingFilename = fileMap.get(basename).getContentDisposition().getParameter("filename");
                LOG.warn("Duplicate basename detected: '{}' and '{}' resolve to same name '{}'. Keeping first file.",
                        existingFilename, filename, basename);
                errorList.add(Map.of("file", filename,
                        "error", "Resolves to the same name as '" + existingFilename + "'; only the first file was stored"));
                continue;
            }

            fileMap.put(basename, attachment);
        }

        // An eventconf.xml in the upload dictates the source order; it is applied after the files are
        // persisted (see reorderSourcesFromEventConf), so it is not stored as a source itself.
        final ManifestOrder manifest = parseEventConfOrder(fileMap);

        List<String> orderedFiles = new ArrayList<>(fileMap.keySet());

        final long dbStartTime = System.currentTimeMillis();
        for (final String fileName : orderedFiles) {
            final Attachment attachment = fileMap.get(fileName);
            if (attachment == null) {
                continue;
            }

            Events fileEvents;
            try (InputStream stream = attachment.getObject(InputStream.class)) {
                fileEvents = parseEventFile(new ByteArrayInputStream(stream.readAllBytes()));
            } catch (Exception e) {
                errorList.add(buildErrorResponse(fileName, e));
                continue;
            }

            try {
                // the source position is decided inside the persisting transaction: an existing source keeps
                // its committed position, a new one is allocated there
                eventConfPersistenceService.persistEventConfFile(
                        fileEvents,
                        buildMetadata(fileName, "", fileEvents, username, now));
                successList.add(buildSuccessResponse(fileName, fileEvents));
            } catch (Exception e) {
                errorList.add(buildErrorResponse(fileName, e));
            }
        }

        final long dbEndTime = System.currentTimeMillis();
        LOG.info("Time to add {} files to DB: {} ms", orderedFiles.size(), (dbEndTime - dbStartTime));

        boolean orderApplied = manifest.failure == null;
        if (manifest.failure != null) {
            errorList.add(buildErrorResponse(EVENTCONF_ORDER_STEP, manifest.failure));
        }
        if (manifest.note != null) {
            // a consumed manifest that produced no order must still show up in the report
            successList.add(Map.of("file", EVENTCONF_ORDER_STEP, "message", manifest.note));
        }
        if (manifest.order != null) {
            try {
                eventConfPersistenceService.reorderSourcesFromEventConf(manifest.order);
                // the report says the order step happened: a manifest-only upload is otherwise empty
                successList.add(Map.of("file", EVENTCONF_ORDER_STEP,
                        "message", "Source order applied (" + manifest.order.size() + " entries)"));
            } catch (Exception e) {
                // The files above are committed, but eventd would now evaluate them in upload order, not in
                // the order the eventconf.xml asked for: that is a failed request, not a footnote.
                LOG.error("Uploaded files were persisted but the eventconf.xml source order could not be applied", e);
                errorList.add(buildErrorResponse(EVENTCONF_ORDER_STEP, e));
                orderApplied = false;
            }
        }

        eventConfPersistenceService.reloadEventsIntoMemory();

        final Map<String, Object> report = Map.of("success", successList, "errors", errorList);
        return (orderApplied ? Response.ok(report) : Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(report)).build();
    }

    /**
     * If an eventconf.xml is part of the upload, removes it from the file map and returns the source
     * names of its {@code <event-file>} entries in file order (first = evaluated first).
     * Returns null when there is no eventconf.xml, it lists no files, or it cannot be parsed
     * (in which case it is left in the map so it shows up in the error list).
     */
    /** Result of looking for an ordering manifest (eventconf.xml) in an upload. */
    private static final class ManifestOrder {
        static final ManifestOrder ABSENT = new ManifestOrder(null, null, null);
        final List<String> order;   // null unless a usable order was parsed
        final Exception failure;    // non-null when a manifest was present but unparseable
        final String note;          // non-null when a manifest was consumed without producing an order

        private ManifestOrder(final List<String> order, final Exception failure, final String note) {
            this.order = order;
            this.failure = failure;
            this.note = note;
        }
    }

    private ManifestOrder parseEventConfOrder(final Map<String, Attachment> fileMap) {
        final Attachment eventConfAttachment = fileMap.get("eventconf");
        if (eventConfAttachment == null) {
            return ManifestOrder.ABSENT;
        }
        fileMap.remove("eventconf");
        try (InputStream stream = eventConfAttachment.getObject(InputStream.class)) {
            final Events eventConfEvents = parseEventFile(new ByteArrayInputStream(stream.readAllBytes()));
            final List<String> eventConfOrder = new ArrayList<>();
            final Set<String> seen = new HashSet<>();
            for (String eventFile : eventConfEvents.getEventFiles()) {
                String name = stripPathAndExtension(eventFile);
                if (name != null && !name.isEmpty() && seen.add(name)) {
                    eventConfOrder.add(name);
                }
            }
            if (eventConfOrder.isEmpty()) {
                LOG.info("eventconf.xml contained no <event-file> entries, falling back to default ordering");
                return new ManifestOrder(null, null,
                        "eventconf.xml contained no <event-file> entries; no order was applied");
            }
            LOG.info("Parsed eventconf.xml with {} event-file entries for ordering", eventConfOrder.size());
            return new ManifestOrder(eventConfOrder, null, null);
        } catch (Exception e) {
            // A manifest was supplied but cannot be honored: that fails the request (see the caller),
            // it must not silently degrade into "default order"
            LOG.error("Failed to parse the uploaded eventconf.xml, its source order cannot be applied", e);
            return new ManifestOrder(null, e, null);
        }
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
            eventConfPersistenceService.updateSourceAndEventEnabled(payload, getUsername(securityContext));
            eventConfPersistenceService.reloadEventsIntoMemory();
            return Response.ok().entity("EventConf sources updated successfully.").build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity("One or more sourceIds were not found: " + ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response filterConfEventsBySourceId(Long sourceId, String eventFilter, String eventSortBy,
                                               String eventOrder, Integer totalRecords, Integer offset,
                                               Integer limit, SecurityContext securityContext) {

        // Return 400 Bad Request on a missing/invalid sourceId or a negative offset/limit;
        // a missing or zero limit means the whole result set in one page
        if (Objects.requireNonNullElse(sourceId, 0L) <= 0L || Objects.requireNonNullElse(offset, 0) < 0
                || Objects.requireNonNullElse(limit, 0) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid sourceId/offset/limit values"))
                    .build();
        }

        // Call service to fetch results
        Map<String, Object> result = eventConfPersistenceService.filterConfEventsBySourceId(sourceId, eventFilter,
                eventSortBy, eventOrder, totalRecords, offset, limit);

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
            eventConfPersistenceService.reloadEventsIntoMemory();
            return Response.ok().entity(Map.of("message", "EventConf sources deleted successfully.")).build();

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "One or more sourceIds were not found: " + ex.getMessage())).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Unexpected error occurred: " + ex.getMessage())).build();
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
            eventConfPersistenceService.enableDisableConfSourcesEvents(sourceId, payload, getUsername(securityContext));
            eventConfPersistenceService.reloadEventsIntoMemory();
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
    public Response getEventConfSourceNamesAndIds(SecurityContext securityContext) throws Exception {
        try {
            List<SourceNameDto> list = SourceNameDto.fromEntity(eventConfSourceDao.getIdToNameMap());
            return Response.ok(list).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to fetch EventConf source names and IDs: " + e.getMessage()).build();
        }
    }

    @Override
    public Response addEventConfSourceEvent(Long sourceId, Event event, SecurityContext securityContext) throws Exception {
        try {
            validateAddEvent(sourceId,event);
            final String username = getUsername(securityContext);
            final var id = eventConfPersistenceService.addEventConfSourceEvent(sourceId,username, event);
            eventConfPersistenceService.reloadEventsIntoMemory();
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

    @Override
    public Response getEventConfSourceEventsByUei(Long sourceId, String uei, SecurityContext securityContext) {
        if (sourceId == null || sourceId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid sourceId: must be a positive number").build();
        }
        if (uei == null || uei.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Query parameter 'uei' is required").build();
        }
        try {
            final List<EventConfEventDto> events =
                    EventConfEventDto.fromEntity(eventConfPersistenceService.findEventsBySourceIdAndUei(sourceId, uei));
            return Response.ok(events).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
        }
    }

    @Override
    public Response upsertEventConfSourceEvent(Long sourceId, Event event, SecurityContext securityContext) throws Exception {
        try {
            validateAddEvent(sourceId, event);
            final String username = getUsername(securityContext);
            final var result = eventConfPersistenceService.upsertEventConfSourceEvent(sourceId, username, event);
            if (result.outcome != EventConfPersistenceService.EventUpsertResult.Outcome.UNCHANGED) {
                eventConfPersistenceService.reloadEventsIntoMemory();
            }
            final var created = result.outcome == EventConfPersistenceService.EventUpsertResult.Outcome.CREATED;
            return Response
                    .status(created ? Response.Status.CREATED : Response.Status.OK)
                    .entity(Map.of(
                            "outcome", result.outcome.name().toLowerCase(),
                            "id", result.id,
                            "eventOrder", result.eventOrder))
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


    
    @Override
    public Response updateEventConfEvent(Long sourceId, Long eventId, EventConfEventEditRequest payload, SecurityContext securityContext) throws Exception {
        if (payload == null || payload.getEvent() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Request body must carry an 'event' definition")).build();
        }
        if (payload.getEnabled() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "The 'enabled' flag must be provided (true/false).")).build();
        }
        try {
            eventConfPersistenceService.updateEventConfEvent(sourceId, eventId, payload, getUsername(securityContext));
            eventConfPersistenceService.reloadEventsIntoMemory();
            return Response.ok().entity(Map.of("message", "EventConfEvent updated successfully.")).build();

        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", ex.getMessage())).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Unexpected error occurred: " + ex.getMessage())).build();
        }
    }

    @Override
    public Response deleteEventsForSource(Long sourceId, EventConfEventDeletePayload payload, SecurityContext securityContext) throws Exception {
        if (sourceId == null || sourceId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid sourceId: must be a positive number")).build();
        }

        if (payload == null || payload.getEventIds() == null || payload.getEventIds().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Event IDs to delete must not be null or empty")).build();
        }

        try {
            eventConfPersistenceService.deleteEventsForSource(sourceId, payload);
            eventConfPersistenceService.reloadEventsIntoMemory();
            return Response.ok().entity(Map.of("message", "EventConf events deleted successfully.")).build();

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "One or more eventIds were not found: " + ex.getMessage()))
                    .build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "Unexpected error occurred: " + ex.getMessage()))
                    .build();
        }
    }

    @Override
    public Response downloadEventConfXmlBySourceId(final Long sourceId, SecurityContext securityContext) {
        if (sourceId == null || sourceId <= 0) {
            return buildXmlError(Response.Status.BAD_REQUEST, "Invalid source ID");
        }

        EventConfSource eventConfSource = eventConfSourceDao.get(sourceId);

        if (eventConfSource == null || eventConfSource.getEvents().isEmpty()) {
            return buildXmlError(Response.Status.NOT_FOUND, "No events found for source ID: " + sourceId);
        }

        final var eventConfEvents = eventConfEventDao.findBySourceId(sourceId);

        StreamingOutput stream = output -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
                Events events = new Events();
                for (final var  eventConfEvent : eventConfEvents) {
                    if (eventConfEvent == null || eventConfEvent.getXmlContent() == null) {
                        continue;
                    }
                    try {
                        Event event = JaxbUtils.unmarshal(Event.class, eventConfEvent.getXmlContent());
                        events.addEvent(event);
                    } catch (Exception e) {
                        LOG.error("Failed to parse event XML for source {}: {}", sourceId, e.getMessage());
                    }
                }
                JaxbUtils.marshal(events, writer);
            }
        };

        // the source name is user-supplied: quotes, backslashes and control characters must not
        // reach the header, where they would smuggle parameters or break the response
        final String safeName = eventConfSource.getName().replaceAll("[\\p{Cntrl}\"\\\\]", "_");
        return Response.ok(stream).type(MediaType.APPLICATION_XML)
                .header("Content-Disposition", "attachment; filename=\"%s.xml\""
                        .formatted(safeName)).build();
    }

    @Override
    public Response getEventsByVendor(final String vendorName, SecurityContext securityContext) {
        try {
            if (vendorName == null || vendorName.isBlank()) {
                LOG.warn("Vendor name is null or blank.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Vendor name must not be null or blank")
                        .build();
            }

            final var results = eventConfEventDao.findEventsByVendor(vendorName);

            if (results == null || results.isEmpty()) {
                LOG.info("No events found for vendor: {}", vendorName);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No events found for vendor: " + vendorName)
                        .build();
            }

            final var eventsDtoList = EventConfEventDto.fromEntity(results);
            return Response.ok(eventsDtoList).build();
        }
        catch (Exception ex) {
            LOG.error("Error retrieving events for vendor: {}", vendorName, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while retrieving events for vendor: " + vendorName
                            + ". Cause: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response addEventConfSource(final AddEventConfSourceRequest request,
                                       SecurityContext securityContext) {
        final Date now = new Date();

        try {
            validateAddSourceRequest(request);
        } catch (IllegalArgumentException ex) {
            LOG.warn("Validation failed for addEventConfSource request. reason={}", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request: " + ex.getMessage())
                    .build();
        }

        try {
            // Duplicate check
            if (eventConfSourceDao.findByName(request.getName()) != null) {
                LOG.warn("Attempt to create duplicate EventConfSource. name='{}'", request.getName());
                return Response.status(Response.Status.CONFLICT)
                        .entity("An EventConfSource with the same name already exists")
                        .build();
            }

            final String username = getUsername(securityContext);

            String vendor = request.getVendor();
            if (vendor == null || vendor.isBlank()) {
                vendor = StringUtils.substringBefore(request.getName(), ".");
            } else {
                vendor = request.getVendor();
            }
            if (vendor.length() > VENDOR_MAX_LENGTH) {
                LOG.warn("Vendor validation failed for EventConfSource name='{}'. reason={}",
                        request.getName(), "Vendor length must not exceed " + VENDOR_MAX_LENGTH + " characters.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid vendor: " + "Vendor length must not exceed " + VENDOR_MAX_LENGTH + " characters.")
                        .build();
            }

            EventConfSource eventConfSource = new EventConfSource();
            eventConfSource.setName(request.getName());
            eventConfSource.setDescription(request.getDescription());
            eventConfSource.setVendor(vendor);
            eventConfSource.setEnabled(true);
            eventConfSource.setEventCount(0);
            eventConfSource.setCreatedTime(now);
            eventConfSource.setLastModified(now);
            eventConfSource.setUploadedBy(username);
            // fileOrder is allocated by the service inside its transaction (max + 1, unique)

            LOG.debug("Persisting EventConfSource name='{}'", request.getName());

            Long eventConfSourceId = eventConfPersistenceService.createEventConfSource(eventConfSource);

            LOG.info("Successfully created EventConfSource id={}, name='{}', user='{}'",
                    eventConfSourceId, request.getName(), username);

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "id", eventConfSourceId,
                            "name", eventConfSource.getName(),
                            "fileOrder", eventConfSource.getFileOrder()
                    ))
                    .build();

        }   catch (DataIntegrityViolationException ex) {

            LOG.warn("Data integrity violation while creating EventConfSource name='{}'. Cause={}",
                    request.getName(),
                    ex.getMostSpecificCause().getMessage(),
                    ex);

            return Response.status(Response.Status.CONFLICT)
                    .entity("EventConfSource could not be created due to a data integrity violation. "
                            + "It may already exist or violate database constraints.")
                    .build();
        }
        catch (Exception ex) {

            LOG.error("Failed to create EventConfSource name='{}'. Error={}",
                    request.getName(), ex.getMessage(), ex);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Unexpected error occurred while creating EventConfSource")
                    .build();
        }
    }

    @Override
    public Response getOrderedEventConfSources(SecurityContext securityContext) {
        try {
            final List<EventConfSource> sources = new ArrayList<>(eventConfSourceDao.findAllByFileOrder());
            Collections.reverse(sources); // highest fileOrder = evaluated first; catch-all (1) ends up last
            return Response.ok(EventConfSourceDto.fromEntity(sources)).build();
        } catch (Exception e) {
            LOG.error("Failed to fetch the ordered event-conf sources", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to fetch the ordered EventConf sources: " + e.getMessage()).build();
        }
    }

    @Override
    public Response updateEventConfSourcesOrder(final EventConfSourceOrderPayload payload, SecurityContext securityContext) throws Exception {
        if (payload == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }
        if (payload.getSourceIds() == null || payload.getSourceIds().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("At least one sourceId must be provided.").build();
        }

        try {
            final int updated = eventConfPersistenceService.reorderEventConfSources(payload.getSourceIds());
            eventConfPersistenceService.reloadEventsIntoMemory();
            return Response.ok(Map.of("updated", updated)).build();

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            LOG.error("Failed to reorder the event-conf sources", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response moveEventConfSourceEvent(final Long sourceId, final Long eventId, final EventConfEventMoveRequest request,
                                             SecurityContext securityContext) throws Exception {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }

        try {
            final int eventOrder = eventConfPersistenceService.moveEventConfEvent(sourceId, eventId, request);
            eventConfPersistenceService.reloadEventsIntoMemory();
            return Response.ok(Map.of("id", eventId, "eventOrder", eventOrder)).build();

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            LOG.error("Failed to move event {} of source {}", eventId, sourceId, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    @Override
    public Response getOrderedEventConfSourceEvents(final Long sourceId, SecurityContext securityContext) {
        try {
            if (sourceId == null || sourceId <= 0 || eventConfSourceDao.get(sourceId) == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("EventConfSource not found for id: " + sourceId).build();
            }
            final List<Object[]> summaries = eventConfPersistenceService.findEventOrderSummaries(sourceId);
            return Response.ok(EventConfEventSummaryDto.fromRows(summaries)).build();
        } catch (Exception e) {
            LOG.error("Failed to fetch the ordered events of source {}", sourceId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to fetch the ordered events: " + e.getMessage()).build();
        }
    }

    @Override
    public Response updateEventConfSourceEventsOrder(final Long sourceId, final EventConfEventOrderPayload payload,
                                                     SecurityContext securityContext) throws Exception {
        if (payload == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request body cannot be null").build();
        }
        if (payload.getEventIds() == null || payload.getEventIds().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("At least one eventId must be provided.").build();
        }

        try {
            final int updated = eventConfPersistenceService.reorderEventConfEvents(sourceId, payload.getEventIds());
            eventConfPersistenceService.reloadEventsIntoMemory();
            return Response.ok(Map.of("updated", updated)).build();

        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (EntityNotFoundException ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            LOG.error("Failed to reorder the events of source {}", sourceId, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error occurred: " + ex.getMessage()).build();
        }
    }

    private void validateAddSourceRequest(final AddEventConfSourceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null.");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Source name must not be null or blank.");
        }
    }

    private Response buildXmlError(Response.Status status, String message) {
        return Response.status(status)
                .entity("<error>%s</error>".formatted(message))
                .type(MediaType.APPLICATION_XML)
                .build();
    }

    @Override
    public Response getEventConfSourceById(Long sourceId, SecurityContext securityContext) {
        try {
            if (sourceId == null || sourceId <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Invalid sourceId provided"))
                        .build();
            }
            final var eventConfSource = eventConfSourceDao.get(sourceId);
            if (eventConfSource == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "EventConfSource not found for id: " + sourceId))
                        .build();
            }
            EventConfSourceDto eventConfSourceDto = EventConfSourceDto.fromEntity(eventConfSource);
            return Response.ok(eventConfSourceDto).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Unexpected error occurred: " + e.getMessage()))
                    .build();
        }
    }

    private Events parseEventFile(final InputStream inputStream) throws Exception {
        return JaxbUtils.unmarshal(Events.class, inputStream);
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
                .map(e -> Map.of("uei", e.getUei(), "label", e.getEventLabel(),
                        "description", Objects.requireNonNullElse(e.getDescr(), ""), "enabled", true))
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

    private EventConfSourceMetadataDto buildMetadata(String fileName, String description, Events events,
                                                     String username, Date now) {
        return new EventConfSourceMetadataDto.Builder()
                .filename(fileName)
                .eventCount(events.getEvents().size())
                .username(username)
                .now(now)
                .vendor(StringUtils.substringBefore(fileName, "."))
                .description(description)
                .build();
    }

    private String stripPathAndExtension(final String filename) {
        if (filename == null) return null;

        // Strip folder paths (handle both / and \ separators)
        String basename = filename;
        int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastSlash != -1) {
            basename = filename.substring(lastSlash + 1);
        }

        // Strip extension
        int dotIndex = basename.lastIndexOf('.');
        String result = (dotIndex == -1) ? basename : basename.substring(0, dotIndex);

        // Trim trailing/leading whitespace from result
        return result.trim();
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
