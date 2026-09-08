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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.model.v2.AlarmCollectionDTO;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.SecurityHelper;
import org.opennms.web.rest.support.StringCollection;
import org.opennms.web.rest.v2.model.AlarmMemoRequest;
import org.opennms.web.rest.v2.model.AlarmPropertyUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.SecurityContext;
import java.util.UUID;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Path("situations")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Tag(name = "Situation", description = """
        Situations API. A situation is an alarm that correlates other alarms: it carries `isSituation`
        and a `relatedAlarms` list and lives in the same table as ordinary alarms, so every alarm
        operation applies to it as well. This resource narrows the collection reads to alarms where
        `isSituation` is true and adds the membership and workflow actions.

        Membership changes are applied by publishing a `uei.opennms.org/alarms/situation` event and
        letting alarmd act on it, so the handler returns before the change is visible. A read issued
        immediately afterwards can still show the previous membership.

        An alarm may belong to at most one situation. Alarms already held by another situation are
        silently skipped rather than reported, so a 200 does not by itself mean every id in
        `alarmIdList` was taken.

        `GET /situations/{id}` and the per-alarm actions under `/situations/{id}` are not restricted
        to situations: they resolve any alarm id. Acting on a situation alarm does not cascade to its
        members, so acknowledging or clearing a situation leaves the correlated alarms as they were.

        Five operations here are the inherited alarm operations, and they carry the alarm wording and
        an `_1`-suffixed operationId: `GET /situations/{id}` (`getAlarmById_1`), `POST /situations`
        (`createAlarm_1`), the form and JSON variants of `PUT /situations/{id}`
        (`updateAlarmProperties_1`, `replaceAlarm_1`) and `DELETE /situations/{id}` (`deleteAlarm_1`).
        Creating and deleting are not implemented on either path.""")
