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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Hidden;
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

import org.apache.cxf.jaxrs.ext.search.SearchContext;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterfaceList;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsSnmpInterface} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeSnmpInterfacesRestService extends AbstractNodeDependentRestService<OnmsSnmpInterface,OnmsSnmpInterface,Integer,Integer> {

    @Autowired
    private SnmpInterfaceDao m_ipInterfaceDao;

    @Override
    protected SnmpInterfaceDao getDao() {
        return m_ipInterfaceDao;
    }

    @Override
    protected Class<OnmsSnmpInterface> getDaoClass() {
        return OnmsSnmpInterface.class;
    }

    @Override
    protected Class<OnmsSnmpInterface> getQueryBeanClass() {
        return OnmsSnmpInterface.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        updateCriteria(uriInfo, builder);
        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsSnmpInterface> createListWrapper(Collection<OnmsSnmpInterface> list) {
        return new OnmsSnmpInterfaceList(list);
    }

    @Override
    protected Response doCreate(SecurityContext securityContext, UriInfo uriInfo, OnmsSnmpInterface snmpInterface) {
        OnmsNode node = getNode(uriInfo);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node was not found.");
        } else if (snmpInterface == null) {
            throw getException(Status.BAD_REQUEST, "SNMP Interface object cannot be null");
        } else if (snmpInterface.getIfIndex() == null) {
            throw getException(Status.BAD_REQUEST, "SNMP Interface's ifIndex cannot be null");
        }
        node.addSnmpInterface(snmpInterface);
        if (snmpInterface.getPrimaryIpInterface() != null) {
            final OnmsIpInterface iface = snmpInterface.getPrimaryIpInterface();
            iface.setSnmpInterface(snmpInterface);
        }
        getDao().save(snmpInterface);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, snmpInterface.getIfIndex())).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsSnmpInterface targetObject, MultivaluedMapImpl params) {
        if (RestUtils.containsProperty(params, "ifIndex")) {
            throw getException(Status.BAD_REQUEST, "Cannot change ifIndex.");
        }
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsSnmpInterface intf) {
        intf.getNode().getSnmpInterfaces().remove(intf);
        getDao().delete(intf);
    }

    @Override
    protected OnmsSnmpInterface doGet(UriInfo uriInfo, Integer ifIndex) {
        final OnmsNode node = getNode(uriInfo);
        return node == null ? null : node.getSnmpInterfaceWithIfIndex(ifIndex);
    }

    // The generic collection and item operations below are inherited from AbstractDaoRestServiceWithDTO.
    // They are overridden here only so that each concrete path carries its own OpenAPI documentation;
    // the JAX-RS annotations repeat the inherited ones and the bodies delegate unchanged.
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the SNMP interfaces of a node",
            description = """
        Return the node's SNMP interfaces. The criteria are restricted to the node in the path, so `_s`
        narrows within the node rather than across the system. `limit` defaults to 10, and the response
        carries a `Content-Range` header of the form `items <offset>-<last>/<totalCount>`.

        This endpoint declares no search-property set, so its `properties` operation answers 204. A
        `_s` term is still resolved against the bean properties of
        the SNMP interface and, under the `node.` prefix, against the joined node, for example
        `_s=ifName==eth0` or `_s=node.label==router-1`. A name that cannot be resolved is answered with
        500.

        `lastCapsdPoll`, `lastSnmpPoll`, `lastIngressFlow` and `lastEgressFlow` come from the same
        fields in both representations but are rendered as epoch milliseconds in JSON and as ISO-8601
        with an offset in XML. `collect`, `poll`, `hasFlows`, `hasIngressFlows`, `hasEgressFlows`,
        `collectionPolicySpecified` and `collectionUserSpecified` are derived from the stored
        `collectFlag` and `pollFlag` values.

        `application/atom+xml` is also accepted and returns the same document as `application/xml`.""",
            operationId = "NodeSnmpInterfacesRestServiceGETSnmpInterfaces",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`. A value that is neither is answered with 500.",
                    example = "274"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching SNMP interfaces.",
                    headers = @Header(name = "Content-Range", description = "`items <offset>-<last>/<totalCount>` for this page.",
                            schema = @Schema(type = "string")),
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsSnmpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "snmpInterface": [
                        {
                          "id": 23745,
                          "ifIndex": 11,
                          "ifDescr": "ApiDoc11-eth0",
                          "ifName": "ApiDoc11-eth0",
                          "ifAlias": "uplink",
                          "ifType": 6,
                          "ifSpeed": 100000000,
                          "ifAdminStatus": 1,
                          "ifOperStatus": 1,
                          "physAddr": "02004c4f4f11",
                          "collectFlag": "C",
                          "collect": true,
                          "collectionPolicySpecified": false,
                          "collectionUserSpecified": false,
                          "pollFlag": "P",
                          "poll": true,
                          "hasFlows": false,
                          "hasIngressFlows": false,
                          "hasEgressFlows": false,
                          "lastIngressFlow": null,
                          "lastEgressFlow": null,
                          "lastCapsdPoll": 1787235330000,
                          "lastSnmpPoll": null,
                          "nodeId": 274
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsSnmpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                    <snmpInterfaces count="1" offset="0" totalCount="1">
                      <snmpInterface collectFlag="C" collect="true" id="23745" ifIndex="11" pollFlag="P" poll="true">
                        <ifAdminStatus>1</ifAdminStatus>
                        <ifAlias>uplink</ifAlias>
                        <ifDescr>ApiDoc11-eth0</ifDescr>
                        <ifName>ApiDoc11-eth0</ifName>
                        <ifOperStatus>1</ifOperStatus>
                        <ifSpeed>100000000</ifSpeed>
                        <ifType>6</ifType>
                        <lastCapsdPoll>2026-08-20T10:15:30-04:00</lastCapsdPoll>
                        <nodeId>274</nodeId>
                        <physAddr>02004c4f4f11</physAddr>
                      </snmpInterface>
                    </snmpInterfaces>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "The node has no matching SNMP interface, or no node has that id. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "badSearch", value = "Error parsing FIQL search"),
                                    @ExampleObject(name = "badNodeCriteria", value = "For input string: \"notanode\"")
                            }))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Count the SNMP interfaces of a node",
            description = """
        Return the number of the node's SNMP interfaces matching `_s` as a bare decimal string.

        Only `text/plain` is produced. A request that sends `Accept: application/json` does not match
        this operation, falls through to the single-interface GET with `count` as the identifier, and is
        answered with 404.""",
            operationId = "NodeSnmpInterfacesRestServiceGETSnmpInterfaceCount",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "274"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Number of matching SNMP interfaces.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "13"))),
            @ApiResponse(responseCode = "404", description = "The request asked for a media type this operation does not produce. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get SNMP interface search properties",
            description = """
        Lists the properties usable in `_s` and `orderBy`. This endpoint does not declare a
        search-property set, so the list is empty and the operation answers 204 for every request,
        including one with a `q` filter. `_s` and `orderBy` still work: they resolve against the bean
        properties of the SNMP interface and of the joined node under the `node.` prefix. The
        equivalent operation on `/snmpinterfaces` does return a property list.""",
            operationId = "NodeSnmpInterfacesRestServiceGETSnmpInterfaceSearchProperties",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`. The result does not depend on it.", example = "274"))
    @ApiResponse(responseCode = "204", description = "No search properties are declared for this endpoint. No body is returned.")
    @Override
    public Response getProperties(@QueryParam("q") final String query) {
        return super.getProperties(query);
    }

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get values of an SNMP interface search property",
            description = """
        Returns the distinct values a search property takes. Because this endpoint declares
        no search properties, no `propertyId` can match and the operation answers 404 for every
        request. The equivalent operation on `/snmpinterfaces` does return values.""",
            operationId = "NodeSnmpInterfacesRestServiceGETSnmpInterfaceSearchPropertyValues",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`. The result does not depend on it.", example = "274"))
    @ApiResponse(responseCode = "404", description = "No search property has that id, which is the case for every id here. No body is returned.")
    @Override
    public Response getPropertyValues(@PathParam("propertyId") final String propertyId, @QueryParam("q") final String query, @QueryParam("limit") final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get an SNMP interface of a node",
            description = """
        Return one SNMP interface of the node. The path identifier is the interface's `ifIndex`, not its
        database `id`; passing a database id normally yields 404. A value that is not an integer is
        rejected by path-parameter conversion, also with 404.

        Timestamps are epoch milliseconds in JSON and ISO-8601 with an offset in XML.""",
            operationId = "NodeSnmpInterfacesRestServiceGETSnmpInterfaceByIfIndex",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "274"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The SNMP interface.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": 23745,
                      "ifIndex": 11,
                      "ifDescr": "ApiDoc11-eth0",
                      "ifName": "ApiDoc11-eth0",
                      "ifAlias": "uplink",
                      "ifType": 6,
                      "ifSpeed": 100000000,
                      "ifAdminStatus": 1,
                      "ifOperStatus": 1,
                      "physAddr": "02004c4f4f11",
                      "collectFlag": "C",
                      "collect": true,
                      "collectionPolicySpecified": false,
                      "collectionUserSpecified": false,
                      "pollFlag": "P",
                      "poll": true,
                      "hasFlows": false,
                      "hasIngressFlows": false,
                      "hasEgressFlows": false,
                      "lastIngressFlow": null,
                      "lastEgressFlow": null,
                      "lastCapsdPoll": 1787235330000,
                      "lastSnmpPoll": null,
                      "nodeId": 274
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                    <snmpInterface collectFlag="C" collect="true" id="23745" ifIndex="11" pollFlag="P" poll="true">
                      <ifAdminStatus>1</ifAdminStatus>
                      <ifAlias>uplink</ifAlias>
                      <ifDescr>ApiDoc11-eth0</ifDescr>
                      <ifName>ApiDoc11-eth0</ifName>
                      <ifOperStatus>1</ifOperStatus>
                      <ifSpeed>100000000</ifSpeed>
                      <ifType>6</ifType>
                      <lastCapsdPoll>2026-08-20T10:15:30-04:00</lastCapsdPoll>
                      <nodeId>274</nodeId>
                      <physAddr>02004c4f4f11</physAddr>
                    </snmpInterface>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "The node has no interface with that `ifIndex`, or the identifier is not an integer. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanode\"")))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo, @Parameter(description = "`ifIndex` of the interface, not its database id.", example = "11") @PathParam("id") final Integer id) {
        return super.get(uriInfo, id);
    }

    @POST
    @Path("{id}")
    @Operation(
            summary = "Rejected: create an SNMP interface at a caller-chosen path",
            description = "Always answered with 404, whether or not the `ifIndex` exists.",
            operationId = "NodeSnmpInterfacesRestServicePOSTSnmpInterfaceSpecific",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`. The result does not depend on it.", example = "274"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "Ignored. Any value produces the same response.",
                            schema = @Schema(type = "string"), example = "11")
            })
    @ApiResponse(responseCode = "404", description = "Not supported. No body is returned.")
    @Override
    public Response createSpecific() {
        return super.createSpecific();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Add an SNMP interface to a node",
            description = """
        Attach an SNMP interface to the node. `ifIndex` is the only required field. No event is sent.

        `collectFlag` and `pollFlag` are the stored collection and polling flags. `collectFlag` of `C`
        or `N` records the choice without a source, `UC` and `UN` mark it user specified, and `PC` and
        `PN` mark it policy specified; `pollFlag` of `P` enables service polling on the interface. The
        derived `collect`, `poll`, `collectionUserSpecified` and `collectionPolicySpecified` values in
        the response follow from them. In XML `ifIndex`, `collectFlag` and `pollFlag` are attributes
        rather than elements, and sending them as elements leaves them unset.

        The `Location` header of the 201 addresses the interface by `ifIndex`.""",
            operationId = "NodeSnmpInterfacesRestServicePOSTSnmpInterface",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "274"))
    @RequestBody(required = true, description = "The SNMP interface to add.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsSnmpInterface.class),
                            examples = @ExampleObject(value = """
                    {
                      "ifIndex": 11,
                      "ifDescr": "ApiDoc11-eth0",
                      "ifName": "ApiDoc11-eth0",
                      "ifAlias": "uplink",
                      "ifType": 6,
                      "ifSpeed": 1000000000,
                      "ifAdminStatus": 1,
                      "ifOperStatus": 1,
                      "physAddr": "02004c4f4f11",
                      "collectFlag": "C",
                      "pollFlag": "P"
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsSnmpInterface.class),
                            examples = @ExampleObject(value = """
                    <snmpInterface ifIndex="11" collectFlag="C" pollFlag="P">
                      <ifDescr>ApiDoc11-eth0</ifDescr>
                      <ifName>ApiDoc11-eth0</ifName>
                      <ifAlias>uplink</ifAlias>
                      <ifType>6</ifType>
                      <ifSpeed>1000000000</ifSpeed>
                    </snmpInterface>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The interface was added. `Location` carries its URI.",
                    headers = @Header(name = "Location", description = "URI of the created SNMP interface, keyed by `ifIndex`.",
                            schema = @Schema(type = "string", example = "http://localhost:8980/opennms/api/v2/nodes/274/snmpinterfaces/11"))),
            @ApiResponse(responseCode = "400", description = "The node was not found, or the body carried no `ifIndex`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "noIfIndex", value = "SNMP Interface's ifIndex cannot be null"),
                                    @ExampleObject(name = "noNode", value = "Node was not found.")
                            })),
            @ApiResponse(responseCode = "415", description = "The body was form-encoded. Send JSON or XML. No body is returned."),
            @ApiResponse(responseCode = "500", description = """
                    The node path segment could not be parsed, the body could not be deserialised, or the node \
                    already has an interface with that `ifIndex`: the unique index on `(nodeid, ifindex)` is \
                    reported as a constraint violation rather than as a conflict.""",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "duplicateIfIndex", value = "could not execute statement; SQL [n/a]; constraint [snmpinterface_nodeid_ifindex_unique_idx]; nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement"),
                                    @ExampleObject(name = "badNodeCriteria", value = "For input string: \"notanode\"")
                            }))
    })
    @Override
    public Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsSnmpInterface object) {
        return super.create(securityContext, uriInfo, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update properties of several SNMP interfaces of a node",
            description = """
        Apply the form parameters as bean properties to every SNMP interface of the node matching `_s`.
        The default `limit` of 10 bounds the selection, so a call without an explicit `limit` touches at
        most ten interfaces; `limit=0` removes the bound. `ifIndex` cannot be set this way. The whole
        call runs in one transaction, so a per-interface failure aborts the batch.""",
            operationId = "NodeSnmpInterfacesRestServicePUTSnmpInterfaces",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "274"))
    @RequestBody(required = true, description = DOC_FORM_BODY,
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "if-alias=ApiDoc11-bulk&collect=N")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "All selected interfaces were updated."),
            @ApiResponse(responseCode = "400", description = "The body tried to change `ifIndex`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot change ifIndex."))),
            @ApiResponse(responseCode = "404", description = "No interface matched. No body is returned."),
            @ApiResponse(responseCode = "415", description = "The body was not form-encoded. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    // Hidden because OpenAPI holds one operation per path and method: the form-encoded PUT on
    // {id} is the one that works, and its description records that a document body answers 501.
    @Hidden
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Override
    public Response update(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @PathParam("id") final Integer id, final OnmsSnmpInterface object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(
            summary = "Update properties of an SNMP interface",
            description = """
        Apply the form parameters as bean properties to one SNMP interface of the node, addressed by
        `ifIndex`. Only the properties named in the body are touched, and no event is sent.

        Parameter names are normalised, so the wire names differ from the JSON and XML field names:
        `if-alias` or `if_alias` reaches `ifAlias` while the camel-case `ifAlias` does not, and the
        stored collection and polling flags are reached as `collect` and `poll` rather than as
        `collectFlag` and `pollFlag`. `ifIndex` is rejected with 400 under any spelling.

        Sending a JSON or XML body to the same path selects a different operation, which answers
        501.""",
            operationId = "NodeSnmpInterfacesRestServicePUTSnmpInterfaceProperties",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "274"))
    @RequestBody(required = true, description = DOC_FORM_BODY,
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = {
                            @ExampleObject(name = "description", value = "if-alias=uplink&if-speed=100000000"),
                            @ExampleObject(name = "flags", value = "collect=C&poll=P"),
                            @ExampleObject(name = "timestamp", value = "last-capsd-poll=2026-08-20T10:15:30.000-0400")
                    }))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The interface was updated. Returned even when no parameter resolved to a writable property."),
            @ApiResponse(responseCode = "400", description = "The body tried to change `ifIndex`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot change ifIndex."))),
            @ApiResponse(responseCode = "404", description = "The node has no interface with that `ifIndex`, or the identifier is not an integer. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or a value could not be converted to the property's type.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanode\"")))
    })
    @Override
    public Response updateProperties(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Parameter(description = "`ifIndex` of the interface, not its database id.", example = "11") @PathParam("id") final Integer id, final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @DELETE
    @Operation(
            summary = "Delete several SNMP interfaces of a node",
            description = """
        Delete every SNMP interface of the node matching `_s`. The default `limit` of 10 bounds the
        selection, so a call without an explicit `limit` deletes at most ten interfaces; `limit=0`
        removes the bound. No event is sent.""",
            operationId = "NodeSnmpInterfacesRestServiceDELETESnmpInterfaces",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "274"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The selected interfaces were deleted."),
            @ApiResponse(responseCode = "404", description = "No interface matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete an SNMP interface",
            description = """
        Delete one SNMP interface of the node, addressed by `ifIndex`. No event is sent.""",
            operationId = "NodeSnmpInterfacesRestServiceDELETESnmpInterfaceByIfIndex",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "274"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The interface was deleted."),
            @ApiResponse(responseCode = "404", description = "The node has no interface with that `ifIndex`, or the identifier is not an integer. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanode\"")))
    })
    @Override
    public Response delete(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Parameter(description = "`ifIndex` of the interface, not its database id.", example = "11") @PathParam("id") final Integer id) {
        return super.delete(securityContext, uriInfo, id);
    }

}
