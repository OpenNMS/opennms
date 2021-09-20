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

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.xml.event.Event;
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
@Transactional
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

    /**
     * <p>getNodes</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNodeList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
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
            final Set<Integer> filteredNodeIds = m_filterDao.getNodeMap(params.getFirst("filterRule")).keySet();
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

        final OnmsNodeList coll = new OnmsNodeList(m_nodeDao.findMatching(crit));
        
        crit.setLimit(null);
        crit.setOffset(null);
        crit.setOrders(new ArrayList<Order>());

        coll.setTotalCount(m_nodeDao.countMatching(crit));

        return coll;
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
    public OnmsNode getNode(@PathParam("nodeCriteria") final String nodeCriteria) {
        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "Node {} was not found.", nodeCriteria);
        }
        return node;
    }

    /**
     * <p>addNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
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
            sendEvent(EventConstants.NODE_ADDED_EVENT_UEI, node.getId(), node.getLabel());
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
    public Response updateNode(@PathParam("nodeCriteria") final String nodeCriteria, final MultivaluedMapImpl params) {
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
    public Response deleteNode(@PathParam("nodeCriteria") final String nodeCriteria) {
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
    public OnmsIpInterfaceResource getIpInterfaceResource(@Context final ResourceContext context) {
        return context.getResource(OnmsIpInterfaceResource.class);
    }

    /**
     * <p>getSnmpInterfaceResource</p>
     *
     * @return a {@link org.opennms.web.rest.OnmsSnmpInterfaceResource} object.
     */
    @Path("{nodeCriteria}/snmpinterfaces")
    public OnmsSnmpInterfaceResource getSnmpInterfaceResource(@Context final ResourceContext context) {
        return context.getResource(OnmsSnmpInterfaceResource.class);
    }

    /**
     * <p>getAssetRecordResource</p>
     *
     * @return a {@link org.opennms.web.rest.AssetRecordResource} object.
     */
    @Path("{nodeCriteria}/assetRecord")
    public AssetRecordResource getAssetRecordResource(@Context final ResourceContext context) {
        return context.getResource(AssetRecordResource.class);
    }

    /**
     * <p>getHardwareInventoryResource</p>
     *
     * @return a {@link org.opennms.web.rest.HardwareInventoryResource} object.
     */
    @Path("{nodeCriteria}/hardwareInventory")
    public HardwareInventoryResource getHardwareInventoryResource(@Context final ResourceContext context) {
        return context.getResource(HardwareInventoryResource.class);
    }

    @GET
    @Path("/{nodeCriteria}/categories")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsCategoryCollection getCategoriesForNode(@PathParam("nodeCriteria") String nodeCriteria) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        return new OnmsCategoryCollection(node.getCategories());
    }
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{nodeCriteria}/categories/{categoryName}")
    public OnmsCategory getCategoryForNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") String categoryName) {
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
    public Response addCategoryToNode(@Context final UriInfo uriInfo, @PathParam("nodeCriteria") final String nodeCriteria, OnmsCategory category) {
        if (category == null) throw getException(Status.BAD_REQUEST, "Category must not be null.");
        return addCategoryToNode(uriInfo, nodeCriteria,  category.getName());
    }
    
    @POST
    @Path("/{nodeCriteria}/categories/{categoryName}")
    public Response addCategoryToNode(@Context final UriInfo uriInfo, @PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") final String categoryName) {
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
    public Response updateCategoryForNode(@PathParam("nodeCriteria") final String nodeCriteria, @PathParam("categoryName") final String categoryName, MultivaluedMapImpl params) {
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
    public Response removeCategoryFromNode(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("categoryName") String categoryName) {
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

    private void sendEvent(final String uei, final int nodeId, String nodeLabel) {
        try {
            final EventBuilder bldr = new EventBuilder(uei, "ReST");
            bldr.setNodeid(nodeId);
            bldr.addParam("nodelabel", nodeLabel);
            m_eventProxy.send(bldr.getEvent());
        } catch (final EventProxyException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}", uei, e.getMessage());
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