public class SituationsRestService extends AlarmRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SituationsRestService.class);
    public enum Action { ACK, UNACK, ESCALATE, CLEAR, ACCEPT}
    static final String SITUATION_LOG_MSG="situationLogMsg";
    static final String DESCR="situationDescr";
    static final String STATUS="situationStatus";
    static final String ID="situationId";
    static final String RELATED_PREFIX="related-reductionKey";
    static final String CREATED="USER_CREATED";
    static final String REMOVED_ALARM="REMOVED_ALARM";
    static final String ADDED_ALARM="ADDED_ALARM";
    static final String ACCEPTED="ACCEPTED";
    static final String REJECTED="REJECTED";
    static final String SOURCE = "Api";
    private final String IS_SITUATION = "isSituation";

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired
    private AcknowledgmentDao m_ackDao;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "List situations",
            description = """
        Return a page of alarms whose `isSituation` is true. The restriction is added to whatever `_s`
        supplies, so `_s` narrows the set of situations rather than widening it to ordinary alarms.
        Only `isSituation` has a registered query behaviour on this resource. Other `alarm.*` properties
        still pass through to the criteria and filter normally; only terms that need a registered type
        conversion, such as `alarm.severity`, fail with 500.

        `relatedAlarms` holds a summary of each member alarm. The situation itself carries the highest
        severity of its members at the time the correlating event was sent.

        Example query: `_s=alarm.isSituation==true&orderBy=lastEventTime&order=desc`.""",
            operationId = "getSituations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of situations.",
                    headers = @Header(name = "Content-Range", description = "Range of rows returned and the total, as `items <from>-<to>/<total>`.",
                            schema = @Schema(type = "string", example = "items 0-0/1")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AlarmCollectionDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "alarm": [
                        {
                          "id": 4552,
                          "uei": "uei.opennms.org/alarms/situation",
                          "location": "Default",
                          "reductionKey": "uei.opennms.org/alarms/situation:de4b1c3d-e028-4712-aafc-ea5304d61b7b",
                          "type": 3,
                          "count": 1,
                          "severity": "NORMAL",
                          "firstEventTime": 1787727667348,
                          "lastEventTime": 1787727667348,
                          "description": "Link flapping on the north uplink",
                          "logMessage": "Created situation with 2 alarms",
                          "relatedAlarms": [
                            {
                              "id": 4549,
                              "type": 2,
                              "severity": "NORMAL",
                              "reductionKey": "uei.opennms.org/nodes/nodeDown::4",
                              "label": "OpenNMS-defined node event: nodeDown"
                            }
                          ],
                          "affectedNodeCount": 1
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "204", description = "No situation matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "`_s` named a property that is not registered on this resource.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "could not resolve property: bogusprop of: org.opennms.netmgt.model.OnmsAlarm")))
    })
    @Override
    public Response get(@Context UriInfo uriInfo,
                        @Context SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @POST
    @Path("create")
    @Transactional
    @Operation(
            summary = "Create a situation from a set of alarms",
            description = """
        Correlate two or more alarms into a new situation. A random UUID is used as the situation id
        and appears in the reduction key of the situation alarm as
        `uei.opennms.org/alarms/situation:<uuid>`.

        An id that names no alarm fails the whole call with 500 when the loaded proxy is first touched.
        Ids whose alarm already belongs to another situation are dropped from the set; if fewer than two
        alarms survive, nothing is created and the call answers 204.

        The situation is created by publishing an event, so the call returns before the situation alarm
        exists. `description` is stored as given and `diagnosticText`, when present, is appended to it
        wrapped in a `<p>Diagnostic: ...</p>` element. `feedback` is accepted and not used.""",
            operationId = "createSituation")
    @RequestBody(required = true, description = "Alarms to correlate, plus the text to describe the situation with.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SituationPayload.class),
                    examples = @ExampleObject(value = """
                    {
                      "alarmIdList": [4549, 4550],
                      "diagnosticText": "Both alarms follow the same uplink flap.",
                      "description": "Link flapping on the north uplink",
                      "feedback": null
                    }""")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The correlating event was published. The situation alarm appears shortly afterwards."),
            @ApiResponse(responseCode = "204", description = "Fewer than two of the ids resolved to an alarm that is free to correlate. Nothing was created."),
            @ApiResponse(responseCode = "500", description = "The body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No content to map to Object due to end of input")))
    })
    public Response create(@Context UriInfo uriInfo, SituationPayload payload) throws InterruptedException {
        String situationId = UUID.randomUUID().toString();
        return handleAssociation(
                payload.getAlarmIdList(),
                payload.getDiagnosticText(),
                payload.getDescription(),
                uriInfo,
                situationId
        );
    }

    @POST
    @Path("associateAlarm")
    @Transactional
    @Operation(
            summary = "Add alarms to an existing situation",
            description = """
        Add the alarms in `alarmIdList` to the situation named by `situationId`. The new membership is
        the union of the current members and the ids that resolved, so this never removes anything.

        An id that names no alarm fails the whole call with 500 when the loaded proxy is first touched.
        Ids whose alarm already belongs to another situation are dropped. When the union equals the
        current membership the call answers 204 and no event is published.

        The change is applied by publishing an event, so the call returns before the membership is
        visible. `feedback` is accepted and not used.""",
            operationId = "associateSituationAlarms")
    @RequestBody(required = true, description = "Situation to add to, and the alarms to add.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AlarmAddRemoveRequest.class),
                    examples = @ExampleObject(value = """
                    {
                      "situationId": 4552,
                      "alarmIdList": [4551],
                      "feedback": null
                    }""")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The membership event was published."),
            @ApiResponse(responseCode = "204", description = "The membership would not change. No event was published."),
            @ApiResponse(responseCode = "400", description = "`situationId` names no alarm, or names an alarm that is not a situation.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid situation ID: 999999")))
    })
    public Response associateAlarm(@Context UriInfo uriInfo,
                                   AlarmAddRemoveRequest request) throws InterruptedException {

        OnmsAlarm situationAlarm = getDao().get(request.getSituationId());
        if (situationAlarm == null || !situationAlarm.isSituation()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid situation ID: " + request.getSituationId())
                    .build();
        }

        Set<OnmsAlarm> toAdd = loadValidAlarms(request.getAlarmIdList(), uriInfo);
        Set<OnmsAlarm> mergedAlarms = new LinkedHashSet<>(situationAlarm.getRelatedAlarms());
        mergedAlarms.addAll(toAdd);
        if (mergedAlarms.equals(situationAlarm.getRelatedAlarms())) {
            return Response.noContent().build();
        }

        String sid = getSituationParamFromAlarm(situationAlarm, ID)
                .orElseGet(() -> {
                    LOG.warn("Could not find situationId on alarm: {}. Using reductionKey.", situationAlarm.getId());
                    return String.valueOf(situationAlarm.getId());
                });

        buildAndSendEvent(mergedAlarms, situationAlarm, sid, ADDED_ALARM, null, null);
        return Response.ok().build();
    }

    @DELETE
    @Path("removeAlarm")
    @Transactional
    @Operation(
            summary = "Remove alarms from a situation",
            description = """
        Drop the alarms in `alarmIdList` from the situation named by `situationId`. Removing every
        member is allowed and leaves the situation alarm in place with no members.

        Unlike the add operation this does not report an unusable `situationId`: an id that names no
        alarm, or names an alarm that is not a situation, answers 204 in the same way as a request that
        would change nothing.

        The change is applied by publishing an event, so the call returns before the membership is
        visible. The request body is read from a DELETE. `feedback` is accepted and not used.""",
            operationId = "removeSituationAlarms")
    @RequestBody(required = true, description = "Situation to remove from, and the alarms to remove.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AlarmAddRemoveRequest.class),
                    examples = @ExampleObject(value = """
                    {
                      "situationId": 4552,
                      "alarmIdList": [4551],
                      "feedback": null
                    }""")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The membership event was published."),
            @ApiResponse(responseCode = "204", description = "The membership would not change, or `situationId` does not name a situation. No event was published.")
    })
    public Response removeAlarm(@Context UriInfo uriInfo,
                                AlarmAddRemoveRequest request) {
        OnmsAlarm situationAlarm = getDao().get(request.getSituationId());
        if (situationAlarm == null || !situationAlarm.isSituation()) {
            return Response.noContent().build();
        }

      return removeAlarmsFromSituation(
                situationAlarm,
                request.getAlarmIdList()
        );
    }


    @POST
    @Path("clear")
    @Transactional
    @Operation(
            summary = "Clear one alarm",
            description = """
        Set the severity of a single alarm to Cleared through the acknowledgement DAO. Despite the
        path and the field name, `situationId` is read as the id of the alarm to clear: any alarm id is
        accepted, and the alarm does not have to be a situation. `alarmIdList` is ignored here.

        The acknowledgement is recorded against the authenticated user.""",
            operationId = "clearSituationAlarm")
    @RequestBody(required = true, description = "The alarm to clear, in `situationId`.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AlarmAddRemoveRequest.class),
                    examples = @ExampleObject(value = """
                    {
                      "situationId": 4552,
                      "alarmIdList": [],
                      "feedback": null
                    }""")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The alarm was cleared."),
            @ApiResponse(responseCode = "400", description = "`situationId` was absent or named no alarm.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "absent", value = "Unable to determine alarm ID to update based on query path."),
                                    @ExampleObject(name = "unknown", value = "Unable to locate alarm with ID '999999'")
                            })),
            @ApiResponse(responseCode = "403", description = "The authenticated user may not clear alarms.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    public Response doAction(
            AlarmAddRemoveRequest req,
            @Context SecurityContext secCtx) {

        Integer alarmId = req.getSituationId();
        try {
            writeLock();
            if (alarmId == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Unable to determine alarm ID to update based on query path.").type(MediaType.TEXT_PLAIN).build();
            }
            final OnmsAlarm alarm = getDao().get(alarmId);
            if (alarm == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Unable to locate alarm with ID '" + alarmId + "'").type(MediaType.TEXT_PLAIN).build();
            }

            final String ackUser = secCtx.getUserPrincipal().getName();
            if (StringUtils.isNotBlank(ackUser)) {
                SecurityHelper.assertUserEditCredentials(secCtx, ackUser);
            }
            clearAlarm(alarm,ackUser);
        } finally {
            writeUnlock();
        }

        return Response.ok().build();
    }


    @POST
    @Path("alarms/clear")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Transactional
    @Operation(
            summary = "Remove alarms from a situation and clear them",
            description = """
        Drop the alarms in `alarmIdList` from the situation named by `situationId`, then set each of
        those alarms to Cleared.

        The removal runs first and decides whether the clear happens at all: when the removal would
        change nothing the call answers 204 and no alarm is cleared. Ids in `alarmIdList` that are not
        members of the situation are still cleared, as long as at least one id causes the membership to
        change.

        The removal is published as an event while the clear is applied directly, so the two halves do
        not become visible at the same time.""",
            operationId = "removeAndClearSituationAlarms")
    @RequestBody(required = true, description = "Situation to remove from, and the alarms to remove and clear.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AlarmAddRemoveRequest.class),
                    examples = @ExampleObject(value = """
                    {
                      "situationId": 4552,
                      "alarmIdList": [4550],
                      "feedback": null
                    }""")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The removal event was published and the listed alarms were cleared."),
            @ApiResponse(responseCode = "204", description = "The membership would not change. Nothing was removed and nothing was cleared."),
            @ApiResponse(responseCode = "400", description = "`situationId` names no alarm, or names an alarm that is not a situation.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid situation ID: 999999"))),
            @ApiResponse(responseCode = "403", description = "The authenticated user may not clear alarms.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    public Response removeAndClear(
            AlarmAddRemoveRequest req,
            @Context SecurityContext secCtx,
            @Context UriInfo uriInfo) throws InterruptedException {

        try {
            writeLock();
            OnmsAlarm situationAlarm = getDao().get(req.getSituationId());
            if (situationAlarm == null || !situationAlarm.isSituation()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid situation ID: " + req.getSituationId())
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }

            Response initialResponse = removeAlarmsFromSituation(
                    situationAlarm,
                    req.getAlarmIdList()
            );
            if (initialResponse.getStatus() != 200) {
                return initialResponse;
            }

            String user = secCtx.getUserPrincipal().getName();
            if (StringUtils.isNotBlank(user)) {
                SecurityHelper.assertUserEditCredentials(secCtx, user);
            }
            clearAlarms(req.getAlarmIdList(), user);

            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Path("accepted/{id}")
    @Operation(
            summary = "Mark a situation accepted",
            description = """
        Record that an operator accepts the correlation, by publishing a situation event whose
        `situationStatus` parameter is `ACCEPTED`. The membership is republished unchanged.

        Acceptance is read back from the `situationStatus` parameter of the last event on the
        situation, so a later membership change overwrites it and the situation can be accepted again.
        Calling this on an already accepted situation answers 304.""",
            operationId = "acceptSituation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The acceptance event was published."),
            @ApiResponse(responseCode = "304", description = "The last event on the situation already reports it accepted. No body is returned."),
            @ApiResponse(responseCode = "404", description = "The id names no alarm, or names an alarm that is not a situation.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Situation not found: 999999")))
    })
    public Response acceptSituation(
            @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                    description = "Database id of the situation alarm.", example = "4552")
            @PathParam("id") Integer id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Situation ID is required")
                    .build();
        }

        OnmsAlarm situation = getDao().get(id);
        if (situation == null || !situation.isSituation()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Situation not found: " + id)
                    .build();
        }

        String currentStatus = getSituationParamFromAlarm(situation, STATUS)
                .orElse("");

        if (ACCEPTED.equals(currentStatus)) {
            LOG.debug("Situation {} already accepted", id);
            return Response
                    .status(Response.Status.NOT_MODIFIED)
                    .entity("Situation " + id + " already accepted")
                    .build();
        }

        String sid = getSituationParamFromAlarm(situation, ID)
                .orElseGet(() -> {
                    LOG.warn("Could not find situationId on alarm: {}. Using reductionKey.", situation.getId());
                    return String.valueOf(situation.getId());
                });

        buildAndSendEvent(
                situation.getRelatedAlarms(),
                situation,
                sid,
                ACCEPTED,
                null,
                null
        );

        return Response.ok().build();
    }


    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Count situations",
            description = """
        Return the number of alarms whose `isSituation` is true, as a plain-text integer. The response
        is `text/plain` only, so a request that asks solely for `application/json` is answered with
        404. `limit` is ignored, but a non-zero `offset` reaches the count query and fails with 500.

        Example query: `_s=alarm.isSituation==true`.""",
            operationId = "getSituationCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching situations.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "1"))),
            @ApiResponse(responseCode = "404", description = "The request did not accept `text/plain`. No body is returned."),
            @ApiResponse(responseCode = "500", description = "`_s` named a property that is not registered on this resource.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "could not resolve property: bogusprop of: org.opennms.netmgt.model.OnmsAlarm")))
    })
    @Override
    public Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "List the properties situations can be queried on",
            description = """
        Return the same property list as `GET /alarms/properties`. Only `alarm.isSituation` is
        registered as a query behaviour on this resource, so most of the listed properties cannot be
        used in `_s` here. `q` filters the list by a case-insensitive substring of the name and a `q`
        that matches nothing yields 200 with an empty list.""",
            operationId = "getSituationProperties")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The matching query properties.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SearchPropertyCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "searchProperty": [
                        {
                          "id": "isSituation",
                          "name": "Is Situation",
                          "type": "BOOLEAN",
                          "orderBy": true,
                          "iplike": false
                        }
                      ]
                    }""")))
    })
    @Override
    public Response getProperties(@Parameter(in = ParameterIn.QUERY, name = "q",
            description = "Case-insensitive substring of the property name.", example = "situation")
                                  @QueryParam("q") final String query) {
        return super.getProperties(query);
    }

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "List the values a query property takes",
            description = """
        Return the distinct values of one query property over the alarm table, not restricted to
        situations. The element type follows the property type, and a property whose type has no value
        listing, such as `isSituation`, answers 204. `propertyId` is the unprefixed id from
        `GET /situations/properties`.""",
            operationId = "getSituationPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values of the property.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 7,
                      "count": 7,
                      "offset": 0,
                      "value": [1, 2, 3, 4, 5, 6, 7]
                    }"""))),
            @ApiResponse(responseCode = "204", description = "The property has a type with no value listing, such as BOOLEAN. No body is returned."),
            @ApiResponse(responseCode = "404", description = "No query property has that id. No body is returned.")
    })
    @Override
    public Response getPropertyValues(
            @Parameter(in = ParameterIn.PATH, name = "propertyId", required = true,
                    description = "Unprefixed property id from `GET /situations/properties`.", example = "severity")
            @PathParam("propertyId") final String propertyId,
            @Parameter(in = ParameterIn.QUERY, name = "q",
                    description = "Substring the value must contain. Database-backed properties match case-insensitively; only fixed value lists are case-sensitive.", example = "situation")
            @QueryParam("q") final String query,
            @Parameter(in = ParameterIn.QUERY, name = "limit",
                    description = "Maximum number of values returned. Applies only to values read from the database.",
                    example = "25")
            @QueryParam("limit") final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @POST
    @Path("{id}")
    @Operation(
            summary = "Rejected: situations cannot be created at a chosen id",
            description = "Always answers 404.",
            operationId = "createSituationWithId",
            parameters = @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                    description = "Alarm database id. The value is not read: every request to this path is answered with 404.",
                    example = "4241"))
    @ApiResponses(@ApiResponse(responseCode = "404", description = "Always. No body is returned."))
    @Override
    public Response createSpecific() {
        return super.createSpecific();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update properties of several situations",
            description = """
        Apply the form parameters to every situation matching `_s`. The default `limit` of 10 applies to
        the selection, so a call without an explicit `limit` touches at most 10 situations. The batch
        runs in one transaction and stops at the first failure, leaving earlier situations already
        changed.

        Example query: `_s=alarm.isSituation==true`.""",
            operationId = "updateSituations")
    @RequestBody(required = true, description = "Alarm properties to apply, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmPropertyUpdateRequest.class),
                    examples = @ExampleObject(value = "ack=true")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Every selected situation was updated. No body is returned."),
            @ApiResponse(responseCode = "404", description = "No situation matched `_s`. No body is returned."),
            @ApiResponse(responseCode = "500", description = "`_s` named a property that is not registered on this resource.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "could not resolve property: bogusprop of: org.opennms.netmgt.model.OnmsAlarm"))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    @Override
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                               @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @DELETE
    @Operation(
            summary = "Not implemented: delete several situations",
            description = """
        Deleting alarms is not implemented. When `_s` selects at least one situation the handler answers
        501 without deleting anything; when nothing matches it answers 404 instead.

        Example query: `_s=alarm.isSituation==true`.""",
            operationId = "deleteSituations")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No situation matched `_s`. No body is returned."),
            @ApiResponse(responseCode = "501", description = "Delete is not implemented. No body is returned.")
    })
    @Override
    public Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                               @Context final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @PUT
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Set the sticky memo on a situation",
            description = """
        Replace the sticky memo on the alarm row. The lookup is not restricted to situations. `body` is
        required; when `user` is omitted the authenticated user is recorded as the author.""",
            operationId = "updateSituationMemo")
    @RequestBody(required = true, description = "Memo text and optional author, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmMemoRequest.class),
                    examples = @ExampleObject(value = "body=Correlation+confirmed+by+the+NOC.&user=admin")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The memo was written. No body is returned."),
            @ApiResponse(responseCode = "400", description = "`body` was absent.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Body cannot be null."))),
            @ApiResponse(responseCode = "404", description = "No alarm has that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Alarm not found."))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    @Override
    public Response updateMemo(@Context final SecurityContext securityContext,
                               @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                       description = "Database id of the situation alarm.", example = "4552")
                               @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        return super.updateMemo(securityContext, alarmId, params);
    }

    @PUT
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Set the journal note on a situation",
            description = """
        Replace the journal note, which is keyed on the reduction key rather than on the alarm row. A
        situation reduction key contains the situation UUID, so the note follows that situation only.
        `body` is required.""",
            operationId = "updateSituationJournal")
    @RequestBody(required = true, description = "Journal text and optional author, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmMemoRequest.class),
                    examples = @ExampleObject(value = "body=Escalated+to+the+transport+team.&user=admin")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The journal note was written. No body is returned."),
            @ApiResponse(responseCode = "400", description = "`body` was absent.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Body cannot be null."))),
            @ApiResponse(responseCode = "404", description = "No alarm has that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Alarm not found."))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    @Override
    public Response updateJournal(@Context final SecurityContext securityContext,
                                  @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                          description = "Database id of the situation alarm.", example = "4552")
                                  @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        return super.updateJournal(securityContext, alarmId, params);
    }

    @DELETE
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Remove the sticky memo from a situation",
            description = """
        Remove the note attached to the alarm row. Removing one that is not set still answers 204. No
        request body is read, although the handler declares
        `application/x-www-form-urlencoded`.""",
            operationId = "deleteSituationMemo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The sticky memo is gone, whether or not one was set. No body is returned."),
            @ApiResponse(responseCode = "404", description = "No alarm has that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Alarm not found."))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    @Override
    public Response removeMemo(@Context final SecurityContext securityContext,
                               @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                       description = "Database id of the situation alarm.", example = "4552")
                               @PathParam("id") final Integer alarmId) {
        return super.removeMemo(securityContext, alarmId);
    }

    @DELETE
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Remove the journal note from a situation",
            description = """
        Remove the note keyed on the reduction key. Removing one that is not set still answers 204. No
        request body is read, although the handler declares
        `application/x-www-form-urlencoded`.""",
            operationId = "deleteSituationJournal")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The journal note is gone, whether or not one was set. No body is returned."),
            @ApiResponse(responseCode = "404", description = "No alarm has that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Alarm not found."))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    @Override
    public Response removeJournal(@Context final SecurityContext securityContext,
                                  @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                          description = "Database id of the situation alarm.", example = "4552")
                                  @PathParam("id") final Integer alarmId) {
        return super.removeJournal(securityContext, alarmId);
    }

    @POST
    @Path("{id}/ticket/create")
    @Operation(
            summary = "Create the trouble ticket for a situation",
            description = """
        Ask the configured ticketer plugin to raise a ticket for the alarm, asynchronously. Members of
        the situation are not ticketed.

        Gated on `opennms.alarmTroubleTicketEnabled` being `true`. With the ticketer disabled every
        call answers 501, including a call naming an alarm that does not exist, because the gate is
        checked before the alarm is looked up.""",
            operationId = "createSituationTicket")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "The request was handed to the ticketer plugin. No body is returned."),
            @ApiResponse(responseCode = "501", description = "The ticketer plugin is disabled.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "AlarmTroubleTicketer is not enabled. Cannot perform operation"))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    @Override
    public Response createTicket(@Context final SecurityContext securityContext,
                                 @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                         description = "Database id of the situation alarm.", example = "4552")
                                 @PathParam("id") final Integer alarmId) throws Exception {
        return super.createTicket(securityContext, alarmId);
    }

    @POST
    @Path("{id}/ticket/update")
    @Operation(
            summary = "Refresh the trouble ticket for a situation",
            description = """
        Ask the configured ticketer plugin to re-read the ticket from the helpdesk system and write the
        current state back onto the alarm, asynchronously.

        Gated on `opennms.alarmTroubleTicketEnabled` being `true`; with the ticketer disabled every
        call answers 501.""",
            operationId = "updateSituationTicket")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "The request was handed to the ticketer plugin. No body is returned."),
            @ApiResponse(responseCode = "501", description = "The ticketer plugin is disabled.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "AlarmTroubleTicketer is not enabled. Cannot perform operation"))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    @Override
    public Response updateTicket(@Context final SecurityContext securityContext,
                                 @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                         description = "Database id of the situation alarm.", example = "4552")
                                 @PathParam("id") final Integer alarmId) throws Exception {
        return super.updateTicket(securityContext, alarmId);
    }

    @POST
    @Path("{id}/ticket/close")
    @Operation(
            summary = "Close the trouble ticket for a situation",
            description = """
        Ask the configured ticketer plugin to close the ticket recorded on the alarm, asynchronously.

        Gated on `opennms.alarmTroubleTicketEnabled` being `true`; with the ticketer disabled every
        call answers 501.""",
            operationId = "closeSituationTicket")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "The request was handed to the ticketer plugin. No body is returned."),
            @ApiResponse(responseCode = "501", description = "The ticketer plugin is disabled.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "AlarmTroubleTicketer is not enabled. Cannot perform operation"))),
            @ApiResponse(responseCode = "403", description = "The caller holds `ROLE_READONLY`, or named somebody other than themselves in `ackUser` without holding `ROLE_ADMIN` or `ROLE_DELEGATE`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator', is not allowed to perform updates to alarms as user 'admin'")))
    })
    @Override
    public Response closeTicket(@Context final SecurityContext securityContext,
                                @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                        description = "Database id of the situation alarm.", example = "4552")
                                @PathParam("id") final Integer alarmId) throws Exception {
        return super.closeTicket(securityContext, alarmId);
    }

    private void clearAlarm(OnmsAlarm alarm, String user) {
        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm, user);
        performAction(ack, Action.CLEAR, Boolean.TRUE);
    }

    private void clearAlarms(List<Integer> alarmIds, String user) {
        for (Integer alarmId : alarmIds) {
            OnmsAlarm alarm = getDao().get(alarmId);
            if (alarm != null) {
                clearAlarm(alarm, user);
            }
        }
    }

    private Response removeAlarmsFromSituation(OnmsAlarm situationAlarm, List<Integer> alarmIdsToRemove) {
        Set<OnmsAlarm> remaining = situationAlarm.getRelatedAlarms().stream()
                .filter(a -> !alarmIdsToRemove.contains(a.getId()))
                .collect(Collectors.toUnmodifiableSet());

        if (situationAlarm.getRelatedAlarms().equals(remaining)) {
            return Response.noContent().build();
        }

        String situationId = getSituationParamFromAlarm(situationAlarm, ID)
                .orElseGet(() -> {
                    LOG.warn("Could not find situationId on alarm {}. Using ID as fallback.", situationAlarm.getId());
                    return Integer.toString(situationAlarm.getId());
                });
        buildAndSendEvent(remaining, situationAlarm, situationId, SituationsRestService.REMOVED_ALARM, null, null);
        return Response.ok().build();
    }

    public void performAction(OnmsAcknowledgment ack, Action action, Boolean value) {
        boolean alarmUpdated = false;
        switch (action) {
            case ACK:
                ack.setAckAction(value
                        ? AckAction.ACKNOWLEDGE
                        : AckAction.UNACKNOWLEDGE);
                alarmUpdated = true;
                break;
            case ESCALATE:
                if (Boolean.TRUE.equals(value)) {
                    ack.setAckAction(AckAction.ESCALATE);
                    alarmUpdated = true;
                }
                break;
            case CLEAR:
                if (Boolean.TRUE.equals(value)) {
                    ack.setAckAction(AckAction.CLEAR);
                    alarmUpdated = true;
                }
                break;
            case ACCEPT:
                break;
        }

        if (alarmUpdated) {
            m_ackDao.processAck(ack);
            m_ackDao.flush();
        }

    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        CriteriaBuilder builder = super.getCriteriaBuilder(uriInfo);
        @SuppressWarnings("unchecked")
        CriteriaBehavior<String> isSituationBehavior =
                (CriteriaBehavior<String>) getCriteriaBehaviors()
                        .get(Aliases.alarm.prop(IS_SITUATION));
        isSituationBehavior.beforeVisit(
                builder,
                "true",
                ConditionType.EQUALS,
                false
        );
        return builder;
    }

    @Override
    protected Map<String, CriteriaBehavior<?>> getCriteriaBehaviors() {
        Map<String, CriteriaBehavior<?>> base = CriteriaBehaviors.ALARM_BEHAVIORS;
        Map<String, CriteriaBehavior<?>> prefixed =
                CriteriaBehaviors.withAliasPrefix(Aliases.alarm, base);
        CriteriaBehavior<?> b = prefixed.get(Aliases.alarm.prop(IS_SITUATION));
        return Collections.singletonMap(Aliases.alarm.prop(IS_SITUATION), b);
    }

    private Response handleAssociation(List<Integer> alarmIds, String diagText, String desctiption, UriInfo uriInfo, String sid) throws InterruptedException {
        Set<OnmsAlarm> alarms = loadValidAlarms(alarmIds, uriInfo);
        if (alarms.size() < 2) {
            return Response.noContent().build();
        }

        buildAndSendEvent(alarms, null, sid, CREATED, diagText, desctiption);
        return Response.ok().build();
    }

    private Set<OnmsAlarm> loadValidAlarms(List<Integer> ids, UriInfo uriInfo) throws InterruptedException {
        Set<OnmsAlarm> alarms = new HashSet<>();
        for (Integer id : ids) {
            OnmsAlarm alarm = getDao().load(id);
            if (alarm != null && alarmIsNotInAnotherSituation(alarm.getReductionKey(), uriInfo)) {
                alarms.add(alarm);
            }
        }
        return alarms;
    }

    private boolean alarmIsNotInAnotherSituation(String reductionKey, UriInfo uriInfo) throws InterruptedException {
        for (OnmsAlarm sit : fetchAllSituationAlarms(uriInfo)) {
            for (OnmsAlarm a : sit.getRelatedAlarms()) {
                if (reductionKey.equals(a.getReductionKey())) {
                    LOG.debug("Alarm {} already in another situation", reductionKey);
                    return false;
                }
            }
        }
        return true;
    }

    protected List<OnmsAlarm> fetchAllSituationAlarms(UriInfo uriInfo) {
        CriteriaBuilder builder = getCriteriaBuilder(uriInfo);
        return getDao().findMatching(builder.toCriteria());
    }

    public static OnmsAlarm getAlarmForDescription(final Collection<OnmsAlarm> alarms) {
        Objects.requireNonNull(alarms, "alarms can not be null");
        if (alarms.isEmpty()) {
            throw new IllegalArgumentException("alarms can not be empty");
        }

        return alarms
                .stream()
                .sorted(Comparator.comparing(OnmsAlarm::getSeverity).thenComparing(x -> x.getLastEventTime().getTime()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("alarms can not be empty"));
    }

    private void buildAndSendEvent(
            Collection<OnmsAlarm> alarms,
            OnmsAlarm situation,
            String sid,
            String status,
            String diagText,
            String desctiption) {

        EventBuilder eb = new EventBuilder()
                .setUei(EventConstants.SITUATION_EVENT_UEI)
                .setSource(SOURCE)
                .setSeverity(maxSeverityLabel(alarms))
                .setTime(new Date())
                .addParam(ID, sid);

        OnmsAlarm desc = null;
        if (!REJECTED.equals(status)) {
            if (CREATED.equals(status)) {
                desc = getAlarmForDescription(alarms);
            } else {
                desc = getAlarmForDescription(situation.getRelatedAlarms());
            }
        }

        String logMsg;
        String descr;
        switch (status) {
            case CREATED:
                logMsg = String.format("Created situation with %d alarms", alarms.size());
                descr = Objects.toString(desctiption, "");
                if (diagText != null) {
                    descr += "<p>Diagnostic: " + diagText + "</p>";
                }
                break;
            case ADDED_ALARM:
                logMsg = String.format("Added alarms to situation %s", sid);
                descr = Objects.toString(situation != null ? situation.getDescription() : null, "");
                break;

            case REMOVED_ALARM:
                logMsg = String.format("Removed alarms from situation %s", sid);
                descr = Objects.toString(situation != null ? situation.getDescription() : null, "");
                break;

            case ACCEPTED:
                logMsg = "Situation accepted";
                descr = Objects.toString(situation != null ? situation.getDescription() : null, "");
                break;
            case REJECTED:
                logMsg = "Situation rejected";
                descr = Objects.toString(situation != null ? situation.getDescription() : null, "");
                break;
            default:
                logMsg = situation.getLogMsg();
                descr = desctiption;
        }

        if (!logMsg.isBlank()) {
            eb.addParam(SITUATION_LOG_MSG, logMsg);
        }
        if (!descr.isBlank()) {
            eb.addParam(DESCR, descr);
        }

        if (desc != null && desc.getNodeId() != null) {
            eb.setNodeid(desc.getNodeId().longValue());
        }

        AtomicInteger idx = new AtomicInteger(0);
        for (String key : alarms.stream().map(OnmsAlarm::getReductionKey).toList()) {
            eb.addParam(RELATED_PREFIX + idx.incrementAndGet(), key);
        }
        eb.addParam(STATUS, status);
        eventForwarder.sendNow(eb.getEvent());
    }

    public String maxSeverityLabel(Collection<OnmsAlarm> alarmSet) {
        final OnmsSeverity maxSeverity = OnmsSeverity.get(
                alarmSet.stream()
                        .mapToInt(a -> a.getSeverity() != null ? a.getSeverity().getId() : OnmsSeverity.INDETERMINATE.getId())
                        .max()
                        .orElseGet(OnmsSeverity.INDETERMINATE::getId)
        );
        return maxSeverity.getLabel();
    }

    private Optional<String> getSituationParamFromAlarm(OnmsAlarm alarm, String name) {
        final OnmsEvent databaseEvent = alarm.getLastEvent();
        if (databaseEvent == null) {
            return Optional.empty();
        }
        final List<OnmsEventParameter> parms = databaseEvent.getEventParameters().stream().filter((x) -> x.getName().equals(name)).toList();
        if (parms == null) {
            return Optional.empty();
        }

        return parms.stream()
                .map(OnmsEventParameter::getValue)
                .findFirst();
    }
}