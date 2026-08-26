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

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.AlarmSummaryCollection;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SecurityHelper;
import org.opennms.web.rest.v1.model.AlarmAckForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("alarmRestService")
@Path("alarms")
@Tag(name = "Alarms", description = "Alarms API V1")
public class AlarmRestService extends AlarmRestServiceBase {

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private AcknowledgmentDao m_ackDao;

    /**
     * <p>
     * getAlarm
     * </p>
     * 
     * @param alarmId
     *            a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAlarm} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{alarmId}")
    @Transactional
    @Operation(
            summary = "Get an alarm",
            description = """
                    Return one alarm by id.
                    The literal path segment `summaries` is handled by this same method and returns per-node alarm
                    summaries instead of a single alarm.
                    The derived schema shows `date-time`; the wire carries epoch milliseconds in JSON and ISO-8601
                    strings in XML.""",
            operationId = "getAlarmV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The alarm, or the node alarm summaries for the `summaries` path segment.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsAlarm.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 4547,
                      "uei": "uei.opennms.org/apidoc/validationAlarm",
                      "description": "Probe alarm",
                      "logMessage": "Probe alarm",
                      "reductionKey": "uei.opennms.org/apidoc/validationAlarm::probe",
                      "severity": "MINOR",
                      "type": 3,
                      "count": 1,
                      "ackId": 4547,
                      "ackUser": "admin",
                      "ackTime": 1787727572573,
                      "firstEventTime": 1787727549288,
                      "lastEventTime": 1787727549288,
                      "suppressedTime": 1787727549288,
                      "suppressedUntil": 1787727549288,
                      "troubleTicket": "TT-4711",
                      "troubleTicketState": "OPEN",
                      "x733ProbableCause": 0,
                      "parameters": [],
                      "lastEvent": {
                        "id": 55315,
                        "uei": "uei.opennms.org/apidoc/validationAlarm",
                        "time": 1787727549288,
                        "createTime": 1787727549292,
                        "source": "ReST",
                        "severity": "MINOR",
                        "log": "Y",
                        "display": "Y",
                        "parameters": []
                      }
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No alarm with that id."),
            @ApiResponse(responseCode = "403", description = "The caller holds none of `ROLE_ADMIN`, `ROLE_REST`, `ROLE_USER` or `ROLE_MOBILE`. The shipped security rules gate the endpoint on the same four roles, so the container answers first with its own HTML error page; the plain-text body below comes from the resource check.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'jroe', is not allowed to read alarms."))),
            @ApiResponse(responseCode = "500", description = "The path segment is neither `summaries` nor an integer.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"abc\"")))
    })
    public Response getAlarm(@Context SecurityContext securityContext,
            @Parameter(description = "Alarm id, or the literal `summaries` for per-node alarm summaries.",
                    example = "4547", required = true)
            @PathParam("alarmId") final String alarmId) {
        SecurityHelper.assertUserReadCredentials(securityContext);
        if ("summaries".equals(alarmId)) {
            final List<AlarmSummary> collection = m_alarmDao.getNodeAlarmSummaries();
            return collection == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(new AlarmSummaryCollection(collection)).build();
        } else {
            final OnmsAlarm alarm = m_alarmDao.get(Integer.valueOf(alarmId));
            return alarm == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(alarm).build();
        }
    }

    /**
     * <p>
     * getCount
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    @Operation(
            summary = "Count all alarms",
            description = "Return the total number of alarm rows as a plain-text integer. Query parameters are "
                    + "ignored.",
            operationId = "getAlarmCountV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The alarm count.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "13"))),
            @ApiResponse(responseCode = "403", description = "The caller holds none of `ROLE_ADMIN`, `ROLE_REST`, `ROLE_USER` or `ROLE_MOBILE`. The shipped security rules gate the endpoint on the same four roles, so the container answers first with its own HTML error page; the plain-text body below comes from the resource check.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'jroe', is not allowed to read alarms.")))
    })
    public String getCount(@Context SecurityContext securityContext) {
        SecurityHelper.assertUserReadCredentials(securityContext);
        return Integer.toString(m_alarmDao.countAll());
    }

    /**
     * <p>
     * getAlarms
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.model.OnmsAlarmCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Search alarms",
            description = """
                    Return alarms matching the query parameters, newest `lastEventTime` first unless `orderBy`
                    says otherwise.
                    Filters are `OnmsAlarm` property names, with `nodeId`, `nodeLabel` and `alarmId` accepted as
                    aliases for `node.id`, `node.label` and `id`. `limit` (default 10), `offset`, `orderBy`,
                    `order`, `match` and `comparator` shape the result. A filter name that is not a property of
                    the entity fails with 500.
                    `totalCount` is the unpaged match count; `count` is the size of this page.""",
            operationId = "getAlarmsV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The matching alarms.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsAlarmCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "alarm": [ {
                          "id": 4547,
                          "uei": "uei.opennms.org/apidoc/validationAlarm",
                          "description": "Probe alarm",
                          "logMessage": "Probe alarm",
                          "reductionKey": "uei.opennms.org/apidoc/validationAlarm::probe",
                          "severity": "MINOR",
                          "type": 3,
                          "count": 1,
                          "ackId": 4547,
                          "ackUser": "admin",
                          "ackTime": 1787727572573,
                          "firstEventTime": 1787727549288,
                          "lastEventTime": 1787727549288,
                          "suppressedTime": 1787727549288,
                          "suppressedUntil": 1787727549288,
                          "troubleTicket": "TT-4711",
                          "troubleTicketState": "OPEN",
                          "x733ProbableCause": 0,
                          "parameters": [],
                          "lastEvent": {
                            "id": 55315,
                            "uei": "uei.opennms.org/apidoc/validationAlarm",
                            "time": 1787727549288,
                            "createTime": 1787727549292,
                            "source": "ReST",
                            "severity": "MINOR",
                            "log": "Y",
                            "display": "Y",
                            "parameters": []
                          }
                        } ]
                    }"""))),
            @ApiResponse(responseCode = "500", description = "A query parameter is not a property of the alarm entity.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null"))),
            @ApiResponse(responseCode = "403", description = "The caller holds none of `ROLE_ADMIN`, `ROLE_REST`, `ROLE_USER` or `ROLE_MOBILE`. The shipped security rules gate the endpoint on the same four roles, so the container answers first with its own HTML error page; the plain-text body below comes from the resource check.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'jroe', is not allowed to read alarms.")))
    })
    public OnmsAlarmCollection getAlarms(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo) {
        SecurityHelper.assertUserReadCredentials(securityContext);
        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters(), false);
        builder.distinct();
        final OnmsAlarmCollection coll = new OnmsAlarmCollection(m_alarmDao.findMatching(builder.toCriteria()));

        // For getting totalCount
        coll.setTotalCount(m_alarmDao.countMatching(builder.count().toCriteria()));

        return coll;
    }

    /**
     * <p>
     * updateAlarm
     * </p>
     * 
     * @param alarmId
     *            a {@link java.lang.String} object.
     * @param ack
     *            a {@link java.lang.Boolean} object.
     */
    @PUT
    @Path("{alarmId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Acknowledge, clear, escalate or ticket one alarm",
            description = """
                    Apply an acknowledgement action and optional trouble-ticket fields to one alarm. The body is
                    form-encoded; JSON is rejected with 415.
                    `ack`, `escalate` and `clear` are checked in that order and only the first one present is
                    acted on. `ticketId` and `ticketState` are applied independently of the acknowledgement, and a
                    `ticketState` outside the enumeration is ignored rather than reported.
                    A body with none of those fields still returns 204 without changing the alarm.""",
            operationId = "updateAlarmV1"
    )
    @RequestBody(required = true, description = "The action to apply.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmAckForm.class),
                    examples = @ExampleObject(value = "ack=true")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The request was processed."),
            @ApiResponse(responseCode = "400", description = "No alarm with that id.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unable to locate alarm with ID '99999999'"))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'jroe', is not allowed to perform updates to alarms as user 'admin'"))),
            @ApiResponse(responseCode = "404", description = "The path segment is not an integer."),
            @ApiResponse(responseCode = "415", description = "The body was not `application/x-www-form-urlencoded`.")
    })
    public Response updateAlarm(@Context final SecurityContext securityContext,
            @Parameter(description = "Alarm id.", example = "4547", required = true)
            @PathParam("alarmId") final Integer alarmId, final MultivaluedMapImpl formProperties) {
        writeLock();

        try {
            if (alarmId == null) {
                return getBadRequestResponse("Unable to determine alarm ID to update based on query path.");
            }

            final String ackValue = formProperties.getFirst("ack");
            formProperties.remove("ack");
            final String escalateValue = formProperties.getFirst("escalate");
            formProperties.remove("escalate");
            final String clearValue = formProperties.getFirst("clear");
            formProperties.remove("clear");
            final String ackUserValue = formProperties.getFirst("ackUser");
            formProperties.remove("ackUser");
            final String ticketIdValue = formProperties.getFirst("ticketId");
            formProperties.remove("ticketId");
            final String ticketStateValue = formProperties.getFirst("ticketState");
            formProperties.remove("ticketState");

            final OnmsAlarm alarm = m_alarmDao.get(alarmId);
            if (alarm == null) {
                return getBadRequestResponse("Unable to locate alarm with ID '" + alarmId + "'");
            }

            boolean alarmUpdated = false;
            if (StringUtils.isNotBlank(ticketIdValue)) {
                alarmUpdated = true;
                alarm.setTTicketId(ticketIdValue);
            }
            if (EnumUtils.isValidEnum(TroubleTicketState.class, ticketStateValue)) {
                alarmUpdated = true;
                alarm.setTTicketState(TroubleTicketState.valueOf(ticketStateValue));
            }
            if (alarmUpdated) {
                m_alarmDao.saveOrUpdate(alarm);
            }

            final String ackUser = ackUserValue == null ? securityContext.getUserPrincipal().getName() : ackUserValue;

            if (ackUser != null && StringUtils.isNotBlank(ackUser)) {
                SecurityHelper.assertUserEditCredentials(securityContext, ackUser);
            }

            final OnmsAcknowledgment acknowledgement = new OnmsAcknowledgment(alarm, ackUser);
            acknowledgement.setAckAction(AckAction.UNSPECIFIED);

            boolean isProcessAck = false;
            if (ackValue != null) {
                isProcessAck = true;
                if (Boolean.parseBoolean(ackValue)) {
                    acknowledgement.setAckAction(AckAction.ACKNOWLEDGE);
                } else {
                    acknowledgement.setAckAction(AckAction.UNACKNOWLEDGE);
                }
            } else if (escalateValue != null) {
                isProcessAck = true;
                if (Boolean.parseBoolean(escalateValue)) {
                    acknowledgement.setAckAction(AckAction.ESCALATE);
                }
            } else if (clearValue != null) {
                isProcessAck = true;
                if (Boolean.parseBoolean(clearValue)) {
                    acknowledgement.setAckAction(AckAction.CLEAR);
                }
            }
            if (isProcessAck) {
                m_ackDao.processAck(acknowledgement);
                m_ackDao.flush();
            }

            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>
     * updateAlarms
     * </p>
     * 
     * @param formProperties
     *            a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Acknowledge, clear or escalate matching alarms",
            description = """
                    Apply one acknowledgement action to every alarm matching the filters in the form body. Fields
                    other than `ack`, `escalate`, `clear` and `ackUser` are read as filters on `OnmsAlarm` property
                    names such as `id` and `severity`, so a body of just `ack=true` matches everything the filter
                    defaults allow and acknowledges it.
                    `alarmId` is accepted as an alias for `id`, but sending both is a 400. `ticketId` and
                    `ticketState` are not honoured here.
                    The `limit` and `offset` the filter parsing derives are reset to unlimited, so the action
                    reaches the whole match set rather than one page of it.""",
            operationId = "updateAlarmsV1"
    )
    @RequestBody(required = true, description = "The action to apply, plus the filters selecting the alarms.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmAckForm.class),
                    examples = @ExampleObject(value = "ack=true&id=4547")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "At least one alarm matched and was processed."),
            @ApiResponse(responseCode = "304", description = "No alarm matched the filters."),
            @ApiResponse(responseCode = "400", description = "None of `ack`, `escalate` or `clear` was supplied, or both `id` and `alarmId` were.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Must supply one of the 'ack', 'escalate', or 'clear' parameters, set to either 'true' or 'false'."))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'jroe', is not allowed to perform updates to alarms as user 'admin'"))),
            @ApiResponse(responseCode = "415", description = "The body was not `application/x-www-form-urlencoded`."),
            @ApiResponse(responseCode = "500", description = "A form parameter is not a property of the alarm entity.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public Response updateAlarms(@Context final SecurityContext securityContext, final MultivaluedMapImpl formProperties) {
        writeLock();

        try {
            final String ackValue = formProperties.getFirst("ack");
            formProperties.remove("ack");
            final String escalateValue = formProperties.getFirst("escalate");
            formProperties.remove("escalate");
            final String clearValue = formProperties.getFirst("clear");
            formProperties.remove("clear");

            final CriteriaBuilder builder = getCriteriaBuilder(formProperties, false);
            builder.distinct();
            builder.limit(0);
            builder.offset(0);

            final String ackUser = formProperties.containsKey("ackUser") ? formProperties.getFirst("ackUser") : securityContext.getUserPrincipal().getName();
            formProperties.remove("ackUser");
            SecurityHelper.assertUserEditCredentials(securityContext, ackUser);

            final List<OnmsAlarm> alarms = m_alarmDao.findMatching(builder.toCriteria());
            for (final OnmsAlarm alarm : alarms) {
                final OnmsAcknowledgment acknowledgement = new OnmsAcknowledgment(alarm, ackUser);
                acknowledgement.setAckAction(AckAction.UNSPECIFIED);
                if (ackValue != null) {
                    if (Boolean.parseBoolean(ackValue)) {
                        acknowledgement.setAckAction(AckAction.ACKNOWLEDGE);
                    } else {
                        acknowledgement.setAckAction(AckAction.UNACKNOWLEDGE);
                    }
                } else if (escalateValue != null) {
                    if (Boolean.parseBoolean(escalateValue)) {
                        acknowledgement.setAckAction(AckAction.ESCALATE);
                    }
                } else if (clearValue != null) {
                    if (Boolean.parseBoolean(clearValue)) {
                        acknowledgement.setAckAction(AckAction.CLEAR);
                    }
                } else {
                    throw getException(Status.BAD_REQUEST, "Must supply one of the 'ack', 'escalate', or 'clear' parameters, set to either 'true' or 'false'.");
                }
                m_ackDao.processAck(acknowledgement);
            }

            return alarms == null || alarms.isEmpty() ? Response.notModified().build() : Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

}
