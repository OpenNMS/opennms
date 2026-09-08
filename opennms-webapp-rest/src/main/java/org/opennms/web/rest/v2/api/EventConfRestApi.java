/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements. See the LICENSE.md file
 * distributed with this work for additional information.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License"); you may not use
 * this file except in compliance with the License.
 * https://www.gnu.org/licenses/agpl-3.0.txt
 */
package org.opennms.web.rest.v2.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.netmgt.model.EventConfEventDto;
import org.opennms.netmgt.model.events.EnableDisableConfSourceEventsPayload;
import org.opennms.netmgt.model.events.EventConfSourceDeletePayload;
import org.opennms.web.rest.v2.model.AddEventConfSourceRequest;
import org.opennms.web.rest.v2.model.EventConfErrorResponse;
import org.opennms.web.rest.v2.model.EventConfEventEditRequest;
import org.opennms.web.rest.v2.model.EventConfEventPageResponse;
import org.opennms.web.rest.v2.model.EventConfSourceCreatedResponse;
import org.opennms.web.rest.v2.model.EventConfSourceDto;
import org.opennms.web.rest.v2.model.EventConfSourcePageResponse;
import org.opennms.web.rest.v2.model.EventConfUploadResponse;
import org.opennms.web.rest.v2.model.SourceNameDto;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.model.events.EventConfSrcEnableDisablePayload;
import org.opennms.web.rest.v2.model.EventConfEventDeletePayload;


import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PATCH;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("eventconf")
@Tag(name = "EventConf", description = """
        EventConf API.

        Event definitions live in the database, grouped into sources that each stand for one event
        configuration file. When two sources define the same event, the source with the higher
        `fileOrder` wins: sources are loaded highest-`fileOrder`-first and the first definition that
        matches is the one applied. Neither the source id nor its name enters into it.

        `fileOrder` carries no unique constraint, so two sources can hold the same value. Sources that
        tie fall back to the order their events were inserted, lowest event id first, an ordering
        nothing but the row ids defines.""")
