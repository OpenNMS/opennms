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
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsEntity;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterfaceList;
import org.opennms.netmgt.model.events.EventBuilder;
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

/**
 * <p>OnmsSnmpInterfaceResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@Component("onmsSnmpInterfaceResource")
@Transactional
public class OnmsSnmpInterfaceResource extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OnmsSnmpInterfaceResource.class);


    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private SnmpInterfaceDao m_snmpInterfaceDao;
    
    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;
    
    /**
     * <p>getSnmpInterfaces</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterfaceList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "List a node's SNMP interfaces",
            description = """
                    Returns the SNMP interfaces of one node, one per ifIndex. Interfaces whose collection flag is
                    `D` are excluded and cannot be included.

                    Every query parameter that is not one of the paging parameters below is read as a property
                    restriction on `OnmsSnmpInterface`. A value of `null` or `notnull` becomes an is-null /
                    is-not-null test. Unknown property names are logged and ignored rather than rejected.

                    `collectFlag` and `pollFlag` in the representation are the bean properties `collect` and
                    `poll`; the boolean `collect` and `poll` in the same body are derived read-only views of them.
                    A collection flag ends in `C` to collect and `N` not to, with an optional `U` or `P` prefix
                    recording whether a user or a provisioning policy set it.""",
            operationId = "listNodeSnmpInterfaces",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "limit",
                            description = "Maximum rows to return. 0 disables the limit.",
                            schema = @Schema(type = "integer", defaultValue = "10"), example = "25"),
                    @Parameter(in = ParameterIn.QUERY, name = "offset",
                            description = "Zero-based index of the first row to return.",
                            schema = @Schema(type = "integer"), example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "orderBy",
                            description = "Property to sort on.",
                            schema = @Schema(type = "string"), example = "ifIndex"),
                    @Parameter(in = ParameterIn.QUERY, name = "order",
                            description = "Sort direction for `orderBy`. Anything other than `desc` is read as ascending.",
                            schema = @Schema(type = "string", allowableValues = {"asc", "desc"}), example = "asc"),
                    @Parameter(in = ParameterIn.QUERY, name = "comparator",
                            description = "Comparison applied to every property restriction in the query.",
                            schema = @Schema(type = "string", defaultValue = "eq",
                                    allowableValues = {"eq", "ne", "gt", "lt", "ge", "le", "like", "ilike", "contains", "iplike"}),
                            example = "ilike"),
                    @Parameter(in = ParameterIn.QUERY, name = "match",
                            description = "Whether the property restrictions are combined with AND or OR.",
                            schema = @Schema(type = "string", defaultValue = "all", allowableValues = {"all", "any"}),
                            example = "any"),
                    @Parameter(in = ParameterIn.QUERY, name = "ifName",
                            description = "Restrict by interface name. One of the generic property restrictions.",
                            schema = @Schema(type = "string"), example = "Gi0/8"),
                    @Parameter(in = ParameterIn.QUERY, name = "collect",
                            description = "Restrict by collection flag. Values end in `C` to collect and `N` not to, with an optional `U` or `P` prefix recording whether a user or a provisioning policy set it: `C`, `N`, `UC`, `UN`, `PC`, `PN`. `D` marks a deleted row.",
                            schema = @Schema(type = "string", allowableValues = {"C", "N", "UC", "UN", "PC", "PN", "D"}),
                            example = "C")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's SNMP interfaces.",
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
                          "id": 23602,
                          "hasIngressFlows": false,
                          "hasEgressFlows": false,
                          "ifIndex": 8,
                          "hasFlows": false,
                          "lastIngressFlow": null,
                          "lastEgressFlow": null,
                          "lastCapsdPoll": null,
                          "ifAlias": "uplink to core",
                          "collectionPolicySpecified": false,
                          "ifDescr": "GigabitEthernet0/8",
                          "ifName": "Gi0/8",
                          "physAddr": "02005e108108",
                          "ifType": 6,
                          "ifSpeed": 1000000000,
                          "ifAdminStatus": 1,
                          "ifOperStatus": 1,
                          "lastSnmpPoll": null,
                          "collectionUserSpecified": false,
                          "collectFlag": "C",
                          "pollFlag": "N",
                          "collect": true,
                          "poll": false,
                          "nodeId": 258
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsSnmpInterfaceList.class),
                                    examples = @ExampleObject(value = """
                    <snmpInterfaces count="1" offset="0" totalCount="1">
                      <snmpInterface collectFlag="C" collect="true" id="23602" ifIndex="8" pollFlag="N" poll="false">
                        <ifAdminStatus>1</ifAdminStatus>
                        <ifAlias>uplink to core</ifAlias>
                        <ifDescr>GigabitEthernet0/8</ifDescr>
                        <ifName>Gi0/8</ifName>
                        <ifOperStatus>1</ifOperStatus>
                        <ifSpeed>1000000000</ifSpeed>
                        <ifType>6</ifType>
                        <nodeId>258</nodeId>
                        <physAddr>02005e108108</physAddr>
                      </snmpInterface>
                    </snmpInterfaces>"""))
                    }),
            @ApiResponse(responseCode = "500", description = "No node matches `nodeCriteria`. This path reports a missing node as a null-pointer failure, not as 400 or 404.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"org.opennms.netmgt.model.OnmsNode.getId()\" because \"node\" is null")))
    })
    public OnmsSnmpInterfaceList getSnmpInterfaces(@Context final UriInfo uriInfo,
                                                   @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                                   @PathParam("nodeCriteria") final String nodeCriteria) {
        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        
        final MultivaluedMap<String,String> params = uriInfo.getQueryParameters();
        
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsSnmpInterface.class);
        builder.ne("collect", "D");
        builder.limit(20);
        applyQueryFilters(params, builder);
        builder.eq("node.id", node.getId());
        
        final OnmsSnmpInterfaceList snmpList = new OnmsSnmpInterfaceList(m_snmpInterfaceDao.findMatching(builder.toCriteria()));
        
        snmpList.setTotalCount(m_snmpInterfaceDao.countMatching(builder.count().toCriteria()));

        return snmpList;
    }

    /**
     * <p>getSnmpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @return a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{ifIndex}")
    @Operation(
            summary = "Get one SNMP interface of a node",
            description = """
                    Returns the SNMP interface with the given ifIndex. Unlike the list endpoint, interfaces whose
                    collection flag is `D` are returned here.

                    `ifIndex` must be an integer. A non-numeric value matches no method and comes back as an empty
                    404 rather than a message.""",
            operationId = "getNodeSnmpInterface"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The SNMP interface.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": 23602,
                      "hasIngressFlows": false,
                      "hasEgressFlows": false,
                      "ifIndex": 8,
                      "hasFlows": false,
                      "lastCapsdPoll": null,
                      "ifAlias": "uplink to core",
                      "collectionPolicySpecified": false,
                      "ifDescr": "GigabitEthernet0/8",
                      "ifName": "Gi0/8",
                      "physAddr": "02005e108108",
                      "ifType": 6,
                      "ifSpeed": 1000000000,
                      "ifAdminStatus": 1,
                      "ifOperStatus": 1,
                      "lastSnmpPoll": null,
                      "collectionUserSpecified": false,
                      "collectFlag": "C",
                      "pollFlag": "N",
                      "collect": true,
                      "poll": false,
                      "nodeId": 258
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsSnmpInterface.class),
                                    examples = @ExampleObject(value = """
                    <snmpInterface collectFlag="C" collect="true" id="23602" ifIndex="8" pollFlag="N" poll="false">
                      <ifAdminStatus>1</ifAdminStatus>
                      <ifAlias>uplink to core</ifAlias>
                      <ifDescr>GigabitEthernet0/8</ifDescr>
                      <ifName>Gi0/8</ifName>
                      <ifOperStatus>1</ifOperStatus>
                      <ifSpeed>1000000000</ifSpeed>
                      <ifType>6</ifType>
                      <nodeId>258</nodeId>
                      <physAddr>02005e108108</physAddr>
                    </snmpInterface>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node has no SNMP interface with that ifIndex, or `ifIndex` was not an integer.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "SNMP Interface 99 was not found on node 258.")))
    })
    public OnmsSnmpInterface getSnmpInterface(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                              @PathParam("nodeCriteria") final String nodeCriteria,
                                              @Parameter(description = "SNMP ifIndex of the interface, unique within the node.", example = "8")
                                              @PathParam("ifIndex") final int ifIndex) {
        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        final OnmsSnmpInterface iface = node.getSnmpInterfaceWithIfIndex(ifIndex);
        if (iface == null) {
            throw getException(Status.NOT_FOUND, "SNMP Interface {} was not found on node {}.", Integer.toString(ifIndex), nodeCriteria);
        }
        return iface;
    }
    
    /**
     * <p>addSnmpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param snmpInterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Operation(
            summary = "Add an SNMP interface to a node",
            description = """
                    Attaches a new SNMP interface to the node. No event is published.

                    XML only. A JSON or form-encoded body is rejected with 415.

                    `ifIndex`, `collectFlag` and `pollFlag` are XML *attributes* on `snmpInterface`, while
                    `ifAlias`, `ifDescr`, `ifName`, `ifType`, `ifSpeed`, `ifAdminStatus`, `ifOperStatus` and
                    `physAddr` are *elements*. Sending `collectFlag` as an element is dropped silently and the
                    interface is stored with the database default of `N`.

                    If the node has a primary IP interface (`snmpPrimary` of `P`), that interface is pointed at
                    the new SNMP interface, which gives it the `ifIndex` and nested `snmpInterface` seen in its
                    own representation. The pairing follows the primary flag, not a matching ifIndex.""",
            operationId = "addNodeSnmpInterface"
    )
    @RequestBody(
            required = true,
            description = "The SNMP interface to add. `ifIndex` identifies it within the node.",
            content = @Content(mediaType = MediaType.APPLICATION_XML,
                    schema = @Schema(implementation = OnmsSnmpInterface.class),
                    examples = @ExampleObject(value = """
                    <snmpInterface ifIndex="8" collectFlag="C" pollFlag="N">
                      <ifAdminStatus>1</ifAdminStatus>
                      <ifAlias>uplink to core</ifAlias>
                      <ifDescr>GigabitEthernet0/8</ifDescr>
                      <ifName>Gi0/8</ifName>
                      <ifOperStatus>1</ifOperStatus>
                      <ifSpeed>1000000000</ifSpeed>
                      <ifType>6</ifType>
                      <physAddr>02005e108108</physAddr>
                    </snmpInterface>"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created. `Location` points at the new interface, keyed by ifIndex."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or the body was empty.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found."),
                                    @ExampleObject(name = "noBody", value = "SNMP interface object cannot be null")
                            })),
            @ApiResponse(responseCode = "415", description = "The body was not XML.")
    })
    public Response addSnmpInterface(@Context final UriInfo uriInfo,
                                     @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                     @PathParam("nodeCriteria") final String nodeCriteria, final OnmsSnmpInterface snmpInterface) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            if (snmpInterface == null) throw getException(Status.BAD_REQUEST, "SNMP interface object cannot be null");
            
            LOG.debug("addSnmpInterface: adding interface {}", snmpInterface);
            node.addSnmpInterface(snmpInterface);
            if (snmpInterface.getPrimaryIpInterface() != null) {
                final OnmsIpInterface iface = snmpInterface.getPrimaryIpInterface();
                iface.setSnmpInterface(snmpInterface);
                // TODO Add important events here
            }
            m_snmpInterfaceDao.save(snmpInterface);
            final Integer ifIndex = snmpInterface.getIfIndex();
            return Response.created(getRedirectUri(uriInfo, ifIndex)).build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>deleteSnmpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{ifIndex}")
    @Operation(
            summary = "Delete one SNMP interface of a node",
            description = """
                    Removes the SNMP interface from the node and saves the node. The delete is synchronous, so the
                    interface is gone by the time the 204 comes back, and no event is published.""",
            operationId = "deleteNodeSnmpInterface"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted. No body."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or the node has no SNMP interface with that ifIndex.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found."),
                                    @ExampleObject(name = "unknownIfIndex", value = "Can't find SNMP interface with ifIndex 9 for node 258")
                            }))
    })
    public Response deleteSnmpInterface(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                        @PathParam("nodeCriteria") final String nodeCriteria,
                                        @Parameter(description = "SNMP ifIndex of the interface to delete.", example = "8")
                                        @PathParam("ifIndex") final int ifIndex) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            
            final OnmsEntity snmpInterface = node.getSnmpInterfaceWithIfIndex(ifIndex);
            if (snmpInterface == null) throw getException(Status.BAD_REQUEST, "Can't find SNMP interface with ifIndex {} for node {}", Integer.toString(ifIndex), nodeCriteria);
    
            LOG.debug("deletSnmpInterface: deleting interface with ifIndex {} from node {}", ifIndex, nodeCriteria);
            node.getSnmpInterfaces().remove(snmpInterface);
            m_nodeDao.saveOrUpdate(node);
            // TODO Add important events here
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>updateSnmpInterface</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{ifIndex}")
    @Operation(
            summary = "Update one SNMP interface of a node",
            description = """
                    Applies form-encoded fields to an existing SNMP interface. Keys are bean property names on
                    `OnmsSnmpInterface`, not the names used in the representation. The collection and poll flags
                    are the properties `collect` and `poll`: `collect=C` is written, while `collectFlag=C`
                    resolves to nothing and the request comes back 304.

                    `nodeId`, `ipInterface` and `ipInterfaces` are skipped, and `id`, `dbId`, `authorizedGroups`,
                    `foreignSource`, `foreignId` and `type` are protected and dropped with a log warning.

                    If the body contains a `collect` key, `reinitializePrimarySnmpInterface` is published for the
                    node, whether or not the value changed anything. It is skipped with a log warning when the
                    node has no primary interface to name in the event.""",
            operationId = "updateNodeSnmpInterface"
    )
    @RequestBody(
            required = true,
            description = "Form-encoded SNMP interface properties to set.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "enableCollection", summary = "Turn data collection on for the interface",
                                    value = "collect=C"),
                            @ExampleObject(name = "labels", summary = "Set the interface description fields",
                                    value = "ifAlias=uplink+to+core&ifName=Gi0%2F8")
                    })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "At least one field was written. No body."),
            @ApiResponse(responseCode = "304", description = "No key in the body resolved to a writable, unprotected property. A `collect` key still triggers the reinitialize event in this case."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, `ifIndex` is negative, or the node has no SNMP interface with that ifIndex.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "unknownIfIndex", value = "Can't find SNMP interface with ifIndex 77 for node 258"),
                                    @ExampleObject(name = "negativeIfIndex", value = "Invalid ifIndex specified for SNMP interface on node 258: -1")
                            })),
            @ApiResponse(responseCode = "500", description = "Publishing `reinitializePrimarySnmpInterface` failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Exception occurred sending event uei.opennms.org/nodes/reinitializePrimarySnmpInterface : connection refused")))
    })
    public Response updateSnmpInterface(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                        @PathParam("nodeCriteria") final String nodeCriteria,
                                        @Parameter(description = "SNMP ifIndex of the interface to update.", example = "8")
                                        @PathParam("ifIndex") final int ifIndex, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            if (ifIndex < 0) throw getException(Status.BAD_REQUEST, "Invalid ifIndex specified for SNMP interface on node {}: {}", nodeCriteria, Integer.toString(ifIndex));
    
            final OnmsSnmpInterface snmpInterface = node.getSnmpInterfaceWithIfIndex(ifIndex);
            if (snmpInterface == null) throw getException(Status.BAD_REQUEST, "Can't find SNMP interface with ifIndex {} for node {}", Integer.toString(ifIndex), nodeCriteria);
    
            LOG.debug("updateSnmpInterface: updating SNMP interface {}", snmpInterface);
    
            boolean modified = false;
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(snmpInterface);
            for(final String key : params.keySet()) {
                // don't try setting the node data
                if ("nodeId".equals(key)) continue;

                // don't try setting ipinterface data
                if ("ipInterface".equals(key) || "ipInterfaces".equals(key)) continue;

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
                LOG.debug("updateSnmpInterface: SNMP interface {} updated", snmpInterface);
                m_snmpInterfaceDao.saveOrUpdate(snmpInterface);
            }
            
            Event e = null;
            if (params.containsKey("collect")) { // TODO Is this still valid if the interface was not modified ?
                // we've updated the collection flag so we need to send an event to redo collection
                final EventBuilder bldr = new EventBuilder(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, "ReST");
                bldr.setNode(node);
                // Bug NMS-4432 says that sometimes the primary SNMP interface is null
                // so we need to check for that before we set the interface
                final OnmsIpInterface iface = node.getPrimaryInterface();
                if (iface == null) {
                    LOG.warn("updateSnmpInterface: Cannot send {} event because node {} has no primary SNMP interface", EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI, node.getId());
                } else {
                    bldr.setInterface(iface.getIpAddress());
                    e = bldr.getEvent();
                }
            }
            
            if (e != null) {
                try {
                    m_eventProxy.send(e);
                } catch (final EventProxyException ex) {
                    throw getException(Response.Status.INTERNAL_SERVER_ERROR, "Exception occurred sending event {} : {}", e.getUei(), ex.getMessage());
                }
            }
            return modified ? Response.noContent().build() : Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

}
