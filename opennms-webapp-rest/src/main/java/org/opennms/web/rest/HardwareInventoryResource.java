/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.Map;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsHwEntityAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

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
@Component
@PerRequest
@Scope("prototype")
@Path("hardwareInventory")
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

    /** The UEI info. */
    @Context 
    private UriInfo m_uriInfo;

    /**
     * Gets the hardware inventory.
     *
     * @param nodeCriteria the node criteria
     * @return the hardware inventory
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsHwEntity getHardwareInventory(@PathParam("nodeCriteria") String nodeCriteria) {
        readLock();
        try {
            OnmsNode node = getOnmsNode(nodeCriteria);
            OnmsHwEntity entity = m_hwEntityDao.findRootByNodeId(node.getId());
            if (entity == null ) {
                throw getException(Status.BAD_REQUEST, "getHardwareInventory: Can't find root hardware entity for node " + nodeCriteria);
            }
            return entity;
        } finally {
            readUnlock();
        }
    }

    /**
     * Gets the hardware entity by index.
     *
     * @param nodeCriteria the node criteria
     * @param entPhysicalIndex the entity physical index
     * @return the hardware entity by index
     */
    @GET
    @Path("{entPhysicalIndex}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsHwEntity getHwEntityByIndex(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("entPhysicalIndex") Integer entPhysicalIndex) {
        readLock();
        try {
            OnmsNode node = getOnmsNode(nodeCriteria);
            return getHwEntity(node.getId(), entPhysicalIndex);
        } finally {
            readUnlock();
        }
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
    public Response setHardwareInventory(@PathParam("nodeCriteria") String nodeCriteria, OnmsHwEntity entity) {
        if (!entity.isRoot()) {
            throw getException(Status.BAD_REQUEST, "setHardwareInventory: The OnmsHwEntity is not a root entity " + entity);
        }
        writeLock();
        try {
            OnmsNode node = getOnmsNode(nodeCriteria);
            fixEntity(node, entity);

            OnmsHwEntity existing = m_hwEntityDao.findRootByNodeId(node.getId());
            if (existing != null && !entity.equals(existing)) {
                LOG.debug("setHardwareInventory: removing existing hardware inventory from node {} ", nodeCriteria);
                m_hwEntityDao.delete(existing);
                m_hwEntityDao.flush();
            }
            m_hwEntityDao.save(entity);

            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
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
    public Response addOrReplaceChild(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("parentEntPhysicalIndex") Integer parentEntPhysicalIndex, OnmsHwEntity child) {
        writeLock();
        try {
            OnmsNode node = getOnmsNode(nodeCriteria);
            fixEntity(node, child);

            OnmsHwEntity parent = getHwEntity(node.getId(), parentEntPhysicalIndex);
            if (parent == null) {
                throw getException(Status.BAD_REQUEST, "Can't find entity on node " + nodeCriteria + " with index " + parentEntPhysicalIndex);
            }
            OnmsHwEntity currentChild = parent.getChildByIndex(child.getEntPhysicalIndex());
            if (currentChild != null) {
                LOG.debug("addOrReplaceChild: removing entity {}", currentChild);
                parent.removeChild(currentChild);
            }
            parent.addChildEntity(child);
            LOG.debug("addOrReplaceChild: updating entity {}", child);
            m_hwEntityDao.save(parent);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
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
    public Response updateHwEntity(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("entPhysicalIndex") Integer entPhysicalIndex, MultivaluedMapImpl params) {
        writeLock();
        try {
            OnmsNode node = getOnmsNode(nodeCriteria);
            OnmsHwEntity entity = getHwEntity(node.getId(), entPhysicalIndex);

            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(entity);
            for(String key : params.keySet()) {
                if (key.startsWith("entPhysical")) {
                    if (wrapper.isWritableProperty(key)) {
                        String stringValue = params.getFirst(key);
                        Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                        wrapper.setPropertyValue(key, value);
                    }
                } else {
                    OnmsHwEntityAttribute attr = entity.getAttribute(key);
                    if (attr != null) {
                        attr.setValue(params.getFirst(key));
                    }
                }
            }
            m_hwEntityDao.save(entity);

            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
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
    public Response deleteHwEntity(@PathParam("nodeCriteria") String nodeCriteria, @PathParam("entPhysicalIndex") Integer entPhysicalIndex) {
        writeLock();
        try {
            OnmsNode node = getOnmsNode(nodeCriteria);
            OnmsHwEntity entity = getHwEntity(node.getId(), entPhysicalIndex);
            m_hwEntityDao.delete(entity);
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
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
            throw getException(Status.BAD_REQUEST, "Can't find node " + nodeCriteria);
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
        if (entity == null ) {
            throw getException(Status.BAD_REQUEST, "Can't find entity on node " + nodeId + " with index " + entPhysicalIndex);
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

}
