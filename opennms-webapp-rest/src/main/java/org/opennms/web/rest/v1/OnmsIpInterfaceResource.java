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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.api.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("onmsIpInterfaceResource")
@Transactional
public class OnmsIpInterfaceResource extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OnmsIpInterfaceResource.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    /**
     * <p>getIpInterfaces</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterfaceList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "List a node's IP interfaces",
            description = """
                    Returns the IP interfaces of one node. Interfaces flagged deleted (`isManaged` of `D`) are
                    excluded and cannot be included.

                    Every query parameter that is not one of the paging parameters below is read as a property
                    restriction on `OnmsIpInterface`. Joins are pre-registered for `monitoredServices.serviceType`
                    as `serviceType` and for `node`. A value of `null` or `notnull` becomes an is-null /
                    is-not-null test. Unknown property names are logged and ignored rather than rejected.

                    `isManaged` is a one-character code: `M` is managed, and only `M` counts as managed. `U`, `F`
                    and `N` are the unmanaged, forced-unmanaged and not-polled variants; `D` marks a deleted row.
                    `snmpPrimary` is `P` for the primary SNMP interface, `S` for secondary and `N` for none.

                    The node lookup is not guarded, so an unknown `nodeCriteria` fails with HTTP 500 rather than
                    400 or 404.""",
            operationId = "listNodeIpInterfaces",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "limit",
                            description = "Maximum rows to return. 0 disables the limit.",
                            schema = @Schema(type = "integer", defaultValue = "10"), example = "25"),
                    @Parameter(in = ParameterIn.QUERY, name = "offset",
                            description = "Zero-based index of the first row to return.",
                            schema = @Schema(type = "integer"), example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "orderBy",
                            description = "Property to sort on.",
                            schema = @Schema(type = "string"), example = "ipAddress"),
                    @Parameter(in = ParameterIn.QUERY, name = "order",
                            description = "Sort direction for `orderBy`. Anything other than `desc` is read as ascending.",
                            schema = @Schema(type = "string", allowableValues = {"asc", "desc"}), example = "asc"),
                    @Parameter(in = ParameterIn.QUERY, name = "comparator",
                            description = "Comparison applied to every property restriction in the query.",
                            schema = @Schema(type = "string", defaultValue = "eq",
                                    allowableValues = {"eq", "ne", "gt", "lt", "ge", "le", "like", "ilike", "contains", "iplike"}),
                            example = "iplike"),
                    @Parameter(in = ParameterIn.QUERY, name = "match",
                            description = "Whether the property restrictions are combined with AND or OR.",
                            schema = @Schema(type = "string", defaultValue = "all", allowableValues = {"all", "any"}),
                            example = "any"),
                    @Parameter(in = ParameterIn.QUERY, name = "ipAddress",
                            description = "Restrict to this address. Example of the generic property-restriction form; pair it with `comparator=iplike` for a range.",
                            schema = @Schema(type = "string"), example = "192.0.2.10"),
                    @Parameter(in = ParameterIn.QUERY, name = "snmpPrimary",
                            description = "Restrict by SNMP primary role.",
                            schema = @Schema(type = "string", allowableValues = {"P", "S", "N"}), example = "P")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's IP interfaces.",
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
                          "ipAddress": "192.0.2.10",
                          "ifIndex": 8,
                          "lastIngressFlow": null,
                          "lastEgressFlow": null,
                          "isManaged": "M",
                          "snmpPrimary": "P",
                          "monitoredServiceCount": 2,
                          "isDown": false,
                          "id": "23601",
                          "hostName": "core-sw-01",
                          "nodeId": 258
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsIpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                    <ipInterfaces count="1" offset="0" totalCount="1">
                      <ipInterface isDown="false" ifIndex="8" id="23601" isManaged="M" monitoredServiceCount="2" snmpPrimary="P">
                        <ipAddress>192.0.2.10</ipAddress>
                        <hostName>core-sw-01</hostName>
                        <nodeId>258</nodeId>
                      </ipInterface>
                    </ipInterfaces>"""))
                    }),
            @ApiResponse(responseCode = "500", description = "No node matches `nodeCriteria`. The node is dereferenced without a null check, so this is a null-pointer failure rather than a 400.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"org.opennms.netmgt.model.OnmsNode.getId()\" because \"node\" is null")))
    })
    public OnmsIpInterfaceList getIpInterfaces(@Context final UriInfo uriInfo,
                                               @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                               @PathParam("nodeCriteria") final String nodeCriteria) {
        LOG.debug("getIpInterfaces: reading interfaces for node {}", nodeCriteria);

        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        
        final MultivaluedMap<String,String> params = uriInfo.getQueryParameters();
        
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsIpInterface.class);
        builder.alias("monitoredServices.serviceType", "serviceType", JoinType.LEFT_JOIN);
        builder.ne("isManaged", "D");
        builder.limit(20);
        applyQueryFilters(params, builder);
        builder.alias("node", "node");
        builder.eq("node.id", node.getId());
        
        final OnmsIpInterfaceList interfaceList = new OnmsIpInterfaceList(m_ipInterfaceDao.findMatching(builder.toCriteria()));

        interfaceList.setTotalCount(m_ipInterfaceDao.countMatching(builder.count().toCriteria()));
        
        return interfaceList;
    }

    /**
     * <p>getIpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{ipAddress}")
    @Operation(
            summary = "Get one IP interface of a node",
            description = """
                    Returns a single IP interface, addressed by its literal address. The address is parsed before
                    the lookup, so an unparseable address behaves the same as an address the node does not have.

                    Unlike the list endpoint, this one does look at interfaces flagged deleted, since it reads the
                    node's own interface set rather than running a query.""",
            operationId = "getNodeIpInterface"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The IP interface. `snmpInterface` is present when the address is bound to a known ifIndex.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                    {
                      "ipAddress": "192.0.2.10",
                      "ifIndex": 8,
                      "lastIngressFlow": null,
                      "lastEgressFlow": null,
                      "isManaged": "M",
                      "snmpPrimary": "P",
                      "monitoredServiceCount": 2,
                      "isDown": false,
                      "id": "23601",
                      "hostName": "core-sw-01",
                      "nodeId": 258
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsIpInterface.class),
                                    examples = @ExampleObject(value = """
                    <ipInterface isDown="false" ifIndex="8" id="23601" isManaged="M" monitoredServiceCount="2" snmpPrimary="P">
                      <ipAddress>192.0.2.10</ipAddress>
                      <hostName>core-sw-01</hostName>
                      <nodeId>258</nodeId>
                    </ipInterface>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "IP Interface 192.0.2.99 was not found on node 258.")))
    })
    public OnmsIpInterface getIpInterface(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                          @PathParam("nodeCriteria") final String nodeCriteria,
                                          @Parameter(description = "Literal IPv4 or IPv6 address of the interface.", example = "192.0.2.10")
                                          @PathParam("ipAddress") final String ipAddress) {
        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        final OnmsIpInterface iface = node.getIpInterfaceByIpAddress(InetAddressUtils.getInetAddress(ipAddress));
        if (iface == null) {
            throw getException(Status.NOT_FOUND, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
        }
        return iface;
    }

    /**
     * <p>addIpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipInterface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Operation(
            summary = "Add an IP interface to a node",
            description = """
                    Attaches a new IP interface to the node and publishes `nodeGainedInterface`, which is what
                    makes pollerd and collectd pick it up.

                    XML only. A JSON or form-encoded body is rejected with 415.

                    `isManaged` and `snmpPrimary` are XML *attributes* on `ipInterface`, while `ipAddress` and
                    `hostName` are *elements*; a value sent as the wrong kind of node is dropped silently.""",
            operationId = "addNodeIpInterface"
    )
    @RequestBody(
            required = true,
            description = "The interface to add. `ipAddress` is required.",
            content = @Content(mediaType = MediaType.APPLICATION_XML,
                    schema = @Schema(implementation = OnmsIpInterface.class),
                    examples = @ExampleObject(value = """
                    <ipInterface isManaged="M" snmpPrimary="P">
                      <ipAddress>192.0.2.10</ipAddress>
                      <hostName>core-sw-01</hostName>
                    </ipInterface>"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created. `Location` points at the new interface."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or the body carries no usable `ipAddress`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found."),
                                    @ExampleObject(name = "noAddress", value = "IP Interface's ipAddress cannot be null")
                            })),
            @ApiResponse(responseCode = "415", description = "The body was not XML."),
            @ApiResponse(responseCode = "500", description = "Publishing `nodeGainedInterface` failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot send event uei.opennms.org/nodes/nodeGainedInterface : connection refused")))
    })
    public Response addIpInterface(@Context final UriInfo uriInfo,
                                   @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                   @PathParam("nodeCriteria") final String nodeCriteria, final OnmsIpInterface ipInterface) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            } else if (ipInterface == null) {
                throw getException(Status.BAD_REQUEST, "IP Interface object cannot be null");
            } else if (ipInterface.getIpAddress() == null) {
                throw getException(Status.BAD_REQUEST, "IP Interface's ipAddress cannot be null");
            } else if (ipInterface.getIpAddress().getAddress() == null) {
                throw getException(Status.BAD_REQUEST, "IP Interface's ipAddress bytes cannot be null");
            }
            LOG.debug("addIpInterface: adding interface {}", ipInterface);
            node.addIpInterface(ipInterface);
            m_ipInterfaceDao.save(ipInterface);

            final EventBuilder bldr = new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "ReST");

            bldr.setNodeid(node.getId());
            bldr.setInterface(ipInterface.getIpAddress());
            sendEvent(bldr);

            return Response.created(getRedirectUri(uriInfo, InetAddressUtils.str(ipInterface.getIpAddress()))).build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>updateIpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{ipAddress}")
    @Operation(
            summary = "Update one IP interface of a node",
            description = """
                    Applies form-encoded fields to an existing IP interface. Keys are bean property names on
                    `OnmsIpInterface`, which do not always match the names in the XML representation, so check the
                    property name rather than copying the attribute name out of a GET response.

                    `nodeId` is skipped so the interface cannot be moved between nodes, and `id`, `dbId`,
                    `authorizedGroups`, `foreignSource`, `foreignId` and `type` are protected and dropped with a
                    log warning. Keys that resolve to nothing writable are ignored, so a request naming only such
                    keys comes back 304.

                    No event is published, so changing `isManaged` here does not by itself tell pollerd.""",
            operationId = "updateNodeIpInterface"
    )
    @RequestBody(
            required = true,
            description = "Form-encoded interface properties to set.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "unmanage", summary = "Mark the interface unmanaged",
                                    value = "isManaged=U"),
                            @ExampleObject(name = "hostname", summary = "Set the reverse name",
                                    value = "hostName=core-sw-01.example.org")
                    })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "At least one field was written. No body."),
            @ApiResponse(responseCode = "304", description = "No key in the body resolved to a writable, unprotected property."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "409", description = "The node has no interface with that address. This path reports the missing interface as a conflict, not a 404.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't find interface with IP address 192.0.2.99 for node 258.")))
    })
    public Response updateIpInterface(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                      @PathParam("nodeCriteria") final String nodeCriteria,
                                      @Parameter(description = "Literal IPv4 or IPv6 address of the interface.", example = "192.0.2.10")
                                      @PathParam("ipAddress") final String ipAddress, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            }
            final OnmsIpInterface ipInterface = node.getIpInterfaceByIpAddress(ipAddress);
            if (ipInterface == null) {
                throw getException(Status.CONFLICT, "Can't find interface with IP address {} for node {}.", ipAddress, nodeCriteria);
            }
            LOG.debug("updateIpInterface: updating ip interface {}", ipInterface);
    
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(ipInterface);
    
            boolean modified = false;
            for(final String key : params.keySet()) {
                // skip nodeId since we already know the node this is associated with and don't want to overwrite it
                if ("nodeId".equals(key)) {
                    continue;
                }
                if (RestUtils.isProtectedProperty(key)) {
                    LOG.warn("Ignoring attempt to set protected property '{}'", key);
                    continue;
                }
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
                    final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                LOG.debug("updateIpInterface: ip interface {} updated", ipInterface);
                m_ipInterfaceDao.saveOrUpdate(ipInterface);
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>deleteIpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{ipAddress}")
    @Operation(
            summary = "Delete one IP interface of a node",
            description = """
                    Publishes `deleteInterface` and returns immediately. The deletion is carried out
                    asynchronously by provisiond, so the interface is normally still readable for a moment after
                    the 202 and the caller has to poll to see the effect.

                    Only the node is checked. Whether the node actually has that interface is not, so a request
                    naming an address the node does not have is accepted and simply has no effect.

                    Removing a node's last IP interface has been observed to take the node with it: provisiond
                    deletes a node left with no interfaces, so a following request for the node returns 404.""",
            operationId = "deleteNodeIpInterface"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "The delete request was published. No body. Completion is not confirmed."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "500", description = "Publishing `deleteInterface` failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot send event uei.opennms.org/internal/capsd/deleteInterface : connection refused")))
    })
    public Response deleteIpInterface(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                      @PathParam("nodeCriteria") final String nodeCriteria,
                                      @Parameter(description = "Literal IPv4 or IPv6 address of the interface.", example = "192.0.2.10")
                                      @PathParam("ipAddress") final String ipAddress) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            }
            LOG.debug("deleteIpInterface: deleting interface {} from node {}", ipAddress, nodeCriteria);

            Event e = EventUtils.createDeleteInterfaceEvent("OpenNMS.REST", node.getId(), ipAddress, -1, -1L);
            sendEvent(e);

            return Response.accepted().build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>getServices</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsMonitoredServiceResource} object.
     */
    @Path("{ipAddress}/services")
    public OnmsMonitoredServiceResource getServices(@Context final ResourceContext context) {
        return context.getResource(OnmsMonitoredServiceResource.class);
    }

    private void sendEvent(EventBuilder eventBuilder) {
        sendEvent(eventBuilder.getEvent());
    }

    private void sendEvent(Event event) {
        try {
            m_eventProxy.send(event);
        } catch (final EventProxyException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}", event.getUei(), e.getMessage());
        }
    }

}
