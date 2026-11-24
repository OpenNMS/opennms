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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsHwEntityAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HardwareInventoryResource.
 *
 *  Retrieve the root entity (all hardware inventory)
 *  GET /nodes/{nodeId}/hardwareInventory
 *
 *  Override the root entity (all hardware inventory)
 *  POST /nodes/{nodeId}/hardwareInventory
 *
 *  Retrieve a specific entity
 *  GET /nodes/{nodeId}/hardwareInventory/{entPhysicalIndex}
 *
 *  Delete a specific entity
 *  DELETE /nodes/{nodeId}/hardwareInventory/{entPhysicalIndex}
 *
 *  Modify an existing entity
 *  PUT /nodes/{nodeId}/hardwareInventory/{entPhysicalIndex}
 *
 *  Add a child entity
 *  POST /nodes/{nodeId}/hardwareInventory/{entPhysicalIndex}
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component("hardwareInventoryResource")
@Path("hardwareInventory")
@Tag(name = "HardwareInventory", description = """
        Hardware Inventory API.

        A node's hardware inventory is the ENTITY-MIB physical tree: one root entity per node, each entity
        addressed by its `entPhysicalIndex`, with vendor-specific values on each entity as `vendorAttributes`.
        The node-scoped path is `/nodes/{nodeCriteria}/hardwareInventory`.

        The document also lists all six operations at paths with no node in them. Those paths cannot supply the
        required `nodeCriteria`, and every such request returns HTTP 500.

        In the request and response bodies `entPhysicalIndex`, `parentPhysicalIndex`, `entityId` and `nodeId` are
        XML *attributes*, while `entPhysicalDescr` and the other `entPhysical*` values are XML *elements*. An
        `entPhysical*` value sent as the wrong kind of node is dropped silently. `nodeId` in the body is
        overwritten with the node from the path.""")
