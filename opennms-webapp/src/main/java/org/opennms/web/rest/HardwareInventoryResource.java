/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("hardwareInventory")
@Transactional
public class HardwareInventoryResource extends OnmsRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private HwEntityDao m_hwEntityDao;

    @Context 
    UriInfo m_uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public OnmsHwEntity getHardwareInventory(@PathParam("nodeCriteria") String nodeCriteria) {
        readLock();
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "getHardwareInventory: Can't find node " + nodeCriteria);
            }
            OnmsHwEntity entity = m_hwEntityDao.findRootByNodeId(node.getId());
            if (entity == null ) {
                throw getException(Status.BAD_REQUEST, "getHardwareInventory: Can't find root hardware entity for node " + nodeCriteria);
            }
            return entity;
        } finally {
            readUnlock();
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setHardwareInventory(@PathParam("nodeCriteria") String nodeCriteria, OnmsHwEntity entity) {
        if (!entity.isRoot()) {
            throw getException(Status.BAD_REQUEST, "setHardwareInventory: The OnmsHwEntity is not a root entity " + entity);
        }
        writeLock();
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) {
                throw getException(Status.BAD_REQUEST, "setHardwareInventory: Can't find node " + nodeCriteria);
            }
            OnmsHwEntity existing = m_hwEntityDao.findRootByNodeId(node.getId());
            if (existing != null ) {
                m_hwEntityDao.delete(existing);
            }
            entity.setNode(node);
            m_hwEntityDao.save(entity);
            return Response.seeOther(m_uriInfo.getRequestUriBuilder().path(node.getNodeId()).build()).build();
        } finally {
            writeUnlock();
        }
    }

}
