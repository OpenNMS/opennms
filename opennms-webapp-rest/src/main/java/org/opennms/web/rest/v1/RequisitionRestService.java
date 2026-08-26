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

import java.text.ParseException;

import javax.annotation.PreDestroy;
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
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.ValidationException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory;
import org.opennms.netmgt.provision.persist.requisition.DeployedRequisitionStats;
import org.opennms.netmgt.provision.persist.requisition.DeployedStats;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAssetCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategoryCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNodeCollection;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.svclayer.api.RequisitionAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>RESTful service to the OpenNMS Provisioning Groups.  In this API, these "groups" of nodes
 * are aptly named and treated as requisitions.</p>
 * <p>This current implementation supports CRUD operations for managing provisioning requisitions.  Requisitions
 * are first POSTed and no provisioning (import) operations are taken.  This is done so that a) the XML can be
 * verified and b) so that the operations can happen at a later time.  They are moved to the deployed state
 * (put in the active requisition repository) when an import is run.
 * <ul>
 * <li>GET/PUT/POST pending requisitions</li>
 * <li>GET pending and deployed count</li>
 * </ul>
 * </p>
 * <p>Example 1: Create a new requisition <i>Note: The foreign-source attribute typically has a 1 to 1
 * relationship to a provisioning group and to the name used in this requisition.  The relationship is
 * implied by name and it is best practice to use the same for all three.  If a foreign source definition
 * exists with the same name, it will be used during the provisioning (import) operations in lieu of the
 * default foreign source</i></p>
 * <pre>
 * curl -X POST \
 *     -H "Content-Type: application/xml" \
 *     -d "&lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *         &lt;model-import xmlns="http://xmlns.opennms.org/xsd/config/model-import"
 *             date-stamp="2009-03-07T17:56:53.123-05:00"
 *             last-import="2009-03-07T17:56:53.117-05:00" foreign-source="site1"&gt;
 *           &lt;node node-label="p-brane" foreign-id="1" &gt;
 *             &lt;interface ip-addr="10.0.1.3" descr="en1" status="1" snmp-primary="P"&gt;
 *               &lt;monitored-service service-name="ICMP"/&gt;
 *               &lt;monitored-service service-name="SNMP"/&gt;
 *             &lt;/interface&gt;
 *             &lt;category name="Production"/&gt;
 *             &lt;category name="Routers"/&gt;
 *           &lt;/node&gt;
 *         &lt;/model-import&gt;" \
 *     -u admin:admin \
 *     http://localhost:8980/opennms/rest/requisitions
 * </pre>
 * <p>Example 2: Query all deployed requisitions</p>
 * <pre>
 * curl -X GET \
 *     -H "Content-Type: application/xml" \
 *     -u admin:admin \
 *        http://localhost:8980/opennms/rest/requisitions/deployed \
 *        2>/dev/null \
 *        |xmllint --format -</pre>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component("requisitionRestService")
@Path("requisitions")
@Tag(name = "Requisitions", description = """
        Requisitions API.

        A requisition is the declarative model of a group of nodes: the nodes, their IP interfaces, the
        monitored services on those interfaces, and the surveillance categories and asset fields on the
        nodes. Editing a requisition changes nothing in the monitored inventory. The nodes are only created,
        updated and deleted when the requisition is imported.

        Requisitions live in two repositories. Writes land in *pending*
        (`etc/imports/pending/{foreignSource}.xml`); `PUT /requisitions/{foreignSource}/import` hands the
        pending document to provisiond, which reconciles the database against it and moves the document to
        *deployed* (`etc/imports/{foreignSource}.xml`). `GET /requisitions` and
        `GET /requisitions/{foreignSource}` read the pending document when there is one and fall back to the
        deployed document, so a read after an edit shows the edit whether or not it has been imported.
        `/requisitions/count` counts pending documents and drops back to zero once an import completes;
        `/requisitions/deployed/count` counts deployed documents.

        The URL tree mirrors the document, and every level can be read, replaced and deleted on its own:

            /requisitions/{foreignSource}
            /requisitions/{foreignSource}/nodes/{foreignId}
            /requisitions/{foreignSource}/nodes/{foreignId}/categories/{category}
            /requisitions/{foreignSource}/nodes/{foreignId}/assets/{parameter}
            /requisitions/{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}
            /requisitions/{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services/{service}

        Editing one node or one interface is the cheaper path for a large requisition, since a `POST` to
        `/requisitions` replaces the whole document.

        Two wire-format points. Dates (`date-stamp`, `last-import`, `last-imported`) are epoch milliseconds
        in JSON and ISO-8601 timestamps in XML, whatever the derived schema says. The `PUT` handlers consume
        `application/x-www-form-urlencoded` only, not JSON or XML.""")
