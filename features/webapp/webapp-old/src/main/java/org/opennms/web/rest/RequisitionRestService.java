/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 21, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.rest;

import java.net.URL;
import java.text.ParseException;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
<p>RESTful service to the OpenNMS Provisioning Groups.  In this API, these "groups" of nodes 
are aptly named and treated as requisitions.</p>
<p>This current implementation supports CRUD operations for managing provisioning requisitions.  Requisitions
are first POSTed and no provisioning (import) operations are taken.  This is done so that a) the XML can be
verified and b) so that the operations can happen at a later time.  They are moved to the deployed state
(put in the active requisition repository) when an import is run.
<ul>
<li>GET/PUT/POST pending requisitions</li>
<li>GET pending and deployed count</li>
</ul>
</p>
<p>Example 1: Create a new requisition <i>Note: The foreign-source attribute typically has a 1 to 1 
relationship to a provisioning group and to the name used in this requisition.  The relationship is 
implied by name and it is best practice to use the same for all three.  If a foreign source definition
exists with the same name, it will be used during the provisioning (import) operations in lieu of the
default foreign source</i></p>
<pre>
curl -X POST \
     -H "Content-Type: application/xml" \
     -d "&lt;?xml version="1.0" encoding="UTF-8"?&gt;
         &lt;model-import xmlns="http://xmlns.opennms.org/xsd/config/model-import" 
             date-stamp="2009-03-07T17:56:53.123-05:00"
             last-import="2009-03-07T17:56:53.117-05:00" foreign-source="site1"&gt;
           &lt;node node-label="p-brane" foreign-id="1" &gt;
             &lt;interface ip-addr="10.0.1.3" descr="en1" status="1" snmp-primary="P"&gt;
               &lt;monitored-service service-name="ICMP"/&gt;
               &lt;monitored-service service-name="SNMP"/&gt;
             &lt;/interface&gt;
             &lt;category name="Production"/&gt;
             &lt;category name="Routers"/&gt;
           &lt;/node&gt;
         &lt;/model-import&gt;" \
     -u admin:admin \
     http://localhost:8980/opennms/rest/requisitions
</pre>
<p>Example 2: Query all deployed requisitions</p>
<pre>
curl -X GET \
     -H "Content-Type: application/xml" \
     -u admin:admin \
        http://localhost:8980/opennms/rest/requisitions/deployed \
        2>/dev/null \
        |xmllint --format -</pre>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("requisitions")
public class RequisitionRestService extends OnmsRestService {
    
    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository m_pendingForeignSourceRepository;
    
    @Autowired
    @Qualifier("deployed")
    private ForeignSourceRepository m_deployedForeignSourceRepository;
    
    @Autowired
    private EventProxy m_eventProxy;
    
    @Context
    UriInfo m_uriInfo;

    @Context
    HttpHeaders m_headers;

    @Context
    SecurityContext m_securityContext;

    /**
     * get a plain text numeric string of the number of deployed requisitions
     * @return
     */
    @GET
    @Path("deployed/count")
    @Produces(MediaType.TEXT_PLAIN)
    public int getDeployedCount() {
        return m_deployedForeignSourceRepository.getRequisitions().size();
    }

    /**
     * Get all the deployed requisitions
     */
    @GET
    @Path("deployed")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionCollection getDeployedRequisitions() throws ParseException {
        return new RequisitionCollection(m_deployedForeignSourceRepository.getRequisitions());
    }

    /**
     * Get all the pending requisitions
     */
    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionCollection getRequisitions() throws ParseException {
        Set<Requisition> reqs = new TreeSet<Requisition>();
        for (String fsName : getActiveForeignSourceNames()) {
            Requisition r = getActiveRequisition(fsName);
            if (r != null) {
                reqs.add(r);
            }
        }
        return new RequisitionCollection(reqs);
    }

    /**
     * get a plain text numeric string of the number of pending requisitions
     * @return
     */
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public int getPendingCount() {
        return m_pendingForeignSourceRepository.getRequisitions().size();
    }

    @GET
    @Path("{foreignSource}")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Requisition getRequisition(@PathParam("foreignSource") String foreignSource) {
        return getActiveRequisition(foreignSource);
    }

