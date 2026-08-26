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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.api.ISO8601DateEditor;
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

import com.google.common.base.Strings;

@Component("assetRecordResource")
@Path("assetRecord")
@Tag(name = "Asset Records", description = """
        Asset Records API.

        An asset record holds the inventory and location metadata attached to one node, and is created together
        with the node. Reach it through the node: `/nodes/{nodeCriteria}/assetRecord`.

        The class also carries its own `@Path("assetRecord")`, so the generated document additionally lists
        `/assetRecord` with no node in the path. Both handlers need a `nodeCriteria` path parameter that the
        bare path cannot supply, so `GET /assetRecord` and `PUT /assetRecord` fail with HTTP 500 on every
        request. Use the node-scoped paths.""")
@Transactional
public class AssetRecordResource extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(AssetRecordResource.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private AssetRecordDao m_assetRecordDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    /**
     * <p>getAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAssetRecord} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Get a node's asset record",
            description = """
                    Returns the asset record of one node. Every node has exactly one, created with the node, so a
                    node that has never been edited still returns a record with `category` set to `Unspecified`
                    and the remaining fields null.

                    `latitude` and `longitude` are read from the record's geolocation and appear alongside the
                    other fields rather than nested. `dateInstalled`, `lease`, `leaseExpires` and
                    `maintContractExpiration` are free-text strings, not dates. `lastModifiedDate` is a real
                    timestamp and serializes as epoch milliseconds in JSON and as an ISO-8601 offset date-time in
                    XML.

                    Reached without a node in the path (`GET /assetRecord`) this fails with HTTP 500.""",
            operationId = "getNodeAssetRecord"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node's asset record. Fields never set are null and are still present in the JSON body.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsAssetRecord.class),
                                    examples = @ExampleObject(value = """
                    {
                      "category": "Unspecified",
                      "id": 23600,
                      "description": "Top-of-rack switch",
                      "assetNumber": "ASSET-0001",
                      "rack": "R7",
                      "building": "HQ",
                      "floor": "2",
                      "room": "210",
                      "city": "Pittsboro",
                      "state": "NC",
                      "region": "Southeast",
                      "latitude": 35.7196,
                      "longitude": -79.1778,
                      "dateInstalled": "2026-01-15",
                      "comment": "Installed during the spring refresh",
                      "lastModifiedBy": "admin",
                      "lastModifiedDate": 1787727690002
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsAssetRecord.class),
                                    examples = @ExampleObject(value = """
                    <assetRecord>
                      <category>Unspecified</category>
                      <city>Pittsboro</city>
                      <id>23600</id>
                      <lastModifiedBy>admin</lastModifiedBy>
                      <lastModifiedDate>2026-08-26T02:55:13.802-04:00</lastModifiedDate>
                      <latitude>35.7196</latitude>
                      <longitude>-79.1778</longitude>
                      <node>258</node>
                      <state>NC</state>
                    </assetRecord>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "getAssetRecord: Can't find node 999999"))),
            @ApiResponse(responseCode = "500", description = "Returned unconditionally when the operation is called as `GET /assetRecord`, with no node in the path.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"String.contains(java.lang.CharSequence)\" because \"lookupCriteria\" is null")))
    })
    public OnmsAssetRecord getAssetRecord(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.",
                                                    example = "Router-Requisition:node1")
                                          @PathParam("nodeCriteria") final String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "getAssetRecord: Can't find node " + nodeCriteria);
        }
        return getAssetRecord(node);
    }
    /**
     * <p>updateAssetRecord</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update a node's asset record",
            description = """
                    Applies form-encoded fields to the node's asset record. Each key is a bean property name on
                    `OnmsAssetRecord`; keys that do not resolve to a writable property are ignored rather than
                    rejected, so a request that names only unknown fields comes back 304.

                    `id`, `dbId`, `nodeId`, `authorizedGroups`, `foreignSource`, `foreignId` and `type` are
                    protected and are dropped with a warning in the log.

                    Nested geolocation fields are reachable directly (`latitude`, `longitude`, `city`, `state`,
                    `zip`, `country`, `address1`, `address2`); the geolocation object is created on demand if the
                    record has none. `Date`-typed properties are parsed with an ISO-8601 editor, but the visible
                    installation and lease fields are plain strings and are stored verbatim.

                    A successful write sets `lastModifiedBy` to the authenticated user and `lastModifiedDate` to
                    now, then publishes `assetInfoChanged`.

                    Reached without a node in the path (`PUT /assetRecord`) this fails with HTTP 500.""",
            operationId = "updateNodeAssetRecord"
    )
    @RequestBody(
            required = true,
            description = "Form-encoded asset fields to set. Only the keys present are touched.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "location", summary = "Set physical location and coordinates",
                                    value = "city=Pittsboro&state=NC&region=Southeast&building=HQ&floor=2&room=210&rack=R7&latitude=35.7196&longitude=-79.1778"),
                            @ExampleObject(name = "inventory", summary = "Set inventory identifiers",
                                    value = "assetNumber=ASSET-0001&serialNumber=SN12345&manufacturer=Example+Corp&modelNumber=X-9000&dateInstalled=2026-01-15")
                    })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "At least one field was written. No body."),
            @ApiResponse(responseCode = "304", description = "No key in the body resolved to a writable, unprotected property, so nothing was written."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "updateAssetRecord: Can't find node 999999"))),
            @ApiResponse(responseCode = "500", description = "Publishing `assetInfoChanged` failed, or the operation was called as `PUT /assetRecord` with no node in the path.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"String.contains(java.lang.CharSequence)\" because \"lookupCriteria\" is null")))
    })
    public Response updateAssetRecord(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.",
                                                 example = "Router-Requisition:node1")
                                      @PathParam("nodeCriteria") final String nodeCriteria
            , @Context final HttpServletRequest request
            , final MultivaluedMapImpl params) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "updateAssetRecord: Can't find node " + nodeCriteria);
        }

        OnmsAssetRecord assetRecord = getAssetRecord(node);
        if (assetRecord == null) {
            throw getException(Status.BAD_REQUEST, "updateAssetRecord: Node " + node  + " could not update ");
        }
        if (assetRecord.getGeolocation() == null) {
            assetRecord.setGeolocation(new OnmsGeolocation());
        }
        LOG.debug("updateAssetRecord: updating asset {}", assetRecord);
        boolean modified = false;
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(assetRecord);
        wrapper.registerCustomEditor(Date.class, new ISO8601DateEditor());
        for(String key : params.keySet()) {
            if (RestUtils.isProtectedProperty(key)) {
                LOG.warn("updateAssetRecord: ignoring attempt to set protected property '{}'", key);
                continue;
            }
            if (wrapper.isWritableProperty(key)) {
                String stringValue = params.getFirst(key);
                Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                wrapper.setPropertyValue(key, value);
                modified = true;
            }
        }
        if (modified) {
            LOG.debug("updateAssetRecord: assetRecord {} updated", assetRecord);
            assetRecord.setLastModifiedBy(Strings.nullToEmpty(request.getRemoteUser()));
            assetRecord.setLastModifiedDate(new Date());
            node.setAssetRecord(assetRecord);
            m_nodeDao.saveOrUpdate(node);
            try {
                sendEvent(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI, node.getId());
            } catch (EventProxyException e) {
                throw getException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
            }
            return Response.noContent().build();
        }

        return Response.notModified().build();
    }

    private static OnmsAssetRecord getAssetRecord(OnmsNode node) {
        return node.getAssetRecord();
    }
    
    private void sendEvent(String uei, int nodeId) throws EventProxyException {
        EventBuilder bldr = new EventBuilder(uei, "ReST");
        bldr.setNodeid(nodeId);
        m_eventProxy.send(bldr.getEvent());
    }

}
