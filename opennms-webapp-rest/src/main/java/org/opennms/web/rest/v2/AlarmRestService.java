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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

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
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.web.rest.mapper.v2.AlarmMapper;
import org.opennms.web.rest.model.v2.AlarmCollectionDTO;
import org.opennms.web.rest.model.v2.AlarmDTO;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.IpLikeCriteriaBehavior;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.SecurityHelper;
import org.opennms.web.rest.support.StringCollection;
import org.opennms.web.rest.v2.model.AlarmMemoRequest;
import org.opennms.web.rest.v2.model.AlarmPropertyUpdateRequest;
import org.opennms.web.svclayer.TroubleTicketProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsAlarm} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Path("alarms")
@Transactional
@Tag(name = "Alarms", description = """
        Alarms API. An alarm is the deduplicated form of one or more events that share a reduction key.
        This API queries alarms, changes acknowledgement and severity, sets sticky and journal notes and
        drives trouble ticket actions. Alarms are raised by alarmd from events whose event configuration
        carries `alarm-data`, not created here.

        Timestamps are rendered differently per representation: JSON carries epoch milliseconds
        (`1787685470949`), XML carries an ISO-8601 string with offset
        (`2026-08-25T15:17:50.949-04:00`). The generated schema shows the XML form for both.

        Collection reads accept a CXF FIQL expression in `_s` together with `limit`, `offset`,
        `orderBy` and `order`. The default page size is 10 and the default sort is `lastEventTime`
        descending. Property names usable in `_s` and `orderBy` are listed by
        `GET /alarms/properties`; naming a property the entity does not have fails with 500 rather
        than 400.""")
public class AlarmRestService extends AbstractDaoRestServiceWithDTO<OnmsAlarm,AlarmDTO,SearchBean,Integer,Integer> {

    @Autowired
    private AlarmDao m_dao;

    @Autowired
    private AcknowledgmentDao m_ackDao;

    @Autowired
    private AlarmRepository m_repository;

    @Autowired
    private TroubleTicketProxy m_troubleTicketProxy;

    @Autowired
    private AlarmMapper m_alarmMapper;

    @Override
    protected AlarmDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsAlarm> getDaoClass() {
        return OnmsAlarm.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass(), Aliases.alarm.toString());

        builder.fetch("lastEvent", FetchType.EAGER);