    /**
     * Returns all nodes for a given requisition
     */
    @GET
    @Path("{foreignSource}/nodes")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionNodeCollection getNodes(@PathParam("foreignSource") String foreignSource) throws ParseException {
        Requisition req = getActiveRequisition(foreignSource);
        if (req == null) {
            return null;
        }
        return new RequisitionNodeCollection(req.getNodes());
    }

    /**
     * Returns the node with the foreign ID specified for the given foreign source
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionNode getNode(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId) throws ParseException {
        Requisition req = getActiveRequisition(foreignSource);
        if (req == null) {
            return null;
        }
        return req.getNode(foreignId);
    }

    /**
     * Returns a collection of interfaces for a given node in the specified foreign source
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionInterfaceCollection getInterfacesForNode(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId) throws ParseException {
        RequisitionNode node = getNode(foreignSource, foreignId);
        if (node != null) {
            return new RequisitionInterfaceCollection(node.getInterfaces());
        }
        return null;
    }

    /**
     * Returns the interface with the given foreign source/foreignid/ipaddress combination.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionInterface getInterfaceForNode(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress) throws ParseException {
        RequisitionNode node = getNode(foreignSource, foreignId);
        if (node != null) {
            return node.getInterface(ipAddress);
        }
        return null;
    }
    
    /**
     * Returns a collection of services for a given foreignSource/foreignId/interface combination.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionMonitoredServiceCollection getServicesForInterface(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress) throws ParseException {
        RequisitionInterface iface = getInterfaceForNode(foreignSource, foreignId, ipAddress);
        if (iface != null) {
            return new RequisitionMonitoredServiceCollection(iface.getMonitoredServices());
        }
        return null;
    }

    /**
     * Returns a service for a given foreignSource/foreignId/interface/service-name combination.
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services/{service}")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionMonitoredService getServiceForInterface(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress, @PathParam("service") String service) throws ParseException {
        RequisitionInterface iface = getInterfaceForNode(foreignSource, foreignId, ipAddress);
        if (iface != null) {
            return iface.getMonitoredService(service);
        }
        return null;
    }

    /**
     * Returns a collection of categories for a given node in the specified foreign source
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/categories")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionCategoryCollection getCategories(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId) throws ParseException {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                return new RequisitionCategoryCollection(node.getCategories());
            }
        }
        return null;
    }

    /**
     * Returns the requested category for a given node in the specified foreign source
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/categories/{category}")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionCategory getCategory(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("category") String category) throws ParseException {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                return node.getCategory(category);
            }
        }
        return null;
    }
    
    /**
     * Returns a collection of assets for a given node in the specified foreign source
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/assets")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionAssetCollection getAssetParameters(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId) throws ParseException {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                return new RequisitionAssetCollection(node.getAssets());
            }
        }
        return null;
    }

    /**
     * Returns the requested category for a given node in the specified foreign source
     */
    @GET
    @Path("{foreignSource}/nodes/{foreignId}/assets/{parameter}")
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public RequisitionAsset getAssetParameter(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("parameter") String parameter) throws ParseException {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                return node.getAsset(parameter);
            }
        }
        return null;
    }
    
    /**
     * Updates or adds a complete requisition with foreign source "foreignSource" 
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    public Response addOrReplaceRequisition(Requisition requisition) {
        debug("addOrReplaceRequisition: Adding requisition %s", requisition.getForeignSource());
        m_pendingForeignSourceRepository.save(requisition);
        return Response.ok(requisition).build();
    }

    /**
     * Updates or adds a node to a requisition 
     */
    @POST
    @Path("{foreignSource}/nodes")
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    public Response addOrReplaceNode(@PathParam("foreignSource") String foreignSource, RequisitionNode node) {
        debug("addOrReplaceNode: Adding node %s to requisition %s", node.getForeignId(), foreignSource);
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            req.putNode(node);
            m_pendingForeignSourceRepository.save(req);
            return Response.ok(req).build();
        }
        return Response.notModified().build();
    }

    /**
     * Updates or adds an interface to a node
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/interfaces")
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    public Response addOrReplaceInterface(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionInterface iface) {
        debug("addOrReplaceInterface: Adding interface %s to node %s/%s", iface, foreignSource, foreignId);
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                node.putInterface(iface);
                m_pendingForeignSourceRepository.save(req);
                return Response.ok(req).build();
            }
        }
        return Response.notModified().build();
    }

    /**
     * Updates or adds a service to an interface
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services")
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    public Response addOrReplaceService(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress, RequisitionMonitoredService service) {
        debug("addOrReplaceService: Adding service %s to node %s/%s, interface %s", service.getServiceName(), foreignSource, foreignId, ipAddress);
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                RequisitionInterface iface = node.getInterface(ipAddress);
                if (iface != null) {
                    iface.putMonitoredService(service);
                    m_pendingForeignSourceRepository.save(req);
                    return Response.ok(req).build();
                }
            }
        }
        return Response.notModified().build();
    }

    /**
     * Updates or adds a category to a node
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/categories")
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    public Response addOrReplaceNodeCategory(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionCategory category) {
        debug("addOrReplaceNodeCategory: Adding category %s to node %s/%s", category.getName(), foreignSource, foreignId);
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                node.putCategory(category);
                m_pendingForeignSourceRepository.save(req);
                return Response.ok(req).build();
            }
        }
        return Response.notModified().build();
    }

    /**
     * Updates or adds an asset parameter to a node
     */
    @POST
    @Path("{foreignSource}/nodes/{foreignId}/assets")
    @Consumes(MediaType.APPLICATION_XML)
    @Transactional
    public Response addOrReplaceNodeAssetParameter(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, RequisitionAsset asset) {
        debug("addOrReplaceNodeCategory: Adding asset %s to node %s/%s", asset.getName(), foreignSource, foreignId);
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                node.putAsset(asset);
                m_pendingForeignSourceRepository.save(req);
                return Response.ok(req).build();
            }
        }
        return Response.notModified().build();
    }

    @PUT
    @Path("{foreignSource}/import")
    @Transactional
    public Response importRequisition(@PathParam("foreignSource") String foreignSource) {
        log().debug("importing requisition for foreign source " + foreignSource);

        Requisition req = getActiveRequisition(foreignSource);
        String url = getActiveUrl(foreignSource).toString();
        EventBuilder bldr = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, "Web");
        bldr.addParam(EventConstants.PARM_URL, url);
        
        try {
            m_eventProxy.send(bldr.getEvent());
        } catch (EventProxyException e) {
            throw new DataAccessResourceFailureException("Unable to send event to import group "+foreignSource, e);
        }
        
        return Response.ok(req).build();
    }
    
    /**
     * Updates the requisition with foreign source "foreignSource"
     */
    @PUT
    @Path("{foreignSource}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateRequisition(@PathParam("foreignSource") String foreignSource, MultivaluedMapImpl params) {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            debug("updateRequisition: updating requisition with foreign source %s", foreignSource);
            setProperties(params, req);
            debug("updateRequisition: requisition with foreign source %s updated", foreignSource);
            m_pendingForeignSourceRepository.save(req);
            return Response.ok(req).build();
        }
        return Response.notModified(foreignSource).build();
    }

    /**
     * Updates the node with foreign id "foreignId" in foreign source "foreignSource"
     */
    @PUT
    @Path("{foreignSource}/nodes/{foreignId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateNode(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, MultivaluedMapImpl params) {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                debug("updateNode: updating node with foreign source %s and foreign id %s", foreignSource, foreignId);
                setProperties(params, node);
                debug("updateNode: node with foreign source %s and foreign id %s updated", foreignSource, foreignId);
                m_pendingForeignSourceRepository.save(req);
                return Response.ok(node).build();
            }
        }
        return Response.notModified(foreignSource + "/" + foreignId).build();
    }

    /**
     * Updates a specific interface
     */
    @PUT
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updateInterface(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress, MultivaluedMapImpl params) {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                RequisitionInterface iface = node.getInterface(ipAddress);
                if (iface != null) {
                    debug("updateInterface: updating interface %s on node %s/%s", ipAddress, foreignSource, foreignId);
                    setProperties(params, iface);
                    debug("updateInterface: interface %s on node %s/%s updated", ipAddress, foreignSource, foreignId);
                    m_pendingForeignSourceRepository.save(req);
                    return Response.ok(node).build();
                }
            }
        }
        return Response.notModified(foreignSource + "/" + foreignId).build();
    }

    /**
     * Deletes the pending requisition with foreign source "foreignSource"
     * @param foreignSource
     * @return
     */
    @DELETE
    @Path("{foreignSource}")
    @Transactional
    public Response deletePendingRequisition(@PathParam("foreignSource") String foreignSource) {
        Requisition req = getActiveRequisition(foreignSource);
        debug("deletePendingRequisition: deleting pending requisition with foreign source %s", foreignSource);
        m_pendingForeignSourceRepository.delete(req);
        return Response.ok(req).build();
    }
    
    /**
     * Deletes the deployed requisition with foreign source "foreignSource"
     * @param foreignSource
     * @return
     */
    @DELETE
    @Path("deployed/{foreignSource}")
    @Transactional
    public Response deleteDeployedRequisition(@PathParam("foreignSource") String foreignSource) {
        Requisition req = getActiveRequisition(foreignSource);
        debug("deleteDeployedRequisition: deleting pending requisition with foreign source %s", foreignSource);
        m_deployedForeignSourceRepository.delete(req);
        return Response.ok(req).build();
    }
    
    /**
     * Delete the node with the given foreign ID for the specified foreign source
     */
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}")
    @Transactional
    public Response deleteNode(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId) {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            req.deleteNode(foreignId);
            m_pendingForeignSourceRepository.save(req);
            return Response.ok(req).build();
        }
        return null;
    }
    
    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}")
    @Transactional
    public Response deleteInterface(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress) {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                node.deleteInterface(ipAddress);
                m_pendingForeignSourceRepository.save(req);
                return Response.ok(req).build();
            }
        }
        return null;
    }

    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/interfaces/{ipAddress}/services/{service}")
    @Transactional
    public Response deleteInterfaceService(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("ipAddress") String ipAddress, @PathParam("service") String service) {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                RequisitionInterface iface = node.getInterface(ipAddress);
                if (iface != null) {
                    iface.deleteMonitoredService(service);
                    m_pendingForeignSourceRepository.save(req);
                    return Response.ok(req).build();
                }
            }
        }
        return null;
    }

    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/categories/{category}")
    @Transactional
    public Response deleteCategory(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("category") String category) {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                node.deleteCategory(category);
                m_pendingForeignSourceRepository.save(req);
                return Response.ok(req).build();
            }
        }
        return null;
    }

    @DELETE
    @Path("{foreignSource}/nodes/{foreignId}/assets/{parameter}")
    @Transactional
    public Response deleteAssetParameter(@PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, @PathParam("parameter") String parameter) {
        Requisition req = getActiveRequisition(foreignSource);
        if (req != null) {
            RequisitionNode node = req.getNode(foreignId);
            if (node != null) {
                node.deleteAsset(parameter);
                m_pendingForeignSourceRepository.save(req);
                return Response.ok(req).build();
            }
        }
        return null;
    }

    private Set<String> getActiveForeignSourceNames() {
        Set<String> fsNames = m_pendingForeignSourceRepository.getActiveForeignSourceNames();
        fsNames.addAll(m_deployedForeignSourceRepository.getActiveForeignSourceNames());
        return fsNames;
    }

    private URL getActiveUrl(String foreignSourceName) {
        Requisition pending = m_pendingForeignSourceRepository.getRequisition(foreignSourceName);
        Requisition deployed = m_deployedForeignSourceRepository.getRequisition(foreignSourceName);
        
        if (pending == null) {
            return m_deployedForeignSourceRepository.getRequisitionURL(foreignSourceName);
        } else if (deployed == null) {
            return m_pendingForeignSourceRepository.getRequisitionURL(foreignSourceName);
        } else if (deployed.getDateStamp().compare(pending.getDateStamp()) > -1) {
            // deployed is newer than pending
            return m_deployedForeignSourceRepository.getRequisitionURL(foreignSourceName);
        }
        return m_pendingForeignSourceRepository.getRequisitionURL(foreignSourceName);
    }

    private Requisition getActiveRequisition(String foreignSourceName) {
        Requisition pending = m_pendingForeignSourceRepository.getRequisition(foreignSourceName);
        Requisition deployed = m_deployedForeignSourceRepository.getRequisition(foreignSourceName);
        
        if (pending == null) {
            return deployed;
        } else if (deployed == null) {
            return pending;
        } else if (deployed.getDateStamp().compare(pending.getDateStamp()) > -1) {
            // deployed is newer than pending
            return deployed;
        }
        return pending;
    }

    private void debug(String format, Object... values) {
//        System.err.println(String.format(format, values));
//        log().debug(String.format(format, values));
    }
    
}