public class RequisitionRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(RequisitionRestService.class);

    @Autowired
    private RequisitionAccessService m_accessService;

    @Autowired
    private ForeignSourceRepositoryFactory m_foreignSourceRepositoryFactory;

    @PreDestroy
    protected void tearDown() {
        if (m_accessService != null) {
            m_accessService.flushAll();
        }
    }

    /**
     * get a plain text numeric string of the number of deployed requisitions
     *
     * @return a int.
     */
    @GET
    @Path("deployed/count")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Count the deployed requisitions",
            description = "Plain-text decimal count of the documents in the deployed repository, that is, of requisitions that have been imported at least once.",
            operationId = "getDeployedRequisitionCount")
    @ApiResponse(responseCode = "200", description = "The count.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string", example = "1"),
                    examples = @ExampleObject(value = "1")))
    public String getDeployedCount() {
        return Integer.toString(m_accessService.getDeployedCount());
    }

    /**
     * get the statistics for the deployed requisitions
     *
     * @return a DeployedStats.
     */
    @GET
    @Path("deployed/stats")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Summarize what is provisioned, per foreign source",
            description = """
                    One entry per foreign source that has nodes in the database, listing the foreign IDs of
                    those nodes and the last import time of the matching deployed requisition. The entries come
                    from the node table rather than from the requisition repository, so a foreign source whose
                    requisition has been deleted while its nodes remain still appears, with `last-imported`
                    null.""",
            operationId = "getDeployedRequisitionStats")
    @ApiResponse(responseCode = "200", description = "Per-foreign-source statistics.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DeployedStats.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 2,
                                      "count": 2,
                                      "offset": 0,
                                      "foreign-source": [
                                        {
                                          "totalCount": 3,
                                          "count": 3,
                                          "offset": 0,
                                          "name": "datacenter-east",
                                          "last-imported": 1787727435518,
                                          "foreign-id": [ "node-1", "node-2", "node-3" ]
                                        },
                                        {
                                          "totalCount": 1,
                                          "count": 1,
                                          "offset": 0,
                                          "name": "selfmonitor",
                                          "last-imported": null,
                                          "foreign-id": [ "1" ]
                                        }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = DeployedStats.class),
                            examples = @ExampleObject(value = """
                                    <deployed-stats xmlns="http://xmlns.opennms.org/xsd/config/model-import" count="1" offset="0" totalCount="1">
                                      <foreign-source name="datacenter-east" count="3" offset="0" totalCount="3" last-imported="2026-08-26T02:57:15.518-04:00">
                                        <foreign-id>node-1</foreign-id>
                                        <foreign-id>node-2</foreign-id>
                                        <foreign-id>node-3</foreign-id>
                                      </foreign-source>
                                    </deployed-stats>"""))
            })
    public DeployedStats getDeployedStats() {
        return m_accessService.getDeployedStats();
    }

    /**
     * get the statistics for a given deployed requisition
     *
     * @return a DeployedRequisitionStats.
     */
    @GET
    @Path("deployed/stats/{foreignSource}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Summarize what is provisioned for one foreign source",
            description = """
                    The foreign IDs of the nodes currently in the database for this foreign source, and the last
                    import time of its deployed requisition.

                    A foreign source with no deployed requisition fails with 500 rather than 404, including one
                    whose nodes still exist after the requisition was deleted. Check
                    `GET /requisitions/deployed/count` or the list from `GET /requisitions/deployed/stats`
                    before calling this for a name you are not sure of.""",
            operationId = "getDeployedRequisitionStatsForForeignSource")
    @ApiResponse(responseCode = "200", description = "Statistics for the foreign source.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DeployedRequisitionStats.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 3,
                                      "count": 3,
                                      "offset": 0,
                                      "name": "datacenter-east",
                                      "last-imported": 1787727435518,
                                      "foreign-id": [ "node-1", "node-2", "node-3" ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = DeployedRequisitionStats.class),
                            examples = @ExampleObject(value = """
                                    <foreign-source xmlns="http://xmlns.opennms.org/xsd/config/model-import" name="datacenter-east" count="3" offset="0" totalCount="3" last-imported="2026-08-26T02:57:15.518-04:00">
                                      <foreign-id>node-1</foreign-id>
                                      <foreign-id>node-2</foreign-id>
                                      <foreign-id>node-3</foreign-id>
                                    </foreign-source>"""))
            })
    @ApiResponse(responseCode = "500", description = "No deployed requisition exists for this foreign source.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Cannot invoke \"org.opennms.netmgt.provision.persist.requisition.Requisition.getLastImportAsDate()\" because \"fs\" is null")))
    public DeployedRequisitionStats getDeployedStats(@Parameter(required = true, description = "Foreign source name of a deployed requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource) {
        return m_accessService.getDeployedStats(foreignSource);
    }

    /**
     * get a plain text with the current selected foreign source repository strategy
     *
     * @return the current strategy.
     */
    @GET
    @Path("repositoryStrategy")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Get the foreign source repository strategy in effect",
            description = """
                    Plain-text name of the repository implementation backing requisitions and foreign source
                    definitions, taken from `org.opennms.provisiond.repositoryImplementation`. The enumeration is
                    `file`, `fastFile`, `caching`, `fastCaching`, `queueing`, `fastQueueing`, `fused` and
                    `fastFused`. `file` stores the documents as XML under `etc/imports` and
                    `etc/foreign-sources`; the queueing variants wrap the file store with an asynchronous writer,
                    which means a write can be acknowledged before it is on disk.""",
            operationId = "getForeignSourceRepositoryStrategy")
    @ApiResponse(responseCode = "200", description = "The configured strategy.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string", example = "file"),
                    examples = @ExampleObject(value = "file")))
    public String getForeignSourceRepositoryStrategy() {
        return m_foreignSourceRepositoryFactory.getRepositoryStrategy().toString();
    }

    /**
     * Get all the deployed requisitions
     *
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("deployed")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the deployed requisitions",
            description = """
                    Every document in the deployed repository, in full. A requisition that has been written but
                    not imported yet does not appear here; use `GET /requisitions` for the pending-and-deployed
                    view. `totalCount` and `count` come back as `null` when the list is empty.""",
            operationId = "getDeployedRequisitions")
    @ApiResponse(responseCode = "200", description = "Deployed requisitions.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 1,
                                      "count": 1,
                                      "offset": 0,
                                      "model-import": [
                                        {
                                          "node": [
                                            {
                                              "interface": [
                                                {
                                                  "monitored-service": [ { "category": [], "meta-data": [], "service-name": "ICMP" } ],
                                                  "category": [],
                                                  "meta-data": [],
                                                  "descr": "eth0",
                                                  "ip-addr": "198.51.100.11",
                                                  "managed": null,
                                                  "status": 1,
                                                  "snmp-primary": "P"
                                                }
                                              ],
                                              "category": [ { "name": "Production" } ],
                                              "asset": [ { "name": "city", "value": "Apex" } ],
                                              "meta-data": [],
                                              "location": null,
                                              "building": "HQ",
                                              "city": null,
                                              "foreign-id": "node-1",
                                              "node-label": "router-1",
                                              "parent-foreign-source": null,
                                              "parent-foreign-id": null,
                                              "parent-node-label": null
                                            }
                                          ],
                                          "date-stamp": 1787727326179,
                                          "foreign-source": "datacenter-east",
                                          "last-import": 1787727435518
                                        }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionCollection.class),
                            examples = @ExampleObject(value = """
                                    <requisitions xmlns="http://xmlns.opennms.org/xsd/config/model-import" count="1" offset="0" totalCount="1">
                                      <model-import date-stamp="2026-08-26T02:55:26.179-04:00"
                                                    last-import="2026-08-26T02:57:15.518-04:00"
                                                    foreign-source="datacenter-east">
                                        <node building="HQ" foreign-id="node-1" node-label="router-1">
                                          <interface descr="eth0" ip-addr="198.51.100.11" status="1" snmp-primary="P">
                                            <monitored-service service-name="ICMP"/>
                                          </interface>
                                          <category name="Production"/>
                                          <asset name="city" value="Apex"/>
                                        </node>
                                      </model-import>
                                    </requisitions>"""))
            })
    public RequisitionCollection getDeployedRequisitions() throws ParseException {
        return m_accessService.getDeployedRequisitions();
    }

    /**
     * Get all the pending requisitions
     *
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List every requisition",
            description = """
                    One document per foreign source, resolved pending-first, so a requisition edited but not yet
                    imported appears here in its edited form. `totalCount` and `count` come back as `null` when
                    the list is empty. For the names alone, `GET /requisitionNames` is the cheaper call.""",
            operationId = "getRequisitions")
    @ApiResponse(responseCode = "200", description = "All requisitions, pending documents preferred over deployed.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 1,
                                      "count": 1,
                                      "offset": 0,
                                      "model-import": [
                                        {
                                          "node": [
                                            {
                                              "interface": [
                                                {
                                                  "monitored-service": [ { "category": [], "meta-data": [], "service-name": "ICMP" } ],
                                                  "category": [],
                                                  "meta-data": [],
                                                  "descr": "eth0",
                                                  "ip-addr": "198.51.100.11",
                                                  "managed": null,
                                                  "status": 1,
                                                  "snmp-primary": "P"
                                                }
                                              ],
                                              "category": [ { "name": "Production" } ],
                                              "asset": [ { "name": "city", "value": "Apex" } ],
                                              "meta-data": [],
                                              "location": null,
                                              "building": "HQ",
                                              "city": null,
                                              "foreign-id": "node-1",
                                              "node-label": "router-1",
                                              "parent-foreign-source": null,
                                              "parent-foreign-id": null,
                                              "parent-node-label": null
                                            }
                                          ],
                                          "date-stamp": 1787727326179,
                                          "foreign-source": "datacenter-east",
                                          "last-import": null
                                        }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionCollection.class),
                            examples = @ExampleObject(value = """
                                    <requisitions xmlns="http://xmlns.opennms.org/xsd/config/model-import" count="1" offset="0" totalCount="1">
                                      <model-import date-stamp="2026-08-26T02:55:26.179-04:00" foreign-source="datacenter-east">
                                        <node building="HQ" foreign-id="node-1" node-label="router-1">
                                          <interface descr="eth0" ip-addr="198.51.100.11" status="1" snmp-primary="P">
                                            <monitored-service service-name="ICMP"/>
                                          </interface>
                                          <category name="Production"/>
                                          <asset name="city" value="Apex"/>
                                        </node>
                                      </model-import>
                                    </requisitions>"""))
            })
    public RequisitionCollection getRequisitions() throws ParseException {
        return m_accessService.getRequisitions();
    }

    /**
     * get a plain text numeric string of the number of pending requisitions
     *
     * @return a int.
     */
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
            summary = "Count the pending requisitions",
            description = """
                    Plain-text decimal count of the documents in the pending repository, that is, of
                    requisitions with edits that have not been imported. A requisition drops out of this count
                    once its import completes, so a healthy steady state is `0`.""",
            operationId = "getPendingRequisitionCount")
    @ApiResponse(responseCode = "200", description = "The count.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string", example = "1"),
                    examples = @ExampleObject(value = "1")))
    public String getPendingCount() {
        return Integer.toString(m_accessService.getPendingCount());
    }

    /**
     * <p>getRequisition</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    @GET
    @Path("{foreignSource}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one requisition",
            description = """
                    The pending document if there is one, otherwise the deployed document. `date-stamp` is when
                    the document was last written; `last-import` is when it was last imported and is null until
                    the first import.""",
            operationId = "getRequisition")
    @ApiResponse(responseCode = "200", description = "The requisition.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Requisition.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "node": [ {
                                      "interface": [
                                        {
                                          "monitored-service": [ { "category": [], "meta-data": [], "service-name": "ICMP" } ],
                                          "category": [],
                                          "meta-data": [],
                                          "descr": "eth0",
                                          "ip-addr": "198.51.100.11",
                                          "managed": null,
                                          "status": 1,
                                          "snmp-primary": "P"
                                        }
                                      ],
                                      "category": [ { "name": "Production" } ],
                                      "asset": [ { "name": "city", "value": "Apex" } ],
                                      "meta-data": [],
                                      "location": null,
                                      "building": "HQ",
                                      "city": null,
                                      "foreign-id": "node-1",
                                      "node-label": "router-1",
                                      "parent-foreign-source": null,
                                      "parent-foreign-id": null,
                                      "parent-node-label": null
                                    } ],
                                      "date-stamp": 1787727326179,
                                      "foreign-source": "datacenter-east",
                                      "last-import": 1787727435518
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = Requisition.class),
                            examples = @ExampleObject(value = """
                                    <model-import xmlns="http://xmlns.opennms.org/xsd/config/model-import"
                                                  date-stamp="2026-08-26T02:55:26.179-04:00"
                                                  last-import="2026-08-26T02:57:15.518-04:00"
                                                  foreign-source="datacenter-east">
                                      <node building="HQ" foreign-id="node-1" node-label="router-1">
                                        <interface descr="eth0" ip-addr="198.51.100.11" status="1" snmp-primary="P">
                                          <monitored-service service-name="ICMP"/>
                                        </interface>
                                        <category name="Production"/>
                                        <asset name="city" value="Apex"/>
                                      </node>
                                    </model-import>"""))
            })
    @ApiResponse(responseCode = "404", description = "No requisition of that name in either repository.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Foreign source 'datacenter-west' not found.")))
    public Requisition getRequisition(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource) {
        final Requisition requisition = m_accessService.getRequisition(foreignSource);
        if (requisition == null) {
            throw getException(Status.NOT_FOUND, "Foreign source '{}' not found.", foreignSource);
        }
        return requisition;
    }

    /**
     * Returns all nodes for a given requisition
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionNodeCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the nodes of a requisition",
            description = "The `node` elements of the resolved requisition, without the requisition envelope.",
            operationId = "getRequisitionNodes")
    @ApiResponse(responseCode = "200", description = "The nodes.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionNodeCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 1,
                                      "count": 1,
                                      "offset": 0,
                                      "node": [ {
                                      "interface": [
                                        {
                                          "monitored-service": [ { "category": [], "meta-data": [], "service-name": "ICMP" } ],
                                          "category": [],
                                          "meta-data": [],
                                          "descr": "eth0",
                                          "ip-addr": "198.51.100.11",
                                          "managed": null,
                                          "status": 1,
                                          "snmp-primary": "P"
                                        }
                                      ],
                                      "category": [ { "name": "Production" } ],
                                      "asset": [ { "name": "city", "value": "Apex" } ],
                                      "meta-data": [],
                                      "location": null,
                                      "building": "HQ",
                                      "city": null,
                                      "foreign-id": "node-1",
                                      "node-label": "router-1",
                                      "parent-foreign-source": null,
                                      "parent-foreign-id": null,
                                      "parent-node-label": null
                                    } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionNodeCollection.class),
                            examples = @ExampleObject(value = """
                                    <nodes xmlns="http://xmlns.opennms.org/xsd/config/model-import" count="1" offset="0" totalCount="1">
                                      <node building="HQ" foreign-id="node-1" node-label="router-1">
                                        <interface descr="eth0" ip-addr="198.51.100.11" status="1" snmp-primary="P">
                                          <monitored-service service-name="ICMP"/>
                                        </interface>
                                        <category name="Production"/>
                                        <asset name="city" value="Apex"/>
                                      </node>
                                    </nodes>"""))
            })
    @ApiResponse(responseCode = "404", description = "No requisition of that name in either repository.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Foreign source 'datacenter-west' not found.")))
    public RequisitionNodeCollection getNodes(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource) throws ParseException {
        final RequisitionNodeCollection results = m_accessService.getNodes(foreignSource);
        if (results == null) {
            throw getException(Status.NOT_FOUND, "Foreign source '{}' not found.", foreignSource);
        }
        return results;
    }

    /**
     * Returns the node with the foreign ID specified for the given foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionNode} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one node of a requisition",
            description = """
                    Looked up by foreign ID, which is the node's identity within the requisition and is what
                    ties the requisitioned node to the provisioned one. The node label is not an identifier
                    here.""",
            operationId = "getRequisitionNode")
    @ApiResponse(responseCode = "200", description = "The node.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionNode.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "interface": [
                                        {
                                          "monitored-service": [ { "category": [], "meta-data": [], "service-name": "ICMP" } ],
                                          "category": [],
                                          "meta-data": [],
                                          "descr": "eth0",
                                          "ip-addr": "198.51.100.11",
                                          "managed": null,
                                          "status": 1,
                                          "snmp-primary": "P"
                                        }
                                      ],
                                      "category": [ { "name": "Production" } ],
                                      "asset": [ { "name": "city", "value": "Apex" } ],
                                      "meta-data": [],
                                      "location": null,
                                      "building": "HQ",
                                      "city": null,
                                      "foreign-id": "node-1",
                                      "node-label": "router-1",
                                      "parent-foreign-source": null,
                                      "parent-foreign-id": null,
                                      "parent-node-label": null
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionNode.class),
                            examples = @ExampleObject(value = """
                                    <node xmlns="http://xmlns.opennms.org/xsd/config/model-import" building="HQ" foreign-id="node-1" node-label="router-1">
                                      <interface descr="eth0" ip-addr="198.51.100.11" status="1" snmp-primary="P">
                                        <monitored-service service-name="ICMP"/>
                                      </interface>
                                      <category name="Production"/>
                                      <asset name="city" value="Apex"/>
                                    </node>"""))
            })
    @ApiResponse(responseCode = "404", description = "No such foreign ID in that requisition, or no such requisition.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Node with Foreign ID 'node-9' and Foreign source 'datacenter-east' not found.")))
    public RequisitionNode getNode(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId) throws ParseException {
        final RequisitionNode node = m_accessService.getNode(foreignSource, foreignId);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "Node with Foreign ID '{}' and Foreign source '{}' not found.", foreignId, foreignSource);
        }
        return node;
    }

    /**
     * Returns a collection of interfaces for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the IP interfaces of a requisitioned node",
            description = "The `interface` elements of the node. Order is the stored order, which is not necessarily the order they were added in.",
            operationId = "getRequisitionInterfaces")
    @ApiResponse(responseCode = "200", description = "The interfaces.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionInterfaceCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 1,
                                      "count": 1,
                                      "offset": 0,
                                      "interface": [ {
                                      "monitored-service": [ { "category": [], "meta-data": [], "service-name": "ICMP" } ],
                                      "category": [],
                                      "meta-data": [],
                                      "descr": "eth0",
                                      "ip-addr": "198.51.100.11",
                                      "managed": null,
                                      "status": 1,
                                      "snmp-primary": "P"
                                    } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionInterfaceCollection.class),
                            examples = @ExampleObject(value = """
                                    <interfaces xmlns="http://xmlns.opennms.org/xsd/config/model-import" count="1" offset="0" totalCount="1">
                                      <interface descr="eth0" ip-addr="198.51.100.11" status="1" snmp-primary="P">
                                        <monitored-service service-name="ICMP"/>
                                      </interface>
                                    </interfaces>"""))
            })
    @ApiResponse(responseCode = "404", description = "No such node in that requisition.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Node with Foreign ID 'node-9' and Foreign source 'datacenter-east' not found.")))
    public RequisitionInterfaceCollection getInterfacesForNode(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId) throws ParseException {
        final RequisitionInterfaceCollection ifaces = m_accessService.getInterfacesForNode(foreignSource, foreignId);
        if (ifaces == null) {
            throw getException(Status.NOT_FOUND, "Node with Foreign ID '{}' and Foreign source '{}' not found.", foreignId, foreignSource);
        }
        return ifaces;
    }

    /**
     * Returns the interface with the given foreign source/foreignid/ipaddress combination.
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param ipAddress     a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one IP interface of a requisitioned node",
            description = """
                    `snmp-primary` is `P` for the primary SNMP interface, `S` for a secondary one and `N` for
                    not eligible. `status` of 3 marks the interface unmanaged; any other value, including the
                    default of 1, leaves it managed.""",
            operationId = "getRequisitionInterface")
    @ApiResponse(responseCode = "200", description = "The interface.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionInterface.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "monitored-service": [ { "category": [], "meta-data": [], "service-name": "ICMP" } ],
                                      "category": [],
                                      "meta-data": [],
                                      "descr": "eth0",
                                      "ip-addr": "198.51.100.11",
                                      "managed": null,
                                      "status": 1,
                                      "snmp-primary": "P"
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionInterface.class),
                            examples = @ExampleObject(value = """
                                    <interface xmlns="http://xmlns.opennms.org/xsd/config/model-import" descr="eth0" ip-addr="198.51.100.11" status="1" snmp-primary="P">
                                      <monitored-service service-name="ICMP"/>
                                    </interface>"""))
            })
    @ApiResponse(responseCode = "404", description = "No such IP on that node, or no such node.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "IP Interface 198.51.100.99 on node with Foreign ID 'node-1' and Foreign source 'datacenter-east' not found.")))
    public RequisitionInterface getInterfaceForNode(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "IP address of the requisitioned interface, exactly as stored.", example = "198.51.100.11") @PathParam("ipAddress") final String ipAddress) throws ParseException {
        final RequisitionInterface iface = m_accessService.getInterfaceForNode(foreignSource, foreignId, ipAddress);
        if (iface == null) {
            throw getException(Status.NOT_FOUND, "IP Interface {} on node with Foreign ID '{}' and Foreign source '{}' not found.", ipAddress, foreignId, foreignSource);
        }
        return iface;
    }

    /**
     * Returns a collection of services for a given foreignSource/foreignId/interface combination.
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param ipAddress     a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the monitored services on a requisitioned interface",
            description = """
                    Services named here are created on the interface at import whether or not a detector finds
                    them. Detectors on the foreign source definition can add further services on top of
                    these.""",
            operationId = "getRequisitionServices")
    @ApiResponse(responseCode = "200", description = "The monitored services.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionMonitoredServiceCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 2,
                                      "count": 2,
                                      "offset": 0,
                                      "monitored-service": [
                                        { "category": [], "meta-data": [], "service-name": "ICMP" },
                                        { "category": [], "meta-data": [], "service-name": "SNMP" }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionMonitoredServiceCollection.class),
                            examples = @ExampleObject(value = """
                                    <services xmlns="http://xmlns.opennms.org/xsd/config/model-import" count="2" offset="0" totalCount="2">
                                      <monitored-service service-name="ICMP"/>
                                      <monitored-service service-name="SNMP"/>
                                    </services>"""))
            })
    @ApiResponse(responseCode = "404", description = "No such IP on that node, or no such node.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "IP Interface 198.51.100.99 on node with Foreign ID 'node-1' and Foreign source 'datacenter-east' not found.")))
    public RequisitionMonitoredServiceCollection getServicesForInterface(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "IP address of the requisitioned interface, exactly as stored.", example = "198.51.100.11") @PathParam("ipAddress") final String ipAddress) throws ParseException {
        final RequisitionMonitoredServiceCollection services = m_accessService.getServicesForInterface(foreignSource, foreignId, ipAddress);
        if (services == null) {
            throw getException(Status.NOT_FOUND, "IP Interface {} on node with Foreign ID '{}' and Foreign source '{}' not found.", ipAddress, foreignId, foreignSource);
        }
        return services;
    }

    /**
     * Returns a service for a given foreignSource/foreignId/interface/service-name combination.
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param ipAddress     a {@link java.lang.String} object.
     * @param service       a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services/{service}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one monitored service on a requisitioned interface",
            description = "Looked up by service name, matched exactly and case-sensitively.",
            operationId = "getRequisitionService")
    @ApiResponse(responseCode = "200", description = "The monitored service.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionMonitoredService.class),
                            examples = @ExampleObject(value = """
                                    { "category": [], "meta-data": [], "service-name": "ICMP" }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionMonitoredService.class),
                            examples = @ExampleObject(value = """
                                    <monitored-service xmlns="http://xmlns.opennms.org/xsd/config/model-import" service-name="ICMP"/>"""))
            })
    @ApiResponse(responseCode = "404", description = "No service of that name on the interface, or the interface or node does not exist.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Monitored Service HTTP on IP Interface 198.51.100.11 on node with Foreign ID 'node-1' and Foreign source 'datacenter-east' not found.")))
    public RequisitionMonitoredService getServiceForInterface(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "IP address of the requisitioned interface, exactly as stored.", example = "198.51.100.11") @PathParam("ipAddress") final String ipAddress, @Parameter(required = true, description = "Monitored service name.", example = "ICMP") @PathParam("service") String service) throws ParseException {
        final RequisitionMonitoredService monitoredService = m_accessService.getServiceForInterface(foreignSource, foreignId, ipAddress, service);
        if (monitoredService == null) {
            throw getException(Status.NOT_FOUND, "Monitored Service {} on IP Interface {} on node with Foreign ID '{}' and Foreign source '{}' not found.", service, ipAddress, foreignId, foreignSource);
        }
        return monitoredService;
    }

    /**
     * Returns a collection of categories for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategoryCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/categories")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the surveillance categories on a requisitioned node",
            description = """
                    Categories are applied to the node at import. A category named here that does not yet exist
                    is created, so the names are not restricted to what
                    `GET /foreignSourcesConfig/categories` returns.""",
            operationId = "getRequisitionNodeCategories")
    @ApiResponse(responseCode = "200", description = "The categories.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionCategoryCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 2,
                                      "count": 2,
                                      "offset": 0,
                                      "category": [ { "name": "Production" }, { "name": "Routers" } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionCategoryCollection.class),
                            examples = @ExampleObject(value = """
                                    <categories xmlns="http://xmlns.opennms.org/xsd/config/model-import" count="2" offset="0" totalCount="2">
                                      <category name="Production"/>
                                      <category name="Routers"/>
                                    </categories>"""))
            })
    @ApiResponse(responseCode = "404", description = "No such node in that requisition.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Node with Foreign ID 'node-9' and Foreign source 'datacenter-east' not found.")))
    public RequisitionCategoryCollection getCategories(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId) throws ParseException {
        final RequisitionCategoryCollection categories = m_accessService.getCategories(foreignSource, foreignId);
        if (categories == null) {
            throw getException(Status.NOT_FOUND, "Node with Foreign ID '{}' and Foreign source '{}' not found.", foreignId, foreignSource);
        }
        return categories;
    }

    /**
     * Returns the requested category for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param category      a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/categories/{category}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one surveillance category on a requisitioned node",
            description = "Looked up by category name, matched exactly and case-sensitively.",
            operationId = "getRequisitionNodeCategory")
    @ApiResponse(responseCode = "200", description = "The category.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionCategory.class),
                            examples = @ExampleObject(value = """
                                    { "name": "Production" }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionCategory.class),
                            examples = @ExampleObject(value = """
                                    <category xmlns="http://xmlns.opennms.org/xsd/config/model-import" name="Production"/>"""))
            })
    @ApiResponse(responseCode = "404", description = "No category of that name on the node, or no such node.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Category Staging on node with Foreign ID 'node-1' and Foreign source 'datacenter-east' not found.")))
    public RequisitionCategory getCategory(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "Surveillance category name.", example = "Production") @PathParam("category") final String category) throws ParseException {
        final RequisitionCategory reqCategory = m_accessService.getCategory(foreignSource, foreignId, category);
        if (reqCategory == null) {
            throw getException(Status.NOT_FOUND, "Category {} on node with Foreign ID '{}' and Foreign source '{}' not found.", category, foreignId, foreignSource);
        }
        return reqCategory;
    }

    /**
     * Returns a collection of assets for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAssetCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/assets")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "List the asset fields set on a requisitioned node",
            description = """
                    Only the fields the requisition sets, not the node's full asset record. `name` must be one
                    of the field names `GET /foreignSourcesConfig/assets` returns; an unrecognized name is
                    accepted into the requisition but has no effect at import.""",
            operationId = "getRequisitionNodeAssets")
    @ApiResponse(responseCode = "200", description = "The asset fields.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionAssetCollection.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "totalCount": 2,
                                      "count": 2,
                                      "offset": 0,
                                      "asset": [ { "name": "city", "value": "Apex" }, { "name": "region", "value": "east" } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionAssetCollection.class),
                            examples = @ExampleObject(value = """
                                    <assets xmlns="http://xmlns.opennms.org/xsd/config/model-import" count="2" offset="0" totalCount="2">
                                      <asset name="city" value="Apex"/>
                                      <asset name="region" value="east"/>
                                    </assets>"""))
            })
    @ApiResponse(responseCode = "404", description = "No such node in that requisition.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Node with Foreign ID 'node-9' and Foreign source 'datacenter-east' not found.")))
    public RequisitionAssetCollection getAssetParameters(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId) throws ParseException {
        final RequisitionAssetCollection assets = m_accessService.getAssetParameters(foreignSource, foreignId);
        if (assets == null) {
            throw getException(Status.NOT_FOUND, "Node with Foreign ID '{}' and Foreign source '{}' not found.", foreignId, foreignSource);
        }
        return assets;
    }

    /**
     * Returns the requested category for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param parameter     a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/assets/{parameter}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one asset field set on a requisitioned node",
            description = "Looked up by asset field name, matched exactly and case-sensitively.",
            operationId = "getRequisitionNodeAsset")
    @ApiResponse(responseCode = "200", description = "The asset field.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionAsset.class),
                            examples = @ExampleObject(value = """
                                    { "name": "city", "value": "Apex" }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionAsset.class),
                            examples = @ExampleObject(value = """
                                    <asset xmlns="http://xmlns.opennms.org/xsd/config/model-import" name="city" value="Apex"/>"""))
            })
    @ApiResponse(responseCode = "404", description = "No asset of that name on the node, or no such node.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Asset serialNumber on node with Foreign ID 'node-1' and Foreign source 'datacenter-east' not found.")))
    public RequisitionAsset getAssetParameter(@Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "Asset field name.", example = "city") @PathParam("parameter") final String parameter) throws ParseException {
        final RequisitionAsset asset = m_accessService.getAssetParameter(foreignSource, foreignId, parameter);
        if (asset == null) {
            throw getException(Status.NOT_FOUND, "Asset {} on node with Foreign ID '{}' and Foreign source '{}' not found.", parameter, foreignId, foreignSource);
        }
        return asset;
    }

    /**
     * Updates or adds a complete requisition with foreign source "foreignSource"
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Add or replace a whole requisition",
            description = """
                    Writes the document to the pending repository under the `foreign-source` carried in the
                    body, not in the URL. The previous pending document is replaced outright rather than merged,
                    so anything absent from the body is dropped. To edit part of a large requisition, post to the
                    node, interface, service, category or asset sub-resource instead.

                    Nothing is provisioned until `PUT /requisitions/{foreignSource}/import` runs.

                    The body is validated before it is written: `foreign-source` must be present and must not
                    contain any of `: / \\ ? & * ' "`, every node needs a `foreign-id`, foreign IDs must be
                    unique within the document, and each interface needs an `ip-addr`. Note that `foreign-source`
                    has a default of `imported-`, so a body that omits it is accepted and stored under that name
                    rather than rejected. `date-stamp` and `last-import` in the body are ignored.""",
            operationId = "addOrReplaceRequisition")
    @RequestBody(required = true, description = "The complete requisition document.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Requisition.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "foreign-source": "datacenter-east",
                                      "node": [
                                        {
                                          "foreign-id": "node-1",
                                          "node-label": "router-1",
                                          "building": "HQ",
                                          "interface": [
                                            {
                                              "ip-addr": "198.51.100.11",
                                              "descr": "eth0",
                                              "snmp-primary": "P",
                                              "status": 1,
                                              "monitored-service": [ { "service-name": "ICMP" } ]
                                            }
                                          ],
                                          "category": [ { "name": "Production" } ],
                                          "asset": [ { "name": "city", "value": "Apex" } ]
                                        }
                                      ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = Requisition.class),
                            examples = @ExampleObject(value = """
                                    <model-import xmlns="http://xmlns.opennms.org/xsd/config/model-import" foreign-source="datacenter-east">
                                      <node foreign-id="node-1" node-label="router-1" building="HQ">
                                        <interface ip-addr="198.51.100.11" descr="eth0" status="1" snmp-primary="P">
                                          <monitored-service service-name="ICMP"/>
                                        </interface>
                                        <category name="Production"/>
                                        <asset name="city" value="Apex"/>
                                      </node>
                                    </model-import>"""))
            })
    @ApiResponse(responseCode = "202", description = "Written to the pending repository. No body.",
            headers = @Header(name = "Location", description = "URI of the stored requisition.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east")))
    @ApiResponse(responseCode = "400", description = "The body failed validation. The message names the offending attribute.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Duplicate nodes found on foreign source datacenter-east: node-1 (2 found)")))
    public Response addOrReplaceRequisition(@Context final UriInfo uriInfo, final Requisition requisition) {
        try {
            requisition.validate();
        } catch (final ValidationException e) {
            LOG.error("error validating incoming requisition with foreign source '{}'", requisition.getForeignSource(), e);
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }
        LOG.info("POST {}: Adding requisition {} (containing {} nodes)", uriInfo.getPath(), requisition.getForeignSource(), requisition.getNodeCount());
        m_accessService.addOrReplaceRequisition(requisition);
        return Response.accepted().header("Location", getRedirectUri(uriInfo, requisition.getForeignSource())).build();
    }

    /**
     * Updates or adds a node to a requisition
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param node          a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionNode} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Add or replace one node in a requisition",
            description = """
                    Keyed on the `foreign-id` in the body: an existing node with that foreign ID is replaced
                    outright, otherwise the node is appended. The rest of the requisition is left alone, which is
                    what makes this the practical way to edit a large requisition.

                    If the requisition does not exist it is created with this node as its only member. Nothing is
                    provisioned until an import runs.""",
            operationId = "addOrReplaceRequisitionNode")
    @RequestBody(required = true, description = "The node, complete with its interfaces, categories and assets.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionNode.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "foreign-id": "node-2",
                                      "node-label": "router-2",
                                      "building": "Annex",
                                      "interface": [
                                        {
                                          "ip-addr": "198.51.100.12",
                                          "descr": "eth0",
                                          "snmp-primary": "P",
                                          "status": 1,
                                          "monitored-service": [ { "service-name": "ICMP" } ]
                                        }
                                      ],
                                      "category": [ { "name": "Routers" } ],
                                      "asset": [ { "name": "region", "value": "east" } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionNode.class),
                            examples = @ExampleObject(value = """
                                    <node xmlns="http://xmlns.opennms.org/xsd/config/model-import" foreign-id="node-2" node-label="router-2" building="Annex">
                                      <interface ip-addr="198.51.100.12" descr="eth0" status="1" snmp-primary="P">
                                        <monitored-service service-name="ICMP"/>
                                      </interface>
                                      <category name="Routers"/>
                                      <asset name="region" value="east"/>
                                    </node>"""))
            })
    @ApiResponse(responseCode = "202", description = "Written to the pending requisition. No body.",
            headers = @Header(name = "Location", description = "URI of the stored node.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east/nodes/node-2")))
    @ApiResponse(responseCode = "400", description = "The body failed validation. The message names the offending attribute.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Requisition node 'foreign-id' is a required attribute!")))
    public Response addOrReplaceNode(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") String foreignSource, RequisitionNode node) {
        try {
            node.validate();
        } catch (final ValidationException e) {
            LOG.error("error validating incoming node '{}'", node, e);
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }
        LOG.info("POST {}: Adding node {} to requisition {}", uriInfo.getPath(), node.getForeignId(), foreignSource);
        m_accessService.addOrReplaceNode(foreignSource, node);
        return Response.accepted().header("Location", getRedirectUri(uriInfo, node.getForeignId())).build();
    }

    /**
     * Updates or adds an interface to a node
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param iface         a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/interfaces")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Add or replace one IP interface on a requisitioned node",
            description = """
                    Keyed on `ip-addr`: an existing interface with that address is replaced outright, otherwise
                    the interface is appended. `snmp-primary` takes `P` (primary), `S` (secondary) or `N` (not
                    eligible). `status` of 3 marks the interface unmanaged; any other value, including the
                    default of 1, leaves it managed.

                    Validation rejects a second interface marked `P` on the same node, two services with the
                    same `service-name` on this interface, and an `ip-addr` that does not parse.""",
            operationId = "addOrReplaceRequisitionInterface")
    @RequestBody(required = true, description = "The interface, with the monitored services it should carry.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionInterface.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "ip-addr": "198.51.100.12",
                                      "descr": "eth1",
                                      "snmp-primary": "S",
                                      "status": 1,
                                      "monitored-service": [ { "service-name": "ICMP" }, { "service-name": "SNMP" } ]
                                    }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionInterface.class),
                            examples = @ExampleObject(value = """
                                    <interface xmlns="http://xmlns.opennms.org/xsd/config/model-import" ip-addr="198.51.100.12" descr="eth1" status="1" snmp-primary="S">
                                      <monitored-service service-name="ICMP"/>
                                      <monitored-service service-name="SNMP"/>
                                    </interface>"""))
            })
    @ApiResponse(responseCode = "202", description = "Written to the pending requisition. No body.",
            headers = @Header(name = "Location", description = "URI of the stored interface.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east/nodes/node-1/interfaces/198.51.100.12")))
    @ApiResponse(responseCode = "400", description = "The body failed validation. The message names the offending attribute.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Requisition interface 'ip-addr' is a required attribute!")))
    public Response addOrReplaceInterface(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") String foreignId, RequisitionInterface iface) {
        try {
            final RequisitionNode node = m_accessService.getNode(foreignSource, foreignId);
            iface.validate(node);
        } catch (final ValidationException e) {
            LOG.error("error validating incoming interface '{}'", iface, e);
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }
        LOG.info("POST {}: Adding interface {} to node {}/{}", uriInfo.getPath(), iface, foreignSource, foreignId);
        m_accessService.addOrReplaceInterface(foreignSource, foreignId, iface);
        return Response.accepted().header("Location", getRedirectUri(uriInfo, InetAddressUtils.str(iface.getIpAddr()))).build();
    }

    /**
     * Updates or adds a service to an interface
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param ipAddress     a {@link java.lang.String} object.
     * @param service       a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Add or replace one monitored service on a requisitioned interface",
            description = """
                    Keyed on `service-name`. The name is free text and is not checked against the configured
                    poller services; `GET /foreignSourcesConfig/services/{groupName}` lists the names that have
                    a monitor behind them.""",
            operationId = "addOrReplaceRequisitionService")
    @RequestBody(required = true, description = "The monitored service.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionMonitoredService.class),
                            examples = @ExampleObject(value = """
                                    { "service-name": "SNMP" }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionMonitoredService.class),
                            examples = @ExampleObject(value = """
                                    <monitored-service xmlns="http://xmlns.opennms.org/xsd/config/model-import" service-name="SNMP"/>"""))
            })
    @ApiResponse(responseCode = "202", description = "Written to the pending requisition. No body.",
            headers = @Header(name = "Location", description = "URI of the stored service.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east/nodes/node-1/interfaces/198.51.100.11/services/SNMP")))
    @ApiResponse(responseCode = "400", description = "The body failed validation. The message names the offending attribute.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Requisition monitored-service 'service-name' is a required attribute!")))
    public Response addOrReplaceService(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") String foreignId, @Parameter(required = true, description = "IP address of the requisitioned interface, exactly as stored.", example = "198.51.100.11") @PathParam("ipAddress") String ipAddress, RequisitionMonitoredService service) {
        try {
            service.validate();
        } catch (final ValidationException e) {
            LOG.error("error validating incoming service '{}'", service, e);
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }
        LOG.info("POST {}: Adding service {} to node {}/{}, interface {}", uriInfo.getPath(), service.getServiceName(), foreignSource, foreignId, ipAddress);
        m_accessService.addOrReplaceService(foreignSource, foreignId, ipAddress, service);
        return Response.accepted().header("Location", getRedirectUri(uriInfo, service.getServiceName())).build();
    }

    /**
     * Updates or adds a category to a node
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param category      a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/categories")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Add or replace one surveillance category on a requisitioned node",
            description = """
                    Keyed on `name`. The category is created at import if it does not already exist, so the name
                    need not appear in `GET /foreignSourcesConfig/categories`.""",
            operationId = "addOrReplaceRequisitionNodeCategory")
    @RequestBody(required = true, description = "The category.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionCategory.class),
                            examples = @ExampleObject(value = """
                                    { "name": "Routers" }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionCategory.class),
                            examples = @ExampleObject(value = """
                                    <category xmlns="http://xmlns.opennms.org/xsd/config/model-import" name="Routers"/>"""))
            })
    @ApiResponse(responseCode = "202", description = "Written to the pending requisition. No body.",
            headers = @Header(name = "Location", description = "URI of the stored category.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east/nodes/node-1/categories/Routers")))
    @ApiResponse(responseCode = "400", description = "The body failed validation. The message names the offending attribute.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Requisition category 'name' is a required attribute!")))
    public Response addOrReplaceNodeCategory(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") String foreignId, RequisitionCategory category) {
        try {
            category.validate();
        } catch (final ValidationException e) {
            LOG.error("error validating incoming category '{}'", category, e);
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }
        LOG.info("POST {}: Adding category {} to node {}/{}", uriInfo.getPath(), category.getName(), foreignSource, foreignId);
        m_accessService.addOrReplaceNodeCategory(foreignSource, foreignId, category);
        return Response.accepted().header("Location", getRedirectUri(uriInfo, category.getName())).build();
    }

    /**
     * Updates or adds an asset parameter to a node
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param asset         a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/assets")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Operation(
            summary = "Add or replace one asset field on a requisitioned node",
            description = """
                    Keyed on `name`, which should be one of the field names
                    `GET /foreignSourcesConfig/assets` returns. An unrecognized name is stored without complaint
                    and then has no effect at import.""",
            operationId = "addOrReplaceRequisitionNodeAsset")
    @RequestBody(required = true, description = "The asset field and its value.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RequisitionAsset.class),
                            examples = @ExampleObject(value = """
                                    { "name": "region", "value": "east" }""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = RequisitionAsset.class),
                            examples = @ExampleObject(value = """
                                    <asset xmlns="http://xmlns.opennms.org/xsd/config/model-import" name="region" value="east"/>"""))
            })
    @ApiResponse(responseCode = "202", description = "Written to the pending requisition. No body.",
            headers = @Header(name = "Location", description = "URI of the stored asset field.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east/nodes/node-1/assets/region")))
    @ApiResponse(responseCode = "400", description = "The body failed validation. The message names the offending attribute.",
            content = @Content(mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "Requisition asset 'name' is a required attribute!")))
    public Response addOrReplaceNodeAssetParameter(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") String foreignId, RequisitionAsset asset) {
        try {
            asset.validate();
        } catch (final ValidationException e) {
            LOG.error("error validating incoming asset '{}'", asset, e);
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }
        LOG.info("POST {}: Adding asset {} to node {}/{}", uriInfo.getPath(), asset.getName(), foreignSource, foreignId);
        m_accessService.addOrReplaceNodeAssetParameter(foreignSource, foreignId, asset);
        return Response.accepted().header("Location", getRedirectUri(uriInfo, asset.getName())).build();
    }

    /**
     * <p>importRequisition</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}/import")
    @Transactional
    @Operation(
            summary = "Import (synchronize) a requisition",
            description = """
                    Snapshots the requisition, sends provisiond a `reloadImport` event pointing at the snapshot,
                    and returns. Provisiond then reconciles the database against the document: nodes in the
                    requisition but not in the database are created, nodes in both are updated, and **nodes in
                    the database but no longer in the requisition are deleted**. On success the document moves
                    from the pending repository to the deployed one, `last-import` is stamped, and
                    `/requisitions/count` drops back to zero.

                    **This is asynchronous.** The 202 means the event was accepted, not that any node exists yet.
                    A large requisition can take minutes, and the request can succeed while the import then
                    fails. Poll `GET /requisitions/deployed/stats/{foreignSource}`, or the node list, rather than
                    treating the 202 as completion. Firing several imports for the same foreign source in quick
                    succession queues them all; they are not coalesced.

                    If there is no pending document, the already-deployed document is re-imported.

                    Deleting a requisition does not undo an import: `DELETE /requisitions/deployed/{foreignSource}`
                    removes the document and leaves the provisioned nodes in place. To remove the nodes, empty
                    the requisition and import it, or delete the nodes through the nodes API.""",
            operationId = "importRequisition")
    @ApiResponse(responseCode = "202", description = "The import event was sent. No body. The import itself has not necessarily started, let alone finished.",
            headers = @Header(name = "Location", description = "URI of the requisition that is being imported.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east")))
    public Response importRequisition(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource,
            @Parameter(description = """
                    How to treat nodes that already exist. `true` runs a full rescan of every node in the
                    requisition; `false` applies the database changes and scans only nodes that were added, so
                    existing nodes are left until their next scheduled scan; `dbonly` applies the database
                    changes and scans nothing.

                    The value is forwarded to provisiond verbatim without being validated here, so a
                    misspelling is accepted with a 202 and then behaves like `false` downstream. Omitting the
                    parameter leaves the event parameter unset, in which case provisiond falls back to
                    `org.opennms.provisiond.scheduleRescanForUpdatedNodes`, whose own default is `true`.""",
                    example = "false",
                    schema = @Schema(type = "string", allowableValues = {"true", "false", "dbonly"}))
            @QueryParam("rescanExisting") final String rescanExisting) {
        LOG.info("PUT {}: Importing requisition for foreign source {}", uriInfo.getPath(), foreignSource);
        m_accessService.importRequisition(foreignSource, rescanExisting);
        return Response.accepted().header("Location", uriInfo.getBaseUriBuilder().path(this.getClass()).path(this.getClass(), "getRequisition").build(foreignSource)).build();
    }

    /**
     * Updates the requisition with foreign source "foreignSource"
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param params        a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Update scalar fields of a requisition",
            description = """
                    Form-encoded key/value pairs applied to the requisition envelope, not to its nodes. Keys are
                    normalized to bean property names, so both `foreign-source` and `foreignSource` reach the
                    same property. `id`, `dbId`, `nodeId` and `authorizedGroups` are protected and skipped with a
                    log line; any other key that does not name a writable property is ignored.

                    The response is 202 whether or not a key matched. Read the resource back to confirm the
                    change.""",
            operationId = "updateRequisition")
    @RequestBody(required = true, description = "Form-encoded field names and values.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = MultivaluedMapImpl.class),
                    examples = @ExampleObject(value = "foreign-source=datacenter-east")))
    @ApiResponse(responseCode = "202", description = "The pending requisition was written. No body. Returned even when no key matched a writable property.",
            headers = @Header(name = "Location", description = "URI of the updated requisition.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east")))
    public Response updateRequisition(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, final MultivaluedMapImpl params) {
        LOG.info("PUT {}: Updated {} with params '{}'", uriInfo.getPath(), foreignSource, params.isEmpty() ? "None" : params.toString());
        m_accessService.updateRequisition(foreignSource, params);
        return Response.accepted().header("Location", getRedirectUri(uriInfo)).build();
    }

    /**
     * Updates the node with foreign id "foreignId" in foreign source "foreignSource"
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param params        a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}/nodes/{foreignId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Update scalar fields of a requisitioned node",
            description = """
                    Form-encoded key/value pairs applied to the node's own fields: `node-label`, `building`,
                    `city`, `location`, `parent-foreign-source`, `parent-foreign-id`, `parent-node-label`. Keys
                    are normalized to bean property names, so `node-label` and `nodeLabel` are equivalent. Keys
                    that name no writable property are ignored, and `id`, `dbId`, `nodeId` and `authorizedGroups`
                    are protected and skipped.

                    Interfaces, categories and assets are collections and are not settable this way; use their
                    sub-resources. The response is 202 whether or not a key matched, so read the node back to
                    confirm.""",
            operationId = "updateRequisitionNode")
    @RequestBody(required = true, description = "Form-encoded field names and values.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = MultivaluedMapImpl.class),
                    examples = @ExampleObject(value = "node-label=router-1a&building=Annex")))
    @ApiResponse(responseCode = "202", description = "The pending requisition was written. No body. Returned even when no key matched a writable property.",
            headers = @Header(name = "Location", description = "URI of the updated node.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east/nodes/node-1")))
    public Response updateNode(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, final MultivaluedMapImpl params) {
        LOG.info("PUT {}: Updated node {}/{} with params '{}'", uriInfo.getPath(), foreignSource, foreignId, params.isEmpty() ? "None" : params.toString());
        m_accessService.updateNode(foreignSource, foreignId, params);
        return Response.accepted().header("Location", getRedirectUri(uriInfo)).build();
    }

    /**
     * Updates a specific interface
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param ipAddress     a {@link java.lang.String} object.
     * @param params        a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(
            summary = "Update scalar fields of a requisitioned IP interface",
            description = """
                    Form-encoded key/value pairs applied to the interface's own fields: `descr`, `status`,
                    `snmp-primary`, `managed`, and `ip-addr` itself. Keys are normalized to bean property names,
                    so `snmp-primary` and `snmpPrimary` are equivalent. `snmp-primary` takes `P`, `S` or `N`.
                    Monitored services are a collection and are not settable this way. The primary-interface and
                    duplicate-service checks that `POST` runs are not applied here.

                    The response is 202 whether or not a key matched.""",
            operationId = "updateRequisitionInterface")
    @RequestBody(required = true, description = "Form-encoded field names and values.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = MultivaluedMapImpl.class),
                    examples = @ExampleObject(value = "descr=eth1&snmp-primary=N")))
    @ApiResponse(responseCode = "202", description = "The pending requisition was written. No body. Returned even when no key matched a writable property.",
            headers = @Header(name = "Location", description = "URI of the updated interface.",
                    schema = @Schema(type = "string", example = "http://localhost:8980/opennms/rest/requisitions/datacenter-east/nodes/node-1/interfaces/198.51.100.11")))
    public Response updateInterface(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "IP address of the requisitioned interface, exactly as stored.", example = "198.51.100.11") @PathParam("ipAddress") final String ipAddress, final MultivaluedMapImpl params) {
        LOG.info("PUT {}: Updated node {}/{} address {} with params '{}'", uriInfo.getPath(), foreignSource, foreignId, ipAddress, params.isEmpty() ? "None" : params.toString());
        m_accessService.updateInterface(foreignSource, foreignId, ipAddress, params);
        return Response.accepted().header("Location", getRedirectUri(uriInfo)).build();
    }

    /**
     * Deletes the pending requisition with foreign source "foreignSource"
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}")
    @Transactional
    @Operation(
            summary = "Delete the pending requisition",
            description = """
                    Removes the pending document, discarding edits that have not been imported. The deployed
                    document and the provisioned nodes are untouched, so a read of the requisition afterwards
                    still returns the last imported version.

                    A foreign source with no pending document is not reported as an error, the response is still
                    202.""",
            operationId = "deletePendingRequisition")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not a pending document existed.")
    public Response deletePendingRequisition(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource) {
        LOG.info("DELETE {}: Deleted the pending requisition for {}", uriInfo.getPath(), foreignSource);
        m_accessService.deletePendingRequisition(foreignSource);
        return Response.accepted().build();
    }

    /**
     * Deletes the deployed requisition with foreign source "foreignSource"
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("deployed/{foreignSource}")
    @Transactional
    @Operation(
            summary = "Delete the deployed requisition",
            description = """
                    Removes the deployed document. The **nodes stay provisioned**: they keep their foreign source
                    and foreign ID and continue to be polled, they simply no longer have a requisition behind
                    them. `GET /requisitions/deployed/stats` still lists the foreign source, with `last-imported`
                    null, and `GET /requisitions/deployed/stats/{foreignSource}` starts failing with a 500 for
                    that name.

                    To retire a group of nodes, empty the requisition and import it, then delete the requisition.
                    A foreign source with no deployed document is not reported as an error, the response is still
                    202.""",
            operationId = "deleteDeployedRequisition")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not a deployed document existed.")
    public Response deleteDeployedRequisition(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource) {
        LOG.info("DELETE {}: Deleted the deployed requisition for {}", uriInfo.getPath(), foreignSource);
        m_accessService.deleteDeployedRequisition(foreignSource);
        return Response.accepted().build();
    }

    /**
     * Delete the node with the given foreign ID for the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}")
    @Transactional
    @Operation(
            summary = "Remove one node from a requisition",
            description = """
                    Removes the node from the pending requisition. The provisioned node is deleted at the next
                    import, not by this call. A foreign ID that is not in the requisition is not reported as an
                    error.""",
            operationId = "deleteRequisitionNode")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not the node was present.")
    public Response deleteNode(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId) {
        LOG.info("DELETE {}: Deleted node {} from {}", uriInfo.getPath(), foreignId, foreignSource);
        m_accessService.deleteNode(foreignSource, foreignId);
        return Response.accepted().build();
    }

    /**
     * <p>deleteInterface</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param ipAddress     a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Transactional
    @Operation(
            summary = "Remove one IP interface from a requisitioned node",
            description = """
                    Removes the interface, and with it its monitored services, from the pending requisition. The
                    provisioned interface is deleted at the next import. An address that is not on the node is
                    not reported as an error.""",
            operationId = "deleteRequisitionInterface")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not the interface was present.")
    public Response deleteInterface(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "IP address of the requisitioned interface, exactly as stored.", example = "198.51.100.11") @PathParam("ipAddress") String ipAddress) {
        LOG.info("DELETE {}: Deleted the IP address {} from node {}/{}", uriInfo.getPath(), ipAddress, foreignSource, foreignId);
        m_accessService.deleteInterface(foreignSource, foreignId, ipAddress);
        return Response.accepted().build();
    }

    /**
     * <p>deleteInterfaceService</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param ipAddress     a {@link java.lang.String} object.
     * @param service       a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services/{service}")
    @Transactional
    @Operation(
            summary = "Remove one monitored service from a requisitioned interface",
            description = """
                    Removes the service from the pending requisition. The provisioned service is deleted at the
                    next import; a detector on the foreign source definition can add it back on the scan that
                    follows. A service name that is not on the interface is not reported as an error.""",
            operationId = "deleteRequisitionService")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not the service was present.")
    public Response deleteInterfaceService(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "IP address of the requisitioned interface, exactly as stored.", example = "198.51.100.11") @PathParam("ipAddress") final String ipAddress, @Parameter(required = true, description = "Monitored service name.", example = "SNMP") @PathParam("service") final String service) {
        LOG.info("DELETE {}: Deleted the service {} from node {}/{} interface {}", uriInfo.getPath(), service, foreignSource, foreignId, ipAddress);
        m_accessService.deleteInterfaceService(foreignSource, foreignId, ipAddress, service);
        return Response.accepted().build();
    }

    /**
     * <p>deleteCategory</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param category      a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/categories/{category}")
    @Transactional
    @Operation(
            summary = "Remove one surveillance category from a requisitioned node",
            description = """
                    Removes the category from the pending requisition; the node leaves the category at the next
                    import. The category itself is not deleted. A category name that is not on the node is not
                    reported as an error.""",
            operationId = "deleteRequisitionNodeCategory")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not the category was present.")
    public Response deleteCategory(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "Surveillance category name.", example = "Production") @PathParam("category") final String category) {
        LOG.info("DELETE {}: Deleted category {} from node {}/{}", uriInfo.getPath(), category, foreignSource, foreignId);
        m_accessService.deleteCategory(foreignSource, foreignId, category);
        return Response.accepted().build();
    }

    /**
     * <p>deleteAssetParameter</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId     a {@link java.lang.String} object.
     * @param parameter     a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/assets/{parameter}")
    @Transactional
    @Operation(
            summary = "Remove one asset field from a requisitioned node",
            description = """
                    Removes the field from the pending requisition, which stops the requisition from setting it.
                    Whether the value already written to the node's asset record is cleared at the next import
                    depends on the import behaviour rather than on this call. An asset name that is not on the
                    node is not reported as an error.""",
            operationId = "deleteRequisitionNodeAsset")
    @ApiResponse(responseCode = "202", description = "Delete attempted. No body. Returned whether or not the asset field was present.")
    public Response deleteAssetParameter(@Context final UriInfo uriInfo, @Parameter(required = true, description = "Foreign source name of the requisition.", example = "datacenter-east") @PathParam("foreignSource") final String foreignSource, @Parameter(required = true, description = "Foreign ID of the node within the requisition.", example = "node-1") @PathParam("foreignId") final String foreignId, @Parameter(required = true, description = "Asset field name.", example = "city") @PathParam("parameter") final String parameter) {
        LOG.info("DELETE {}: Deleted asset value {} from {}/{}", uriInfo.getPath(), parameter, foreignSource, foreignId);
        m_accessService.deleteAssetParameter(foreignSource, foreignId, parameter);
        return Response.accepted().build();
    }
}