public interface EventConfRestApi {

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @Operation(
            summary = "Upload eventconf files",
            description = """
        Upload one or more eventconf files, optionally including `eventconf.xml` to set file order.
        Every part must be named `upload`. A part named `eventconf.xml` is not stored as a source: its
        `<event-file>` entries set the search order of the remaining sources instead.
        Files that fail to parse are reported in `errors` while the rest are still stored, so a 200 does
        not by itself mean every file was accepted.""",
            operationId = "uploadEventConfFiles"
    )
    @RequestBody(
            required = true,
            description = "Multipart form with one `upload` part per eventconf file.",
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(type = "string", format = "binary"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload processed. Per-file outcomes are split between `success` and `errors`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfUploadResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "success": [
                        {
                          "file": "Cisco.syslog.events",
                          "eventCount": 2,
                          "vendor": "Cisco",
                          "events": [
                            {
                              "uei": "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN",
                              "label": "Cisco Syslog: LINK-3-UPDOWN",
                              "description": "Cisco Syslog: LINK-3-UPDOWN",
                              "enabled": true
                            }
                          ]
                        }
                      ],
                      "errors": [
                        {
                          "file": "Broken.events",
                          "error": "UnmarshalException: unexpected element (uri:\\"\\", local:\\"evnts\\")"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "400", description = "The multipart body has no `upload` part; rejected before the handler with an empty body. A file that fails to parse is not a 400: it is reported inside `errors` with 200.")
    })
    Response uploadEventConfFiles(@Multipart("upload") List<Attachment> attachments,
                                  @Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("filter")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Filter EventConf Records",
            description = """
        Fetch EventConf records based on provided filters such as UEI, vendor, source and name.
        Each filter is optional; omitted filters are not applied. `limit` must be at least 1.
        `createdTime` and `lastModified` come back as epoch milliseconds, not as the date-time strings the schema shows.""",
            operationId = "filterEventConf"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "EventConf records retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = EventConfEventDto.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "id": 4211,
                        "uei": "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN",
                        "eventLabel": "Cisco Syslog: LINK-3-UPDOWN",
                        "description": "Interface state change reported by a Cisco device.",
                        "enabled": true,
                        "xmlContent": "<event xmlns=\\"http://xmlns.opennms.org/xsd/eventconf\\">...</event>",
                        "createdTime": 1787670300817,
                        "lastModified": 1787670300817,
                        "modifiedBy": "admin",
                        "sourceName": "Cisco.syslog.events",
                        "vendor": "Cisco",
                        "fileOrder": 24,
                        "severity": "Warning"
                      }
                    ]"""))),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content),
            @ApiResponse(responseCode = "204", description = "No matching EventConf records found for the given criteria",
                    content = @Content)
    })
    Response filterEventConf(
            @Parameter(description = "Exact UEI to match.", example = "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN")
            @QueryParam("uei") String uei,
            @Parameter(description = "Vendor to match.", example = "Cisco")
            @QueryParam("vendor") String vendor,
            @Parameter(description = "Source name to match.", example = "Cisco.syslog.events")
            @QueryParam("sourceName") String sourceName,
            @Parameter(description = "Zero-based index of the first record to return.", example = "0")
            @QueryParam("offset") int offset,
            @Parameter(description = "Maximum number of records to return.", example = "20")
            @QueryParam("limit") int limit,
            @Context SecurityContext securityContext );

    @PATCH
    @Path("/sources/status")
    @Produces("application/json")
    @Consumes("application/json")
    @Operation(
            summary = "Enable/Disable EventConf Sources",
            description = """
        Enable or disable one or more sources, cascading to their events when asked.
        `cascadeToEvents` has to be present: omitting it fails with a 500.
        sourceIds that do not exist are ignored rather than reported.
        The event definitions are reloaded into memory once the update commits.""",
            operationId = "enableDisableEventConfSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "EventConf sources updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Invalid request: missing body, missing `enabled`, or empty `sourceIds`",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "At least one sourceId must be provided."))),
            @ApiResponse(responseCode = "500", description = "Update failed, including when `cascadeToEvents` is absent",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unexpected error occurred: Cannot invoke \"java.lang.Boolean.booleanValue()\"")))
    })
    Response enableDisableEventConfSources(
            @RequestBody(required = true,
                    description = "Sources to update and the state to set. All three members are required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfSrcEnableDisablePayload.class),
                            examples = @ExampleObject(value = """
                    {
                      "enabled": false,
                      "cascadeToEvents": true,
                      "sourceIds": [17, 18]
                    }""")))
            EventConfSrcEnableDisablePayload eventConfSrcEnableDisablePayload,
            @Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("filter/{sourceId}/events")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get EventConfEvents by Source ID with filtering and sorting",
            description = """
        Retrieves EventConfEvent records for the given source ID with optional filtering, sorting, and pagination.
        - `eventFilter`: case-insensitive match on UEI, Event Label, or Description.
        - `eventSortBy`: sort field `uei`, `eventLabel`, `description`, `enabled` defaults to `createdTime` if invalid.
        - `eventOrder`: `asc` or `desc` (default: `desc`).
        - `offset` and `limit`: for pagination.

