/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.v1;

import static org.opennms.netmgt.provision.persist.requisition.RequisitionMapper.toPersistenceModel;
import static org.opennms.netmgt.provision.persist.requisition.RequisitionMapper.toRestModel;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.ValidationException;

import org.opennms.netmgt.model.requisition.OnmsRequisition;
import org.opennms.netmgt.model.requisition.OnmsRequisitionInterface;
import org.opennms.netmgt.model.requisition.OnmsRequisitionMonitoredService;
import org.opennms.netmgt.model.requisition.OnmsRequisitionNode;
import org.opennms.netmgt.provision.persist.RequisitionService;
import org.opennms.netmgt.provision.persist.requisition.DeployedRequisitionStats;
import org.opennms.netmgt.provision.persist.requisition.DeployedStats;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
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
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *<p>RESTful service to the OpenNMS Provisioning Groups.  In this API, these "groups" of nodes
 *are aptly named and treated as requisitions.</p>
 *<p>This current implementation supports CRUD operations for managing provisioning requisitions.  Requisitions
 *are first POSTed and no provisioning (import) operations are taken.  This is done so that a) the XML can be
 *verified and b) so that the operations can happen at a later time.  They are moved to the deployed state
 *(put in the active requisition repository) when an import is run.
 *<ul>
 *<li>GET/PUT/POST pending requisitions</li>
 *<li>GET pending and deployed count</li>
 *</ul>
 *</p>
 *<p>Example 1: Create a new requisition <i>Note: The foreign-source attribute typically has a 1 to 1
 *relationship to a provisioning group and to the name used in this requisition.  The relationship is
 *implied by name and it is best practice to use the same for all three.  If a foreign source definition
 *exists with the same name, it will be used during the provisioning (import) operations in lieu of the
 *default foreign source</i></p>
 *<pre>
 *curl -X POST \
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
 *</pre>
 *<p>Example 2: Query all deployed requisitions</p>
 *<pre>
 *curl -X GET \
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
@Transactional
// TODO MVR there is no longer a difference between pending and deployed. this must be reworked everywhere or at least kept compatible
public class RequisitionRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(RequisitionRestService.class);

    @Autowired
    private RequisitionService requisitionService;

    /**
     * get a plain text numeric string of the number of deployed requisitions
     *
     * @return a int.
     */
    @GET
    @Path("deployed/count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDeployedCount() {
        return Integer.toString(requisitionService.getDeployedCount());
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
        return requisitionService.getDeployedStats();
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
        return requisitionService.getDeployedStats(foreignSource);
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
        // TODO MVR decide what to do
        // This is no longer configurable.
        // We may remove this resource after all
        return "database";
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
    public RequisitionCollection getDeployedRequisitions() throws ParseException {
        // TODO MVR there is no difference between getRequisitions(), getPendingRequisitions() or getDeployedRequisitions(), what should we do about it?
        return getRequisitions();
    }

    /**
     * Get all the pending requisitions
     *
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionCollection getRequisitions() throws ParseException {
        return new RequisitionCollection(
                requisitionService.getRequisitions().stream()
                        .map(r -> toRestModel(r))
                        .collect(Collectors.toList()));
    }

    /**
     * get a plain text numeric string of the number of pending requisitions
     *
     * @return a int.
     */
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getPendingCount() {
        return "0"; // TODO MVR shall we introduce pending state?!
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
    public Requisition getRequisition(@PathParam("foreignSource") final String foreignSource) {
        final OnmsRequisition requisition = loadRequisition(foreignSource);
        return toRestModel(requisition);
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
    public RequisitionNodeCollection getNodes(@PathParam("foreignSource") final String foreignSource) throws ParseException {
        final List<OnmsRequisitionNode> results = loadRequisition(foreignSource).getNodes();
        return new RequisitionNodeCollection(results.stream()
                .map(n -> toRestModel(n))
                .collect(Collectors.toList()));
    }

    /**
     * Returns the node with the foreign ID specified for the given foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionNode} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionNode getNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) throws ParseException {
        final OnmsRequisitionNode node = loadNode(foreignSource, foreignId);
        return toRestModel(node);
    }

    /**
     * Returns a collection of interfaces for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionInterfaceCollection getInterfacesForNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) throws ParseException {
        final List<OnmsRequisitionInterface> ifaces = loadNode(foreignSource, foreignId).getInterfaces();
        return new RequisitionInterfaceCollection(ifaces.stream()
                .map(iface -> toRestModel(iface))
                .collect(Collectors.toList()));
    }


    /**
     * Returns the interface with the given foreign source/foreignid/ipaddress combination.
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionInterface getInterfaceForNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress) throws ParseException {
        final OnmsRequisitionInterface iface = loadInterface(foreignSource, foreignId, ipAddress);
        return toRestModel(iface);
    }

    /**
     * Returns a collection of services for a given foreignSource/foreignId/interface combination.
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionMonitoredServiceCollection getServicesForInterface(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress) throws ParseException {
        final List<OnmsRequisitionMonitoredService> services = loadInterface(foreignSource, foreignId, ipAddress).getMonitoredServices();
        return new RequisitionMonitoredServiceCollection(services.stream()
                .map(service -> toRestModel(service))
                .collect(Collectors.toList()));
    }

    /**
     * Returns a service for a given foreignSource/foreignId/interface/service-name combination.
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param service a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services/{service}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionMonitoredService getServiceForInterface(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress, @PathParam("service") String service) throws ParseException {
        final OnmsRequisitionMonitoredService monitoredService = loadInterface(foreignSource, foreignId, ipAddress).getMonitoredService(service);
        if (monitoredService == null) {
            throw getException(Status.NOT_FOUND, "Monitored Service {} on IP Interface {} on node with Foreign ID '{}' and Foreign source '{}' not found.", service, ipAddress, foreignId, foreignSource);
        }
        return toRestModel(monitoredService);
    }

    /**
     * Returns a collection of categories for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategoryCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/categories")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionCategoryCollection getCategories(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) throws ParseException {
        final Set<String> categories = loadNode(foreignSource, foreignId).getCategories();
        return new RequisitionCategoryCollection(categories);
    }

    /**
     * Returns the requested category for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param category a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/categories/{category}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public String getCategory(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("category") final String category) throws ParseException {
        OnmsRequisitionNode node = loadNode(foreignSource, foreignId);
        if (!node.getCategories().contains(category)) {
            throw getException(Status.NOT_FOUND, "Category {} on node with Foreign ID '{}' and Foreign source '{}' not found.", category, foreignId, foreignSource);
        }
        return category;
    }

    /**
     * Returns a collection of assets for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAssetCollection} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/assets")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionAssetCollection getAssetParameters(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) throws ParseException {
        final Map<String, String> assets = loadNode(foreignSource, foreignId).getAssets();
        return new RequisitionAssetCollection(assets);
    }

    /**
     * Returns the requested category for a given node in the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param parameter a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     * @throws java.text.ParseException if any.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/assets/{parameter}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionAsset getAssetParameter(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("parameter") final String parameter) throws ParseException {
        final Map<String, String> assets = loadNode(foreignSource, foreignId).getAssets();
        if (!assets.containsKey(parameter)) {
            throw getException(Status.NOT_FOUND, "Asset {} on node with Foreign ID '{}' and Foreign source '{}' not found.", parameter, foreignId, foreignSource);
        }
        return new RequisitionAsset(parameter, assets.get(parameter));
    }

    /**
     * Updates or adds a complete requisition with foreign source "foreignSource"
     *
     * @param requisition a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response addOrReplaceRequisition(@Context final UriInfo uriInfo, final Requisition requisition) {
        try {
            requisition.validate();
        } catch (final ValidationException e) {
            LOG.debug("error validating incoming requisition with foreign source '{}'", requisition.getForeignSource(), e);
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }

        debug("addOrReplaceRequisition: Adding requisition %s (containing %d nodes)", requisition.getForeignSource(), requisition.getNodeCount());
        requisitionService.saveOrUpdateRequisition(toPersistenceModel(requisition));
        return Response.accepted().header("Location", getRedirectUri(uriInfo, requisition.getForeignSource())).build();
    }

    /**
     * Updates or adds a node to a requisition
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionNode} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response addOrReplaceNode(@Context final UriInfo uriInfo, @PathParam("foreignSource") String foreignSource, RequisitionNode node) {
        debug("addOrReplaceNode: Adding node %s to requisition %s", node.getForeignId(), foreignSource);

        // TODO MVR move to service layer
        final OnmsRequisition persistedRequisition = loadRequisition(foreignSource);
        requisitionService.saveOrUpdateNode(persistedRequisition, toPersistenceModel(node));
        return Response.accepted().header("Location", getRedirectUri(uriInfo, node.getForeignId())).build();
    }

    /**
     * Updates or adds an interface to a node
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionInterface} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/interfaces")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response addOrReplaceInterface(@Context final UriInfo uriInfo, @PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionInterface iface) {
        debug("addOrReplaceInterface: Adding interface %s to node %s/%s", iface, foreignSource, foreignId);

        final OnmsRequisitionNode parentNode = loadNode(foreignSource, foreignId);
        requisitionService.saveOrUpdateInterface(parentNode, toPersistenceModel(iface));
        return Response.accepted().header("Location", getRedirectUri(uriInfo, iface.getIpAddr())).build();
    }

    /**
     * Updates or adds a service to an interface
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param service a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response addOrReplaceService(@Context final UriInfo uriInfo, @PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress, RequisitionMonitoredService service) {
        debug("addOrReplaceService: Adding service %s to node %s/%s, interface %s", service.getServiceName(), foreignSource, foreignId, ipAddress);

        OnmsRequisitionInterface persistedInterface = loadInterface(foreignSource, foreignId, ipAddress);
        requisitionService.saveOrUpdateService(persistedInterface, toPersistenceModel(service));
        return Response.accepted().header("Location", getRedirectUri(uriInfo, service.getServiceName())).build();
    }

    /**
     * Updates or adds a category to a node
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/categories")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response addOrReplaceNodeCategory(@Context final UriInfo uriInfo, @PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionCategory category) {
        debug("addOrReplaceNodeCategory: Adding category %s to node %s/%s", category.getName(), foreignSource, foreignId);

        OnmsRequisitionNode requisitionNode = loadNode(foreignSource, foreignId);
        requisitionNode.addCategory(category.getName());
        requisitionService.saveOrUpdateRequisition(requisitionNode.getRequisition());
        return Response.accepted().header("Location", getRedirectUri(uriInfo, category.getName())).build();
    }

    /**
     * Updates or adds an asset parameter to a node
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/assets")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response addOrReplaceNodeAssetParameter(@Context final UriInfo uriInfo, @PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionAsset asset) {
        debug("addOrReplaceNodeCategory: Adding asset %s to node %s/%s", asset.getName(), foreignSource, foreignId);

        OnmsRequisitionNode requisitionNode = loadNode(foreignSource, foreignId);
        requisitionNode.addAsset(asset.getName(), asset.getValue());
        requisitionService.saveOrUpdateRequisition(requisitionNode.getRequisition());
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
    public Response importRequisition(@Context final UriInfo uriInfo, @PathParam("foreignSource") final String foreignSource, @QueryParam("rescanExisting") final String rescanExisting) {
        debug("importRequisition: Importing requisition for foreign source %s", foreignSource);
        ImportRequest importRequest = new ImportRequest("Web").withRescanExisting(rescanExisting).withForeignSource(foreignSource);
        requisitionService.triggerImport(importRequest);
        return Response.accepted().header("Location", uriInfo.getBaseUriBuilder()
                .path(this.getClass()).path(this.getClass(), "getRequisition")
                .build(importRequest.getForeignSource())).build();
    }

    /**
     * Updates the requisition with foreign source "foreignSource"
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    // TODO MVR make deprecated use saveOrReplaceRequisition instead
    public Response updateRequisition(@Context final UriInfo uriInfo, @PathParam("foreignSource") final String foreignSource, final MultivaluedMapImpl params) {
        final Requisition requisition = toRestModel(loadRequisition(foreignSource));
        updateRequisition(requisition, params);
        requisitionService.saveOrUpdateRequisition(toPersistenceModel(requisition));
        return Response.accepted().header("Location", getRedirectUri(uriInfo)).build();
    }

    /**
     * Updates the node with foreign id "foreignId" in foreign source "foreignSource"
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}/nodes/{foreignId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    // TODO MVR make deprecated use saveOrReplaceNode instead
    public Response updateNode(@Context final UriInfo uriInfo, @PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, final MultivaluedMapImpl params) {
        RequisitionNode node = toRestModel(loadNode(foreignSource, foreignId));
        updateNode(foreignSource, node, params);
        requisitionService.saveOrUpdateNode(loadRequisition(foreignSource), toPersistenceModel(node));
        return Response.accepted().header("Location", getRedirectUri(uriInfo)).build();
    }

    /**
     * Updates a specific interface
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    // TODO MVR make deprecated use saveOrReplaceNode instead
    public Response updateInterface(@Context final UriInfo uriInfo, @PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress, final MultivaluedMapImpl params) {
        RequisitionInterface iface = toRestModel(loadInterface(foreignSource, foreignId, ipAddress));
        updateInterface(foreignSource, foreignId, iface, params);
        requisitionService.saveOrUpdateInterface(loadNode(foreignSource, foreignId), toPersistenceModel(iface));
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
    public Response deletePendingRequisition(@PathParam("foreignSource") final String foreignSource) {
        // TODO MVR before this was "deletePendingRequisition" but there is no pending anymore, what should we do about it?
        requisitionService.deleteRequisition(foreignSource);
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
    public Response deleteDeployedRequisition(@PathParam("foreignSource") final String foreignSource) {
        requisitionService.deleteRequisition(foreignSource);
        return Response.accepted().build();
    }

    /**
     * Delete the node with the given foreign ID for the specified foreign source
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}")
    public Response deleteNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) {
        OnmsRequisitionNode node = loadNode(foreignSource, foreignId);
        node.getRequisition().removeNode(node);
        requisitionService.saveOrUpdateRequisition(node.getRequisition());
        return Response.accepted().build();
    }

    /**
     * <p>deleteInterface</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    public Response deleteInterface(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") String ipAddress) {
        OnmsRequisitionInterface iface = loadInterface(foreignSource, foreignId, ipAddress);
        iface.getNode().removeInterface(iface);
        requisitionService.saveOrUpdateRequisition(iface.getNode().getRequisition());
        return Response.accepted().build();
    }

    /**
     * <p>deleteInterfaceService</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param service a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services/{service}")
    public Response deleteInterfaceService(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress, @PathParam("service") final String service) {
        OnmsRequisitionMonitoredService entity = loadService(foreignSource, foreignId, ipAddress, service);
        entity.getIpInterface().removeMonitoredService(entity);
        requisitionService.saveOrUpdateRequisition(entity.getIpInterface().getNode().getRequisition());
        return Response.accepted().build();
    }

    /**
     * <p>deleteCategory</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param category a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/categories/{category}")
    public Response deleteCategory(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("category") final String category) {
        OnmsRequisitionNode node = loadNode(foreignSource, foreignId);
        node.removeCategory(category);
        requisitionService.saveOrUpdateRequisition(node.getRequisition());
        return Response.accepted().build();
    }

    /**
     * <p>deleteAssetParameter</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param parameter a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/assets/{parameter}")
    public Response deleteAssetParameter(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("parameter") final String parameter) {
        OnmsRequisitionNode node = loadNode(foreignSource, foreignId);
        node.getAssets().remove(parameter);
        requisitionService.saveOrUpdateRequisition(node.getRequisition());
        return Response.accepted().build();
    }

    void debug(final String format, final Object... values) {
        LOG.debug(format, values);
    }

    private OnmsRequisition loadRequisition(String foreignSource) {
        OnmsRequisition requisition = requisitionService.getRequisition(foreignSource);
        if (requisition == null) {
            throw getException(Status.NOT_FOUND, "Requisition '{}' not found.", foreignSource);
        }
        return requisition;
    }

    private OnmsRequisitionNode loadNode(String foreignSource, String foreignId) {
        OnmsRequisitionNode node = loadRequisition(foreignSource).getNode(foreignId);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "Node with Foreign ID '{}' and Foreign source '{}' not found.", foreignId, foreignSource);
        }
        return node;
    }

    private OnmsRequisitionInterface loadInterface(String foreignSource, String foreignId, String ipAddress) {
        OnmsRequisitionInterface ipInterface = loadNode(foreignSource, foreignId).getInterface(ipAddress);
        if (ipInterface == null) {
            throw getException(Status.NOT_FOUND, "IP Interface {} on node with Foreign ID '{}' and Foreign source '{}' not found.", ipAddress, foreignId, foreignSource);
        }
        return ipInterface;
    }

    private OnmsRequisitionMonitoredService loadService(String foreignSource, String foreignId, String ipAddress, String serviceName) {
        OnmsRequisitionMonitoredService monitoredService = loadInterface(foreignSource, foreignId, ipAddress).getMonitoredService(serviceName);
        if (monitoredService == null) {
            throw getException(Status.NOT_FOUND, "Service {} on interface with ip '{}' on node with Foreign ID '{}' and Foreign source '{}' not found.", serviceName, ipAddress, foreignId, getForeignSourceRepositoryStrategy());
        }
        return monitoredService;
    }

    // TODO MVR ...
    private static void updateRequisition(Requisition requisition, final MultivaluedMap<String,String> params) {
        LOG.debug("updateRequisition: Updating requisition with foreign source {}", requisition.getForeignSource());
        if (params.isEmpty()) return;
        RestUtils.setBeanProperties(requisition, params);
        LOG.debug("updateRequisition: Requisition with foreign source {} updated", requisition);
    }

    // TODO MVR ...
    private static void updateNode(String foreignSource, RequisitionNode node, final MultivaluedMap<String,String> params) {
        LOG.debug("updateNode: Updating node with foreign source {} and foreign id {}", foreignSource, node.getForeignId());
        if (params.isEmpty()) return;
        RestUtils.setBeanProperties(node, params);
        LOG.debug("updateNode: Node with foreign source {} and foreign id {} updated", foreignSource, node.getForeignId());
    }

    // TODO MVR ...
    private static void updateInterface(final String foreignSource, final String foreignId, RequisitionInterface iface, MultivaluedMap<String,String> params) {
        LOG.debug("updateInterface: Updating interface {} on node {}/{}", iface.getIpAddr(), foreignSource, foreignId);
        if (params.isEmpty()) return;
        RestUtils.setBeanProperties(iface, params);
        LOG.debug("updateInterface: Interface {} on node {}/{} updated",  iface.getIpAddr(), foreignSource, foreignId);
    }
}
