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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for OnmsNode entity
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component("nodeRestService")
@Path("nodes")
@Tag(name = "Nodes", description = """
        Nodes API.

        Wherever a path contains `nodeCriteria`, two forms are accepted: the database node id (`258`) and
        `foreignSource:foreignId` (`Router-Requisition:node1`). The colon form is resolved by splitting on the
        first colon, so a foreign source or foreign id containing a colon cannot be addressed this way, and a
        purely numeric value is always read as a database id. Both forms work on every operation below.

        Timestamps serialize differently per representation: JSON carries epoch milliseconds
        (`"createTime": 1787727313802`) while XML carries an ISO-8601 offset date-time
        (`<createTime>2026-08-26T02:55:13.802-04:00</createTime>`). The schemas shown here are derived from the
        Java types and say `date-time` for both. Node `id` is a string in JSON and an attribute in XML.

        Interfaces, services, categories, asset data and hardware inventory hang off `/nodes/{nodeCriteria}/...`
        and are documented under their own operations.""")
public class NodeRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(NodeRestService.class);

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private FilterDao m_filterDao;

    @Autowired
    private CategoryDao m_categoryDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Autowired
    private SessionUtils m_sessionUtils;

    /**
     * <p>getNodes</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNodeList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Search nodes",
            description = """
                    Returns a page of nodes ordered by label. Nodes of type `D` (deleted) are excluded unless a
                    `type` parameter is present, in which case the restriction is left to the caller.

                    Every query parameter that is not one of the paging parameters below is read as a property
                    restriction on `OnmsNode`. Joins are pre-registered for `snmpInterfaces` as `snmpInterface`,
                    `ipInterfaces` as `ipInterface`, `categories` as `category`, `assetRecord`, `location` and
                    `ipInterfaces.monitoredServices.serviceType` as `serviceType`, so restrictions such as
                    `category.name`, `ipInterface.ipAddress`, `assetRecord.city` and `serviceType.name` all
                    resolve. A value of `null` or `notnull` becomes an is-null / is-not-null test. Unknown
                    property names are logged and ignored rather than rejected.

                    Restrict by database id with `id`, not with `nodeId`: `nodeId` is not usable as a criteria
                    property and any request that sends it fails with 500, including the documented
                    comma-separated form.

                    `filterRule` is evaluated separately, by the filter engine, and is mutually exclusive with the
                    property restrictions in the sense that it replaces them: matching node ids are resolved
                    first, then the remaining parameters are re-applied on top of that id set. A rule that matches
                    nothing returns an empty list with `totalCount` 0 rather than an error.""",
            operationId = "searchNodes",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "limit",
                            description = "Maximum rows to return. 0 disables the limit.",
                            schema = @Schema(type = "integer", defaultValue = "10"), example = "25"),
                    @Parameter(in = ParameterIn.QUERY, name = "offset",
                            description = "Zero-based index of the first row to return.",
                            schema = @Schema(type = "integer"), example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "orderBy",
                            description = "Property to sort on, replacing the default `label` ordering.",
                            schema = @Schema(type = "string"), example = "id"),
                    @Parameter(in = ParameterIn.QUERY, name = "order",
                            description = "Sort direction for `orderBy`. Anything other than `desc` is read as ascending.",
                            schema = @Schema(type = "string", allowableValues = {"asc", "desc"}), example = "desc"),
                    @Parameter(in = ParameterIn.QUERY, name = "comparator",
                            description = "Comparison applied to every property restriction in the query.",
                            schema = @Schema(type = "string", defaultValue = "eq",
                                    allowableValues = {"eq", "ne", "gt", "lt", "ge", "le", "like", "ilike", "contains", "iplike"}),
                            example = "contains"),
                    @Parameter(in = ParameterIn.QUERY, name = "match",
                            description = "Whether the property restrictions are combined with AND or OR.",
                            schema = @Schema(type = "string", defaultValue = "all", allowableValues = {"all", "any"}),
                            example = "any"),
                    @Parameter(in = ParameterIn.QUERY, name = "filterRule",
                            description = "Filter-engine expression. Node ids are resolved through the filter engine and the rest of the query is applied to that set.",
                            schema = @Schema(type = "string"), example = "catincRouters"),
                    @Parameter(in = ParameterIn.QUERY, name = "type",
                            description = "Node type. Sending any value, including `A`, also disables the implicit exclusion of deleted nodes.",
                            schema = @Schema(type = "string", allowableValues = {"A", "D"}), example = "A"),
                    @Parameter(in = ParameterIn.QUERY, name = "id",
                            description = "Database node id. Use this rather than `nodeId`.",
                            schema = @Schema(type = "integer"), example = "258"),
                    @Parameter(in = ParameterIn.QUERY, name = "label",
                            description = "Node label. Example of the generic property-restriction form; pair it with `comparator=contains` for a substring search.",
                            schema = @Schema(type = "string"), example = "core-sw-01"),
                    @Parameter(in = ParameterIn.QUERY, name = "foreignSource",
                            description = "Requisition that owns the node.",
                            schema = @Schema(type = "string"), example = "Router-Requisition"),
                    @Parameter(in = ParameterIn.QUERY, name = "category.name",
                            description = "Restrict to nodes in this surveillance category.",
                            schema = @Schema(type = "string"), example = "Routers")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matching nodes. `count` is the size of the page, `totalCount` the size of the whole match.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsNodeList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "node": [
                        {
                          "location": "Default",
                          "foreignSource": "Router-Requisition",
                          "foreignId": "node1",
                          "categories": [
                            { "id": 1, "authorizedGroups": [], "name": "Routers" }
                          ],
                          "sysContact": "noc@example.org",
                          "sysDescription": "Example router",
                          "sysLocation": "Rack 7",
                          "sysName": "core-sw-01",
                          "sysObjectId": ".1.3.6.1.4.1.9.1.1",
                          "labelSource": "U",
                          "createTime": 1787727313802,
                          "lastIngressFlow": null,
                          "lastEgressFlow": null,
                          "lastCapsdPoll": null,
                          "type": "A",
                          "label": "core-sw-01",
                          "nodeParentID": null,
                          "id": "258"
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsNodeList.class),
                                    examples = @ExampleObject(value = """
                    <nodes count="1" offset="0" totalCount="1">
                      <node foreignId="node1" foreignSource="Router-Requisition" label="core-sw-01" id="258" type="A">
                        <createTime>2026-08-26T02:55:13.802-04:00</createTime>
                        <labelSource>U</labelSource>
                        <location>Default</location>
                        <sysContact>noc@example.org</sysContact>
                        <sysDescription>Example router</sysDescription>
                        <sysLocation>Rack 7</sysLocation>
                        <sysName>core-sw-01</sysName>
                        <sysObjectId>.1.3.6.1.4.1.9.1.1</sysObjectId>
                      </node>
                    </nodes>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "`filterRule` could not be parsed. The parse error is deliberately not echoed, because it can contain the generated SQL.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid 'filterRule' in request."))),
            @ApiResponse(responseCode = "500", description = "A query parameter could not be turned into a criteria restriction. `nodeId` does this on every request.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public OnmsNodeList getNodes(@Context final UriInfo uriInfo) {
        final MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        final String type = params.getFirst("type");

        final CriteriaBuilder builder = getCriteriaBuilder(params);
        Criteria crit = null;

        if (params.size() == 1 && params.getFirst("nodeId") != null && params.getFirst("nodeId").contains(",")) {
            // we've been specifically asked for a list of nodes by ID
            final List<Integer> nodeIds = new ArrayList<>();
            for (final String id : params.getFirst("nodeId").split(",")) {
                nodeIds.add(Integer.valueOf(id));
            }
            crit = filterForNodeIds(builder, nodeIds).toCriteria();
        } else if (params.getFirst("filterRule") != null) {
            Set<Integer> filteredNodeIds = null;

            try {
                filteredNodeIds = m_filterDao.getNodeMap(params.getFirst("filterRule")).keySet();
            } catch (FilterParseException fpe) {
                // do not rethrow, the exception may contain the actual SQL query which should not be seen by consumers
                throw getException(Status.BAD_REQUEST, "Invalid 'filterRule' in request.");
            }

            if (filteredNodeIds.size() < 1) {
                // The "in" criteria fails if the list of node ids is empty
                final OnmsNodeList coll = new OnmsNodeList(Collections.emptyList());
                coll.setTotalCount(0);
                return coll;
            }

            // Apply the criteria without the filter rule
            params.remove("filterRule");
            final CriteriaBuilder filterRuleCriteriaBuilder = getCriteriaBuilder(params);
            crit = filterForNodeIds(filterRuleCriteriaBuilder, filteredNodeIds).toCriteria();
        } else {
            applyQueryFilters(params, builder);
            builder.orderBy("label").asc();

            crit = builder.toCriteria();

            if (type == null) {
                final List<Restriction> restrictions = new ArrayList<Restriction>(crit.getRestrictions());
                restrictions.add(Restrictions.ne("type", "D"));
                crit.setRestrictions(restrictions);
            }
        }

        final Criteria criteria = crit;
        return m_sessionUtils.withReadOnlyTransaction(() -> {
            final OnmsNodeList coll = new OnmsNodeList(m_nodeDao.findMatching(criteria));

            criteria.setLimit(null);
            criteria.setOffset(null);
            criteria.setOrders(new ArrayList<Order>());

            coll.setTotalCount(m_nodeDao.countMatching(criteria));

            return coll;
        });
    }

    private static CriteriaBuilder filterForNodeIds(CriteriaBuilder builder, Collection<Integer> nodeIds) {
        return builder.ne("type", "D")
                .in("id", nodeIds)
                .distinct();
    }

    /**
     * <p>getNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{nodeCriteria}")
    @Transactional
    @Operation(
            summary = "Get one node",
            description = """
                    Returns a single node, addressed either by database id or by `foreignSource:foreignId`.
                    Deleted nodes are not filtered out here, unlike in the search endpoint.

                    The node's asset record is embedded in the response, but the search endpoint's paged result
                    embeds it too, so `/nodes/{nodeCriteria}/assetRecord` is only needed when the asset record is
                    all that is wanted.""",
            operationId = "getNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsNode.class),
                                    examples = @ExampleObject(value = """
                    {
                      "location": "Default",
                      "foreignSource": "Router-Requisition",
                      "foreignId": "node1",
                      "assetRecord": {
                        "category": "Unspecified",
                        "id": 23600,
                        "lastModifiedBy": "admin",
                        "lastModifiedDate": 1787727690002
                      },
                      "categories": [
                        { "id": 1, "authorizedGroups": [], "name": "Routers" }
                      ],
                      "sysContact": "noc@example.org",
                      "sysName": "core-sw-01",
                      "sysObjectId": ".1.3.6.1.4.1.9.1.1",
                      "labelSource": "U",
                      "createTime": 1787727313802,
                      "type": "A",
                      "label": "core-sw-01",
                      "nodeParentID": null,
                      "id": "258"
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsNode.class),
                                    examples = @ExampleObject(value = """
                    <node foreignId="node1" foreignSource="Router-Requisition" label="core-sw-01" id="258" type="A">
                      <assetRecord>
                        <category>Unspecified</category>
                        <id>23600</id>
                        <lastModifiedBy>admin</lastModifiedBy>
                        <lastModifiedDate>2026-08-26T02:55:13.802-04:00</lastModifiedDate>
                        <node>258</node>
                      </assetRecord>
                      <createTime>2026-08-26T02:55:13.802-04:00</createTime>
                      <labelSource>U</labelSource>
                      <location>Default</location>
                      <sysContact>noc@example.org</sysContact>
                      <sysName>core-sw-01</sysName>
                      <sysObjectId>.1.3.6.1.4.1.9.1.1</sysObjectId>
                    </node>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found.")))
    })
    public OnmsNode getNode(@Parameter(description = "Node identifier: either the database node id (`258`) or `foreignSource:foreignId` (`Router-Requisition:node1`). Both forms are accepted.",
                                       example = "Router-Requisition:node1")
                            @PathParam("nodeCriteria") final String nodeCriteria) {
        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "Node {} was not found.", nodeCriteria);
        }
        return node;
    }

    /**
     * <p>rescanNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{nodeCriteria}/rescan")
    @Transactional
    @Operation(
            summary = "Request a provisiond rescan of a node",
            description = """
                    Publishes `uei.opennms.org/internal/importer/reloadImport` for the node with
                    `rescanExisting=true` and returns immediately. The scan itself is asynchronous, so a 204 means
                    the request was queued, not that the node has been rescanned.

                    No request body is read. The method declares `@Consumes(application/x-www-form-urlencoded)`,
                    so a `Content-Type` is only needed if a body is sent at all; an empty PUT with no
                    `Content-Type` is accepted.""",
            operationId = "rescanNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The rescan request was published. No body."),
            @ApiResponse(responseCode = "404", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "500", description = "Publishing the rescan event failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot send event uei.opennms.org/internal/importer/reloadImport : connection refused")))
    })
    public Response rescanNode(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                               @PathParam("nodeCriteria") final String nodeCriteria) {
        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "Node {} was not found.", nodeCriteria);
        }
        
        final Event e = EventUtils.createNodeRescanEvent("ReST", node.getId());
        sendEvent(e);
        return Response.noContent().build();
    }

    /**
     * <p>addNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    @Operation(
            summary = "Create a node",
            description = """
                    Creates a node directly in the database and publishes `nodeAdded`. This bypasses provisioning:
                    a node created this way is not owned by a requisition unless `foreignSource` and `foreignId`
                    are supplied, and a later requisition import can delete or duplicate it.

                    XML only. A JSON or form-encoded body is rejected with 415.

                    `type` is required. `location` defaults to the default monitoring location when omitted. An
                    asset record supplied in the body is attached to the node automatically.

                    `label`, `foreignSource`, `foreignId` and `type` are XML *attributes* on `node`, while
                    `labelSource`, `location`, `sysContact`, `sysDescription`, `sysLocation`, `sysName` and
                    `sysObjectId` are *elements*. A value sent as the wrong kind of node is dropped silently.""",
            operationId = "addNode"
    )
    @RequestBody(
            required = true,
            description = "The node to create. `type` is required; `A` for an active node.",
            content = @Content(mediaType = MediaType.APPLICATION_XML,
                    schema = @Schema(implementation = OnmsNode.class),
                    examples = @ExampleObject(value = """
                    <node type="A" label="core-sw-01" foreignSource="Router-Requisition" foreignId="node1">
                      <labelSource>U</labelSource>
                      <sysContact>noc@example.org</sysContact>
                      <sysDescription>Example router</sysDescription>
                      <sysLocation>Rack 7</sysLocation>
                      <sysName>core-sw-01</sysName>
                      <sysObjectId>.1.3.6.1.4.1.9.1.1</sysObjectId>
                    </node>"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created. `Location` points at `/nodes/{id}` with the assigned database id."),
            @ApiResponse(responseCode = "400", description = "`type` was not set.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node type must be set."))),
            @ApiResponse(responseCode = "415", description = "The body was not XML."),
            @ApiResponse(responseCode = "500", description = "Publishing `nodeAdded` failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot send event uei.opennms.org/nodes/nodeAdded : connection refused")))
    })
    public Response addNode(@Context final UriInfo uriInfo, final OnmsNode node) {
        writeLock();
        
        try {
            if (node.getLocation() == null) {
                OnmsMonitoringLocation location = m_locationDao.getDefaultLocation();
                LOG.debug("addNode: Assigning new node to default location: {}", location.getLocationName());
                node.setLocation(location);
            }

            // see NMS-8019
            if (node.getType() == null) {
                throw getException(Status.BAD_REQUEST, "Node type must be set.");
            }

            // see NMS-9855
            if (node.getAssetRecord() != null && node.getAssetRecord().getNode() == null) {
                node.getAssetRecord().setNode(node);
            }

            LOG.debug("addNode: Adding node {}", node);
            m_nodeDao.save(node);
            
            final Event e = EventUtils.createNodeAddedEvent("Web", node.getId(), node.getLabel(), null, null);
            sendEvent(e);
            
            return Response.created(uriInfo.getRequestUriBuilder().path(node.getNodeId()).build()).build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>updateNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{nodeCriteria}")
    @Transactional
    @Operation(
            summary = "Update a node",
            description = """
                    Applies form-encoded fields to the node. Keys are bean property names on `OnmsNode`, and
                    nested paths such as `assetRecord.city` resolve, so this endpoint also reaches the asset
                    record. Keys that resolve to nothing writable are ignored, so a request naming only such keys
                    comes back 304.

                    `id`, `dbId`, `nodeId`, `authorizedGroups`, `foreignSource`, `foreignId` and `type` are
                    protected and dropped with a log warning, including when reached through a nested path. Node
                    identity and requisition ownership therefore cannot be changed here.

                    No event is published, so daemons are not told about the change.

                    A missing node is reported as 400, not 404, unlike `GET /nodes/{nodeCriteria}`.""",
            operationId = "updateNode"
    )
    @RequestBody(
            required = true,
            description = "Form-encoded node properties to set.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "sysFields", summary = "Set the SNMP system fields",
                                    value = "sysLocation=Rack+7&sysContact=noc%40example.org"),
                            @ExampleObject(name = "assetThroughNode", summary = "Set an asset field through the node",
                                    value = "assetRecord.city=Pittsboro&assetRecord.state=NC")
                    })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "At least one field was written. No body."),
            @ApiResponse(responseCode = "304", description = "No key in the body resolved to a writable, unprotected property."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found.")))
    })
    public Response updateNode(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                               @PathParam("nodeCriteria") final String nodeCriteria, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            if (node.getAssetRecord().getGeolocation() == null) {
                node.getAssetRecord().setGeolocation(new OnmsGeolocation());
            }
    
            LOG.debug("updateNode: updating node {}", node);
    
            boolean modified = false;
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(node);
            for(final String key : params.keySet()) {
                if (RestUtils.isProtectedProperty(key)) {
                    LOG.warn("updateNode: ignoring attempt to set protected property '{}'", key);
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
                LOG.debug("updateNode: node {} updated", node);
                m_nodeDao.saveOrUpdate(node);
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>deleteNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{nodeCriteria}")
    @Transactional
    @Operation(
            summary = "Delete a node",
            description = """
                    Publishes `deleteNode` and returns immediately. The deletion is carried out asynchronously by
                    provisiond and takes the node's interfaces, services, asset record, hardware inventory and
                    category assignments with it, so the node is normally still readable for a moment after the
                    202 and the caller has to poll to see the effect.

                    Nothing prevents a requisitioned node from being deleted here. The requisition still lists it,
                    so the next import recreates it.""",
            operationId = "deleteNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "The delete request was published. No body. Completion is not confirmed."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "500", description = "Publishing `deleteNode` failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot send event uei.opennms.org/internal/capsd/deleteNode : connection refused")))
    })
    public Response deleteNode(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                               @PathParam("nodeCriteria") final String nodeCriteria) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
    
            LOG.debug("deleteNode: deleting node {}", nodeCriteria);

            Event e = EventUtils.createDeleteNodeEvent("OpenNMS.REST", node.getId(), -1L);
            sendEvent(e);

            return Response.accepted().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>getIpInterfaceResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsIpInterfaceResource} object.
     */
    @Path("{nodeCriteria}/ipinterfaces")
    @Transactional
    public OnmsIpInterfaceResource getIpInterfaceResource(@Context final ResourceContext context) {
        return context.getResource(OnmsIpInterfaceResource.class);
    }

    /**
     * <p>getSnmpInterfaceResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsSnmpInterfaceResource} object.
     */
    @Path("{nodeCriteria}/snmpinterfaces")
    @Transactional
    public OnmsSnmpInterfaceResource getSnmpInterfaceResource(@Context final ResourceContext context) {
        return context.getResource(OnmsSnmpInterfaceResource.class);
    }

    /**
     * <p>getAssetRecordResource</p>
     *
     * @return a {@link org.opennms.web.rest.AssetRecordResource} object.
     */
    @Path("{nodeCriteria}/assetRecord")
    @Transactional
    public AssetRecordResource getAssetRecordResource(@Context final ResourceContext context) {
        return context.getResource(AssetRecordResource.class);
    }

    /**
     * <p>getHardwareInventoryResource</p>
     *
     * @return a {@link org.opennms.web.rest.HardwareInventoryResource} object.
     */
    @Path("{nodeCriteria}/hardwareInventory")
    @Transactional
    public HardwareInventoryResource getHardwareInventoryResource(@Context final ResourceContext context) {
        return context.getResource(HardwareInventoryResource.class);
    }

    @GET
    @Path("/{nodeCriteria}/categories")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    @Operation(
            summary = "List the categories assigned to a node",
            description = """
                    Returns the categories currently on the node. On a node with no categories the collection comes
                    back with `totalCount` and `count` null rather than 0.

                    `GET /categories/nodes/{nodeCriteria}` is the same operation under the categories tree.""",
            operationId = "listNodeCategories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's categories.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "category": [
                        { "id": 1, "authorizedGroups": [], "name": "Routers" },
                        { "id": 4, "description": "Production estate", "authorizedGroups": ["Admin"], "name": "Production" }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategoryCollection.class),
                                    examples = @ExampleObject(value = """
                    <categories count="2" offset="0" totalCount="2">
                      <category id="1" name="Routers"/>
                      <category id="4" name="Production">
                        <authorizedGroups>Admin</authorizedGroups>
                        <description>Production estate</description>
                      </category>
                    </categories>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found.")))
    })
    public OnmsCategoryCollection getCategoriesForNode(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                                       @PathParam("nodeCriteria") String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        return new OnmsCategoryCollection(node.getCategories());
    }
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{nodeCriteria}/categories/{categoryName}")
    @Transactional
    @Operation(
            summary = "Check whether a node carries a category",
            description = """
                    Returns the category if it is assigned to the node, so this doubles as a membership test. A
                    category that exists but is not on the node is a 404, not a 200.""",
            operationId = "getNodeCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The category is assigned to the node.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsCategory.class),
                                    examples = @ExampleObject(value = """
                    { "id": 1, "description": "Routing devices", "authorizedGroups": [], "name": "Routers" }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsCategory.class),
                                    examples = @ExampleObject(value = """
                    <category id="1" name="Routers">
                      <description>Routing devices</description>
                    </category>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node exists but does not carry that category.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't find category Test for node 258.")))
    })
    public OnmsCategory getCategoryForNode(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                           @PathParam("nodeCriteria") String nodeCriteria,
                                           @Parameter(description = "Category name.", example = "Routers")
                                           @PathParam("categoryName") String categoryName) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        final OnmsCategory cat = getCategory(node, categoryName);
        if (cat == null) {
            throw getException(Status.NOT_FOUND, "Can't find category {} for node {}.", categoryName, nodeCriteria);
        }
        return cat;
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/{nodeCriteria}/categories")
    @Transactional
    @Operation(
            summary = "Assign an existing category to a node, naming it in the body",
            description = """
                    Adds an already-defined category to the node. Only the `name` in the body is used; everything
                    else, `description` included, is ignored, and no category is created here.

                    XML only. A JSON or form-encoded body is rejected with 415. `name` is an XML *attribute* on
                    `category`.

                    Unlike the variant that names the category in the path, the 201 `Location` of this operation
                    is a working URL.""",
            operationId = "addNodeCategoryFromBody"
    )
    @RequestBody(
            required = true,
            description = "Names the category to assign. Only `name` is read.",
            content = @Content(mediaType = MediaType.APPLICATION_XML,
                    schema = @Schema(implementation = OnmsCategory.class),
                    examples = @ExampleObject(value = "<category name=\"Routers\"/>"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The category was added. `Location` points at the category on the node."),
            @ApiResponse(responseCode = "400", description = "No such node, no such category, the category is already on the node, or the body was empty.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "alreadyAssigned", value = "Category 'Routers' already added to node '258'"),
                                    @ExampleObject(name = "unknownCategory", value = "Category NoSuchCat was not found."),
                                    @ExampleObject(name = "noBody", value = "Category must not be null.")
                            })),
            @ApiResponse(responseCode = "415", description = "The body was not XML.")
    })
    public Response addCategoryToNode(@Context final UriInfo uriInfo,
                                      @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                      @PathParam("nodeCriteria") final String nodeCriteria, OnmsCategory category) {
        if (category == null) throw getException(Status.BAD_REQUEST, "Category must not be null.");
        return addCategoryToNode(uriInfo, nodeCriteria,  category.getName());
    }
    
    @POST
    @Path("/{nodeCriteria}/categories/{categoryName}")
    @Transactional
    @Operation(
            summary = "Assign an existing category to a node, naming it in the path",
            description = """
                    Adds an already-defined category to the node. No request body is read and none is required, so
                    this is the simplest of the three ways to assign a category. The category is not created here;
                    an unknown name is a 400.

                    The `Location` header of the 201 is built by appending the category name to the request URI,
                    so it comes back with the name repeated (`/nodes/258/categories/Routers/Routers`) and is not a
                    usable URL.""",
            operationId = "addNodeCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The category was added. `Location` repeats the category name and is not a working URL."),
            @ApiResponse(responseCode = "400", description = "No such node, no such category, or the category is already on the node.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "alreadyAssigned", value = "Category 'Routers' already added to node '258'"),
                                    @ExampleObject(name = "unknownCategory", value = "Category NoSuchCat was not found."),
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found.")
                            }))
    })
    public Response addCategoryToNode(@Context final UriInfo uriInfo,
                                      @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                      @PathParam("nodeCriteria") String nodeCriteria,
                                      @Parameter(description = "Name of an existing category.", example = "Routers")
                                      @PathParam("categoryName") final String categoryName) {
        writeLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            }
            OnmsCategory found = m_categoryDao.findByName(categoryName);
            if (found == null) {
                throw getException(Status.BAD_REQUEST, "Category {} was not found.", categoryName);
            }
            if (!node.getCategories().contains(found)) {
                LOG.debug("addCategory: Adding category {} to node {}", found, nodeCriteria);
                node.addCategory(found);
                m_nodeDao.save(node);
                return Response.created(getRedirectUri(uriInfo, categoryName)).build();
            } else {
                throw getException(Status.BAD_REQUEST, "Category '{}' already added to node '{}'", categoryName, nodeCriteria);
            }
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{nodeCriteria}/categories/{categoryName}")
    @Transactional
    @Operation(
            summary = "Update a category definition, addressed through a node that carries it",
            description = """
                    Applies form-encoded fields to the category itself, not to the node-category assignment. The
                    node in the path only selects which category is meant; the write is global, so every other
                    node in that category sees the change.

                    Keys are bean property names on `OnmsCategory`. `name` is protected here, along with `id`,
                    `dbId`, `nodeId`, `authorizedGroups`, `foreignSource`, `foreignId` and `type`, so the category
                    cannot be renamed through this path. `PUT /categories/{categoryName}` does allow a rename.

                    The response is 204 whether or not anything was written; this operation never returns 304.""",
            operationId = "updateNodeCategory"
    )
    @RequestBody(
            required = true,
            description = "Form-encoded category fields to set.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(name = "description", summary = "Set the category description",
                            value = "description=Routing+devices"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Request accepted. Returned even when no key matched a writable, unprotected property."),
            @ApiResponse(responseCode = "400", description = "No such node, or the node does not carry that category.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "notOnNode", value = "Category Test not found on node 258"),
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found.")
                            }))
    })
    public Response updateCategoryForNode(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                          @PathParam("nodeCriteria") final String nodeCriteria,
                                          @Parameter(description = "Name of a category the node carries.", example = "Routers")
                                          @PathParam("categoryName") final String categoryName, MultivaluedMapImpl params) {
        writeLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            }
            OnmsCategory category = getCategory(node, categoryName);
            if (category == null) {
                throw getException(Status.BAD_REQUEST, "Category {} not found on node {}", categoryName, nodeCriteria);
            }
            LOG.debug("updateCategory: updating category {}", category);
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(category);
            boolean updated = false;
            for(String key : params.keySet()) {
                if (RestUtils.isProtectedProperty(key, Collections.singleton("name"))) {
                    LOG.warn("updateCategoryForNode: ignoring attempt to set protected property '{}'", key);
                    continue;
                }
                if (wrapper.isWritableProperty(key)) {
                    String stringValue = params.getFirst(key);
                    Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                    updated = true;
                }
            }
            if (updated) {
                LOG.debug("updateCategory: category {} updated", category);
                m_categoryDao.saveOrUpdate(category);
            } else {
                LOG.debug("updateCategory: no fields updated in category {}", category);
            }
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("/{nodeCriteria}/categories/{categoryName}")
    @Transactional
    @Operation(
            summary = "Remove a category from a node",
            description = """
                    Detaches the category from the node. The category definition itself is left alone, so it stays
                    on every other node that carries it.""",
            operationId = "removeNodeCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The category is no longer on the node. No body."),
            @ApiResponse(responseCode = "400", description = "No such node, or the node does not carry that category.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "notOnNode", value = "Category Routers not found on node 258"),
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found.")
                            }))
    })
    public Response removeCategoryFromNode(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                           @PathParam("nodeCriteria") String nodeCriteria,
                                           @Parameter(description = "Name of a category the node carries.", example = "Routers")
                                           @PathParam("categoryName") String categoryName) {
        writeLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            }
            OnmsCategory category = getCategory(node, categoryName);
            if (category == null) {
                throw getException(Status.BAD_REQUEST, "Category {} not found on node {}", categoryName, nodeCriteria);
            }
            LOG.debug("deleteCaegory: deleting category {} from node {}", categoryName, nodeCriteria);
            node.getCategories().remove(category);
            m_nodeDao.saveOrUpdate(node);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    private static OnmsCategory getCategory(OnmsNode node, String categoryName) {
        for (OnmsCategory category : node.getCategories()) {
            if (category.getName().equals(categoryName)) {
                return category;
            }
        }
        return null;
    }

    private void sendEvent(Event event) {
        try {
            m_eventProxy.send(event);
        } catch (final EventProxyException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}", event.getUei(), e.getMessage());
        }
    }

    private static CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.alias("snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("categories", "category", JoinType.LEFT_JOIN);
        builder.alias("assetRecord", "assetRecord", JoinType.LEFT_JOIN);
        builder.alias("location", "location", JoinType.LEFT_JOIN);
        builder.alias("ipInterfaces.monitoredServices.serviceType", "serviceType", JoinType.LEFT_JOIN);

        applyQueryFilters(params, builder);
        return builder;
    }

}