        The response wraps the page in `eventConfSourceList`; the entries are events, not sources.
        `createdTime` and `lastModified` come back as epoch milliseconds, not as the date-time strings the schema shows.""",
            operationId = "filterConfEventBySourceId"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "EventConf records retrieved successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(implementation = EventConfEventPageResponse.class),
                        examples = @ExampleObject(value = """
                {
                  "totalRecords": 132,
                  "eventConfSourceList": [
                    {
                      "id": 4211,
                      "uei": "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN",
                      "eventLabel": "Cisco Syslog: LINK-3-UPDOWN",
                      "description": "Interface state change reported by a Cisco device.",
                      "enabled": true,
                      "xmlContent": "<event xmlns=\\"http://xmlns.opennms.org/xsd/eventconf\\">...</event>",
                      "createdTime": 1787670300817,
                      "lastModified": 1787670300817,
                      "modifiedBy": "admin",
                      "sourceName": "Cisco.syslog.events",
                      "vendor": "Cisco",
                      "fileOrder": 24,
                      "severity": "Warning"
                    }
                  ]
                }"""))),
        @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                content = @Content(mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(implementation = EventConfErrorResponse.class),
                        examples = @ExampleObject(value = "{\"error\": \"Invalid sourceId/offset/limit values\"}"))),
        @ApiResponse(responseCode = "204", description = "No matching EventConfEvent record found for the given criteria",
                content = @Content)
    })
    Response filterConfEventsBySourceId(
            @Parameter(description = "Identifier of the source whose events are listed.", example = "17", required = true)
            @PathParam("sourceId") Long sourceId,
            @Parameter(description = "Case-insensitive substring matched against UEI, event label, and description.",
                    example = "LINK-3")
            @QueryParam("eventFilter") String eventFilter,
            @Parameter(description = "Sort field: `uei`, `eventLabel`, `description`, `severity`, or `enabled`. Anything else sorts by `createdTime`.",
                    example = "uei",
                    schema = @Schema(allowableValues = {"uei", "eventLabel", "description", "severity", "enabled"}))
            @QueryParam("eventSortBy") String eventSortBy,
            @Parameter(description = "Sort direction.", example = "asc",
                    schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "desc"))
            @QueryParam("eventOrder") String eventOrder,
            @Parameter(description = "Total matching records as already known to the caller. When present, the count query is not run.",
                    example = "132")
            @QueryParam("totalRecords") Integer totalRecords,
            @Parameter(description = "Zero-based index of the first record to return.", example = "0")
            @QueryParam("offset") Integer offset,
            @Parameter(description = "Maximum number of records to return.", example = "20")
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext );


    @DELETE
    @Path("/sources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Operation(
            summary = "Delete EventConf Sources",
            description = """
        Delete one or more eventConf sources by their IDs.
        Deleting a source also removes the events it holds.
        sourceIds that do not exist are ignored rather than reported.""",
            operationId = "deleteEventConfSources"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sources deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "EventConf sources deleted successfully."))),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid IDs)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "At least one sourceId must be provided."))),
            @ApiResponse(responseCode = "500", description = "Delete failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string")))
    })
    Response deleteEventConfSources(
            @RequestBody(required = true,
                    description = "Sources to delete.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfSourceDeletePayload.class),
                            examples = @ExampleObject(value = "{\"sourceIds\": [17, 18]}")))
            EventConfSourceDeletePayload eventConfSourceDeletePayload,
            @Context SecurityContext securityContext) throws Exception;

    @PATCH
    @Path("/sources/{sourceId}/events/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    @Operation(
            summary = "Update EventConf Sources events",
            description = """
        Sets the enabled flag on the listed events of one source and reloads the event definitions.
        eventsIds that do not exist are ignored rather than reported.""",
            operationId = "enableDisableConfSourcesEvents"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "EventConfEvents updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid IDs)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "At least one eventConfEventsIds must be provided."))),
            @ApiResponse(responseCode = "500", description = "Update failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string")))
    })
    Response enableDisableEventConfSourcesEvents(
            @Parameter(description = "Identifier of the source owning the events.", example = "17", required = true)
            @PathParam("sourceId") final Long sourceId,
            @RequestBody(required = true,
                    description = "Events to update and the state to set. Note the field is `enable`, not `enabled`.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EnableDisableConfSourceEventsPayload.class),
                            examples = @ExampleObject(value = """
                    {
                      "enable": true,
                      "eventsIds": [4211, 4212]
                    }""")))
            EnableDisableConfSourceEventsPayload enableDisableConfSourceEventsPayload,
            @Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("filter/sources")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Filter EventConfSource Records",
            description = """
        Fetch EventConfSource records based on provided filters such as name, vendor, description, fileOrder and eventCount.
        `filter` is a single case-insensitive term matched across those fields.
        `sortBy` has to be supplied: without it the response carries the correct `totalRecords` and an
        empty `eventConfSourceList`. Any value is accepted; an unrecognised one sorts by `createdTime`.""",
            operationId = "filterEventConfSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "EventConfSource records retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfSourcePageResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalRecords": 41,
                      "eventConfSourceList": [
                        {
                          "id": 17,
                          "name": "Cisco.syslog.events",
                          "description": "Syslog events forwarded by Cisco IOS devices",
                          "vendor": "Cisco",
                          "fileOrder": 24,
                          "enabled": true,
                          "eventCount": 132,
                          "createdTime": 1787670300817,
                          "lastModified": 1787670354466,
                          "uploadedBy": "admin"
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "400", description = "Bad Request – invalid or missing input parameters",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Invalid offset/limit values\"}"))),
            @ApiResponse(responseCode = "204", description = "No matching EventConfSource records found for the given criteria",
                    content = @Content)
    })
    Response filterEventConfSource(
            @Parameter(description = "Case-insensitive term matched against name, vendor, and description, and against the UEIs and event labels of the source's events.", example = "cisco")
            @QueryParam("filter") String filter,
            @Parameter(description = "Sort field: `name`, `vendor`, `description`, `fileOrder`, or `eventCount`. Anything else sorts by `createdTime`.",
                    example = "name", required = true,
                    schema = @Schema(allowableValues = {"name", "vendor", "description", "fileOrder", "eventCount"}))
            @QueryParam("sortBy") String sortBy,
            @Parameter(description = "Sort direction.", example = "asc",
                    schema = @Schema(allowableValues = {"asc", "desc"}, defaultValue = "desc"))
            @QueryParam("order") String order,
            @Parameter(description = "Total matching records as already known to the caller. When present, the count query is not run.",
                    example = "41")
            @QueryParam("totalRecords") Integer totalRecords,
            @Parameter(description = "Zero-based index of the first record to return.", example = "0")
            @QueryParam("offset") Integer offset,
            @Parameter(description = "Maximum number of records to return.", example = "20")
            @QueryParam("limit") Integer limit,
            @Context SecurityContext securityContext );



    @GET
    @Path("/sources/names")
    @Produces("application/json")
    @Operation(
            summary = "Get EventConf Source Names",
            description = "Retrieve the names of all EventConf sources stored in the database.",
            operationId = "getEventConfSourcesNames"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved source names",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples = @ExampleObject(value = """
                    ["Cisco.syslog.events", "Juniper.traps.events", "opennms.catch-all.events"]"""))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to fetch EventConf source names: ...")))
    })
    Response getEventConfSourcesNames(@Context SecurityContext securityContext) throws Exception;

    @GET
    @Path("/sources/names-and-ids")
    @Produces("application/json")
    @Operation(
            summary = "Get EventConf Source Names and IDs",
            description = "Retrieve the names and IDs of all EventConf sources stored in the database.",
            operationId = "getEventConfSourceNamesAndIds"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved source names and IDs",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = SourceNameDto.class)),
                            examples = @ExampleObject(value = """
                    [
                      {"id": 17, "name": "Cisco.syslog.events"},
                      {"id": 18, "name": "Juniper.traps.events"}
                    ]"""))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to fetch EventConf source names and IDs: ...")))
    })
    Response getEventConfSourceNamesAndIds(@Context SecurityContext securityContext) throws Exception;

    @POST
    @Path("/sources/{sourceId}/events")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a new event to an EventConfSource",
            description = """
        Creates and adds a new event under the given EventConfSource by its ID.
        `uei`, `event-label`, and `severity` are required; the remaining members follow the eventconf schema.
        The response body is the new event's identifier.""",
            operationId = "addEventConfSourceEvent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "integer", format = "int64"),
                            examples = @ExampleObject(value = "4213"))),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid data)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid event payload: Event 'severity' is required"))),
            @ApiResponse(responseCode = "404", description = "EventConfSource not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Source with ID 17 not found"))),
            @ApiResponse(responseCode = "500", description = "The body could not be deserialized (for example the XML spellings sent as JSON), or persisting the event failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string")))})
    Response addEventConfSourceEvent(
            @Parameter(description = "Identifier of the source the event is added to.", example = "17", required = true)
            @PathParam("sourceId") final Long sourceId,
            @RequestBody(required = true,
                    description = """
                            Event definition, using the same structure as an <event> element in an eventconf file.
                            The JSON form goes through Jackson rather than JAXB, so `logmsg.dest` takes the enum
                            constant name (`LOGNDISPLAY`) and the message text is `logmsg.value`; the XML form takes
                            the eventconf spellings. Sending the XML spellings as JSON fails with a 500.""",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = Event.class),
                                    examples = @ExampleObject(value = """
                            {
                              "uei": "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN",
                              "event-label": "Cisco Syslog: LINK-3-UPDOWN",
                              "severity": "Warning",
                              "descr": "Interface state change reported by a Cisco device.",
                              "logmsg": {
                                "dest": "LOGNDISPLAY",
                                "value": "Interface state change on %interface%."
                              }
                            }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = Event.class),
                                    examples = @ExampleObject(value = """
                            <event xmlns="http://xmlns.opennms.org/xsd/eventconf">
                              <uei>uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN</uei>
                              <event-label>Cisco Syslog: LINK-3-UPDOWN</event-label>
                              <descr>Interface state change reported by a Cisco device.</descr>
                              <logmsg dest="logndisplay">Interface state change on %interface%.</logmsg>
                              <severity>Warning</severity>
                            </event>"""))
                    })
            Event event,
            @Context SecurityContext securityContext) throws Exception;



    @PUT
    @Path("/sources/{sourceId}/events/{eventId}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces("application/json")
    @Operation(
            summary = "Update EventConf Event",
            description = """
        Update an eventConf event by sourceId and eventId.
        `event` is required: the whole definition is replaced from it, and `enabled` is applied with it.
        A body carrying only `enabled` fails with a 500, as does an unknown sourceId or eventId.""",
            operationId = "updateEventConfEvent"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "EventConfEvent updated successfully."))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Request body cannot be null"))),
            @ApiResponse(responseCode = "500", description = "Update failed, including when the event does not exist or the body omits `event`",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unexpected error occurred: Failed to update EventConfEvent XML for eventId=4211")))
    })
    Response updateEventConfEvent(
            @Parameter(description = "Identifier of the source owning the event.", example = "17", required = true)
            @PathParam("sourceId") final Long sourceId,
            @Parameter(description = "Identifier of the event to update.", example = "4211", required = true)
            @PathParam("eventId") final Long eventId,
            @RequestBody(required = true,
                    description = "Replacement event definition. `event` must be present.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfEventEditRequest.class),
                            examples = @ExampleObject(value = """
                            {
                              "enabled": true,
                              "event": {
                                "uei": "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN",
                                "event-label": "Cisco Syslog: LINK-3-UPDOWN",
                                "severity": "Minor"
                              }
                            }""")))
            EventConfEventEditRequest payload,
            @Context SecurityContext securityContext) throws Exception;
    @DELETE
    @Path("/sources/{sourceId}/events")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Delete Events for a Source",
            description = "Delete one or more events belonging to the specified eventConf source.",
            operationId = "deleteEventsForSource"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "EventConf events deleted successfully."))),
            @ApiResponse(responseCode = "500", description = "Missing or empty event ids. The IllegalArgumentException from the handler is mapped by the generic provider as 500 text/plain, not 400.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Event IDs to delete must not be null or empty"))),
            @ApiResponse(responseCode = "404", description = "Source or one or more events not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "One or more eventIds were not found: 9999")))
    })
    Response deleteEventsForSource(
            @Parameter(description = "Identifier of the source owning the events.", example = "17", required = true)
            @PathParam("sourceId") Long sourceId,
            @RequestBody(required = true,
                    description = "Events to delete.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfEventDeletePayload.class),
                            examples = @ExampleObject(value = "{\"eventIds\": [4211, 4212]}")))
            EventConfEventDeletePayload eventConfEventDeletePayload,
            @Context SecurityContext securityContext
    ) throws Exception;

    @GET
    @Path("/sources/{sourceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get EventConfSource by ID",
            description = "Retrieve an EventConfSource by its unique identifier.",
            operationId = "getEventConfSourceById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "EventConfSource retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfSourceDto.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 17,
                      "name": "Cisco.syslog.events",
                      "description": "Syslog events forwarded by Cisco IOS devices",
                      "vendor": "Cisco",
                      "fileOrder": 24,
                      "enabled": true,
                      "eventCount": 132,
                      "createdTime": 1787670300817,
                      "lastModified": 1787670354466,
                      "uploadedBy": "admin"
                    }"""))),
            @ApiResponse(responseCode = "404", description = "EventConfSource not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"EventConfSource not found for id: 17\"}"))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Invalid sourceId provided\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"Unexpected error occurred: ...\"}")))

    })
    Response getEventConfSourceById(
            @Parameter(description = "Identifier of the source to retrieve.", example = "17", required = true)
            @PathParam("sourceId") Long sourceId,
            @Context SecurityContext securityContext
    );


    @GET
    @Path("/sources/{sourceId}/events/download")
    @Produces(MediaType.APPLICATION_XML)
    @Operation(
            summary = "Download EventConf XML for a Source",
            description = """
            Downloads all EventConf events associated with the specified source ID.
            The body is an eventconf `<events>` document, returned as an attachment named after the source.
        """,
            operationId = "downloadEventConfXmlBySourceId"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "EventConf XML downloaded successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string", format = "binary"),
                            examples = @ExampleObject(value = """
                    <events xmlns="http://xmlns.opennms.org/xsd/eventconf">
                      <event>
                        <uei>uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN</uei>
                        <event-label>Cisco Syslog: LINK-3-UPDOWN</event-label>
                        <descr>Interface state change reported by a Cisco device.</descr>
                        <logmsg dest="logndisplay">Interface state change on %interface%.</logmsg>
                        <severity>Warning</severity>
                      </event>
                    </events>"""))),
            @ApiResponse(responseCode = "400", description = "Invalid or missing source ID",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<error>Invalid source ID</error>"))),
            @ApiResponse(responseCode = "404", description = "No events found for the specified source ID",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "<error>No events found for source ID: 17</error>")))
    })
    Response downloadEventConfXmlBySourceId(
            @Parameter(description = "Identifier of the source to export.", example = "17", required = true)
            @PathParam("sourceId") Long sourceId,
            @Context SecurityContext securityContext
    ) throws Exception;

    @GET
    @Path("/vendors/{vendorName}/events")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get EventConf Events by Vendor",
            description = """
        Returns all EventConf events associated with the specified vendor name.
        `createdTime` and `lastModified` come back as epoch milliseconds, not as the date-time strings the schema shows.""",
            operationId = "getEventsByVendor"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "EventConf Events retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(implementation = EventConfEventDto.class)),
                            examples = @ExampleObject(value = """
                    [
                      {
                        "id": 4211,
                        "uei": "uei.opennms.org/vendor/cisco/syslog/LINK-3-UPDOWN",
                        "eventLabel": "Cisco Syslog: LINK-3-UPDOWN",
                        "description": "Interface state change reported by a Cisco device.",
                        "enabled": true,
                        "xmlContent": "<event xmlns=\\"http://xmlns.opennms.org/xsd/eventconf\\">...</event>",
                        "createdTime": 1787670300817,
                        "lastModified": 1787670300817,
                        "modifiedBy": "admin",
                        "sourceName": "Cisco.syslog.events",
                        "vendor": "Cisco",
                        "fileOrder": 24,
                        "severity": "Warning"
                      }
                    ]"""))),
            @ApiResponse(responseCode = "400", description = "Invalid or missing vendor name",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Vendor name must not be null or blank"))),
            @ApiResponse(responseCode = "404", description = "No events found for the specified vendor",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No events found for vendor: Cisco"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error while retrieving events for vendor: Cisco. Cause: ...")))
    })
    Response getEventsByVendor(
            @Parameter(description = "Vendor name to match.", example = "Cisco", required = true)
            @PathParam("vendorName") String vendorName,
            @Context SecurityContext securityContext
    ) throws Exception;

    @POST
    @Path("/sources/eventConfSource")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add a new EventConfSource",
            description = """
        Creates and adds a new EventConfSource.
        The source starts empty and enabled, and is assigned the highest `fileOrder` so it is searched first.""",
            operationId = "addEventConfSource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "EventConfSource created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EventConfSourceCreatedResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 42,
                      "name": "Cisco.syslog.events",
                      "fileOrder": 25
                    }"""))),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing/invalid data)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid request: Source name must not be null or blank."))),
            @ApiResponse(responseCode = "409", description = "A source with the same name already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "An EventConfSource with the same name already exists"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unexpected error occurred while creating EventConfSource")))})
    Response addEventConfSource(
            @RequestBody(required = true,
                    description = "Source to create.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AddEventConfSourceRequest.class),
                            examples = @ExampleObject(value = """
                    {
                      "name": "Cisco.syslog.events",
                      "description": "Syslog events forwarded by Cisco IOS devices",
                      "vendor": "Cisco"
                    }""")))
            final AddEventConfSourceRequest request,
            @Context SecurityContext securityContext) throws Exception;

}
