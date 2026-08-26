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
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceContext;
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

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMetaDataList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.support.SearchPropertyCollection;
import org.opennms.web.rest.support.StringCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsIpInterface} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeIpInterfacesRestService extends AbstractNodeDependentRestService<OnmsIpInterface,OnmsIpInterface,Integer,String> {

    @Autowired
    private IpInterfaceDao m_dao;

    @Override
    protected IpInterfaceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsIpInterface> getDaoClass() {
        return OnmsIpInterface.class;
    }

    @Override
    protected Class<OnmsIpInterface> getQueryBeanClass() {
        return OnmsIpInterface.class;
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.IP_INTERFACE_SERVICE_PROPERTIES;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());

        // 1st level JOINs
        builder.alias("snmpInterface", Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("monitoredServices", Aliases.monitoredService.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        // TODO: Only add this alias when filtering so that we can specify a join condition
        builder.alias("monitoredService.serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // TODO: Remove this once the join conditions are in place
        builder.distinct();

        updateCriteria(uriInfo, builder);

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsIpInterface> createListWrapper(Collection<OnmsIpInterface> list) {
        return new OnmsIpInterfaceList(list);
    }

    @Override
    protected Response doCreate(SecurityContext securityContext, UriInfo uriInfo, OnmsIpInterface ipInterface) {
        OnmsNode node = getNode(uriInfo);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node was not found.");
        } else if (ipInterface == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface object cannot be null");
        } else if (ipInterface.getIpAddress() == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface's ipAddress cannot be null");
        } else if (ipInterface.getIpAddress().getAddress() == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface's ipAddress bytes cannot be null");
        }
        node.addIpInterface(ipInterface);
        getDao().save(ipInterface);

        final Event event = EventUtils.createNodeGainedInterfaceEvent("ReST", node.getId(), ipInterface.getIpAddress());
        sendEvent(event);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, ipInterface.getIpAddress().getHostAddress())).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsIpInterface targetObject, MultivaluedMapImpl params) {
        if (RestUtils.containsProperty(params, "ipAddress")) {
            throw getException(Status.BAD_REQUEST, "Cannot change the IP address.");
        }
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsIpInterface intf) {
        intf.getNode().getIpInterfaces().remove(intf);
        getDao().delete(intf);
        final Event e = EventUtils.createDeleteInterfaceEvent("ReST", intf.getNodeId(), intf.getIpAddress().getHostAddress(), -1, -1L);
        sendEvent(e);
    }

    @Override
    protected OnmsIpInterface doGet(UriInfo uriInfo, String ipAddress) {
        final OnmsNode node = getNode(uriInfo);
        final OnmsIpInterface iface = node == null ? null : node.getIpInterfaceByIpAddress(ipAddress);
        if (iface != null) {
            getDao().initialize(iface.getSnmpInterface());
        }
		return iface;
    }

    @Path("{id}/services")
    public NodeMonitoredServiceRestService getMonitoredServicesResource(@Context final ResourceContext context) {
        return context.getResource(NodeMonitoredServiceRestService.class);
    }

    protected OnmsIpInterface getInterface(final UriInfo uriInfo, final String ipAddress) {
        final OnmsNode node = getNode(uriInfo);
        return node.getIpInterfaceByIpAddress(ipAddress);
    }

    // The generic collection and item operations below are inherited from AbstractDaoRestServiceWithDTO.
    // They are overridden here only so that each concrete path carries its own OpenAPI documentation;
    // the bodies delegate unchanged.
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the IP interfaces of a node",
            description = """
        Return the node's IP interfaces. The criteria are restricted to the node in the path, so `_s`
        narrows within the node rather than across the system. `limit` defaults to 10.

        `isDown` and `monitoredServiceCount` are derived, not stored. In JSON `id` is a string while
        `nodeId` is a number, and `lastIngressFlow` and `lastEgressFlow` are epoch milliseconds rather
        than the `string/date-time` the derived schema shows. A FIQL term on `ipAddress` is rejected
        with 500; filter on `ipHostName` or `isManaged` instead.

        Example query: `_s=isManaged==M&orderBy=ipHostName`.""",
            operationId = "NodeIpInterfacesRestServiceGETIpInterfaces",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`. A value that is neither is answered with 500.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching IP interfaces.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsIpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "ipInterface": [
                        {
                          "id": "23615",
                          "nodeId": 257,
                          "ipAddress": "192.0.2.31",
                          "hostName": "apidoc.example.org",
                          "ifIndex": null,
                          "isManaged": "M",
                          "snmpPrimary": "P",
                          "monitoredServiceCount": 0,
                          "isDown": true,
                          "lastIngressFlow": null,
                          "lastEgressFlow": null
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsIpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                    <ipInterfaces count="1" offset="0" totalCount="1">
                      <ipInterface isDown="true" id="23615" isManaged="M" monitoredServiceCount="0" snmpPrimary="P">
                        <ipAddress>192.0.2.31</ipAddress>
                        <hostName>apidoc.example.org</hostName>
                        <nodeId>257</nodeId>
                      </ipInterface>
                    </ipInterfaces>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "The node has no matching IP interface. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Count the IP interfaces of a node",
            description = """
        Return the number of the node's IP interfaces matching `_s` as a bare decimal string.

        Example query: `_s=isManaged==M`.""",
            operationId = "NodeIpInterfacesRestServiceGETIpInterfaceCount",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Number of matching IP interfaces.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "2"))),
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
            summary = "Get IP interface search properties",
            description = """
        List the properties that may appear in a `_s` expression or in `orderBy` for this resource. The
        set spans the interface, its SNMP interface, its monitored services and the owning node, so it
        is wider than the fields of the interface itself.""",
            operationId = "NodeIpInterfacesRestServiceGETIpInterfaceSearchProperties",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`. The property list does not depend on it.", example = "257"))
    @ApiResponse(responseCode = "200", description = "Supported search properties.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SearchPropertyCollection.class),
                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "searchProperty": [
                        {"id": "ipHostName", "name": "Hostname", "type": "STRING", "orderBy": true, "iplike": false},
                        {"id": "isManaged", "name": "Management Status", "type": "STRING", "orderBy": true, "iplike": false}
                      ]
                    }""")))
    @Override
    public Response getProperties(
            @Parameter(in = ParameterIn.QUERY, name = "q",
                    description = "Case-insensitive substring matched against the property `name`, not its id.", example = "Managed")
            @QueryParam("q") final String query) {
        return super.getProperties(query);
    }

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get values of an IP interface search property",
            description = """
        Return the distinct values a search property takes. The values are read from the whole table
        rather than from the interfaces of the node in the path.""",
            operationId = "NodeIpInterfacesRestServiceGETIpInterfaceSearchPropertyValues",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Distinct values of the property.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = StringCollection.class),
                            examples = @ExampleObject(value = """
                    {"totalCount": 2, "count": 2, "offset": 0, "value": ["M", "U"]}"""))),
            @ApiResponse(responseCode = "404", description = "No search property has that id. No body is returned.")
    })
    @Override
    public Response getPropertyValues(
            @Parameter(in = ParameterIn.PATH, name = "propertyId",
                    description = "Property id as reported by the `properties` operation.", example = "isManaged")
            @PathParam("propertyId") final String propertyId,
            @Parameter(in = ParameterIn.QUERY, name = "q", description = "Substring the value must contain.", example = "M")
            @QueryParam("q") final String query,
            @Parameter(in = ParameterIn.QUERY, name = "limit", description = "Maximum number of values to return.", example = "10")
            @QueryParam("limit") final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get an IP interface of a node",
            description = """
        Return one IP interface of the node, addressed by its IP address rather than by its database
        id. The address has to match the stored form exactly; there is no wildcard or hostname
        lookup.""",
            operationId = "NodeIpInterfacesRestServiceGETIpInterfaceByAddress",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The IP interface.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": "23615",
                      "nodeId": 257,
                      "ipAddress": "192.0.2.31",
                      "hostName": "apidoc.example.org",
                      "ifIndex": null,
                      "isManaged": "M",
                      "snmpPrimary": "P",
                      "monitoredServiceCount": 0,
                      "isDown": true,
                      "lastIngressFlow": null,
                      "lastEgressFlow": null
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                    <ipInterface isDown="true" id="23615" isManaged="M" monitoredServiceCount="0" snmpPrimary="P">
                      <ipAddress>192.0.2.31</ipAddress>
                      <hostName>apidoc.example.org</hostName>
                      <nodeId>257</nodeId>
                    </ipInterface>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "The node has no interface with that address. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response get(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "IP address of the interface, not its database id.", example = "192.0.2.31")
            @PathParam("id") final String id) {
        return super.get(uriInfo, id);
    }

    @POST
    @Path("{id}")
    @Operation(
            summary = "Rejected: create an IP interface at a caller-chosen path",
            description = "Always answered with 404. Post the interface to the collection instead; its address travels in the body.",
            operationId = "NodeIpInterfacesRestServicePOSTIpInterfaceSpecific",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria", required = true,
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                            description = "IP address of the interface. The value is not read: every request to this path is answered with 404.",
                            example = "192.0.2.31")
            })
    @ApiResponse(responseCode = "404", description = "Not supported. No body is returned.")
    @Override
    public Response createSpecific() {
        return super.createSpecific();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Add an IP interface to a node",
            description = """
        Attach an IP interface to the node and send a `nodeGainedInterface` event. `ipAddress` is the
        only required field. Set `snmpPrimary` to `P` to make the interface the node's primary SNMP
        address, `S` for secondary, `N` for not eligible. `isManaged` of `M` marks the interface as
        managed and `U` as unmanaged. The new interface's URI is returned in the `Location` header.""",
            operationId = "NodeIpInterfacesRestServicePOSTIpInterface",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                    description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(required = true, description = "The IP interface to add.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsIpInterface.class),
                            examples = @ExampleObject(value = """
                    {
                      "ipAddress": "192.0.2.31",
                      "hostName": "apidoc.example.org",
                      "isManaged": "M",
                      "snmpPrimary": "P"
                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsIpInterface.class),
                            examples = @ExampleObject(value = """
                    <ipInterface isManaged="M" snmpPrimary="P">
                      <ipAddress>192.0.2.31</ipAddress>
                      <hostName>apidoc.example.org</hostName>
                    </ipInterface>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The interface was added. `Location` carries its URI.",
                    headers = @Header(name = "Location", description = "URI of the created IP interface.",
                            schema = @Schema(type = "string", example = "http://localhost:8980/opennms/api/v2/nodes/257/ipinterfaces/192.0.2.31"))),
            @ApiResponse(responseCode = "400", description = "The node was not found, or the body carried no usable IP address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "no address", value = "IP Interface's ipAddress cannot be null"),
                                    @ExampleObject(name = "no node", value = "Node was not found.")
                            })),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsIpInterface object) {
        return super.create(securityContext, uriInfo, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update properties of several IP interfaces of a node",
            description = """
        Apply the form parameters as bean properties to every IP interface of the node matching `_s`.
        The default `limit` of 10 applies to the selection. `ipAddress` cannot be set this way. The
        whole call runs in one transaction, so a per-interface failure aborts the batch.

        Example query: `_s=isManaged==U&limit=100`.""",
            operationId = "NodeIpInterfacesRestServicePUTIpInterfaces",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(required = true, description = "IP interface bean properties to set, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "is-managed=M")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "All selected interfaces were updated."),
            @ApiResponse(responseCode = "400", description = "The body tried to change `ipAddress`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot change the IP address."))),
            @ApiResponse(responseCode = "404", description = "No interface matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(
            summary = "Not implemented: replace an IP interface from a document",
            description = """
        `AbstractDaoRestServiceWithDTO.doUpdate` is not overridden for IP interfaces, so this variant
        cannot succeed. It also binds `{id}` as an integer while the collection addresses interfaces by
        IP address, so an address in the path is answered with 404 before the handler runs. Use the
        form-encoded `PUT` on the same path to change properties.""",
            operationId = "NodeIpInterfacesRestServicePUTIpInterfaceDocument",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(description = "Ignored.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OnmsIpInterface.class),
                            examples = @ExampleObject(value = "{\"isManaged\": \"M\"}")),
                    @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = OnmsIpInterface.class),
                            examples = @ExampleObject(value = "<ipInterface isManaged=\"M\"/>"))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "`{id}` is not an integer, which is the case for every IP address."),
            @ApiResponse(responseCode = "501", description = "Replacing an IP interface from a document is not implemented. No body is returned.")
    })
    @Override
    public Response update(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Bound as an integer here, unlike every other operation on this path.", example = "23615")
            @PathParam("id") final Integer id,
            final OnmsIpInterface object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(
            summary = "Update properties of an IP interface",
            description = """
        Apply the form parameters as bean properties to one IP interface of the node. Only the
        properties present in the body are touched. `ipAddress` is rejected: delete the interface and
        add it again to renumber it. No event is sent, so daemons holding the previous value are not
        notified.""",
            operationId = "NodeIpInterfacesRestServicePUTIpInterfaceProperties",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(required = true, description = "IP interface bean properties to set, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "ip-host-name=apidoc-renamed.example.org&is-managed=M")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The interface was updated."),
            @ApiResponse(responseCode = "400", description = "The body tried to change `ipAddress`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot change the IP address."))),
            @ApiResponse(responseCode = "404", description = "The node has no interface with that address. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response updateProperties(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("id") final String id,
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @DELETE
    @Operation(
            summary = "Delete several IP interfaces of a node",
            description = """
        Delete every IP interface of the node matching `_s` and send a `deleteInterface` event for each.
        The default `limit` of 10 applies to the selection, so a call without an explicit `limit`
        deletes at most 10 interfaces. The interfaces' monitored services and metadata go with them.

        Example query: `_s=isManaged==U&limit=100`.""",
            operationId = "NodeIpInterfacesRestServiceDELETEIpInterfaces",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
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
            summary = "Delete an IP interface",
            description = """
        Delete one IP interface of the node and send a `deleteInterface` event. Its monitored services
        and metadata go with it.""",
            operationId = "NodeIpInterfacesRestServiceDELETEIpInterfaceByAddress",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The interface was deleted."),
            @ApiResponse(responseCode = "404", description = "The node has no interface with that address. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response delete(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("id") final String id) {
        return super.delete(securityContext, uriInfo, id);
    }

    @GET
    @Path("{ipAddress}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get all metadata of an IP interface",
            description = """
        Return every metadata entry attached to the interface, across all contexts. An empty result is
        still a 200; `totalCount` and `count` are then `null` rather than `0`. A node that does not
        exist is answered with 500 rather than 400.""",
            operationId = "NodeIpInterfacesRestServiceGETMetaDataByIpAddress",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metadata entries of the interface.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMetaDataList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "metaData": [{"context": "X-ApiDoc", "key": "vlan", "value": "42"}]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMetaDataList.class),
                                    examples = @ExampleObject(value = """
                    <meta-data-list count="1" offset="0" totalCount="1">
                      <meta-data><context>X-ApiDoc</context><key>vlan</key><value>42</value></meta-data>
                    </meta-data-list>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "The node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "getMetaData: Can't find interface 198.51.100.99"))),
            @ApiResponse(responseCode = "500", description = "The node does not exist, or the node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"org.opennms.netmgt.model.OnmsNode.getIpInterfaceByIpAddress(String)\" because \"node\" is null")))
    })
    public OnmsMetaDataList getMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "ipAddress",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("ipAddress") String ipAddress) {
        final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

        if (intf == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find interface " + ipAddress);
        }

        return new OnmsMetaDataList(intf.getMetaData());
    }

    @GET
    @Path("{ipAddress}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get metadata of an IP interface in one context",
            description = """
        Return the interface's metadata entries whose context matches exactly, case-sensitively. A
        context that holds no entries and a context that does not exist both give a 200 with an empty
        list.""",
            operationId = "NodeIpInterfacesRestServiceGETMetaDataByIpAddressAndContext",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metadata entries in that context, possibly empty.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMetaDataList.class),
                            examples = {
                                    @ExampleObject(name = "match", value = """
                    {"totalCount": 1, "count": 1, "offset": 0, "metaData": [{"context": "X-ApiDoc", "key": "vlan", "value": "42"}]}"""),
                                    @ExampleObject(name = "no match", value = """
                    {"totalCount": null, "count": null, "offset": 0, "metaData": []}""")
                            })),
            @ApiResponse(responseCode = "400", description = "The node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "getMetaData: Can't find interface 198.51.100.99"))),
            @ApiResponse(responseCode = "500", description = "The node does not exist, or its path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public OnmsMetaDataList getMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "ipAddress",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("ipAddress") String ipAddress,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context to filter on.", example = "X-ApiDoc")
            @PathParam("context") String context) {
        final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

        if (intf == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find interface " + ipAddress);
        }

        return new OnmsMetaDataList(intf.getMetaData().stream()
                .filter(e -> context.equals(e.getContext()))
                .collect(Collectors.toList()));
    }

    @GET
    @Path("{ipAddress}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one metadata entry of an IP interface",
            description = """
        Return the interface's metadata entries matching both context and key. At most one entry can
        match, but the response is still the list envelope.""",
            operationId = "NodeIpInterfacesRestServiceGETMetaDataByIpAddressAndContextAndKey",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The matching entry, or an empty list.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMetaDataList.class),
                            examples = @ExampleObject(value = """
                    {"totalCount": 1, "count": 1, "offset": 0, "metaData": [{"context": "X-ApiDoc", "key": "vlan", "value": "42"}]}"""))),
            @ApiResponse(responseCode = "400", description = "The node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "getMetaData: Can't find interface 198.51.100.99"))),
            @ApiResponse(responseCode = "500", description = "The node does not exist, or its path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public OnmsMetaDataList getMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "ipAddress",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("ipAddress") String ipAddress,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context.", example = "X-ApiDoc")
            @PathParam("context") String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key within the context.", example = "vlan")
            @PathParam("key") String key) {
        final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

        if (intf == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find interface " + ipAddress);
        }

        return new OnmsMetaDataList(intf.getMetaData().stream()
                .filter(e -> context.equals(e.getContext()) && key.equals(e.getKey()))
                .collect(Collectors.toList()));
    }

    @DELETE
    @Path("{ipAddress}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete a metadata context of an IP interface",
            description = """
        Remove every metadata entry of the interface in the given context. Deleting a context that
        holds no entries is also a 204. The context is checked before the interface is looked up, so a
        non-`X-` context is answered with 403 even for an interface that does not exist.""",
            operationId = "NodeIpInterfacesRestServiceDELETEMetaDataByIpAddressAndContext",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The context was removed."),
            @ApiResponse(responseCode = "400", description = "The node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "deleteMetaData: Can't find interface 198.51.100.99"))),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`. Only user-defined contexts may be written through this API.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The node does not exist, or its path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response deleteMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "ipAddress",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("ipAddress") final String ipAddress,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context to remove. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") final String context) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

            if (intf == null) {
                throw getException(Status.BAD_REQUEST, "deleteMetaData: Can't find interface " + ipAddress);
            }
            intf.removeMetaData(context);
            getDao().update(intf);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{ipAddress}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete one metadata entry of an IP interface",
            description = """
        Remove a single metadata entry of the interface. Deleting a key that does not exist is also a
        204.""",
            operationId = "NodeIpInterfacesRestServiceDELETEMetaDataByIpAddressAndContextAndKey",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was removed."),
            @ApiResponse(responseCode = "400", description = "The node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "deleteMetaData: Can't find interface 198.51.100.99"))),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The node does not exist, or its path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response deleteMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "ipAddress",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("ipAddress") final String ipAddress,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key to remove.", example = "vlan")
            @PathParam("key") final String key) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

            if (intf == null) {
                throw getException(Status.BAD_REQUEST, "deleteMetaData: Can't find interface " + ipAddress);
            }
            intf.removeMetaData(context, key);
            getDao().update(intf);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Path("{ipAddress}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace a metadata entry of an IP interface",
            description = """
        Set one metadata entry on the interface from the request body. An existing entry with the same
        context and key is overwritten, so this is an upsert rather than an append. No `@Consumes` is
        declared, so both JSON and XML bodies are accepted; the XML root element is `meta-data`.""",
            operationId = "NodeIpInterfacesRestServicePOSTMetaDataByIpAddress",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(required = true, description = "The metadata entry to set. The context must start with `X-`.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMetaData.class),
                            examples = @ExampleObject(value = """
                    {"context": "X-ApiDoc", "key": "vlan", "value": "42"}""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsMetaData.class),
                            examples = @ExampleObject(value = """
                    <meta-data><context>X-ApiDoc</context><key>vlan</key><value>42</value></meta-data>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was stored."),
            @ApiResponse(responseCode = "400", description = "The node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "postMetaData: Can't find interface 198.51.100.99"))),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The node does not exist, its path segment could not be parsed, or the body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response postMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "ipAddress",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("ipAddress") final String ipAddress,
            final OnmsMetaData entity) {
        checkUserDefinedMetadataContext(entity.getContext());

        writeLock();
        try {
            final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

            if (intf == null) {
                throw getException(Status.BAD_REQUEST, "postMetaData: Can't find interface " + ipAddress);
            }
            intf.addMetaData(entity.getContext(), entity.getKey(), entity.getValue());
            getDao().update(intf);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{ipAddress}/metadata/{context}/{key}/{value}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Set a metadata entry of an IP interface from the path",
            description = """
        Set one metadata entry on the interface with context, key and value all taken from the path. An
        existing entry with the same context and key is overwritten. A value containing `/` has to be
        percent-encoded, and an empty value cannot be expressed this way; use the `POST` form for
        those.""",
            operationId = "NodeIpInterfacesRestServicePUTMetaDataByIpAddress",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was stored."),
            @ApiResponse(responseCode = "400", description = "The node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "putMetaData: Can't find interface 198.51.100.99"))),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The node does not exist, or its path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response putMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "ipAddress",
                    description = "IP address of the interface.", example = "192.0.2.31")
            @PathParam("ipAddress") final String ipAddress,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") final String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key.", example = "vlan")
            @PathParam("key") final String key,
            @Parameter(in = ParameterIn.PATH, name = "value",
                    description = "Metadata value.", example = "42")
            @PathParam("value") final String value) {        checkUserDefinedMetadataContext(context);
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsIpInterface intf = getInterface(uriInfo, ipAddress);

            if (intf == null) {
                throw getException(Status.BAD_REQUEST, "putMetaData: Can't find interface " + ipAddress);
            }
            intf.addMetaData(context, key, value);
            getDao().update(intf);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }
}
