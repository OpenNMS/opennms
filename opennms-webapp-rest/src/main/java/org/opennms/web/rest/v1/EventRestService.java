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

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventCollection;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.model.AckOnlyForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("eventRestService")
@Path("events")
@Tag(name = "Events", description = "Events API")
public class EventRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(EventRestService.class);
    private static final DateTimeFormatter ISO8601_FORMATTER_MILLIS = ISODateTimeFormat.dateTime();
    private static final DateTimeFormatter ISO8601_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    private EventIpcManager m_eventForwarder;

    /**
     * <p>
     * getEvent
     * </p>
     * 
     * @param eventId
     *            a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsEvent} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{eventId}")
    @Transactional
    @Operation(
            summary = "Get an event",
            description = """
                    Return one persisted event by id.
                    The derived schema shows `date-time`; `time` and `createTime` are epoch milliseconds in JSON
                    and ISO-8601 strings in XML.""",
            operationId = "getEventV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The event.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsEvent.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 55273,
                      "uei": "uei.opennms.org/internal/capsd/snmpConflictsWithDb",
                      "time": 1787716800000,
                      "createTime": 1787727510202,
                      "source": "ReST",
                      "severity": "WARNING",
                      "description": "Probe event",
                      "logMessage": "Probe event",
                      "log": "Y",
                      "display": "Y",
                      "serviceType": null,
                      "ifIndex": null,
                      "parameters": [
                        { "name": "probe", "value": "1", "type": "string" }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No event with that id, or the path segment is not an integer.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Event object 99999999 was not found.")))
    })
    public OnmsEvent getEvent(
            @Parameter(description = "Event id.", example = "55273", required = true)
            @PathParam("eventId") final Long eventId) {
        final OnmsEvent e = m_eventDao.get(eventId);
        if (e == null) {
            throw getException(Status.NOT_FOUND, "Event object {} was not found.", Long.toString(eventId));
        }
        return e;
    }

    /**
     * returns a plaintext string being the number of events
     * 
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    @Operation(
            summary = "Count all events",
            description = "Return the total number of event rows as a plain-text integer. Query parameters are "
                    + "ignored.",
            operationId = "getEventCountV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The event count.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "55420")))
    })
    public String getCount() {
        return Integer.toString(m_eventDao.countAll());
    }

    /**
     * Returns all the events which match the filter/query in the query
     * parameters
     * 
     * @return Collection of OnmsEventCollection (ready to be XML-ified)
     * @throws java.text.ParseException
     *             if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Search events",
            description = """
                    Return events matching the query parameters, oldest `eventTime` first unless `orderBy` says
                    otherwise.
                    Filters are `OnmsEvent` property names, so they are `eventUei`, `eventSource` and
                    `eventSeverity` rather than the `uei`, `source` and `severity` spellings the response uses.
                    `node.id`, `node.label`, `ipInterface.*`, `snmpInterface.*` and `serviceType.*` are reachable
                    through their aliases. `limit` (default 10), `offset`, `orderBy`, `order`, `match` and
                    `comparator` shape the result. A filter name that is not a property of the entity fails with
                    500.""",
            operationId = "getEventsV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The matching events.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsEventCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 3,
                      "count": 1,
                      "offset": 0,
                      "event": [ {
                          "id": 55273,
                          "uei": "uei.opennms.org/internal/capsd/snmpConflictsWithDb",
                          "time": 1787716800000,
                          "createTime": 1787727510202,
                          "source": "ReST",
                          "severity": "WARNING",
                          "description": "Probe event",
                          "logMessage": "Probe event",
                          "log": "Y",
                          "display": "Y",
                          "serviceType": null,
                          "ifIndex": null,
                          "parameters": [
                            { "name": "probe", "value": "1", "type": "string" }
                          ]
                        } ]
                    }"""))),
            @ApiResponse(responseCode = "500", description = "A query parameter is not a property of the event entity.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public OnmsEventCollection getEvents(@Context final UriInfo uriInfo) throws ParseException {
        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters());
        builder.orderBy("eventTime").asc();

        final OnmsEventCollection coll = new OnmsEventCollection(m_eventDao.findMatching(builder.toCriteria()));
        coll.setTotalCount(m_eventDao.countMatching(builder.count().toCriteria()));

        return coll;
    }

    /**
     * Returns all the events which match the filter/query in the query
     * parameters
     * 
     * @return Collection of OnmsEventCollection (ready to be XML-ified)
     * @throws java.text.ParseException
     *             if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("between")
    @Transactional
    @Operation(
            summary = "Search events in a time range",
            description = """
                    Return events whose timestamp column falls between `begin` and `end`. `begin` defaults to the
                    epoch and `end` to now, and `column` defaults to `eventTime`.
                    Both bounds are parsed as ISO-8601 and a timezone offset is required, so
                    `2026-08-26T00:00:00-04:00` and `2026-08-26T00:00:00Z` parse while
                    `2026-08-26T00:00:00` is rejected with 400. Fractional seconds are optional.
                    Remaining query parameters are applied as event filters exactly as on `GET /events`, and
                    `match` is forced to `all`. A `column` that is not a property of the entity fails with 500.""",
            operationId = "getEventsBetweenV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The matching events.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsEventCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 3,
                      "count": 1,
                      "offset": 0,
                      "event": [ {
                          "id": 55273,
                          "uei": "uei.opennms.org/internal/capsd/snmpConflictsWithDb",
                          "time": 1787716800000,
                          "createTime": 1787727510202,
                          "source": "ReST",
                          "severity": "WARNING",
                          "description": "Probe event",
                          "logMessage": "Probe event",
                          "log": "Y",
                          "display": "Y",
                          "serviceType": null,
                          "ifIndex": null,
                          "parameters": [
                            { "name": "probe", "value": "1", "type": "string" }
                          ]
                        } ]
                    }"""))),
            @ApiResponse(responseCode = "400", description = "`begin` or `end` could not be parsed.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't parse start date"))),
            @ApiResponse(responseCode = "500", description = "`column` or another query parameter is not a property of the event entity.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public OnmsEventCollection getEventsBetween(@Context final UriInfo uriInfo) throws ParseException {
        final MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

        final String column;
        if (params.containsKey("column")) {
            column = params.getFirst("column");
            params.remove("column");
        } else {
            column = "eventTime";
        }
        Date begin;
        if (params.containsKey("begin")) {
            try {
                begin = ISO8601_FORMATTER.parseLocalDateTime(params.getFirst("begin")).toDate();
            } catch (final Throwable t1) {
                try {
                    begin = ISO8601_FORMATTER_MILLIS.parseDateTime(params.getFirst("begin")).toDate();
                } catch (final Throwable t2) {
                    throw getException(Status.BAD_REQUEST, "Can't parse start date");
                }
            }
            params.remove("begin");
        } else {
            begin = new Date(0);
        }
        Date end;
        if (params.containsKey("end")) {
            try {
                end = ISO8601_FORMATTER.parseLocalDateTime(params.getFirst("end")).toDate();
            } catch (final Throwable t1) {
                try {
                    end = ISO8601_FORMATTER_MILLIS.parseLocalDateTime(params.getFirst("end")).toDate();
                } catch (final Throwable t2) {
                    throw getException(Status.BAD_REQUEST, "Can't parse end date");
                }
            }
            params.remove("end");
        } else {
            end = new Date();
        }

        final CriteriaBuilder builder = getCriteriaBuilder(params);
        builder.match("all");
        try {
            builder.between(column, begin, end);
        } catch (final Throwable t) {
            throw getException(Status.BAD_REQUEST, "Unable to parse " + begin + " and " + end + " as dates!");
        }

        final OnmsEventCollection coll = new OnmsEventCollection(m_eventDao.findMatching(builder.toCriteria()));
        coll.setTotalCount(m_eventDao.countMatching(builder.count().toCriteria()));

        return coll;
    }

    /**
     * Updates the event with id "eventid" If the "ack" parameter is "true",
     * then acks the events as the current logged in user, otherwise unacks
     * the events
     * 
     * @param eventId
     *            a {@link java.lang.Integer} object.
     * @param ack
     *            a {@link java.lang.Boolean} object.
     */
    @PUT
    @Path("{eventId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Acknowledge or unacknowledge one event",
            description = """
                    Set or clear the acknowledgement on one event. The acknowledging user is taken from the
                    authenticated principal and cannot be overridden.
                    `ack` has to be present. Any value other than `true` parses as `false` and unacknowledges the
                    event.""",
            operationId = "updateEventV1"
    )
    @RequestBody(required = true, description = "The acknowledge flag.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AckOnlyForm.class),
                    examples = @ExampleObject(value = "ack=true")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The event was updated."),
            @ApiResponse(responseCode = "400", description = "`ack` was absent.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Must supply the 'ack' parameter, set to either 'true' or 'false'"))),
            @ApiResponse(responseCode = "404", description = "No event with that id.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Event object 99999999 was not found."))),
            @ApiResponse(responseCode = "415", description = "The body was not `application/x-www-form-urlencoded`.")
    })
    public Response updateEvent(@Context final SecurityContext securityContext,
            @Parameter(description = "Event id.", example = "55273", required = true)
            @PathParam("eventId") final Long eventId, @FormParam("ack") final Boolean ack) {
        writeLock();

        try {
            final OnmsEvent event = getEvent(eventId);
            if (ack == null) {
                throw getException(Status.BAD_REQUEST, "Must supply the 'ack' parameter, set to either 'true' or 'false'");
            }
            processEventAck(securityContext, event, ack);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Updates all the events that match any filter/query supplied in the
     * form. If the "ack" parameter is "true", then acks the events as the
     * current logged in user, otherwise unacks the events
     * 
     * @param formProperties
     *            Map of the parameters passed in by form encoding
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Acknowledge or unacknowledge matching events",
            description = """
                    Set or clear the acknowledgement on every event matching the filters in the form body. Fields
                    other than `ack` are read as filters on `OnmsEvent` property names such as `eventSource`,
                    `eventUei` and `id`.
                    `ack` is optional here and defaults to `false`, and only the exact string `true` acknowledges.
                    The default limit of 10 applies, so a large match set is processed one page at a time.
                    A body that matches nothing still returns 204.""",
            operationId = "updateEventsV1"
    )
    @RequestBody(required = true, description = "The acknowledge flag, plus the filters selecting the events.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AckOnlyForm.class),
                    examples = @ExampleObject(value = "ack=true&eventSource=ReST")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The request was processed, whether or not anything matched."),
            @ApiResponse(responseCode = "415", description = "The body was not `application/x-www-form-urlencoded`."),
            @ApiResponse(responseCode = "500", description = "A form parameter is not a property of the event entity.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public Response updateEvents(@Context final SecurityContext securityContext, final MultivaluedMapImpl formProperties) {
        writeLock();

        try {
            Boolean ack = false;
            if (formProperties.containsKey("ack")) {
                ack = "true".equals(formProperties.getFirst("ack"));
                formProperties.remove("ack");
            }

            final CriteriaBuilder builder = getCriteriaBuilder(formProperties);
            builder.orderBy("eventTime").desc();

            for (final OnmsEvent event : m_eventDao.findMatching(builder.toCriteria())) {
                processEventAck(securityContext, event, ack);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    private void processEventAck(final SecurityContext securityContext, final OnmsEvent event, final Boolean ack) {
        if (ack) {
            event.setEventAckTime(new Date());
            event.setEventAckUser(securityContext.getUserPrincipal().getName());
        } else {
            event.setEventAckTime(null);
            event.setEventAckUser(null);
        }
        m_eventDao.save(event);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Send an event",
            description = """
                    Hand an event to eventd. `source` defaults to `ReST` and `time` to now when they are absent.
                    The 202 is returned as soon as the event is queued, before eventd accepts or persists it. Bean
                    validation runs first, but only `source` and `time` carry constraints and both are defaulted,
                    so a body without a `uei` returns 202.
                    Whether the event produces an alarm depends on the matching event definition, or on an
                    `alarm-data` element supplied in the body.
                    In XML the child elements have to appear in schema order (`uei`, `source`, `nodeid`, `time`,
                    `host`, `interface`, `snmphost`, `service`, `snmp`, `parms`, `descr`, `logmsg`, `severity`, and
                    so on) and the description element is `descr`, not `description`. In JSON the `logmsg` and
                    parameter `value` bodies are carried in a field named `value`, since Jackson maps the
                    `@XmlValue` field to that name. Getting either wrong is a 500.
                    `application/atom+xml` is also consumed and is unmarshalled as the same XML document.""",
            operationId = "publishEventV1"
    )
    @RequestBody(required = true, description = "The event to send.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = org.opennms.netmgt.xml.event.Event.class),
                            examples = @ExampleObject(value = """
                    {
                      "uei": "uei.opennms.org/internal/capsd/snmpConflictsWithDb",
                      "source": "ReST",
                      "descr": "Probe event",
                      "logmsg": { "dest": "logndisplay", "value": "Probe event" },
                      "severity": "Warning",
                      "parms": [
                        {
                          "parmName": "probe",
                          "value": { "type": "string", "encoding": "text", "value": "1" }
                        }
                      ]
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = org.opennms.netmgt.xml.event.Event.class),
                            examples = @ExampleObject(value = """
                    <event>
                      <uei>uei.opennms.org/internal/capsd/snmpConflictsWithDb</uei>
                      <source>ReST</source>
                      <time>2026-08-26T00:00:00-04:00</time>
                      <parms>
                        <parm>
                          <parmName>probe</parmName>
                          <value type="string" encoding="text">1</value>
                        </parm>
                      </parms>
                      <descr>Probe event</descr>
                      <logmsg dest="logndisplay">Probe event</logmsg>
                      <severity>Warning</severity>
                    </event>""")),
                    @Content(mediaType = MediaType.APPLICATION_ATOM_XML,
                            schema = @Schema(implementation = org.opennms.netmgt.xml.event.Event.class),
                            examples = @ExampleObject(value = """
                    <event>
                      <uei>uei.opennms.org/internal/capsd/snmpConflictsWithDb</uei>
                      <source>ReST</source>
                      <time>2026-08-26T00:00:00-04:00</time>
                      <parms>
                        <parm>
                          <parmName>probe</parmName>
                          <value type="string" encoding="text">1</value>
                        </parm>
                      </parms>
                      <descr>Probe event</descr>
                      <logmsg dest="logndisplay">Probe event</logmsg>
                      <severity>Warning</severity>
                    </event>"""))
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "The event was handed to eventd."),
            @ApiResponse(responseCode = "400", description = "Bean validation failed, or the event could not be sent.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "1 errors found while validating event."))),
            @ApiResponse(responseCode = "500", description = "The body could not be unmarshalled: an unknown field, or XML elements out of schema order.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unrecognized field \"description\" (Class org.opennms.netmgt.xml.event.Event), not marked as ignorable")))
    })
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

    private static CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsEvent.class);
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("node.location", "location", JoinType.LEFT_JOIN);
        builder.alias("serviceType", "serviceType", JoinType.LEFT_JOIN);

        applyQueryFilters(params, builder);
        return builder;
    }

}

