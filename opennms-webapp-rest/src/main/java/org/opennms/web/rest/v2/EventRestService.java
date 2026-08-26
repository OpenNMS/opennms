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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.mapper.v2.EventMapper;
import org.opennms.web.rest.model.v2.EventCollectionDTO;
import org.opennms.web.rest.model.v2.EventDTO;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.IpLikeCriteriaBehavior;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v2.api.EventRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsEvent} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class EventRestService extends AbstractDaoRestServiceWithDTO<OnmsEvent,EventDTO,SearchBean,Long,Long> implements EventRestApi {

    @Autowired
    private EventDao m_dao;

    @Autowired
    private EventMapper m_eventMapper;

    @Override
    protected EventDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsEvent> getDaoClass() {
        return OnmsEvent.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass(), Aliases.event.toString());

        // 1st level JOINs
        builder.alias("alarm", Aliases.alarm.toString(), JoinType.LEFT_JOIN);
        builder.alias("distPoller", Aliases.distPoller.toString(), JoinType.LEFT_JOIN);
        builder.alias("node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering by category so that we can specify a join condition
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("ipInterfaces"), Aliases.ipInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.ipInterface.prop("ipAddress"), Aliases.event.prop("ipAddr")), Restrictions.isNull(Aliases.ipInterface.prop("ipAddress"))));
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("snmpInterfaces"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.snmpInterface.prop("ifIndex"), Aliases.event.prop("ifIndex")), Restrictions.isNull(Aliases.snmpInterface.prop("ifIndex"))));

        builder.orderBy("eventTime").desc(); // order by event time by default

        return builder;
    }

    @Override
    protected JaxbListWrapper<EventDTO> createListWrapper(Collection<EventDTO> list) {
        return new EventCollectionDTO(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.EVENT_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String, String> getSearchBeanPropertyMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("event.uei", "event.eventUei");
        return map;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.EVENT_BEHAVIORS);
        // Allow iplike queries on ipAddr
        map.put("ipAddr", new IpLikeCriteriaBehavior("ipAddr"));

        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.event, CriteriaBehaviors.EVENT_BEHAVIORS));
        // Allow iplike queries on event.ipAddr
        map.put(Aliases.event.prop("ipAddr"), new IpLikeCriteriaBehavior("ipAddr"));

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.alarm, CriteriaBehaviors.ALARM_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.distPoller, CriteriaBehaviors.DIST_POLLER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.eventParameter, CriteriaBehaviors.EVENT_PARAMETER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.SERVICE_TYPE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.ipInterface, CriteriaBehaviors.IP_INTERFACE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        return map;
    }

    @Override
    protected OnmsEvent doGet(UriInfo uriInfo, Long id) {
        return getDao().get(id);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List events",
            description = """
        Return a page of events. `application/atom+xml` yields the same document as
        `application/xml`, not an Atom feed. The event table is the largest in an OpenNMS database, so
        a call without `_s` and without `limit` still costs a full count over it.

        Example query: `_s=event.eventUei==uei.opennms.org/nodes/nodeDown&orderBy=eventTime&order=desc`.""",
            operationId = "getEvents")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "A page of events.",
                    headers = @Header(name = "Content-Range", description = "Range of rows returned and the total, as `items <from>-<to>/<total>`.",
                            schema = @Schema(type = "string", example = "items 0-0/55178")),
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = EventCollectionDTO.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 55178,
                      "count": 1,
                      "offset": 0,
                      "event": [
                        {
                          "id": 55168,
                          "uei": "uei.opennms.org/perspective/nodes/nodeLostService",
                          "label": "OpenNMS-defined perspective poller event: A perspective poller detected a node lost service",
                          "time": 1787685470949,
                          "createTime": 1787685470954,
                          "source": "PerspectivePoller",
                          "ipAddress": "127.0.0.1",
                          "serviceType": { "id": 3, "name": "SNMP" },
                          "severity": "MINOR",
                          "log": "Y",
                          "display": "Y",
                          "nodeId": 2,
                          "nodeLabel": "loopback-001",
                          "location": "Default",
                          "parameters": [
                            { "name": "eventReason", "value": "SNMP poll failed", "type": "string" }
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = EventCollectionDTO.class),
                                    examples = @ExampleObject(value = """
                    <events count="1" offset="0" totalCount="55178">
                      <event id="55168" severity="MINOR" log="Y" display="Y">
                        <uei>uei.opennms.org/perspective/nodes/nodeLostService</uei>
                        <time>2026-08-25T15:17:50.949-04:00</time>
                        <source>PerspectivePoller</source>
                        <ipAddress>127.0.0.1</ipAddress>
                        <serviceType id="3"><name>SNMP</name></serviceType>
                        <parameters>
                          <parameter name="eventReason" value="SNMP poll failed" type="string"/>
                        </parameters>
                        <nodeId>2</nodeId>
                        <location>Default</location>
                      </event>
                    </events>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No event matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "`_s` or `orderBy` named a property the entity does not have.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "could not resolve property: bogus of: org.opennms.netmgt.model.OnmsEvent")))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one event",
            description = "Return a single event by its database id.",
            operationId = "getEventById")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The event.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = EventDTO.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": 55168,
                      "uei": "uei.opennms.org/perspective/nodes/nodeLostService",
                      "label": "OpenNMS-defined perspective poller event: A perspective poller detected a node lost service",
                      "time": 1787685470949,
                      "createTime": 1787685470954,
                      "source": "PerspectivePoller",
                      "severity": "MINOR",
                      "log": "Y",
                      "display": "Y",
                      "nodeId": 2,
                      "location": "Default"
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = EventDTO.class),
                                    examples = @ExampleObject(value = """
                    <event id="55168" severity="MINOR" log="Y" display="Y">
                      <uei>uei.opennms.org/perspective/nodes/nodeLostService</uei>
                      <time>2026-08-25T15:17:50.949-04:00</time>
                      <source>PerspectivePoller</source>
                      <nodeId>2</nodeId>
                      <location>Default</location>
                    </event>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No event has that id. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The path segment is not a number.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "object is not an instance of declaring class")))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo,
                        @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                description = "Event database id.", example = "55168")
                        @PathParam("id") final Long id) {
        return super.get(uriInfo, id);
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Count events",
            description = """
        Return the number of events matching `_s` as a plain-text integer. The response is
        `text/plain` only, so a request that asks solely for `application/json` is answered with 404.
        `limit` and `offset` are ignored: the count covers the whole match.

        Example query: `_s=event.eventUei==uei.opennms.org/nodes/nodeDown`.""",
            operationId = "getEventCount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The number of matching events.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "55179"))),
            @ApiResponse(responseCode = "404", description = "The request did not accept `text/plain`. No body is returned."),
            @ApiResponse(responseCode = "500", description = "`_s` named a property the entity does not have.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "could not resolve property: bogus of: org.opennms.netmgt.model.OnmsEvent")))
    })
    @Override
    public Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "List the properties events can be queried on",
            description = """
        Return the property names accepted by `_s` and `orderBy`, with their type and whether they
        support `iplike`. The UEI is exposed as `eventUei`, not `uei`. `q` filters the list by a
        case-insensitive substring of the name; a `q` that matches nothing yields 200 with an empty
        list, not 204.""",
            operationId = "getEventProperties")
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
                        { "id": "eventUei", "name": "UEI", "type": "STRING", "orderBy": true, "iplike": false },
                        { "id": "alarm.uei", "name": "Alarm: UEI", "type": "STRING", "orderBy": true, "iplike": false }
                      ]
                    }""")))
    })
    @Override
    public Response getProperties(@Parameter(in = ParameterIn.QUERY, name = "q",
            description = "Case-insensitive substring of the property name.", example = "uei")
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
        `select distinct` over the event table. The element type follows the property type: strings for
        `STRING` and `IP_ADDRESS`, numbers for `INTEGER`, `LONG` and `FLOAT`, and epoch milliseconds in
        JSON for `TIMESTAMP`. `propertyId` is the unprefixed id from `GET /events/properties`, so
        `eventUei` rather than `event.eventUei`. Without `limit` this reads every distinct value in the
        event table.""",
            operationId = "getEventPropertyValues")
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
                        "uei.opennms.org/internal/authentication/failure",
                        "uei.opennms.org/internal/capsd/deleteNode"
                      ]
                    }"""),
                                    @ExampleObject(name = "timestamp property", value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "value": [1786133772883, 1786133772913]
                    }""")
                            })),
            @ApiResponse(responseCode = "204", description = "The property has a type with no value listing. No body is returned."),
            @ApiResponse(responseCode = "404", description = "No query property has that id. No body is returned.")
    })
    @Override
    public Response getPropertyValues(
            @Parameter(in = ParameterIn.PATH, name = "propertyId", required = true,
                    description = "Unprefixed property id from `GET /events/properties`.", example = "eventUei")
            @PathParam("propertyId") final String propertyId,
            @Parameter(in = ParameterIn.QUERY, name = "q",
                    description = "Case-sensitive substring the value must contain.", example = "nodeDown")
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
            summary = "Broken: create an event at a chosen id",
            description = """
        Inherited from the generic DAO resource and not reachable in practice. This implementation is
        proxied on its interface, so a method the interface does not declare cannot be invoked on the
        proxy and the call fails with 500 before any of the handler runs.

        Publish events with `POST /events` instead.""",
            operationId = "createEventWithId",
            parameters = @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                    description = "Event database id. The value is not read: the call fails before the handler runs.",
                    example = "1"))
    @ApiResponses(@ApiResponse(responseCode = "500", description = "Always.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "object is not an instance of declaring class"))))
    @Override
    public Response createSpecific() {
        return super.createSpecific();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Broken: update properties of several events",
            description = """
        Inherited from the generic DAO resource and not reachable in practice. This implementation is
        proxied on its interface, so a method the interface does not declare cannot be invoked on the
        proxy and the call fails with 500 before any of the handler runs.

        Events are an immutable record; there is no supported way to change one.""",
            operationId = "updateEvents")
    @RequestBody(description = "Event properties, form-encoded. Never read.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "eventSeverity=5")))
    @ApiResponses(@ApiResponse(responseCode = "500", description = "Always.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "object is not an instance of declaring class"))))
    @Override
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                               @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(
            summary = "Broken: replace an event",
            description = """
        Inherited from the generic DAO resource and not reachable in practice. This implementation is
        proxied on its interface, so a method the interface does not declare cannot be invoked on the
        proxy and the call fails with 500 before any of the handler runs.

        Events are an immutable record; there is no supported way to change one.""",
            operationId = "replaceEvent")
    @RequestBody(description = "Event document. Never read.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EventDTO.class),
                    examples = @ExampleObject(value = "{}")))
    @ApiResponses(@ApiResponse(responseCode = "500", description = "Always.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "object is not an instance of declaring class"))))
    @Override
    public Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                           @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                   description = "Event database id.", example = "55168")
                           @PathParam("id") final Long id, final OnmsEvent object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(
            summary = "Broken: update properties of an event",
            description = """
        Inherited from the generic DAO resource and not reachable in practice. This implementation is
        proxied on its interface, so a method the interface does not declare cannot be invoked on the
        proxy and the call fails with 500 before any of the handler runs.

        Events are an immutable record; there is no supported way to change one.""",
            operationId = "updateEventProperties")
    @RequestBody(description = "Event properties, form-encoded. Never read.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "eventSeverity=5")))
    @ApiResponses(@ApiResponse(responseCode = "500", description = "Always.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "object is not an instance of declaring class"))))
    @Override
    public Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                                     @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                             description = "Event database id.", example = "55168")
                                     @PathParam("id") final Long id, final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @DELETE
    @Operation(
            summary = "Broken: delete several events",
            description = """
        Inherited from the generic DAO resource and not reachable in practice. This implementation is
        proxied on its interface, so a method the interface does not declare cannot be invoked on the
        proxy and the call fails with 500 before any of the handler runs.

        Event retention is handled by vacuumd, not through this API.""",
            operationId = "deleteEvents")
    @ApiResponses(@ApiResponse(responseCode = "500", description = "Always.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "object is not an instance of declaring class"))))
    @Override
    public Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                               @Context final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Broken: delete one event",
            description = """
        Inherited from the generic DAO resource and not reachable in practice. This implementation is
        proxied on its interface, so a method the interface does not declare cannot be invoked on the
        proxy and the call fails with 500 before any of the handler runs.

        Event retention is handled by vacuumd, not through this API.""",
            operationId = "deleteEvent")
    @ApiResponses(@ApiResponse(responseCode = "500", description = "Always.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "object is not an instance of declaring class"))))
    @Override
    public Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo,
                           @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                                   description = "Event database id.", example = "55168")
                           @PathParam("id") final Long id) {
        return super.delete(securityContext, uriInfo, id);
    }

    /**
     * NOTE: This method defines an unused parameter of 0 length in the @Path annotation
     * in order to get CXF to prioritize this method definition instead of the create method
     * defined in the parent class.
     *
     * We cannot simply override the parent method, since the class types are different:
     * we want to receive a {@link org.opennms.netmgt.xml.event.Event} whereas the parent class
     * receives a {@link  org.opennms.netmgt.model.OnmsEvent}.
     *
     * @param event the event to forward
     * @return a response containing "no content" (204) when the event was succesfully forwarded
     */
    @Override
    public Response create(Event event) {
        if (event.getTime() == null) event.setTime(new Date());
        if (event.getSource() == null) event.setSource("ReST");

        sendEvent(event);
        return Response.noContent().build();
    }

    @Override
    public EventDTO mapEntityToDTO(OnmsEvent entity) {
        return m_eventMapper.eventToEventDTO(entity);
    }

    @Override
    public OnmsEvent mapDTOToEntity(EventDTO dto) {
        return m_eventMapper.eventDTOToEvent(dto);
    }

}
