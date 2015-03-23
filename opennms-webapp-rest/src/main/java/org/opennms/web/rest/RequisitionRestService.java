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

package org.opennms.web.rest;

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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.ValidationException;

import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory;
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
import org.opennms.web.svclayer.api.RequisitionAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;


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
@Component
@PerRequest
@Scope("prototype")
@Path("requisitions")
public class RequisitionRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(RequisitionRestService.class);


    @Autowired
    private RequisitionAccessService m_accessService;

    @Autowired
    private ForeignSourceRepositoryFactory m_foreignSourceRepositoryFactory;

    @Context
    UriInfo m_uriInfo;

    @Context
    HttpHeaders m_headers;

    @Context
    SecurityContext m_securityContext;

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
    public String getDeployedCount() {
        return Integer.toString(m_accessService.getDeployedCount());
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
    public Requisition getRequisition(@PathParam("foreignSource") final String foreignSource) {
        return m_accessService.getRequisition(foreignSource);
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
        final RequisitionNodeCollection results = m_accessService.getNodes(foreignSource);

        if (results == null) {
            throw getException(Response.Status.NOT_FOUND, "Foreign source '" + foreignSource + "' not found.");
        }
        return results;
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
        final RequisitionNode node = m_accessService.getNode(foreignSource, foreignId);

        if (node == null) {
            throw getException(Response.Status.NOT_FOUND, "Node with Foreign ID '" + foreignId +"' and Foreign source '" + foreignSource + "' not found.");
        }
        return node;


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
        final RequisitionInterfaceCollection ifaces = m_accessService.getInterfacesForNode(foreignSource, foreignId);

        if (ifaces == null) {
            throw getException(Response.Status.NOT_FOUND, "Foreign ID '" + foreignId + "' not found in foreign source '" + foreignSource + "'.");
        }

        return ifaces;
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
        final RequisitionInterface iface = m_accessService.getInterfaceForNode(foreignSource, foreignId, ipAddress);

        if (iface == null) {
            throw getException(Response.Status.NOT_FOUND, "Foreign ID '" + foreignId + "' not found in foreign source '" + foreignSource + "'.");
        }

        return iface;
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
        final RequisitionMonitoredServiceCollection services = m_accessService.getServicesForInterface(foreignSource, foreignId, ipAddress);

        if (services == null) {
            throw getException(Response.Status.NOT_FOUND, "Unable to locate IP address '" + ipAddress + "' in " + foreignSource + ":" + foreignId + ".");
        }

        return services;
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
        final RequisitionMonitoredService monitoredService = m_accessService.getServiceForInterface(foreignSource, foreignId, ipAddress, service);

        if (monitoredService == null) {
            throw getException(Response.Status.NOT_FOUND, "Unable to locate IP address '" + ipAddress + "' in " + foreignSource + ":" + foreignId + ".");
        }

        return monitoredService;
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
        final RequisitionCategoryCollection categories = m_accessService.getCategories(foreignSource, foreignId);

        if (categories == null) {
            throw getException(Response.Status.NOT_FOUND, "Unable to location node with ForeignSource: " + foreignSource + " and ForeignId: " + foreignId);
        }

        return categories;
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
    public RequisitionCategory getCategory(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("category") final String category) throws ParseException {
        final RequisitionCategory reqCategory = m_accessService.getCategory(foreignSource, foreignId, category);

        if (reqCategory == null) {
            throw getException(Response.Status.NOT_FOUND, "Unable to find category " + category + " on node with Foreign ID '" + foreignId + "' and foreign source '" + foreignSource + "'.");
        }
        return reqCategory;
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
        final RequisitionAssetCollection assets = m_accessService.getAssetParameters(foreignSource, foreignId);

        if (assets == null) {
            throw getException(Response.Status.NOT_FOUND, "Unable able to find assets for node with foreign ID '" + foreignId + "' not found in foreign source '" + foreignSource + "'.");
        }
        return assets;
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
        final RequisitionAsset asset = m_accessService.getAssetParameter(foreignSource, foreignId, parameter);

        if (asset == null) {
            throw getException(Response.Status.NOT_FOUND, "Unable to find asset " + parameter + " for node with foreign ID '" + foreignId + "' not found in foreign source '" + foreignSource + "'.");
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
    public Response addOrReplaceRequisition(final Requisition requisition) {
        try {
            requisition.validate();
        } catch (final ValidationException e) {
            LOG.debug("error validating incoming requisition with foreign source '{}'", requisition.getForeignSource(), e);
            throw getException(Status.BAD_REQUEST, e.getMessage());
        }

        debug("addOrReplaceRequisition: Adding requisition %s (containing %d nodes)", requisition.getForeignSource(), requisition.getNodeCount());
        m_accessService.addOrReplaceRequisition(requisition);
        return Response.seeOther(getRedirectUri(m_uriInfo, requisition.getForeignSource())).build();

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
    @Transactional
    public Response addOrReplaceNode(@PathParam("foreignSource") String foreignSource, RequisitionNode node) {
        debug("addOrReplaceNode: Adding node %s to requisition %s", node.getForeignId(), foreignSource);
        m_accessService.addOrReplaceNode(foreignSource, node);
        return Response.seeOther(getRedirectUri(m_uriInfo, node.getForeignId())).build();
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
    @Transactional
    public Response addOrReplaceInterface(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionInterface iface) {
        debug("addOrReplaceInterface: Adding interface %s to node %s/%s", iface, foreignSource, foreignId);
        m_accessService.addOrReplaceInterface(foreignSource, foreignId, iface);
        return Response.seeOther(getRedirectUri(m_uriInfo, iface.getIpAddr())).build();
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
    @Transactional
    public Response addOrReplaceService(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress, RequisitionMonitoredService service) {
        debug("addOrReplaceService: Adding service %s to node %s/%s, interface %s", service.getServiceName(), foreignSource, foreignId, ipAddress);
        m_accessService.addOrReplaceService(foreignSource, foreignId, ipAddress, service);
        return Response.seeOther(getRedirectUri(m_uriInfo, service.getServiceName())).build();
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
    @Transactional
    public Response addOrReplaceNodeCategory(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionCategory category) {
        debug("addOrReplaceNodeCategory: Adding category %s to node %s/%s", category.getName(), foreignSource, foreignId);
        m_accessService.addOrReplaceNodeCategory(foreignSource, foreignId, category);
        return Response.seeOther(getRedirectUri(m_uriInfo, category.getName())).build();
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
    @Transactional
    public Response addOrReplaceNodeAssetParameter(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionAsset asset) {
        debug("addOrReplaceNodeCategory: Adding asset %s to node %s/%s", asset.getName(), foreignSource, foreignId);
        m_accessService.addOrReplaceNodeAssetParameter(foreignSource, foreignId, asset);
        return Response.seeOther(getRedirectUri(m_uriInfo, asset.getName())).build();
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
    public Response importRequisition(@PathParam("foreignSource") final String foreignSource, @QueryParam("rescanExisting") final String rescanExisting) {
        debug("importRequisition: Importing requisition for foreign source %s", foreignSource);
        m_accessService.importRequisition(foreignSource, rescanExisting);
        return Response.seeOther(m_uriInfo.getBaseUriBuilder().path(this.getClass()).path(this.getClass(), "getRequisition").build(foreignSource)).build();
    }

    /**
     * Updates the requisition with foreign source "foreignSource"
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateRequisition(@PathParam("foreignSource") final String foreignSource, final MultivaluedMapImpl params) {
        m_accessService.updateRequisition(foreignSource, params);
        return Response.seeOther(getRedirectUri(m_uriInfo)).build();
    }

    /**
     * Updates the node with foreign id "foreignId" in foreign source "foreignSource"
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}/nodes/{foreignId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, final MultivaluedMapImpl params) {
        m_accessService.updateNode(foreignSource, foreignId, params);
        return Response.seeOther(getRedirectUri(m_uriInfo)).build();
    }

    /**
     * Updates a specific interface
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateInterface(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress, final MultivaluedMapImpl params) {
        m_accessService.updateInterface(foreignSource, foreignId, ipAddress, params);
        return Response.seeOther(getRedirectUri(m_uriInfo)).build();
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
    public Response deletePendingRequisition(@PathParam("foreignSource") final String foreignSource) {
        m_accessService.deletePendingRequisition(foreignSource);
        return Response.ok().build();
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
    public Response deleteDeployedRequisition(@PathParam("foreignSource") final String foreignSource) {
        m_accessService.deleteDeployedRequisition(foreignSource);
        return Response.ok().build();
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
    @Transactional
    public Response deleteNode(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) {
        m_accessService.deleteNode(foreignSource, foreignId);
        return Response.ok().build();
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
    @Transactional
    public Response deleteInterface(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") String ipAddress) {
        m_accessService.deleteInterface(foreignSource, foreignId, ipAddress);
        return Response.ok().build();

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
    @Transactional
    public Response deleteInterfaceService(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("ipAddress") final String ipAddress, @PathParam("service") final String service) {
        m_accessService.deleteInterfaceService(foreignSource, foreignId, ipAddress, service);
        return Response.ok().build();
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
    @Transactional
    public Response deleteCategory(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("category") final String category) {
        m_accessService.deleteCategory(foreignSource, foreignId, category);
        return Response.ok().build();
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
    @Transactional
    public Response deleteAssetParameter(@PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId, @PathParam("parameter") final String parameter) {
        m_accessService.deleteAssetParameter(foreignSource, foreignId, parameter);
        return Response.ok().build();
    }

    void debug(final String format, final Object... values) {
        LOG.debug(format, values);
    }
}
