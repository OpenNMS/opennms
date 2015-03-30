/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

import java.util.ArrayList;
import java.util.List;

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

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for OnmsNode entity
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component
@PerRequest
@Scope("prototype")
@Path("nodes")
@Transactional
public class NodeRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(NodeRestService.class);
    
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private CategoryDao m_categoryDao;
    
    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;
    
    @Context 
    UriInfo m_uriInfo;
    
    @Context
    ResourceContext m_context;

    /**
     * <p>getNodes</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNodeList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public OnmsNodeList getNodes() {
        readLock();
        
        try {
            final MultivaluedMap<String, String> params = m_uriInfo.getQueryParameters();
            final String type = params.getFirst("type");

            final CriteriaBuilder builder = getCriteriaBuilder(params);
            Criteria crit = null;

            if (params.size() == 1 && params.getFirst("nodeId") != null && params.getFirst("nodeId").contains(",")) {
                // we've been specifically asked for a list of nodes by ID

                final List<Integer> nodeIds = new ArrayList<Integer>();
                for (final String id : params.getFirst("nodeId").split(",")) {
                    nodeIds.add(Integer.valueOf(id));
                }
                builder.ne("type", "D");
                builder.in("id", nodeIds);
                builder.distinct();

                crit = builder.toCriteria();
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

            final OnmsNodeList coll = new OnmsNodeList(m_nodeDao.findMatching(crit));
            
            crit.setLimit(null);
            crit.setOffset(null);
            crit.setOrders(new ArrayList<Order>());
    
            coll.setTotalCount(m_nodeDao.countMatching(crit));
    
            return coll;
        } finally {
            readUnlock();
        }
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
    public OnmsNode getNode(@PathParam("nodeCriteria") final String nodeCriteria) {
        readLock();
        try {
            return m_nodeDao.get(nodeCriteria);
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>addNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addNode(final OnmsNode node) {
        writeLock();
        
        try {
            LOG.debug("addNode: Adding node {}", node);
            m_nodeDao.save(node);
            try {
                sendEvent(EventConstants.NODE_ADDED_EVENT_UEI, node.getId(), node.getLabel());
            } catch (EventProxyException ex) {
                throw getException(Status.BAD_REQUEST, ex.getMessage());
            }
            return Response.seeOther(m_uriInfo.getRequestUriBuilder().path(node.getNodeId()).build()).build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>updateNode</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{nodeCriteria}")
    public Response updateNode(@PathParam("nodeCriteria") final String nodeCriteria, final MultivaluedMapImpl params) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "updateNode: Can't find node " + nodeCriteria);
    
            LOG.debug("updateNode: updating node {}", node);
    
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(node);
            for(final String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    final String stringValue = params.getFirst(key);
    				final Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
    
            LOG.debug("updateNode: node {} updated", node);
            m_nodeDao.saveOrUpdate(node);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
            // return Response.ok(node).build();
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
    public Response deleteNode(@PathParam("nodeCriteria") final String nodeCriteria) {
        writeLock();
        
        try {
            final OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "deleteNode: Can't find node " + nodeCriteria);
    
            LOG.debug("deleteNode: deleting node {}", nodeCriteria);
            m_nodeDao.delete(node);
            try {
                sendEvent(EventConstants.NODE_DELETED_EVENT_UEI, node.getId(), node.getLabel());
            } catch (final EventProxyException ex) {
                throw getException(Status.BAD_REQUEST, ex.getMessage());
            }
            return Response.ok().build();
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
    public OnmsIpInterfaceResource getIpInterfaceResource() {
        return m_context.getResource(OnmsIpInterfaceResource.class);
    }

    /**
     * <p>getSnmpInterfaceResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsSnmpInterfaceResource} object.
     */
    @Path("{nodeCriteria}/snmpinterfaces")
    public OnmsSnmpInterfaceResource getSnmpInterfaceResource() {
        return m_context.getResource(OnmsSnmpInterfaceResource.class);
    }

    /**
     * <p>getAssetRecordResource</p>
     *
     * @return a {@link org.opennms.web.rest.AssetRecordResource} object.
     */
    @Path("{nodeCriteria}/assetRecord")
    public AssetRecordResource getAssetRecordResource() {
        return m_context.getResource(AssetRecordResource.class);
    }

    /**
     * <p>getHardwareInventoryResource</p>
     *
     * @return a {@link org.opennms.web.rest.HardwareInventoryResource} object.
     */
    @Path("{nodeCriteria}/hardwareInventory")
    public HardwareInventoryResource getHardwareInventoryResource() {
        return m_context.getResource(HardwareInventoryResource.class);
    }

    @GET
    @Path("/{nodeCriteria}/categories")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsCategoryCollection getCategoriesForNode(@PathParam("nodeCriteria") String nodeCriteria) {
        readLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "getCategories: Can't find node " + nodeCriteria);
            }
            return new OnmsCategoryCollection(node.getCategories());
        } finally {
            readUnlock();
        }
    }
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{nodeCriteria}/categories/{categoryName}")
    public OnmsCategory getCategoryForNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") String categoryName) {
        readLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "getCategory: Can't find node " + nodeCriteria);
            }
            return getCategory(node, categoryName);
        } finally {
            readUnlock();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/{nodeCriteria}/categories")
    public Response addCategoryToNode(@PathParam("nodeCriteria") final String nodeCriteria, OnmsCategory category) {
        if (category == null) throw getException(Status.BAD_REQUEST, "Category must not be null.");
        return addCategoryToNode(nodeCriteria,  category.getName());
    }
    
    @PUT
    @Path("/{nodeCriteria}/categories/{categoryName}")
    public Response addCategoryToNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") final String categoryName) {
        writeLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "addCategory: Can't find node " + nodeCriteria);
            }
            OnmsCategory found = m_categoryDao.findByName(categoryName);
            if (found == null) {
                throw getException(Status.BAD_REQUEST, "addCategory: Can't find category " + categoryName);
            }
            if (!node.getCategories().contains(found)) {
                LOG.debug("addCategory: Adding category {} to node {}", found, nodeCriteria);
                node.addCategory(found);
                m_nodeDao.save(node);
                return Response.seeOther(getRedirectUri(m_uriInfo, categoryName)).build();
            } else {
                throw getException(Status.BAD_REQUEST, "addCategory: Category '{}' already added to node '{}'", categoryName, nodeCriteria);
            }
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{nodeCriteria}/categories/{categoryName}")
    public Response updateCategoryForNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") String categoryName, MultivaluedMapImpl params) {
        writeLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "updateCategory: Can't find node " + nodeCriteria);
            }
            OnmsCategory category = getCategory(node, categoryName);
            if (category == null) {
                throw getException(Status.BAD_REQUEST, "updateCategory: Category " + categoryName + " not found on node " + nodeCriteria);
            }
            LOG.debug("updateCategory: updating category {}", category);
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(category);
            for(String key : params.keySet()) {
                if (wrapper.isWritableProperty(key)) {
                    String stringValue = params.getFirst(key);
                    Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    wrapper.setPropertyValue(key, value);
                }
            }
            LOG.debug("updateCategory: category {} updated", category);
            m_nodeDao.saveOrUpdate(node);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("/{nodeCriteria}/categories/{categoryName}")
    public Response removeCategoryFromNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") String categoryName) {
        writeLock();

        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "deleteCaegory: Can't find node " + nodeCriteria);
            }
            OnmsCategory category = getCategory(node, categoryName);
            if (category == null) {
                throw getException(Status.BAD_REQUEST, "deleteCaegory: Category " + categoryName + " not found on node " + nodeCriteria);
            }
            LOG.debug("deleteCaegory: deleting category {} from node {}", categoryName, nodeCriteria);
            node.getCategories().remove(category);
            m_nodeDao.saveOrUpdate(node);
            return Response.ok().build();
        } finally {
            writeUnlock();
        }
    }

    private OnmsCategory getCategory(OnmsNode node, String categoryName) {
        for (OnmsCategory category : node.getCategories()) {
            if (category.getName().equals(categoryName)) {
                return category;
            }
        }
        return null;
    }

    private void sendEvent(final String uei, final int nodeId, String nodeLabel) throws EventProxyException {
        final EventBuilder bldr = new EventBuilder(uei, getClass().getName());
        bldr.setNodeid(nodeId);
        bldr.addParam("nodelabel", nodeLabel);
        m_eventProxy.send(bldr.getEvent());
    }

    private CriteriaBuilder getCriteriaBuilder(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.alias("snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("categories", "category", JoinType.LEFT_JOIN);
        builder.alias("assetRecord", "assetRecord", JoinType.LEFT_JOIN);
        builder.alias("ipInterfaces.monitoredServices.serviceType", "serviceType", JoinType.LEFT_JOIN);

        applyQueryFilters(params, builder);
        return builder;
    }

}