@Transactional
public class HardwareInventoryResource extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(HardwareInventoryResource.class);

    /** The node DAO. */
    @Autowired
    private NodeDao m_nodeDao;

    /** The hardware entity DAO. */
    @Autowired
    private HwEntityDao m_hwEntityDao;

    /** The hardware entity attribute type DAO. */
    @Autowired
    private HwEntityAttributeTypeDao m_hwEntityAttribTypeDao;

    /**
     * Gets the hardware inventory.
     *
     * @param nodeCriteria the node criteria
     * @return the root hardware entity
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Get a node's whole hardware inventory",
            description = """
                    Returns the root hardware entity for the node with its descendants nested under `children`.
                    A node that has never been scanned for ENTITY-MIB data, or whose inventory has been deleted,
                    has no root entity and returns 404.""",
            operationId = "getNodeHardwareInventory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The root entity and its subtree.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsHwEntity.class),
                                    examples = @ExampleObject(value = """
                    {
                      "entPhysicalName": "Chassis",
                      "entPhysicalIndex": 1,
                      "entPhysicalDescr": "Example 9000 chassis",
                      "entPhysicalClass": "chassis",
                      "entPhysicalSerialNum": "SN0001",
                      "entPhysicalModelName": "X-9000",
                      "entPhysicalIsFRU": false,
                      "children": [
                        {
                          "entPhysicalName": "Slot 1",
                          "entPhysicalIndex": 2,
                          "entPhysicalDescr": "Example line card",
                          "entPhysicalClass": "module",
                          "entPhysicalSerialNum": "SN0002",
                          "entPhysicalIsFRU": true,
                          "children": [],
                          "parentPhysicalIndex": 1,
                          "hwEntityAliases": [],
                          "entityId": "23658",
                          "vendorAttributes": [],
                          "nodeId": 258
                        }
                      ],
                      "parentPhysicalIndex": null,
                      "hwEntityAliases": [],
                      "entityId": "23657",
                      "vendorAttributes": [
                        { "value": "1", "name": "entPhysicalFanState", "class": "integer", "oid": ".1.3.6.1.4.1.9.9.13.1.4.1.3" }
                      ],
                      "nodeId": 258
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsHwEntity.class),
                                    examples = @ExampleObject(value = """
                    <hwEntity entPhysicalIndex="1" nodeId="258" entityId="23657">
                      <children>
                        <hwEntity entPhysicalIndex="2" nodeId="258" entityId="23658" parentPhysicalIndex="1">
                          <children/>
                          <entPhysicalClass>module</entPhysicalClass>
                          <entPhysicalDescr>Example line card</entPhysicalDescr>
                          <entPhysicalIsFRU>true</entPhysicalIsFRU>
                          <entPhysicalName>Slot 1</entPhysicalName>
                          <vendorAttributes/>
                        </hwEntity>
                      </children>
                      <entPhysicalClass>chassis</entPhysicalClass>
                      <entPhysicalDescr>Example 9000 chassis</entPhysicalDescr>
                      <entPhysicalModelName>X-9000</entPhysicalModelName>
                      <entPhysicalName>Chassis</entPhysicalName>
                      <vendorAttributes>
                        <hwEntityAttribute class="integer" name="entPhysicalFanState" oid=".1.3.6.1.4.1.9.9.13.1.4.1.3" value="1"/>
                      </vendorAttributes>
                    </hwEntity>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node has no hardware inventory.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't find root hardware entity for node 258."))),
            @ApiResponse(responseCode = "500", description = "Returned unconditionally when the operation is reached with no node in the path.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"String.contains(java.lang.CharSequence)\" because \"lookupCriteria\" is null")))
    })
    public OnmsHwEntity getHardwareInventory(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                             @PathParam("nodeCriteria") String nodeCriteria) {
        final OnmsNode node = getOnmsNode(nodeCriteria);
        final OnmsHwEntity entity = m_hwEntityDao.findRootByNodeId(node.getId());
        if (entity == null) {
            throw getException(Status.NOT_FOUND, "Can't find root hardware entity for node {}.", nodeCriteria);
        }
        return entity;
    }

    /**
     * Gets the hardware entity by index.
     *
     * @param nodeCriteria the node criteria
     * @param entPhysicalIndex the entity physical index
     * @return the hardware entity
     */
    @GET
    @Path("{entPhysicalIndex}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Get one hardware entity by its physical index",
            description = """
                    Returns a single entity from the node's inventory, with its own subtree nested under
                    `children`. The index is the ENTITY-MIB `entPhysicalIndex`, unique per node, not a database
                    key.""",
            operationId = "getNodeHardwareEntity"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The entity and its subtree.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsHwEntity.class),
                                    examples = @ExampleObject(value = """
                    {
                      "entPhysicalName": "Slot 1",
                      "entPhysicalIndex": 2,
                      "entPhysicalDescr": "Example line card",
                      "entPhysicalClass": "module",
                      "entPhysicalSerialNum": "SN0002",
                      "entPhysicalIsFRU": true,
                      "children": [],
                      "parentPhysicalIndex": 1,
                      "hwEntityAliases": [],
                      "entityId": "23658",
                      "vendorAttributes": [],
                      "nodeId": 258
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsHwEntity.class),
                                    examples = @ExampleObject(value = """
                    <hwEntity entPhysicalIndex="2" nodeId="258" entityId="23658" parentPhysicalIndex="1">
                      <children/>
                      <entPhysicalClass>module</entPhysicalClass>
                      <entPhysicalDescr>Example line card</entPhysicalDescr>
                      <entPhysicalIsFRU>true</entPhysicalIsFRU>
                      <entPhysicalName>Slot 1</entPhysicalName>
                      <vendorAttributes/>
                    </hwEntity>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node has no entity with that index.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't find entity with index 99 on node 258."))),
            @ApiResponse(responseCode = "500", description = "Returned unconditionally when the operation is reached with no node in the path.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"String.contains(java.lang.CharSequence)\" because \"lookupCriteria\" is null")))
    })
    public OnmsHwEntity getHwEntityByIndex(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                           @PathParam("nodeCriteria") String nodeCriteria,
                                           @Parameter(description = "ENTITY-MIB entPhysicalIndex of the entity, unique within the node.", example = "2")
                                           @PathParam("entPhysicalIndex") Integer entPhysicalIndex) {
        final OnmsNode node = getOnmsNode(nodeCriteria);
        return getHwEntity(node.getId(), entPhysicalIndex);
    }

    /**
     * Sets the hardware inventory (root object)
     *
     * @param nodeCriteria the node criteria
     * @param entity the root entity object
     * @return the response
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Replace a node's whole hardware inventory",
            description = """
                    Stores the posted entity as the node's root entity, together with everything nested under
                    `children`. Any inventory the node already had is deleted first, so this replaces rather than
                    merges.

                    Attribute types named in `vendorAttributes` are created on demand from the `name`, `oid` and
                    `class` given, and are keyed by name, so an existing type keeps its stored OID and class.
                    A new type whose `oid` is not a dotted-numeric string is rejected with 400.

                    The "not a root entity" 400 is unreachable: the check passes whenever the parent reference is
                    unset, and a request body cannot set it.""",
            operationId = "setNodeHardwareInventory"
    )
    @RequestBody(
            required = true,
            description = "The root entity, with any descendants nested under `children`.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsHwEntity.class),
                            examples = @ExampleObject(value = """
                    <hwEntity entPhysicalIndex="1">
                      <entPhysicalClass>chassis</entPhysicalClass>
                      <entPhysicalDescr>Example 9000 chassis</entPhysicalDescr>
                      <entPhysicalModelName>X-9000</entPhysicalModelName>
                      <entPhysicalName>Chassis</entPhysicalName>
                      <entPhysicalSerialNum>SN0001</entPhysicalSerialNum>
                      <entPhysicalIsFRU>false</entPhysicalIsFRU>
                      <vendorAttributes>
                        <hwEntityAttribute class="integer" name="entPhysicalFanState" oid=".1.3.6.1.4.1.9.9.13.1.4.1.3" value="1"/>
                      </vendorAttributes>
                      <children>
                        <hwEntity entPhysicalIndex="2">
                          <entPhysicalClass>module</entPhysicalClass>
                          <entPhysicalDescr>Example line card</entPhysicalDescr>
                          <entPhysicalName>Slot 1</entPhysicalName>
                          <entPhysicalSerialNum>SN0002</entPhysicalSerialNum>
                          <entPhysicalIsFRU>true</entPhysicalIsFRU>
                        </hwEntity>
                      </children>
                    </hwEntity>""")),
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsHwEntity.class),
                            examples = @ExampleObject(value = """
                    {
                      "entPhysicalIndex": 1,
                      "entPhysicalClass": "chassis",
                      "entPhysicalDescr": "Example 9000 chassis",
                      "entPhysicalModelName": "X-9000",
                      "entPhysicalName": "Chassis",
                      "entPhysicalSerialNum": "SN0001",
                      "entPhysicalIsFRU": false,
                      "vendorAttributes": [
                        { "name": "entPhysicalFanState", "oid": ".1.3.6.1.4.1.9.9.13.1.4.1.3", "class": "integer", "value": "1" }
                      ],
                      "children": [
                        {
                          "entPhysicalIndex": 2,
                          "entPhysicalClass": "module",
                          "entPhysicalDescr": "Example line card",
                          "entPhysicalName": "Slot 1",
                          "entPhysicalSerialNum": "SN0002",
                          "entPhysicalIsFRU": true
                        }
                      ]
                    }"""))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The inventory was stored. No body."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or a new attribute type carries a non-numeric OID.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found."),
                                    @ExampleObject(name = "badOid", value = "OID {not-an-oid} provided in entity is not valid.")
                            })),
            @ApiResponse(responseCode = "415", description = "The body was form-encoded. Send XML or JSON."),
            @ApiResponse(responseCode = "500", description = "Returned unconditionally when the operation is reached with no node in the path.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"String.contains(java.lang.CharSequence)\" because \"lookupCriteria\" is null")))
    })
    public Response setHardwareInventory(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                         @PathParam("nodeCriteria") String nodeCriteria, OnmsHwEntity entity) {
        if (!entity.isRoot()) {
            throw getException(Status.BAD_REQUEST, "The hardware entity is not a root entity {}.", entity.toString());
        }
        writeLock();
        try {
            final OnmsNode node = getOnmsNode(nodeCriteria);
            fixEntity(node, entity);

            final OnmsHwEntity existing = m_hwEntityDao.findRootByNodeId(node.getId());
            if (existing != null && !entity.equals(existing)) {
                LOG.debug("setHardwareInventory: removing existing hardware inventory from node {} ", nodeCriteria);
                m_hwEntityDao.delete(existing);
                m_hwEntityDao.flush();
            }
            m_hwEntityDao.save(entity);

            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Adds or replaces a child entity.
     *
     * @param nodeCriteria the node criteria
     * @param parentEntPhysicalIndex the parent entity physical index
     * @param child the child
     * @return the response
     */
    @POST
    @Path("{parentEntPhysicalIndex}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Add or replace one child entity",
            description = """
                    Attaches the posted entity, and anything nested under its `children`, beneath the entity whose
                    index is in the path. If the parent already has a child with the same `entPhysicalIndex`, that
                    child and its subtree are removed first, so posting twice replaces rather than duplicates.

                    Attribute types are created on demand exactly as for the root POST.""",
            operationId = "addNodeHardwareChildEntity"
    )
    @RequestBody(
            required = true,
            description = "The child entity to attach. `entPhysicalIndex` identifies it within the node.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsHwEntity.class),
                            examples = @ExampleObject(value = """
                    <hwEntity entPhysicalIndex="3">
                      <entPhysicalClass>port</entPhysicalClass>
                      <entPhysicalDescr>Example gigabit port</entPhysicalDescr>
                      <entPhysicalName>Gi0/1</entPhysicalName>
                      <entPhysicalIsFRU>false</entPhysicalIsFRU>
                    </hwEntity>""")),
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsHwEntity.class),
                            examples = @ExampleObject(value = """
                    {
                      "entPhysicalIndex": 3,
                      "entPhysicalClass": "port",
                      "entPhysicalDescr": "Example gigabit port",
                      "entPhysicalName": "Gi0/1",
                      "entPhysicalIsFRU": false
                    }"""))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The child was attached, replacing any previous child with the same index. No body."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or a new attribute type carries a non-numeric OID.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node has no entity with the parent index.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't find entity with index 77 on node 258."))),
            @ApiResponse(responseCode = "415", description = "The body was form-encoded. Send XML or JSON."),
            @ApiResponse(responseCode = "500", description = "Returned unconditionally when the operation is reached with no node in the path.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"String.contains(java.lang.CharSequence)\" because \"lookupCriteria\" is null")))
    })
    public Response addOrReplaceChild(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                      @PathParam("nodeCriteria") String nodeCriteria,
                                      @Parameter(description = "entPhysicalIndex of the entity the child is attached to.", example = "1")
                                      @PathParam("parentEntPhysicalIndex") Integer parentEntPhysicalIndex, OnmsHwEntity child) {
        writeLock();
        try {
            final OnmsNode node = getOnmsNode(nodeCriteria);
            fixEntity(node, child);

            final OnmsHwEntity parent = getHwEntity(node.getId(), parentEntPhysicalIndex);
            OnmsHwEntity currentChild = parent.getChildByIndex(child.getEntPhysicalIndex());
            if (currentChild != null) {
                LOG.debug("addOrReplaceChild: removing entity {}", currentChild);
                parent.removeChild(currentChild);
            }
            parent.addChildEntity(child);
            LOG.debug("addOrReplaceChild: updating entity {}", child);
            m_hwEntityDao.save(parent);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Update hardware entity.
     *
     * @param nodeCriteria the node criteria
     * @param entPhysicalIndex the entity physical index
     * @param params the parameters
     * @return the response
     */
    @PUT
    @Path("{entPhysicalIndex}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update one hardware entity",
            description = """
                    Applies form-encoded fields to a single entity. Keys are routed by their prefix: a key that
                    starts with `entPhysical` is set as a bean property on the entity, and any other key is
                    matched against the names of the entity's existing `vendorAttributes` and sets that
                    attribute's value. A vendor attribute the entity does not already carry cannot be added here.

                    A vendor attribute whose type name itself begins with `entPhysical` is unreachable, because
                    the key is taken as a bean property. `entPhysicalMfgDate` is a `Date` property with no
                    registered string converter, so sending it fails with 500.

                    The modified flag starts out true, so a request whose keys match nothing still returns 204 and
                    rewrites the entity unchanged. It never returns 304.""",
            operationId = "updateNodeHardwareEntity"
    )
    @RequestBody(
            required = true,
            description = "Form-encoded entity properties and vendor attribute values.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "entityProperties", summary = "Set properties on the entity itself",
                                    value = "entPhysicalName=Slot+1&entPhysicalSerialNum=SN0002-B&entPhysicalAlias=uplink+card"),
                            @ExampleObject(name = "vendorAttribute", summary = "Set an existing vendor attribute by its type name",
                                    value = "entPhysicalFanState=2")
                    })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The entity was saved. Returned even when no key matched anything."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node has no entity with that index.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't find entity with index 77 on node 258."))),
            @ApiResponse(responseCode = "500", description = "A value could not be converted to the property's type, or the operation was reached with no node in the path.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "dateProperty", value = "Failed to convert value of type 'java.lang.String' to required type 'java.util.Date'; nested exception is java.lang.IllegalStateException: Cannot convert value of type 'java.lang.String' to required type 'java.util.Date': no matching editors or conversion strategy found"),
                                    @ExampleObject(name = "noNodeInPath", value = "Cannot invoke \"String.contains(java.lang.CharSequence)\" because \"lookupCriteria\" is null")
                            }))
    })
    public Response updateHwEntity(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                   @PathParam("nodeCriteria") String nodeCriteria,
                                   @Parameter(description = "entPhysicalIndex of the entity to update.", example = "2")
                                   @PathParam("entPhysicalIndex") Integer entPhysicalIndex, MultivaluedMapImpl params) {
        writeLock();
        try {
            final OnmsNode node = getOnmsNode(nodeCriteria);
            final OnmsHwEntity entity = getHwEntity(node.getId(), entPhysicalIndex);

            boolean modified = true;
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
            for(String key : params.keySet()) {
                if (key.startsWith("entPhysical")) {
                    if (wrapper.isWritableProperty(key)) {
                        String stringValue = params.getFirst(key);
                        Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                        wrapper.setPropertyValue(key, value);
                        modified = true;
                    }
                } else {
                    OnmsHwEntityAttribute attr = entity.getAttribute(key);
                    if (attr != null) {
                        attr.setValue(params.getFirst(key));
                        modified = true;
                    }
                }
            }
            if (modified) {
                m_hwEntityDao.save(entity);
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Delete hardware entity.
     *
     * @param nodeCriteria the node criteria
     * @param entPhysicalIndex the entity physical index
     * @return the response
     */
    @DELETE
    @Path("{entPhysicalIndex}")
    @Operation(
            summary = "Delete one hardware entity",
            description = """
                    Deletes the entity with the given index. Deleting the root entity clears the node's whole
                    inventory.

                    Deleting a non-root entity has been observed to remove the entire tree, root included, rather
                    than just that entity and its descendants.""",
            operationId = "deleteNodeHardwareEntity"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted. No body."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Node 999999 was not found."))),
            @ApiResponse(responseCode = "404", description = "The node has no entity with that index.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can't find entity with index 2 on node 258."))),
            @ApiResponse(responseCode = "500", description = "Returned unconditionally when the operation is reached with no node in the path.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"String.contains(java.lang.CharSequence)\" because \"lookupCriteria\" is null")))
    })
    public Response deleteHwEntity(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                   @PathParam("nodeCriteria") final String nodeCriteria,
                                   @Parameter(description = "entPhysicalIndex of the entity to delete. Index 1 is normally the root.", example = "2")
                                   @PathParam("entPhysicalIndex") Integer entPhysicalIndex) {
        writeLock();
        try {
            final OnmsNode node = getOnmsNode(nodeCriteria);
            final OnmsHwEntity entity = getHwEntity(node.getId(), entPhysicalIndex);
            m_hwEntityDao.delete(entity);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * Gets the node.
     *
     * @param nodeCriteria the node criteria
     * @return the node
     */
    private OnmsNode getOnmsNode(String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        return node;
    }

    /**
     * Gets the hardware entity.
     *
     * @param nodeId the node id
     * @param entPhysicalIndex the entity physical index
     * @return the hardware entity
     */
    private OnmsHwEntity getHwEntity(Integer nodeId, Integer entPhysicalIndex) {
        OnmsHwEntity entity = m_hwEntityDao.findEntityByIndex(nodeId, entPhysicalIndex);
        if (entity == null) {
            throw getException(Status.NOT_FOUND, "Can't find entity with index {} on node {}.", Integer.toString(entPhysicalIndex), Integer.toString(nodeId));
        }
        return entity;
    }

    /**
     * Fix entity.
     *
     * @param node the node
     * @param entity the entity
     */
    private void fixEntity(OnmsNode node, OnmsHwEntity entity) {
        entity.setNode(node);
        entity.fixRelationships();

        Map<String,HwEntityAttributeType> typesMap = new HashMap<String, HwEntityAttributeType>();
        updateTypes(typesMap, entity);
        m_hwEntityAttribTypeDao.flush();
    }

    /**
     * Update types.
     *
     * @param typesMap the types map
     * @param entity the entity
     */
    private void updateTypes(Map<String, HwEntityAttributeType> typesMap, OnmsHwEntity entity) {
        for (OnmsHwEntityAttribute a : entity.getHwEntityAttributes()) {
            final String typeName = a.getTypeName();
            if (!typesMap.containsKey(typeName)) {
                HwEntityAttributeType t = m_hwEntityAttribTypeDao.findTypeByName(typeName);
                if (t == null) {
                    t = a.getType();
                    if(!isValidOid(t.getOid())){
                        throw getException(Status.BAD_REQUEST, "OID {" +  t.getOid()  + "} provided in entity is not valid.");
                    }
                    m_hwEntityAttribTypeDao.save(t);
                }
                typesMap.put(t.getName(), t);
            }
            a.setType(typesMap.get(typeName));
        }
        for (OnmsHwEntity child : entity.getChildren()) {
            updateTypes(typesMap, child);
        }
    }

    /**
     * @param oId
     * @return boolean value
     */
    private boolean isValidOid(String oId){
        return Pattern.compile("^[\\.0-9]*$").matcher(oId).matches();
    }

}
