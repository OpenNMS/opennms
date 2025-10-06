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
@Tag(name = "Requisitions", description = "Requisitions API")
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
    @Operation(summary = "Get the number of deployed requisitions", description = "Get the number of deployed requisitions (returns plaintext rather than XML or JSON).", responses = {@ApiResponse(responseCode = "200", description = "Get the number of deployed requisitions (returns plaintext rather than XML or JSON).")})
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
    public DeployedRequisitionStats getDeployedStats(@PathParam("foreignSource") final String foreignSource) {
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
    @Operation(summary = "Get a list of all deployed (active) requisitions.", responses = {@ApiResponse(responseCode = "200", description = "Get a list of all deployed (active) requisitions.")})
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
    @Operation(summary = "Get all active requisitions.", responses = {@ApiResponse(responseCode = "200", description = "Get all active requisitions.")

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
    @Operation(summary = "Get the number of undeployed requisitions", responses = {@ApiResponse(responseCode = "200", description = "Get the number of undeployed requisitions (returns plaintext rather than XML or JSON).")})
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
    @Operation(summary = "Get the active requisition for the given foreign source name.", responses = {@ApiResponse(responseCode = "200",
            description = "Get the active requisition for the given foreign source name."), @ApiResponse(responseCode = "404")})
    public Requisition getRequisition(@PathParam("foreignSource") final String foreignSource) {
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
    @Operation(summary = "Get the list of nodes being requisitioned for the given foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the list of nodes being requisitioned for the given foreign source name."

    ), @ApiResponse(responseCode = "404")})
    public RequisitionNodeCollection getNodes(@PathParam("foreignSource") final String foreignSource) throws ParseException {
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
    @Operation(summary = "Get the node with the given foreign ID for the given foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the node with the given foreign ID for the given foreign source name."), @ApiResponse(responseCode = "404")})
    public RequisitionNode getNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) throws ParseException {
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
    @Operation(summary = "Get the interfaces for the node with the given foreign ID and foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the interfaces for the node with the given foreign ID and foreign source name."), @ApiResponse(responseCode = "404")

    })
    public RequisitionInterfaceCollection getInterfacesForNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) throws ParseException {
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
    @Operation(summary = "Get the interface with the given IP for the node with the specified foreign ID and foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the interface with the given IP for the node with the specified foreign ID and foreign source name."), @ApiResponse(responseCode = "404")})
    public RequisitionInterface getInterfaceForNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress) throws ParseException {
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
    @Operation(summary = "Get the services for the interface with the specified IP address, foreign ID, and foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the services for the interface with the specified IP address, foreign ID, and foreign source name."), @ApiResponse(responseCode = "404")

    })
    public RequisitionMonitoredServiceCollection getServicesForInterface(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress) throws ParseException {
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
    @Operation(summary = "Get the given service with the specified IP address, foreign ID, and foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the given service with the specified IP address, foreign ID, and foreign source name."), @ApiResponse(responseCode = "404")

    })
    public RequisitionMonitoredService getServiceForInterface(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress, @PathParam("service") String service) throws ParseException {
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
    @Operation(summary = "Get the categories for the node with the given foreign ID and foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the categories for the node with the given foreign ID and foreign source name."), @ApiResponse(responseCode = "404")

    })
    public RequisitionCategoryCollection getCategories(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) throws ParseException {
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
    @Operation(summary = "Get the category with the given name for the node with the specified foreign ID and foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the category with the given name for the node with the specified foreign ID and foreign source name."), @ApiResponse(responseCode = "404")

    })
    public RequisitionCategory getCategory(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("category") final String category) throws ParseException {
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
    @Operation(summary = "Get the assets for the node with the given foreign ID and foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the assets for the node with the given foreign ID and foreign source name."), @ApiResponse(responseCode = "404")

    })
    public RequisitionAssetCollection getAssetParameters(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) throws ParseException {
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
    @Operation(summary = "Get the value of the asset for the given assetName for the node with the given foreign ID and foreign source name.", responses = {@ApiResponse(responseCode = "200", description = "Get the value of the asset for the given assetName for the node with the given foreign ID and foreign source name."), @ApiResponse(responseCode = "404")

    })
    public RequisitionAsset getAssetParameter(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("parameter") final String parameter) throws ParseException {
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
    @Operation(summary = "Adds or replaces a requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Adds or replaces a requisition.")})
    public Response addOrReplaceRequisition(@Context final UriInfo uriInfo, @RequestBody(description = "Requisition") final Requisition requisition) {
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
    @Operation(summary = "Adds or replaces a node in the specified requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Adds or replaces a node in the specified requisition. This operation can be very helpful when working with large requisitions.")})
    public Response addOrReplaceNode(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") String foreignSource, @RequestBody(description = "Node") RequisitionNode node) {
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
    @Operation(summary = "Adds or replaces an interface for the given node in the specified requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Adds or replaces an interface for the given node in the specified requisition.")})
    public Response addOrReplaceInterface(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") String foreignSource, @Parameter(required = true) @PathParam("foreignId") String foreignId, @RequestBody(description = "Interface") RequisitionInterface iface) {
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
    @Operation(summary = "Adds or replaces a service on the given interface in the specified requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Adds or replaces a service on the given interface in the specified requisition.")})
    public Response addOrReplaceService(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") String foreignSource, @Parameter(required = true) @PathParam("foreignId") String foreignId, @Parameter(required = true) @PathParam("ipAddress") String ipAddress, @RequestBody(description = "Monitored Service") RequisitionMonitoredService service) {
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
    @Operation(summary = "Adds or replaces a category for the given node in the specified requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Adds or replaces a category for the given node in the specified requisition.")})
    public Response addOrReplaceNodeCategory(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") String foreignSource, @Parameter(required = true) @PathParam("foreignId") String foreignId, @RequestBody(description = "Category") RequisitionCategory category) {
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
    @Operation(summary = "Adds or replaces an asset for the given node in the specified requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Adds or replaces an asset for the given node in the specified requisition.")})
    public Response addOrReplaceNodeAssetParameter(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") String foreignSource, @Parameter(required = true) @PathParam("foreignId") String foreignId, @RequestBody(description = "Asset") RequisitionAsset asset) {
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
    @Operation(summary = "Performs an import or synchronize on the specified requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Performs an import or synchronize on the specified requisition. This turns the \"active\" requisition into a \"deployed\" requisition.")})
    public Response importRequisition(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @Parameter(description = "If it is newly added or removed. Existing nodes are not scanned until the next rescan interval. This request type is useful when applying changes to a subset of nodes in a requisition.") @QueryParam("rescanExisting") final String rescanExisting) {
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
    @Operation(summary = "Update the specified requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Update the specified requisition.")})
    public Response updateRequisition(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @RequestBody(description = "Requisition key/value pairs") final MultivaluedMapImpl params) {
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
    @Operation(summary = "Update the specified node for the given requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Update the specified node for the given requisition.")})
    public Response updateNode(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @Parameter(required = true) @PathParam("foreignId") final String foreignId, @RequestBody(description = "Node key/value pairs") final MultivaluedMapImpl params) {
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
    @Operation(summary = "Update the specified IP address for the given node and requisition.", responses = {@ApiResponse(headers = @Header(name = "Location"), responseCode = "202", description = "Update the specified IP address for the given node and requisition.")})
    public Response updateInterface(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @Parameter(required = true) @PathParam("foreignId") final String foreignId, @Parameter(required = true) @PathParam("ipAddress") final String ipAddress, @RequestBody(description = "Interface key/value pairs") final MultivaluedMapImpl params) {
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
    @Operation(summary = "Delete the pending requisition for the named foreign source.", responses = @ApiResponse(responseCode = "202", description = "Delete the pending requisition for the named foreign source."))
    public Response deletePendingRequisition(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource) {
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
    @Operation(summary = "Delete the active requisition for the named foreign source.", responses = @ApiResponse(responseCode = "202", description = "Delete the active requisition for the named foreign source."))
    public Response deleteDeployedRequisition(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource) {
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
    @Operation(summary = "Delete the node with the given foreign ID from the given requisition.", responses = @ApiResponse(responseCode = "202", description = "Delete the node with the given foreign ID from the given requisition."))
    public Response deleteNode(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @Parameter(required = true) @PathParam("foreignId") final String foreignId) {
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
    @Operation(summary = "Delete the IP address from the requisitioned node with the given foreign ID.", description = "Delete the IP address from the requisitioned node with the given foreign ID.", responses = @ApiResponse(responseCode = "202"))
    public Response deleteInterface(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @Parameter(required = true) @PathParam("foreignId") final String foreignId, @Parameter(required = true) @PathParam("ipAddress") String ipAddress) {
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
    @Operation(summary = "Delete the service from the requisitioned interface with the given IP address and foreign ID.", responses = @ApiResponse(responseCode = "202", description = "Delete the service from the requisitioned interface with the given IP address and foreign ID."))
    public Response deleteInterfaceService(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @Parameter(required = true) @PathParam("foreignId") final String foreignId, @Parameter(required = true) @PathParam("ipAddress") final String ipAddress, @Parameter(required = true) @PathParam("service") final String service) {
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
    @Operation(summary = "Delete the category from the node with the given foreign ID.", responses = @ApiResponse(responseCode = "202", description = "Delete the category from the node with the given foreign ID."))
    public Response deleteCategory(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @Parameter(required = true) @PathParam("foreignId") final String foreignId, @Parameter(required = true) @PathParam("category") final String category) {
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
    @Operation(summary = "Delete the field from the node’s assets with the given foreign ID and asset name.", responses = @ApiResponse(responseCode = "202", description = "Delete the field from the node’s assets with the given foreign ID and asset name."))
    public Response deleteAssetParameter(@Context final UriInfo uriInfo, @Parameter(required = true) @PathParam("foreignSource") final String foreignSource, @Parameter(required = true) @PathParam("foreignId") final String foreignId, @Parameter(required = true) @PathParam("parameter") final String parameter) {
        LOG.info("DELETE {}: Deleted asset value {} from {}/{}", uriInfo.getPath(), parameter, foreignSource, foreignId);
        m_accessService.deleteAssetParameter(foreignSource, foreignId, parameter);
        return Response.accepted().build();
    }
}