        // 1st level JOINs
        builder.alias("lastEvent", "lastEvent", JoinType.LEFT_JOIN);
        builder.alias("distPoller", Aliases.distPoller.toString(), JoinType.LEFT_JOIN);
        builder.alias("node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("ipInterfaces"), Aliases.ipInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.ipInterface.prop("ipAddress"), Aliases.alarm.prop("ipAddr")), Restrictions.isNull(Aliases.ipInterface.prop("ipAddress"))));
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("snmpInterfaces"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.snmpInterface.prop("ifIndex"), Aliases.alarm.prop("ifIndex")), Restrictions.isNull(Aliases.snmpInterface.prop("ifIndex"))));

        builder.orderBy("lastEventTime").desc(); // order by last event time by default

        return builder;
    }

    @Override
    protected JaxbListWrapper<AlarmDTO> createListWrapper(Collection<AlarmDTO> list) {
        return new AlarmCollectionDTO(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.ALARM_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String, CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String, CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.ALARM_BEHAVIORS);
        // Allow iplike queries on ipAddr
        map.put("ipAddr", new IpLikeCriteriaBehavior("ipAddr"));

        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.alarm, CriteriaBehaviors.ALARM_BEHAVIORS));
        // Allow iplike queries on alarm.ipAddr
        map.put(Aliases.alarm.prop("ipAddr"), new IpLikeCriteriaBehavior("ipAddr"));

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.distPoller, CriteriaBehaviors.DIST_POLLER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix("lastEvent", CriteriaBehaviors.EVENT_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.SERVICE_TYPE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.eventParameter, CriteriaBehaviors.ALARM_LASTEVENT_PARAMETER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.ipInterface, CriteriaBehaviors.IP_INTERFACE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        return map;
    }

    @Override
    protected OnmsAlarm doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsAlarm alarm, MultivaluedMapImpl params) {
        final String ticketIdValue = params.getFirst("ticketId");
        final String ticketStateValue = params.getFirst("ticketState");

        boolean isAlarmUpdated = false;
        if (StringUtils.isNotBlank(ticketIdValue)) {
            isAlarmUpdated = true;
            alarm.setTTicketId(ticketIdValue);
        }
        if (EnumUtils.isValidEnum(TroubleTicketState.class, ticketStateValue)) {
            isAlarmUpdated = true;
            alarm.setTTicketState(TroubleTicketState.valueOf(ticketStateValue));
        }
        if (isAlarmUpdated) {
            getDao().saveOrUpdate(alarm);
        }

        final String ackValue = params.getFirst("ack");
        final String escalateValue = params.getFirst("escalate");
        final String clearValue = params.getFirst("clear");
        final String ackUserValue = params.getFirst("ackUser");

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
        }

        return Response.noContent().build();
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List alarms",
            description = """
        Return a page of alarms. `application/atom+xml` yields the same document as
        `application/xml`, not an Atom feed. `relatedAlarms` is present only on alarms that are
        situations.

        Example query: `_s=alarm.severity==MINOR&orderBy=lastEventTime&order=desc`.""",
            operationId = "getAlarms")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of alarms.",
                    headers = @Header(name = "Content-Range", description = "Range of rows returned and the total, as `items <from>-<to>/<total>`.",
                            schema = @Schema(type = "string", example = "items 0-1/16")),
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = AlarmCollectionDTO.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 1,
                      "offset": 0,
                      "alarm": [
                        {
                          "id": 4241,
                          "uei": "uei.opennms.org/perspective/nodes/nodeLostService",
                          "location": "Default",
                          "nodeId": 2,
                          "nodeLabel": "loopback-001",
                          "ipAddress": "127.0.0.1",
                          "serviceType": { "id": 3, "name": "SNMP" },
                          "reductionKey": "uei.opennms.org/perspective/nodes/nodeLostService:Default:2:127.0.0.1:SNMP",
                          "type": 1,
                          "count": 2,
                          "severity": "MINOR",
                          "firstEventTime": 1787684981802,
                          "lastEventTime": 1787685470949,
                          "description": "<p>A SNMP outage was identified on interface 127.0.0.1.</p>",
                          "logMessage": "SNMP outage identified on interface 127.0.0.1.",
                          "x733ProbableCause": 0,
                          "affectedNodeCount": 1
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = AlarmCollectionDTO.class),
                                    examples = @ExampleObject(value = """
                    <alarms count="1" offset="0" totalCount="2">
                      <alarm id="4241" type="1" count="2" severity="MINOR">
                        <uei>uei.opennms.org/perspective/nodes/nodeLostService</uei>
                        <location>Default</location>
                        <nodeId>2</nodeId>
                        <nodeLabel>loopback-001</nodeLabel>
                        <ipAddress>127.0.0.1</ipAddress>
                        <serviceType id="3"><name>SNMP</name></serviceType>
                        <reductionKey>uei.opennms.org/perspective/nodes/nodeLostService:Default:2:127.0.0.1:SNMP</reductionKey>
                        <firstEventTime>2026-08-25T15:09:41.802-04:00</firstEventTime>
                        <lastEventTime>2026-08-25T15:17:50.949-04:00</lastEventTime>
                        <x733ProbableCause>0</x733ProbableCause>
                        <affectedNodeCount>1</affectedNodeCount>
                      </alarm>
                    </alarms>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No alarm matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "`_s` or `orderBy` named a property the entity does not have.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "could not resolve property: bogusprop of: org.opennms.netmgt.model.OnmsAlarm")))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Count alarms",
            description = """
        Return the number of alarms matching `_s` as a plain-text integer. The response is
        `text/plain` only, so a request that asks solely for `application/json` is answered with 404.
        `limit` and `offset` are ignored here: the count covers the whole match.

        Example query: `_s=alarm.severity==MINOR`.""",
            operationId = "getAlarmCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching alarms.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "2"))),
            @ApiResponse(responseCode = "404", description = "The request did not accept `text/plain`. No body is returned."),
            @ApiResponse(responseCode = "500", description = "`_s` named a property the entity does not have.",
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
            summary = "List the properties alarms can be queried on",
            description = """
        Return the property names accepted by `_s` and `orderBy`, with their type and whether they
        support `iplike`. Properties whose value set is fixed carry a `values` map of code to label.
        `q` filters the list by a case-insensitive substring of the name; a `q` that matches nothing
        yields 200 with an empty list, not 204.""",
            operationId = "getAlarmProperties")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The matching query properties.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SearchPropertyCollection.class),
                            examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "searchProperty": [
                        {
                          "id": "severity",
                          "name": "Severity",
                          "type": "INTEGER",
                          "orderBy": true,
                          "iplike": false,
                          "values": { "1": "Indeterminate", "2": "Cleared", "3": "Normal" }
                        },
                        {
                          "id": "alarm.severity",
                          "name": "Alarm: Severity",
                          "type": "INTEGER",
                          "orderBy": true,
                          "iplike": false
                        }
                      ]
                    }""")))
    })
    @Override
    public Response getProperties(@Parameter(in = ParameterIn.QUERY, name = "q",
            description = "Case-insensitive substring of the property name.", example = "severity")
                                  @QueryParam("q") final String query) {
        return super.getProperties(query);
    }

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "List the values a query property takes",
            description = """
        Return the distinct values of one query property, either from its fixed value set or from a
        `select distinct` over the alarm table. The element type follows the property type: strings for
        `STRING` and `IP_ADDRESS`, numbers for `INTEGER`, `LONG` and `FLOAT`, and epoch milliseconds in
        JSON for `TIMESTAMP`. `propertyId` is the unprefixed id from `GET /alarms/properties`, so
        `severity` rather than `alarm.severity`.""",
            operationId = "getAlarmPropertyValues")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The distinct values of the property.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = StringCollection.class),
                            examples = {
                                    @ExampleObject(name = "string property", value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "value": [
                        "uei.opennms.org/nodes/dataCollectionFailed",
                        "uei.opennms.org/perspective/nodes/nodeLostService"
                      ]
                    }"""),
                                    @ExampleObject(name = "timestamp property", value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "value": [1787685468093, 1787685470949]
                    }""")
                            })),
            @ApiResponse(responseCode = "204", description = "The property has a type with no value listing, such as BOOLEAN. No body is returned."),
            @ApiResponse(responseCode = "404", description = "No query property has that id. No body is returned.")
    })
    @Override
    public Response getPropertyValues(
            @Parameter(in = ParameterIn.PATH, name = "propertyId", required = true,
                    description = "Unprefixed property id from `GET /alarms/properties`.", example = "severity")
            @PathParam("propertyId") final String propertyId,
            @Parameter(in = ParameterIn.QUERY, name = "q",
                    description = "Case-sensitive substring the value must contain.", example = "nodeLost")
            @QueryParam("q") final String query,
            @Parameter(in = ParameterIn.QUERY, name = "limit",
                    description = "Maximum number of values returned. Applies only to values read from the database.",
                    example = "25")
            @QueryParam("limit") final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one alarm",
            description = """
        Return a single alarm by its database id. `relatedAlarms` is present only when the alarm is a
        situation.

        A situation is itself an alarm, so `/situations/{id}` resolves the same way and this operation
        also serves that path. Neither path restricts the lookup, so a situation id works here and an
        ordinary alarm id works there.""",
            operationId = "getAlarmById")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The alarm.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = AlarmDTO.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": 4241,
                      "uei": "uei.opennms.org/perspective/nodes/nodeLostService",
                      "location": "Default",
                      "nodeId": 2,
                      "nodeLabel": "loopback-001",
                      "ipAddress": "127.0.0.1",
                      "serviceType": { "id": 3, "name": "SNMP" },
                      "reductionKey": "uei.opennms.org/perspective/nodes/nodeLostService:Default:2:127.0.0.1:SNMP",
                      "type": 1,
                      "count": 2,
                      "severity": "MINOR",
                      "firstEventTime": 1787684981802,
                      "lastEventTime": 1787685470949,
                      "x733ProbableCause": 0,
                      "affectedNodeCount": 1
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = AlarmDTO.class),
                                    examples = @ExampleObject(value = """
                    <alarm id="4241" type="1" count="2" severity="MINOR">
                      <uei>uei.opennms.org/perspective/nodes/nodeLostService</uei>
                      <reductionKey>uei.opennms.org/perspective/nodes/nodeLostService:Default:2:127.0.0.1:SNMP</reductionKey>
                      <lastEventTime>2026-08-25T15:17:50.949-04:00</lastEventTime>
                    </alarm>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No alarm has that id. No body is returned.")
    })
    @Override
    public Response get(@Context final UriInfo uriInfo,
                        @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                description = "Alarm database id.", example = "4241")
                        @PathParam("id") final Integer id) {
        return super.get(uriInfo, id);
    }

    @POST
    @Path("{id}")
    @Operation(
            summary = "Rejected: alarms cannot be created at a chosen id",
            description = "Always answers 404.",
            operationId = "createAlarmWithId",
            parameters = @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                    description = "Alarm database id. The value is not read: every request to this path is answered with 404.",
                    example = "4241"))
    @ApiResponses(@ApiResponse(responseCode = "404", description = "Always. No body is returned."))
    @Override
    public Response createSpecific() {
        return super.createSpecific();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Not implemented: create an alarm",
            description = """
        Creating an alarm from a document is not implemented and answers 501. This operation also
        serves `POST /situations`, which behaves identically.

        The body is still deserialised and mapped before the 501 is produced, so a body that omits
        `relatedAlarms` fails earlier with 500: the entity setter dereferences the missing collection.
        Sending `"relatedAlarms": []` reaches the 501.""",
            operationId = "createAlarm")
    @RequestBody(description = "Alarm document. `relatedAlarms` has to be present; without it the call answers 500.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AlarmDTO.class),
                            examples = @ExampleObject(value = "{ \"relatedAlarms\": [] }")),
                    @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = AlarmDTO.class),
                            examples = @ExampleObject(value = "<alarm><relatedAlarms/></alarm>"))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "The body omitted `relatedAlarms`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"java.util.Set.forEach(java.util.function.Consumer)\" because \"alarms\" is null"))),
            @ApiResponse(responseCode = "501", description = "Create is not implemented. No body is returned.")
    })
    @Override
    public Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final AlarmDTO object) {
        return super.create(securityContext, uriInfo, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update properties of several alarms",
            description = """
        Apply the form parameters to every alarm matching `_s`. The default `limit` of 10 applies to
        the selection, so a call without an explicit `limit` touches at most 10 alarms. Acknowledgement
        changes go through the acknowledgement DAO, which writes an `acks` row per alarm and updates
        the alarm in place.

        The whole batch runs in one transaction and stops at the first alarm whose update fails, so a
        4xx or 5xx leaves earlier alarms already changed.

        Example query: `_s=alarm.id==4241`.""",
            operationId = "updateAlarms")
    @RequestBody(required = true, description = "Alarm properties to apply, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmPropertyUpdateRequest.class),
                    examples = @ExampleObject(value = "ack=true")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Every selected alarm was updated. No body is returned."),
            @ApiResponse(responseCode = "404", description = "No alarm matched `_s`. No body is returned."),
            @ApiResponse(responseCode = "500", description = "`_s` named a property the entity does not have.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "could not resolve property: bogusprop of: org.opennms.netmgt.model.OnmsAlarm")))
    })
    @Override
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                               @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(
            summary = "Not implemented: replace an alarm",
            description = """
        Replacing an alarm from a document is not implemented and answers 501. The form-encoded
        variant of `PUT /alarms/{id}` changes acknowledgement, severity and ticket fields.""",
            operationId = "replaceAlarm")
    @RequestBody(description = "Alarm document. Not applied.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = AlarmDTO.class),
                            examples = @ExampleObject(value = "{}")),
                    @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = AlarmDTO.class),
                            examples = @ExampleObject(value = "<alarm/>"))
            })
    @ApiResponses(@ApiResponse(responseCode = "501", description = "Replace is not implemented. No body is returned."))
    @Override
    public Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                           @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                   description = "Alarm database id.", example = "4241")
                           @PathParam("id") final Integer id, final OnmsAlarm object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(
            summary = "Acknowledge, clear, escalate or ticket one alarm",
            description = """
        Apply the form parameters to one alarm. Only the parameters present are acted on: they cover
        acknowledging, un-acknowledging, escalating and clearing an alarm, and recording a trouble
        ticket id and state.

        A `ticketState` outside the enumerated names is ignored, and a caller with ROLE_ADMIN may set
        `ackUser` to any name, so neither case is reported as an error.

        This operation also serves `PUT /situations/{id}`, where it acts on the situation alarm alone:
        clearing or acknowledging a situation leaves its member alarms untouched.
        `POST /situations/alarms/clear` clears members. A body sent as JSON or XML rather than
        form-encoded reaches a different handler that answers 501.""",
            operationId = "updateAlarmProperties")
    @RequestBody(required = true, description = "Alarm properties to apply, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmPropertyUpdateRequest.class),
                    examples = {
                            @ExampleObject(name = "acknowledge", value = "ack=true"),
                            @ExampleObject(name = "clear", value = "clear=true"),
                            @ExampleObject(name = "record a ticket", value = "ticketId=INC0012345&ticketState=OPEN")
                    }))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The alarm was updated. No body is returned."),
            @ApiResponse(responseCode = "403", description = "The caller may not act on behalf of the `ackUser` named.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User 'operator' cannot act on behalf of user 'admin'"))),
            @ApiResponse(responseCode = "404", description = "No alarm has that id. No body is returned.")
    })
    @Override
    public Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                                     @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                             description = "Alarm database id.", example = "4241")
                                     @PathParam("id") final Integer id, final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @DELETE
    @Operation(
            summary = "Not implemented: delete several alarms",
            description = """
        Deleting alarms is not implemented. When `_s` selects at least one alarm the handler answers
        501 without deleting anything; when nothing matches it answers 404 instead, so the status code
        reports whether the filter matched rather than whether anything was deleted.

        Example query: `_s=alarm.id==4241`.""",
            operationId = "deleteAlarms")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No alarm matched `_s`. No body is returned."),
            @ApiResponse(responseCode = "501", description = "Delete is not implemented. No body is returned.")
    })
    @Override
    public Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                               @Context final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Not implemented: delete one alarm",
            description = """
        Deleting an alarm is not implemented. An existing alarm answers 501 and an unknown id answers
        404, so the status code reports whether the alarm exists rather than whether it was deleted.

        This operation also serves `DELETE /situations/{id}`.""",
            operationId = "deleteAlarm")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "No alarm has that id. No body is returned."),
            @ApiResponse(responseCode = "501", description = "Delete is not implemented. No body is returned.")
    })
    @Override
    public Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                           @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                   description = "Alarm database id.", example = "4241")
                           @PathParam("id") final Integer id) {
        return super.delete(securityContext, uriInfo, id);
    }

    @PUT
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Set the sticky memo on an alarm",
            description = """
        Replace the sticky memo, the note attached to the alarm row itself. `body` is required. When
        `user` is omitted the authenticated user is recorded as the author.""",
            operationId = "updateAlarmMemo")
    @RequestBody(required = true, description = "Memo text and optional author, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmMemoRequest.class),
                    examples = @ExampleObject(value = "body=Waiting+on+the+carrier.&user=admin")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The memo was written. No body is returned."),
            @ApiResponse(responseCode = "400", description = "`body` was absent.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Body cannot be null."))),
            @ApiResponse(responseCode = "403", description = "The caller may not act on behalf of the `user` named.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User operator cannot act on behalf of user admin"))),
            @ApiResponse(responseCode = "404", description = "No alarm has that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Alarm not found.")))
    })
    public Response updateMemo(@Context final SecurityContext securityContext,
                               @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                       description = "Alarm database id.", example = "4241")
                               @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
        SecurityHelper.assertUserEditCredentials(securityContext, user);
        final String body = params.getFirst("body");
        if (body == null) throw getException(Status.BAD_REQUEST, "Body cannot be null.");
        if (m_repository.getAlarm(alarmId) == null) throw getException(Status.NOT_FOUND, "Alarm not found.");
        m_repository.updateStickyMemo(alarmId, body, user);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Set the journal note on an alarm",
            description = """
        Replace the journal note, which is keyed on the reduction key rather than on the alarm row, so a
        later alarm reduced onto the same key carries the same note. `body` is required. When `user` is
        omitted the authenticated user is recorded as the author.""",
            operationId = "updateAlarmJournal")
    @RequestBody(required = true, description = "Memo text and optional author, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = AlarmMemoRequest.class),
                    examples = @ExampleObject(value = "body=Carrier+ticket+INC0012345+raised.&user=admin")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The journal note was written. No body is returned."),
            @ApiResponse(responseCode = "400", description = "`body` was absent.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Body cannot be null."))),
            @ApiResponse(responseCode = "403", description = "The caller may not act on behalf of the `user` named.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User operator cannot act on behalf of user admin"))),
            @ApiResponse(responseCode = "404", description = "No alarm has that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Alarm not found.")))
    })
    public Response updateJournal(@Context final SecurityContext securityContext,
                                  @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                          description = "Alarm database id.", example = "4241")
                                  @PathParam("id") final Integer alarmId, final MultivaluedMapImpl params) {
        final String user = params.containsKey("user") ? params.getFirst("user") : securityContext.getUserPrincipal().getName();
        SecurityHelper.assertUserEditCredentials(securityContext, user);
        final String body = params.getFirst("body");
        if (body == null) throw getException(Status.BAD_REQUEST, "Body cannot be null.");
        if (m_repository.getAlarm(alarmId) == null) throw getException(Status.NOT_FOUND, "Alarm not found.");
        m_repository.updateReductionKeyMemo(alarmId, body, user);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/memo")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Remove the sticky memo from an alarm",
            description = """
        Remove the note attached to the alarm row. Removing one that is not set still answers 204. No
        request body is read, although the handler declares `application/x-www-form-urlencoded`.""",
            operationId = "deleteAlarmMemo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The sticky memo is gone, whether or not one was set. No body is returned."),
            @ApiResponse(responseCode = "403", description = "The authenticated user may not edit alarms.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User operator cannot act on behalf of user admin"))),
            @ApiResponse(responseCode = "404", description = "No alarm has that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Alarm not found.")))
    })
    public Response removeMemo(@Context final SecurityContext securityContext,
                               @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                       description = "Alarm database id.", example = "4241")
                               @PathParam("id") final Integer alarmId) {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());
        if (m_repository.getAlarm(alarmId) == null) throw getException(Status.NOT_FOUND, "Alarm not found.");
        m_repository.removeStickyMemo(alarmId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}/journal")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Remove the journal note from an alarm",
            description = """
        Remove the note keyed on the reduction key. Removing one that is not set still answers 204. No
        request body is read, although the handler declares `application/x-www-form-urlencoded`.""",
            operationId = "deleteAlarmJournal")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The journal note is gone, whether or not one was set. No body is returned."),
            @ApiResponse(responseCode = "403", description = "The authenticated user may not edit alarms.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User operator cannot act on behalf of user admin"))),
            @ApiResponse(responseCode = "404", description = "No alarm has that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Alarm not found.")))
    })
    public Response removeJournal(@Context final SecurityContext securityContext,
                                  @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                          description = "Alarm database id.", example = "4241")
                                  @PathParam("id") final Integer alarmId) {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());
        if (m_repository.getAlarm(alarmId) == null) throw getException(Status.NOT_FOUND, "Alarm not found.");
        m_repository.removeReductionKeyMemo(alarmId);
        return Response.noContent().build();
    }

    @POST
    @Path("{id}/ticket/create")
    @Operation(
            summary = "Create the trouble ticket for an alarm",
            description = """
        Ask the configured ticketer plugin to raise a ticket for the alarm. The authenticated user is
        passed to the plugin as the `user` parameter.

        The action is asynchronous: the handler hands the request to the ticketer plugin and answers
        202 without waiting for the helpdesk system. Progress shows up in the `troubleTicket` and
        `troubleTicketState` fields of the alarm.

        The ticket operations are gated on `opennms.alarmTroubleTicketEnabled` being `true`. With the
        ticketer disabled every call answers 501, including a call naming an alarm that does not
        exist, because the gate is checked before the alarm is looked up.""",
            operationId = "createAlarmTicket")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "The request was handed to the ticketer plugin. No body is returned."),
            @ApiResponse(responseCode = "403", description = "The authenticated user may not edit alarms.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User operator cannot act on behalf of user admin"))),
            @ApiResponse(responseCode = "501", description = "The ticketer plugin is disabled.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "AlarmTroubleTicketer is not enabled. Cannot perform operation")))
    })
    public Response createTicket(@Context final SecurityContext securityContext,
                                 @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                         description = "Alarm database id.", example = "4241")
                                 @PathParam("id") final Integer alarmId) throws Exception {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());

        return runIfTicketerPluginIsEnabled(() -> {
            final Map<String, String> parameters = new HashMap<>();
            parameters.put(EventConstants.PARM_USER, securityContext.getUserPrincipal().getName());
            m_troubleTicketProxy.createTicket(alarmId, parameters);
            return Response.status(Status.ACCEPTED).build();
        });
    }

    @POST
    @Path("{id}/ticket/update")
    @Operation(
            summary = "Refresh the trouble ticket for an alarm",
            description = """
        Ask the configured ticketer plugin to re-read the ticket from the helpdesk system and write the
        current state back onto the alarm.

        The action is asynchronous: the handler hands the request to the ticketer plugin and answers
        202 without waiting for the helpdesk system. Progress shows up in the `troubleTicket` and
        `troubleTicketState` fields of the alarm.

        The ticket operations are gated on `opennms.alarmTroubleTicketEnabled` being `true`. With the
        ticketer disabled every call answers 501, including a call naming an alarm that does not
        exist, because the gate is checked before the alarm is looked up.""",
            operationId = "updateAlarmTicket")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "The request was handed to the ticketer plugin. No body is returned."),
            @ApiResponse(responseCode = "403", description = "The authenticated user may not edit alarms.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User operator cannot act on behalf of user admin"))),
            @ApiResponse(responseCode = "501", description = "The ticketer plugin is disabled.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "AlarmTroubleTicketer is not enabled. Cannot perform operation")))
    })
    public Response updateTicket(@Context final SecurityContext securityContext,
                                 @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                         description = "Alarm database id.", example = "4241")
                                 @PathParam("id") final Integer alarmId) throws Exception {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());

        return runIfTicketerPluginIsEnabled(() -> {
            m_troubleTicketProxy.updateTicket(alarmId);
            return Response.status(Status.ACCEPTED).build();
        });
    }

    @POST
    @Path("{id}/ticket/close")
    @Operation(
            summary = "Close the trouble ticket for an alarm",
            description = """
        Ask the configured ticketer plugin to close the ticket recorded on the alarm.

        The action is asynchronous: the handler hands the request to the ticketer plugin and answers
        202 without waiting for the helpdesk system. Progress shows up in the `troubleTicket` and
        `troubleTicketState` fields of the alarm.

        The ticket operations are gated on `opennms.alarmTroubleTicketEnabled` being `true`. With the
        ticketer disabled every call answers 501, including a call naming an alarm that does not
        exist, because the gate is checked before the alarm is looked up.""",
            operationId = "closeAlarmTicket")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "The request was handed to the ticketer plugin. No body is returned."),
            @ApiResponse(responseCode = "403", description = "The authenticated user may not edit alarms.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "User operator cannot act on behalf of user admin"))),
            @ApiResponse(responseCode = "501", description = "The ticketer plugin is disabled.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "AlarmTroubleTicketer is not enabled. Cannot perform operation")))
    })
    public Response closeTicket(@Context final SecurityContext securityContext,
                                @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                        description = "Alarm database id.", example = "4241")
                                @PathParam("id") final Integer alarmId) throws Exception {
        SecurityHelper.assertUserEditCredentials(securityContext, securityContext.getUserPrincipal().getName());

        return runIfTicketerPluginIsEnabled(() -> {
            m_troubleTicketProxy.closeTicket(alarmId);
            return Response.status(Status.ACCEPTED).build();
        });
    }

    private Response runIfTicketerPluginIsEnabled(Callable<Response> callable) throws Exception {
        if (!isTicketerPluginEnabled()) {
            return Response.status(Status.NOT_IMPLEMENTED).entity("AlarmTroubleTicketer is not enabled. Cannot perform operation").build();
        }
        Objects.requireNonNull(callable);
        final Response response = callable.call();
        return response;
    }

    private boolean isTicketerPluginEnabled() {
        return "true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled"));
    }

    @Override
    public AlarmDTO mapEntityToDTO(OnmsAlarm alarm) {
        return m_alarmMapper.alarmToAlarmDTO(alarm);
    }

    @Override
    public OnmsAlarm mapDTOToEntity(AlarmDTO dto) {
        return m_alarmMapper.alarmDTOToAlarm(dto);
    }
}
